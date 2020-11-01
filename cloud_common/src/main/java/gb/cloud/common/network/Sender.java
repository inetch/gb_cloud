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

public class Sender {
    private static void send(JSONObject header, Path path, Channel channel, ChannelFutureListener finishListener) throws IOException{
        System.out.println("send header and file");
        byte[] bytes = header.toString().getBytes(StandardCharsets.UTF_8);
        ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer(bytes.length);
        long fileSize = Files.size(path);
        FileRegion region = new DefaultFileRegion(path.toFile(), 0, fileSize);
        buf.writeBytes(bytes);
        channel.write(buf);
        ChannelFuture transferOperationFuture = channel.writeAndFlush(region);
        if (finishListener != null) {
            transferOperationFuture.addListener(finishListener);
        }
    }

    private static void send(JSONObject header, Channel channel, ChannelFutureListener finishListener){
        System.out.println("send header");
        byte[] bytes = header.toString().getBytes(StandardCharsets.UTF_8);
        ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer(bytes.length);
        buf.writeBytes(bytes);
        ChannelFuture transferOperationFuture = channel.writeAndFlush(buf);
        if (finishListener != null) {
            transferOperationFuture.addListener(finishListener);
        }
    }

    public static void sendMessage(CommandMessage message, boolean isResponse, Channel channel, ChannelFutureListener finishListener) throws IOException {
        System.out.println("sendMessage");
        JSONObject header = new JSONObject();
        JSONObject fileEntry = new JSONObject();
        Path path = message.getFilePath();
        header.put(CommonSettings.J_COMMAND, message.getCommand().toString());
        if(isResponse){
            header.put(CommonSettings.J_RESULT, message.isOk());
            if(!message.isOk()){
                header.put(CommonSettings.J_MESSAGE, message.getErrorMessage());
                send(header, channel, finishListener);
            }
        }

        if(message.isOk() || !isResponse) {
            switch (message.getCommand()) {
                case LOGIN:
                case REGISTER:
                    header.put(CommonSettings.J_USERNAME, message.getUser().getLogin());
                    if (!isResponse) {
                        header.put(CommonSettings.J_PASSWORD, message.getUser().getPassword());
                    }
                    send(header, channel, finishListener);
                    break;
                case PUSH_FILE: //from client to server
                    fileEntry.put(CommonSettings.J_FILENAME, path.getFileName().toString());
                    fileEntry.put(CommonSettings.J_SIZE, Files.size(path));
                    header.put(CommonSettings.J_FILE, fileEntry);
                    if (isResponse) {
                        send(header, channel, finishListener);
                    } else {
                        send(header, path, channel, finishListener);
                    }
                    break;

                case PULL_FILE: //from server to client
                    if (!isResponse) break;
                    fileEntry.put(CommonSettings.J_FILENAME, path.getFileName().toString());
                    fileEntry.put(CommonSettings.J_SIZE, Files.size(path));
                    header.put(CommonSettings.J_FILE, fileEntry);
                    send(header, path, channel, finishListener);
                    break;

                case PUSH_TREE:
                    if (isResponse) {
                        header.put(CommonSettings.J_LIST, JSONProcessor.listTree(path));
                    }
                case PULL_TREE:
                    send(header, channel, finishListener);
                    break;
            }
        }
    }

}

