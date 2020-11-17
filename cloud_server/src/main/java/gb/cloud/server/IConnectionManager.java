package gb.cloud.server;

import io.netty.channel.ChannelHandlerContext;

public interface IConnectionManager {
    public IClientConnection getConnection(ChannelHandlerContext context);
}
