package gb.cloud.server.handlers;

import gb.cloud.common.network.Command;
import gb.cloud.common.network.CommandMessage;
import gb.cloud.common.network.ResponseMessage;
import gb.cloud.server.ClientConnection;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

public class HandshakeHandler extends ChannelInboundHandlerAdapter {
    private ClientConnection connection;
    public HandshakeHandler(ClientConnection connection) {
        this.connection = connection;
    }

    @Override
    public void channelRead(ChannelHandlerContext context, Object message) throws Exception {
        if(message instanceof CommandMessage){
            CommandMessage commandMessage = (CommandMessage) message;
            System.out.println(commandMessage.getCommand());
            commandMessage.setResponse(new ResponseMessage(true, commandMessage.getCommand()));

            System.out.println(this.connection.serverState++);

            context.writeAndFlush(commandMessage);

            context.fireChannelRead(commandMessage);
        }else{
//            ByteBuf

            System.out.println("Unexpected incoming object: " + message.getClass().getName());
            ResponseMessage response = new ResponseMessage(false, message);
            context.fireChannelRead(response);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) throws Exception {
        cause.printStackTrace();
        context.close();
    }
}