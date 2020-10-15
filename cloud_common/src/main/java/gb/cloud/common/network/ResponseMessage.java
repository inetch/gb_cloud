package gb.cloud.common.network;

import java.io.Serializable;

public class ResponseMessage extends AbstractMessage {
    private boolean result;
    private Command lastCommand;

    public ResponseMessage(boolean result, Command lastCommand){
        this.result = result;
        this.lastCommand = lastCommand;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }
}
