package gb.cloud.server;

import gb.cloud.common.CommonSettings;
import gb.cloud.common.HeaderProcessor;
import gb.cloud.common.JSONProcessor;
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
    private int bracketCounter  = 0;
    private StringBuffer headerBuffer = new StringBuffer();
    private JSONObject header;

    private BufferedOutputStream out;
    private long receivedFileLength;
    private long fileLength;

    private HeaderProcessor headerProcessor;
    private final String fileDirectory;

    private final ClientConnection client;

    JSONParser parser = new JSONParser();

    public ServerHandler(String fileDirectory, ClientConnection client){
        headerProcessor = new HeaderProcessor(fileDirectory);
        this.fileDirectory = fileDirectory;
        this.client = client;
    }

    @Override
    public void channelRead(ChannelHandlerContext context, Object message) throws Exception {
        ByteBuf buf = (ByteBuf)message;
        System.out.println("response handler channel read!");

        while (buf.readableBytes() > 0){
            if (currentState == State.IDLE) {
                char c = (char)buf.readByte();
                System.out.print(c);
                if (c == '{'){
                    currentState = State.READ_HEADER;
                    System.out.println("to read header");
                    headerBuffer.append(c);
                    bracketCounter++;
                }
            }

            if(currentState == State.READ_HEADER){
                char c = (char)buf.readByte();
                headerBuffer.append(c);
                if (c == '{') {
                    bracketCounter++;
                }
                if (c == '}') {
                    bracketCounter--;
                }
                if (bracketCounter == 0) {
                    currentState = State.HEADER_GOT;
                    System.out.println("header got");
                    parser.reset();
                    System.out.println("parser ready");
                    String stringHeader = headerBuffer.toString();
                    System.out.println("going to parse the string");
                    System.out.println(stringHeader);
                    header = (JSONObject) parser.parse(stringHeader);
                    System.out.println("header parsed");
                }
            }

            if(currentState == State.HEADER_GOT){
                System.out.println("going to process header");
                CommandMessage commandMessage = headerProcessor.processHeader(header);
                System.out.println(commandMessage.getCommand());

                if (commandMessage.getCommand() == Command.LOGIN){
                    commandMessage.setResult(true);
                    client.setAuthorized(true);
                    client.setUsername(commandMessage.getUser().getLogin());
                    Sender.sendMessage(commandMessage, true, context.channel(), null);
                    currentState = State.IDLE;
                }

                if (commandMessage.getCommand() == Command.PULL_FILE){ //send to client
                    if(client.isAuthorized()){
                        commandMessage.setResult(true);
                    }else{
                        commandMessage.setResult(false);
                        commandMessage.setErrorMessage("Unauthorize request");
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
