package gb.cloud.server.handlers;

import gb.cloud.common.network.CommandMessage;
import gb.cloud.common.network.ResponseMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

public class OutboundHandler extends ChannelOutboundHandlerAdapter {
    @Override
    public void write(ChannelHandlerContext context, Object message, ChannelPromise promise) throws Exception {
        if (message instanceof CommandMessage) {
            context.writeAndFlush(((CommandMessage) message).getResponse());
        } else if (message instanceof ResponseMessage){
            context.writeAndFlush(message);
        } else {
            ResponseMessage errorResponse = new ResponseMessage(false);
            errorResponse.setText("Server error");
            context.writeAndFlush(errorResponse);
            throw new Exception("Unexpected message class: " + message.getClass().getName());
        }
    }
}
