package gb.cloud.server;

import gb.cloud.server.db.IDBMain;
import io.netty.channel.ChannelHandlerContext;

import java.util.Hashtable;
import java.util.Map;

public class ConnectionManager implements IConnectionManager{
    private final Map<ChannelHandlerContext, IClientConnection> connectionMap;
    IDBMain db;

    public ConnectionManager(IDBMain db){
        this.connectionMap = new Hashtable<>();
        this.db = db;
    }

    public IClientConnection getConnection(ChannelHandlerContext context){
        IClientConnection client;
        if(connectionMap.containsKey(context)){
            client = connectionMap.get(context);
        }else{
            client = new ClientConnection(db);
            connectionMap.put(context, client);
        }
        return client;
    }
}
