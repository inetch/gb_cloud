package gb.cloud.common.network;

import java.io.Serializable;

public class AbstractMessage implements Serializable {
    private ResponseMessage response;
    private String text;

    public void setResponse(ResponseMessage response){
        this.response = response;
    }

    public ResponseMessage getResponse(){
        return this.response;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
