package gb.cloud.common.network;

import gb.cloud.common.CommonSettings;
import gb.cloud.common.HeaderProcessor;
import gb.cloud.common.JSONProcessor;
import gb.cloud.common.network.Command;
import gb.cloud.common.network.CommandMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;

public class TwoWayHandler extends ChannelInboundHandlerAdapter {
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

    JSONParser parser = new JSONParser();

    public TwoWayHandler(String fileDirectory){
        headerProcessor = new HeaderProcessor(fileDirectory);
        this.fileDirectory = fileDirectory;
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
                System.out.print(c);
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
                    header = (JSONObject) parser.parse(headerBuffer.toString());
                }
            }

            if(currentState == State.HEADER_GOT){
                System.out.println("going to process header");
                CommandMessage commandMessage = headerProcessor.processHeader(header);
                System.out.println(commandMessage.getCommand());
                /*System.out.println(commandMessage.getUser().getLogin());
                System.out.println(commandMessage.getUser().getPasswordHash());*/
                if (commandMessage.getCommand() == Command.SEND_FILE){
                    System.out.println(commandMessage.getFilePath());
                    System.out.println(commandMessage.getFileSize());
                    currentState = State.READ_FILE;
                    receivedFileLength = 0L;
                    fileLength = commandMessage.getFileSize();
                    out = new BufferedOutputStream(new FileOutputStream(commandMessage.getFilePath().toFile()));
                }

                if (commandMessage.getCommand() == Command.PULL_FILE){
                    Sender.sendFile(commandMessage.getFilePath(), context.channel(), future -> {
                        if (!future.isSuccess()) {
                            future.cause().printStackTrace();
                        }else{
                            System.out.println("Successful sent");
                        }
                    });
                }

                if (commandMessage.getCommand() == Command.PULL_TREE){
                    JSONObject outHeader = new JSONObject();
                    outHeader.put(CommonSettings.J_COMMAND, Command.SEND_TREE.toString());
                    outHeader.put(CommonSettings.J_LIST, JSONProcessor.listTree(fileDirectory));
                    Sender.sendHeader(outHeader, context.channel(), future -> {
                        if (!future.isSuccess()) {
                            future.cause().printStackTrace();
                        }else{
                            System.out.println("Successful sent");
                        }
                    });
                }

                if(commandMessage.getCommand() == Command.SEND_TREE){
                    System.out.println(commandMessage.getFileTree());
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
            }
        }
        if (buf.readableBytes() == 0) {
            buf.release();
        }
    }
}
