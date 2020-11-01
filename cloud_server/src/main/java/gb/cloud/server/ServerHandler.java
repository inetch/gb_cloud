package gb.cloud.server;

import gb.cloud.common.header.HeaderProcessor;
import gb.cloud.common.header.StreamHeader;
import gb.cloud.common.network.Command;
import gb.cloud.common.network.CommandMessage;
import gb.cloud.common.network.Sender;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.nio.file.Paths;

public class ServerHandler extends ChannelInboundHandlerAdapter {
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

    private final ClientConnection client;

    JSONParser parser = new JSONParser();

    public ServerHandler(String fileDirectory, ClientConnection client){
        this.client = client;
    }

    @Override
    public void channelRead(ChannelHandlerContext context, Object message) throws Exception {
        ByteBuf buf = (ByteBuf)message;
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
                CommandMessage commandMessage = HeaderProcessor.processHeader(header, ServerSettings.FILE_DIRECTORY);
                System.out.println(commandMessage.getCommand());

                if (commandMessage.getCommand() == Command.LOGIN || commandMessage.getCommand() == Command.REGISTER){
                    boolean loggedIn;
                    if (commandMessage.getCommand() == Command.LOGIN) {
                        loggedIn = client.getDb().loginUser(commandMessage.getUser());
                    }else{
                        loggedIn = client.getDb().registerUser(commandMessage.getUser());
                    }
                    commandMessage.setResult(loggedIn);
                    client.setAuthorized(loggedIn);
                    client.setUsername(commandMessage.getUser().getLogin());
                    Sender.sendMessage(commandMessage, true, context.channel(), null);
                    currentState = State.IDLE;
                }

                if (commandMessage.getCommand() == Command.PULL_FILE){ //send to client
                    if(client.isAuthorized()){
                        commandMessage.setResult(true);
                    }else{
                        commandMessage.setResult(false);
                        commandMessage.setErrorMessage("Unauthorized request");
                    }
                    Sender.sendMessage(commandMessage, true, context.channel(), null);

                    currentState = State.IDLE;
                }

                if (commandMessage.getCommand() == Command.PULL_TREE){
                    CommandMessage responseMessage = new CommandMessage(Command.PUSH_TREE);
                    responseMessage.setResult(true);
                    responseMessage.setFilePath(Paths.get(ServerSettings.FILE_DIRECTORY));
                    Sender.sendMessage(responseMessage, true, context.channel(), null);
                    currentState = State.IDLE;
                }

                if (commandMessage.getCommand() == Command.PUSH_FILE){ //to server
                    receivedFileLength = 0L;
                    fileLength = commandMessage.getFileSize();
                    out = new BufferedOutputStream(new FileOutputStream(commandMessage.getFilePath().toFile()));

                    currentState = State.READ_FILE;
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
                }

                currentState = State.IDLE;
            }
        }
        if (buf.readableBytes() == 0) {
            buf.release();
        }
    }
}
