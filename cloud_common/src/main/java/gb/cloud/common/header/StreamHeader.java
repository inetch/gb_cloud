package gb.cloud.common.header;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class StreamHeader {
    private int bracketCounter;
    private StringBuffer headerBuffer;
    private boolean isFinished;
    private enum HState {
        NORMAL
        , QUOTED
        , MASKED
    }
    private HState hState = HState.NORMAL;

    private JSONObject json;

    public StreamHeader(){
        bracketCounter = 0;
    }

    /**Returns TRUE if the JSON header is started
     * */
    public boolean start(char c){
        boolean res;
        if(c == '{') {
            headerBuffer = new StringBuffer(); //work, GC, work
            headerBuffer.append(c);
            bracketCounter++;
            isFinished = false;
            res = true;
            json = new JSONObject();
        }else{
            res = false;
        }
        System.out.println("start: " + headerBuffer.toString());
        return res;
    }

    /**Returns TRUE if the JSON header is finished
     * */
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

            JSONParser parser = new JSONParser();
            parser.reset();
            String stringHeader = headerBuffer.toString();

            try {
                json = (JSONObject) parser.parse(stringHeader);
            }catch (ParseException e){
                e.printStackTrace();
            }

        }else{
            isFinished = false;
        }
        return isFinished;
    }

    public JSONObject getJson(){
        return json;
    }
}
