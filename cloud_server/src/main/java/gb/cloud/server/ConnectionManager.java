package gb.cloud.server;

import gb.cloud.server.db.IDBMain;
import io.netty.channel.ChannelHandlerContext;

import java.util.Hashtable;
import java.util.Map;

public class ConnectionManager implements IConnectionManager{
    private final Map<ChannelHandlerContext, IClientConnection> connectionMap;
    IDBMain Db;

    public ConnectionManager(IDBMain Db){
        this.connectionMap = new Hashtable<>();
        this.Db = Db;
    }

    public ConnectionManager(){
        this.connectionMap = new Hashtable<>();
    }

    public void setDb(IDBMain Db){
        this.Db = Db;
    }

    public IClientConnection getConnection(ChannelHandlerContext context){
        IClientConnection client;
        if(connectionMap.containsKey(context)){
            client = connectionMap.get(context);
        }else{
            client = new ClientConnection(Db);
            connectionMap.put(context, client);
        }
        return client;
    }
}
