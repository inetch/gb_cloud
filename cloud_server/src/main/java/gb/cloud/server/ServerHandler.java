package gb.cloud.server;

import gb.cloud.common.header.HeaderProcessor;
import gb.cloud.common.header.StreamHeader;
import gb.cloud.common.network.Command;
import gb.cloud.common.network.CommandMessage;
import gb.cloud.common.network.Sender;
import gb.cloud.server.db.DBMain;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Hashtable;

public class ServerHandler extends ChannelInboundHandlerAdapter {
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

    /*thread-safe analog of the HashMap
     * */
    Hashtable<ChannelHandlerContext, ClientConnection> connectionMap;


    StreamHeader streamHeader = new StreamHeader();

    public ServerHandler(Hashtable<ChannelHandlerContext, ClientConnection> connectionMap){
        this.connectionMap = connectionMap;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext context, Object message) throws Exception {
        ClientConnection client;

        if(connectionMap.containsKey(context)){
            client = connectionMap.get(context);
        }else{
            client = new ClientConnection(new DBMain(ServerSettings.DB_FILE));
            connectionMap.put(context, client);
        }

        ByteBuf buf = (ByteBuf)message;

        while (buf.readableBytes() > 0){
            if (currentState == State.IDLE) {
                if(streamHeader.start(buf.readByte())){
                    currentState = State.READ_HEADER;
                    System.out.println("to read header");
                }
            }

            if(currentState == State.READ_HEADER){
                if (streamHeader.next(buf.readByte())) {
                    currentState = State.HEADER_GOT;
                    System.out.println("header got");
                }
            }

            if(currentState == State.HEADER_GOT){
                System.out.println("going to process header");
                CommandMessage commandMessage = HeaderProcessor.processHeader(streamHeader.getJson(), ServerSettings.FILE_DIRECTORY);
                System.out.println(commandMessage.getCommand());

                if (commandMessage.getCommand() == Command.LOGIN || commandMessage.getCommand() == Command.REGISTER){
                    boolean loggedIn;
                    try {
                        if (commandMessage.getCommand() == Command.LOGIN) {
                            loggedIn = client.login(commandMessage.getUser());
                            if(!loggedIn){
                                commandMessage.setErrorMessage("Invalid username or password");
                            }
                        } else {
                            loggedIn = client.register(commandMessage.getUser());
                            if(!loggedIn){
                                commandMessage.setErrorMessage("User exists already");
                            }
                        }
                    }catch(IOException e){
                        loggedIn = false;
                        commandMessage.setErrorMessage("Server error: " + e.getMessage());
                        e.printStackTrace();
                    }
                    commandMessage.setResult(loggedIn);
                    if(loggedIn){
                        commandMessage.setFilePath(client.getUserPath());
                    }
                    Sender.sendMessage(commandMessage, true, context, null);
                    currentState = State.IDLE;
                }else{
                    if(!client.isAuthorized()){
                        commandMessage.setResult(false);
                        commandMessage.setErrorMessage("Unauthorized request");
                        Sender.sendMessage(commandMessage, true, context, null);
                        currentState = State.IDLE;
                    }
                }

                if (commandMessage.getCommand() == Command.PULL_FILE){ //send to client
                    Sender.sendMessage(commandMessage, true, context, null);
                    currentState = State.IDLE;
                }

                if (commandMessage.getCommand() == Command.PULL_TREE){
                    CommandMessage responseMessage = new CommandMessage(Command.PUSH_TREE);
                    responseMessage.setFilePath(client.getUserPath());
                    responseMessage.setResult(true);
                    Sender.sendMessage(responseMessage, true, context, null);
                    currentState = State.IDLE;
                }

                if (commandMessage.getCommand() == Command.CREATE_DIR){
                    Path folder = Paths.get(ServerSettings.FILE_DIRECTORY + commandMessage.getTargetFolder());
                    CommandMessage responseMessage = new CommandMessage(Command.CREATE_DIR);
                    responseMessage.setTargetFolder(commandMessage.getTargetFolder());
                    if(Files.exists(folder)){
                        responseMessage.setResult(false);
                        responseMessage.setErrorMessage("Folder already exists!");
                    }else{
                        try {
                            Files.createDirectory(folder);
                            responseMessage.setResult(true);
                        }catch (IOException e){
                            responseMessage.setResult(false);
                            responseMessage.setErrorMessage("Server error: " + e.getMessage());
                        }
                    }
                    Sender.sendMessage(responseMessage, true, context, null);
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

            }
        }
        if (buf.readableBytes() == 0) {
            buf.release();
        }
    }
}
