package gb.cloud.server;

import gb.cloud.common.CommonSettings;
import gb.cloud.common.header.IStreamHeader;
import gb.cloud.common.header.StreamHeader;
import gb.cloud.server.db.DBMain;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.Hashtable;
import java.util.Map;

public class Server {
    private Map<ChannelHandlerContext, IClientConnection> connectionMap;
    private IStreamHeader streamHeader;

    public void run() throws Exception {
        EventLoopGroup mainGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        connectionMap = new Hashtable<>();
        streamHeader = new StreamHeader();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(mainGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel)/* throws Exception*/ {
                            socketChannel.pipeline().addLast(
                                    new ServerHandler(connectionMap, streamHeader)
                            );
                        }
                    });
            ChannelFuture future = b.bind(CommonSettings.SERVER_PORT).sync();
            future.channel().closeFuture().sync();
        } finally {
            mainGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        new Server().run();
    }
}
