package gb.cloud.client.network;

import gb.cloud.client.ClientSettings;
import gb.cloud.common.header.HeaderProcessor;
import gb.cloud.common.header.StreamHeader;
import gb.cloud.common.network.CommandMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.json.simple.JSONObject;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Path;

public class ClientHandler extends ChannelInboundHandlerAdapter {
    private enum State {
        IDLE
        , READ_HEADER
        , HEADER_GOT
        , READ_FILE
    }

    private State currentState  = State.IDLE;

    private BufferedOutputStream out;
    private long receivedFileLength;
    private long fileLength;

    private final IResponse response;

    private final StreamHeader streamHeader = new StreamHeader();
    private CommandMessage commandMessage = null;

    public ClientHandler(IResponse response){
        this.response = response;
    }

    private void gotResponse(CommandMessage message){
        if(message.isOk()){
            response.gotOk(message);
        }else{
            response.gotError(message);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext context, Object message) throws Exception {
        System.out.println("channelRead, state: " + currentState);
        ByteBuf buf = (ByteBuf)message;

        while (buf.readableBytes() > 0){
            char c = (char)buf.readByte();
            System.out.print(c);
            if (currentState == State.IDLE) {
                if(streamHeader.start(c)){
                    currentState = State.READ_HEADER;
                    continue;
                }
            }

            if(currentState == State.READ_HEADER){
                if (streamHeader.next(c)) {
                    System.out.println("\ngoing to process header");
                    commandMessage = HeaderProcessor.processHeader(streamHeader.getJson(), ClientSettings.FILE_DIRECTORY);
                    System.out.println(commandMessage.getCommand());

                    switch (commandMessage.getCommand()){
                        case REGISTER:
                        case LOGIN:
                        case PUSH_FILE:
                        case PUSH_TREE:
                            currentState = State.IDLE;
                            gotResponse(commandMessage);
                            break;
                        case PULL_FILE:
                            currentState = State.READ_FILE;
                            receivedFileLength = 0L;
                            fileLength = commandMessage.getFileSize();
                            out = new BufferedOutputStream(new FileOutputStream(commandMessage.getFilePath().toFile()));
                            break;
                        default:
                            response.networkError();
                    }
                }
                continue;
            }

            if(currentState == State.READ_FILE){
                if(receivedFileLength < fileLength){
                    out.write(c);
                    receivedFileLength++;
                }
                if(receivedFileLength == fileLength){
                    currentState = State.IDLE;
                    System.out.println("File received!");
                    out.close();
                    gotResponse(commandMessage);
                    currentState = State.IDLE;
                }
                continue;

                /*while (buf.readableBytes() > 0 && receivedFileLength < fileLength){
                    out.write(buf.readByte());
                    receivedFileLength++;
                }

                if(receivedFileLength == fileLength){
                    currentState = State.IDLE;
                    System.out.println("File received!");
                    out.close();
                    gotResponse(commandMessage);
                    currentState = State.IDLE;
                }*/
            }
        }
        if (buf.readableBytes() == 0) {
            buf.release();
        }
    }
}
