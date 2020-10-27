package gb.cloud.client.network;

import gb.cloud.common.CommonSettings;
import gb.cloud.common.network.Command;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import org.json.simple.JSONObject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class Sender {
    public static void sendHeader(JSONObject header, Channel channel, ChannelFutureListener finishListener) throws IOException {
        byte[] bytes = header.toString().getBytes(StandardCharsets.UTF_8);

        ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer(bytes.length);
        buf.writeBytes(bytes);

        if (finishListener != null) {
            ChannelFuture transferOperationFuture = channel.writeAndFlush(buf);
            transferOperationFuture.addListener(finishListener);
        } else {
            channel.write(buf);
        }
    }

    public static void sendFile(JSONObject header, Path path, Channel channel, ChannelFutureListener finishListener) throws IOException {
        header.put(CommonSettings.J_COMMAND, Command.SEND_FILE.toString());

        long fileSize = Files.size(path);

        JSONObject fileEntry = new JSONObject();
        fileEntry.put(CommonSettings.J_FILENAME, path.getFileName().toString());
        fileEntry.put(CommonSettings.J_SIZE, fileSize);
        header.put(CommonSettings.J_FILE, fileEntry);

        sendHeader(header, channel, null);

        FileRegion region = new DefaultFileRegion(path.toFile(), 0, fileSize);

        ChannelFuture transferOperationFuture = channel.writeAndFlush(region);
        if (finishListener != null) {
            transferOperationFuture.addListener(finishListener);
        }
    }

}

