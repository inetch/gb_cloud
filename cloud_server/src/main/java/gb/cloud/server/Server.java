package gb.cloud.server;

import gb.cloud.common.CommonSettings;
import gb.cloud.server.handlers.OutboundHandler;
import gb.cloud.server.handlers.HandshakeHandler;
import gb.cloud.server.handlers.RawByteHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

public class Server {
    public void run() throws Exception {
        EventLoopGroup mainGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(mainGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        private ClientConnection connection = new ClientConnection();

                        protected void initChannel(SocketChannel socketChannel)/* throws Exception*/ {
                            socketChannel.pipeline().addLast(
                              /*      new ObjectDecoder(CommonSettings.MAX_NETWORK_OBJECT_SIZE, ClassResolvers.cacheDisabled(null))
                                  , new ObjectEncoder()
                                  , new OutboundHandler()
                                  , new HandshakeHandler(connection)*/
                                    new RawByteHandler()
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
