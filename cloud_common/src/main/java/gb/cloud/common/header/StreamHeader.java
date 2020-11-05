package gb.cloud.common.header;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

/*Parses incoming bytes to JSON header
* */
public class StreamHeader {
    private int bracketCounter;
    private ByteArrayOutputStream byteStream;
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
    public boolean start(byte c){
        boolean res;
        if(c == '{') {
            byteStream = new ByteArrayOutputStream(); //work, GC, work
            byteStream.write(c);
            bracketCounter++;
            isFinished = false;
            res = true;
            json = new JSONObject();
        }else{
            res = false;
        }
        return res;
    }

    /**Returns TRUE if the JSON header is finished
     * */
    public boolean next(byte c){
        byteStream.write(c);
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
            String streamHeader;
            try {
                streamHeader = byteStream.toString("UTF-8");
            }catch (UnsupportedEncodingException e){
                e.printStackTrace();
                streamHeader = "";
            }

            System.out.println(streamHeader);

            try {
                json = (JSONObject) parser.parse(streamHeader);
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
