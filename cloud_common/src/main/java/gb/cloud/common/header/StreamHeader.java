package gb.cloud.common.header;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class StreamHeader {
    private int bracketCounter;
    private final StringBuffer headerBuffer;
    private boolean isFinished;
    private enum HState {
        NORMAL
        , QUOTED
        , MASKED
    }
    private HState hState = HState.NORMAL;

    public StreamHeader(StringBuffer headerBuffer){
        this.headerBuffer = headerBuffer;
        bracketCounter = 0;
    }

    public void start(char c){
        headerBuffer.append(c);
        bracketCounter++;
        isFinished = false;
    }

    public boolean next(char c){
        headerBuffer.append(c);
        if (hState == HState.NORMAL) {
            if (c == '{') {
                bracketCounter++;
            }
            if (c == '}') {
                bracketCounter--;
            }
            if (c == '"') {
                hState = HState.QUOTED;
            }
        }else{
            if (hState == HState.QUOTED){
                if (c == '"'){
                    hState = HState.NORMAL;
                }
                if (c == '\\'){
                    hState = HState.MASKED;
                }
            }
            if (hState == HState.MASKED){
                hState = HState.QUOTED;
            }
        }
        if(bracketCounter == 0){
            isFinished = true;
        }else{
            isFinished = false;
        }
        return isFinished;
    }

    public JSONObject toJSON(){
        if (!isFinished){
            new JSONObject();
        }

        JSONObject header;
        JSONParser parser = new JSONParser();
        parser.reset();
        String stringHeader = headerBuffer.toString();
        System.out.println(stringHeader);

        try {
            header = (JSONObject) parser.parse(stringHeader);
        }catch (ParseException e){
            e.printStackTrace();
            return new JSONObject();
        }

        return header;
    }
}
