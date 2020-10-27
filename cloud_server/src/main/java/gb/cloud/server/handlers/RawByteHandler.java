package gb.cloud.server.handlers;

import gb.cloud.common.network.Command;
import gb.cloud.common.network.CommandMessage;
import gb.cloud.server.HeaderProcessor;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.util.Arrays;

public class RawByteHandler  extends ChannelInboundHandlerAdapter {
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

    JSONParser parser = new JSONParser();

    @Override
    public void channelRead(ChannelHandlerContext context, Object message) throws Exception{
        ByteBuf buf = (ByteBuf)message;
        System.out.println("channel read!");

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
                CommandMessage commandMessage = HeaderProcessor.processHeader(header);
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

/*
public enum State {
        IDLE, NAME_LENGTH, NAME, FILE_LENGTH, FILE
    }

    private State currentState = State.IDLE;
    private int nextLength;
    private long fileLength;
    private long receivedFileLength;
    private BufferedOutputStream out;

    //FIRST_BYTE, INT , FILE_NAME, FILE_LEN, FILE_DATA
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);
        while (buf.readableBytes() > 0) {
            if (currentState == State.IDLE) {
                byte readed = buf.readByte();
                if (readed == (byte) 25) {
                    currentState = State.NAME_LENGTH;
                    receivedFileLength = 0L;
                    System.out.println("STATE: Start file receiving");
                } else {
                    System.out.println("ERROR: Invalid first byte - " + readed);
                }
            }

* */