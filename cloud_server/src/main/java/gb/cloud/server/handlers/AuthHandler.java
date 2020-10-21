package gb.cloud.server.handlers;

import gb.cloud.common.network.Command;
import gb.cloud.common.network.CommandMessage;
import gb.cloud.common.network.ResponseMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class AuthHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext context, Object message) throws Exception {
        if(message instanceof CommandMessage){
            CommandMessage commandMessage = (CommandMessage) message;

            if(commandMessage.getCommand() == Command.LOGIN) {
                //todo: get the user from the DB
                //todo: check the hashes
                System.out.println("User logged in: " + commandMessage.getUser().getLogin());

                //if ok
                commandMessage.getResponse().setResult(true);
                context.pipeline().remove(this);
            }else if (commandMessage.getCommand() == Command.REGISTER) {
                //todo: register the user
                System.out.println("User registered: " + commandMessage.getUser().getLogin());

                //if ok
                commandMessage.getResponse().setResult(true);
                context.pipeline().remove(this);
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
/*
public class MainHandler extends ChannelInboundHandlerAdapter {

    static HashMap<Integer, Types> typesHashMap;
    static {
        typesHashMap.put(1,Types.FILE);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FileRequest) {
            FileRequest fr = (FileRequest) msg;
            if (Files.exists(Paths.get("server_storage/" + fr.getFilename()))) {
                FileMessage fm = new FileMessage(Paths.get("server_storage/" + fr.getFilename()));
                ctx.writeAndFlush(fm);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
} */