package gb.cloud.server.handlers;

import gb.cloud.common.network.Command;
import gb.cloud.common.network.CommandMessage;
import gb.cloud.common.network.ResponseMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class HandshakeHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext context, Object message) throws Exception {
        if(message instanceof CommandMessage){
            CommandMessage commandMessage = (CommandMessage) message;
            System.out.println(commandMessage.getCommand());
            commandMessage.setResponse(new ResponseMessage(true, commandMessage.getCommand()));

            context.fireChannelRead(commandMessage);
        }else{
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