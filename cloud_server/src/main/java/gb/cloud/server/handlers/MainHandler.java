package gb.cloud.server.handlers;

import gb.cloud.common.network.Command;
import gb.cloud.common.network.CommandMessage;
import gb.cloud.common.network.ResponseMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class MainHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext context, Object message) throws Exception {
        if(message instanceof CommandMessage){
            CommandMessage commandMessage = (CommandMessage) message;

            switch (commandMessage.getCommand()){
                case LOGOUT:
                    //add auth handler before this
                    break;
                case SEND_FILE:
                    //get the file?
                    break;
                case PULL_FILE:
                    //send the file to the client
                    break;
                case RENAME_FILE:
                    //rename the file
                    break;
                case DELETE_FILE:
                    //remove the file
                    break;
                case PULL_TREE:
                    //generate the folders tree and send it
                    break;
                default:
                    commandMessage.getResponse().setResult(false);
            }

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
