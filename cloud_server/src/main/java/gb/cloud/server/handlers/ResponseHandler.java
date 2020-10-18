package gb.cloud.server.handlers;

import gb.cloud.common.network.CommandMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ResponseHandler extends ChannelInboundHandlerAdapter {
     @Override
    public void channelRead(ChannelHandlerContext context, Object message) throws Exception {
        if(message instanceof CommandMessage){
            System.out.println(((CommandMessage) message).getCommand());
        }else{
            throw new Exception("Unexpected incoming object: " + message.getClass().getName());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) throws Exception {
        cause.printStackTrace();
        context.close();
    }
}
