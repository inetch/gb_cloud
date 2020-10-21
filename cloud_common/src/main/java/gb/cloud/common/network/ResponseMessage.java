package gb.cloud.common.network;

import java.io.Serializable;

public class ResponseMessage extends AbstractMessage {
    private boolean result;
    private Command lastCommand;
    private Object originalMessage;

    public ResponseMessage(boolean result, Command lastCommand){
        this.result = result;
        this.lastCommand = lastCommand;
    }

    public ResponseMessage(boolean result){
        this.result = result;
    }

    public ResponseMessage(boolean result, Object originalMessage){
        this.result = result;
        this.originalMessage = originalMessage;
    }

    public boolean getResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public void setOriginalMessage(Object originalMessage){
        this.originalMessage = originalMessage;
    }

    public Object getOriginalMessage(){
        return this.originalMessage;
    }

    @Override
    public String toString() {
        return "ResponseMessage{" +
                "result=" + result +
                ", lastCommand=" + lastCommand +
                '}';
    }
}
