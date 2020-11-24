package gb.cloud.common.header;

import org.json.simple.JSONObject;

public interface IStreamHeader {
    boolean start(byte c);
    boolean next(byte c);
    JSONObject getJson();
}
