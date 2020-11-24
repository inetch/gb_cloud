package gb.cloud.common.network;

import gb.cloud.common.CommonSettings;
import gb.cloud.common.header.JSONProcessor;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import org.json.simple.JSONObject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;


/*
Serializes the CommandMessage object to JSON, and sends JSON header and the byte-file, if needed
* */
public class Sender {
    private static void send(JSONObject header, Path path, ChannelHandlerContext context, ChannelFutureListener finishListener) throws IOException{
        byte[] bytes = header.toString().getBytes(StandardCharsets.UTF_8);
        ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer(bytes.length);
        long fileSize = Files.size(path);
        FileRegion region = new DefaultFileRegion(path.toFile(), 0, fileSize);
        buf.writeBytes(bytes);
        context.write(buf);
        ChannelFuture transferOperationFuture = context.writeAndFlush(region);
        if (finishListener != null) {
            transferOperationFuture.addListener(finishListener);
        }
    }

    private static void send(JSONObject header, ChannelHandlerContext context, ChannelFutureListener finishListener){
        byte[] bytes = header.toString().getBytes(StandardCharsets.UTF_8);
        ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer(bytes.length);
        buf.writeBytes(bytes);
        ChannelFuture transferOperationFuture = context.writeAndFlush(buf);
        if (finishListener != null) {
            transferOperationFuture.addListener(finishListener);
        }
    }

    public static void sendMessage(ICommandMessage message, boolean isResponse, ChannelHandlerContext context, ChannelFutureListener finishListener) throws IOException {
        JSONObject header = new JSONObject();
        JSONObject fileEntry = new JSONObject();
        Path path = message.getFilePath();
        header.put(CommonSettings.J_COMMAND, message.getCommand().toString());
        if(isResponse){
            header.put(CommonSettings.J_RESULT, message.isOk());
            if(!message.isOk()){
                header.put(CommonSettings.J_MESSAGE, message.getErrorMessage());
                send(header, context, finishListener);
            }
        }

        if(message.isOk() || !isResponse) {
            switch (message.getCommand()) {
                case LOGIN:
                case REGISTER:
                    header.put(CommonSettings.J_USERNAME, message.getUser().getLogin());
                    if (!isResponse) {
                        header.put(CommonSettings.J_PASSWORD, message.getUser().getPassword());
                    }else{//the file list in case of the user is successfully logged in
                        header.put(CommonSettings.J_TREE, JSONProcessor.listTree(path));
                    }
                    send(header, context, finishListener);
                    break;
                case PUSH_FILE: //from client to server
                    fileEntry.put(CommonSettings.J_FILENAME, path.getFileName().toString());
                    fileEntry.put(CommonSettings.J_SIZE, Files.size(path));
                    header.put(CommonSettings.J_FILE, fileEntry);
                    if (isResponse) {
                        send(header, context, finishListener);
                    } else {
                        header.put(CommonSettings.J_FOLDER, message.getTargetFolder());
                        send(header, path, context, finishListener);
                    }
                    break;

                case PULL_FILE: //from server to client
                    if(isResponse){
                        fileEntry.put(CommonSettings.J_FILENAME, path.getFileName().toString());
                        fileEntry.put(CommonSettings.J_SIZE, Files.size(path));
                        header.put(CommonSettings.J_FILE, fileEntry);
                        send(header, path, context, finishListener);
                    }else{
                        fileEntry.put(CommonSettings.J_FILENAME, path.toString());
                        header.put(CommonSettings.J_FILE, fileEntry);
                        send(header, context, finishListener);
                    }
                    break;

                case PUSH_TREE:
                    if (isResponse) {
                        header.put(CommonSettings.J_TREE, JSONProcessor.listTree(path));
                    }
                case PULL_TREE:
                    send(header, context, finishListener);
                    break;

                case CREATE_DIR:
                    header.put(CommonSettings.J_FOLDER, message.getTargetFolder());
                    send(header, context, finishListener);
                    break;
            }
        }
    }

}

