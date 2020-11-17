package gb.cloud.server;

import gb.cloud.common.CommonSettings;
import gb.cloud.server.db.DBSQLite;
import gb.cloud.server.db.IDBMain;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class Server {
    private IConnectionManager connections;
    private IDBMain db;

    public void run() throws Exception {
        EventLoopGroup mainGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        ApplicationContext context = new ClassPathXmlApplicationContext("context.xml");
        /*Camera camera = context.getBean("camera", Camera.class);
        camera.doPhotograph();*/

        //db = new DBSQLite("org.sqlite.JDBC", "jdbc:sqlite:" + ServerSettings.DB_FILE);
        db = context.getBean("dbmain", DBSQLite.class);
        connections = new ConnectionManager(db);

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(mainGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel)/* throws Exception*/ {
                            socketChannel.pipeline().addLast(
                                    new ServerHandler(connections)
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
