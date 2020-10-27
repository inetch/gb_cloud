package gb.cloud.common;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JSONProcessor {
    public static JSONObject getPathEntry(Path path) {
        JSONObject json = new JSONObject();
        json.put(CommonSettings.J_FILENAME, path.getFileName().toString());
        if(!Files.isDirectory(path)){
            long size = 0L;
            try {
                size = Files.size(path);
            }catch (IOException e){
                e.printStackTrace();
            }
            json.put(CommonSettings.J_SIZE, size);
        }
        return json;
    }

    public static JSONObject listTree(Path path){
        DirectoryStream<Path> stream = null;
        JSONObject json = new JSONObject();
        if(!Files.isDirectory(path)){
            json.put(CommonSettings.J_FILE, getPathEntry(path));
        }else {
            JSONArray folder = new JSONArray();
            try {
                stream = Files.newDirectoryStream(path);
                for (Path p : stream) {
                    folder.add(listTree(p));
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    stream.close();
                } catch (IOException | NullPointerException ee) {
                    ee.printStackTrace();
                }
            }
            JSONObject entry = new JSONObject();
            entry.put(CommonSettings.J_FILENAME, path.getFileName().toString());
            entry.put(CommonSettings.J_LIST, folder);
            json.put(CommonSettings.J_FOLDER, entry);
        }
        return json;
    }

    public static JSONObject listTree(String startDirectory){
        return listTree(Paths.get(startDirectory));
    }
}
