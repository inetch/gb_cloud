package gb.cloud.client.network;

import gb.cloud.client.ClientSettings;
import gb.cloud.common.header.HeaderProcessor;
import gb.cloud.common.header.StreamHeader;
import gb.cloud.common.network.CommandMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;

public class ClientHandler extends ChannelInboundHandlerAdapter {
    private enum State {
        IDLE
        , READ_HEADER
        , HEADER_GOT
        , READ_FILE
    }

    private State currentState  = State.IDLE;
    private JSONObject header;

    private BufferedOutputStream out;
    private long receivedFileLength;
    private long fileLength;

    private HeaderProcessor headerProcessor;
    private final IResponse response;

    JSONParser parser = new JSONParser();

    public ClientHandler(String fileDirectory, IResponse response){
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
    public void channelRead(ChannelHandlerContext context, Object message) throws Exception {
        ByteBuf buf = (ByteBuf)message;
        CommandMessage commandMessage = null;
        System.out.println("response handler channel read!");
        StringBuffer headerBuffer= new StringBuffer();
        StreamHeader streamHeader = new StreamHeader(headerBuffer);

        while (buf.readableBytes() > 0){
            if (currentState == State.IDLE) {
                char c = (char)buf.readByte();
                System.out.print(c);
                if (c == '{'){
                    currentState = State.READ_HEADER;
                    streamHeader.start(c);
                    System.out.println("to read header");
                }
            }

            if(currentState == State.READ_HEADER){
                if (streamHeader.next((char)buf.readByte())) {
                    currentState = State.HEADER_GOT;
                    System.out.println("header got");
                    header = streamHeader.toJSON();
                }
            }

            if(currentState == State.HEADER_GOT){
                System.out.println("going to process header");
                commandMessage = HeaderProcessor.processHeader(header, ClientSettings.FILE_DIRECTORY);
                System.out.println(commandMessage.getCommand());

                switch (commandMessage.getCommand()){
                    case REGISTER:
                    case LOGIN:
                    case PUSH_FILE:
                    case PUSH_TREE:
                        gotResponse(commandMessage);
                        currentState = State.IDLE;
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

            if(currentState == State.READ_FILE){
                while (buf.readableBytes() > 0 && receivedFileLength < fileLength){
                    out.write(buf.readByte());
                    receivedFileLength++;
                }

                if(receivedFileLength == fileLength){
                    currentState = State.IDLE;
                    System.out.println("File received!");
                    out.close();
                    gotResponse(commandMessage);
                    currentState = State.IDLE;
                }
            }
        }
        if (buf.readableBytes() == 0) {
            buf.release();
        }
    }
}
