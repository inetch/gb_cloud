package gb.cloud.common.header;

import gb.cloud.common.CommonSettings;
import gb.cloud.common.FileTreeElement;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/*Utilities for making JSON from a tree, or tree from JSON
* */
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

    public static void parseJSONTree(JSONArray list, DefaultMutableTreeNode node){
        if(list == null) return;
        list.stream()
                .sorted((o1, o2) -> {
                    JSONObject json1 = (JSONObject)o1;
                    JSONObject json2 = (JSONObject)o2;
                    if(json1.containsKey(CommonSettings.J_FOLDER)){
                        if(json2.containsKey(CommonSettings.J_FILE)){
                            return -1;
                        }else if(json2.containsKey(CommonSettings.J_FOLDER)){
                            String s1 = (String)((JSONObject)json1.get(CommonSettings.J_FOLDER)).get(CommonSettings.J_FILENAME);
                            String s2 = (String)((JSONObject)json2.get(CommonSettings.J_FOLDER)).get(CommonSettings.J_FILENAME);
                            return s1.compareTo(s2);
                        }else{
                            return 0;
                        }
                    }else if (json1.containsKey(CommonSettings.J_FILE)){
                        if(json2.containsKey(CommonSettings.J_FOLDER)){
                            return 1;
                        }else if(json2.containsKey(CommonSettings.J_FILE)){
                            String s1 = (String)((JSONObject)json1.get(CommonSettings.J_FILE)).get(CommonSettings.J_FILENAME);
                            String s2 = (String)((JSONObject)json2.get(CommonSettings.J_FILE)).get(CommonSettings.J_FILENAME);
                            return s1.compareTo(s2);
                        }else{
                            return 0;
                        }
                    }
                    return 0;
                })
                .forEach(o -> {
                    JSONObject json = (JSONObject)o;
                    if(json.containsKey(CommonSettings.J_FILE)){
                        JSONObject file = (JSONObject) json.get(CommonSettings.J_FILE);
                        node.add(new DefaultMutableTreeNode(new FileTreeElement((String)file.get(CommonSettings.J_FILENAME), false, (long)file.get(CommonSettings.J_SIZE))));
                    }else if(json.containsKey(CommonSettings.J_FOLDER)){
                        JSONObject file = (JSONObject) json.get(CommonSettings.J_FOLDER);
                        DefaultMutableTreeNode folderNode = new DefaultMutableTreeNode(new FileTreeElement((String)file.get(CommonSettings.J_FILENAME), true));
                        parseJSONTree((JSONArray) file.get(CommonSettings.J_LIST), folderNode);
                        node.add(folderNode);
                    }else{
                        System.out.println("WTF?: " + json);
                    }
                });
    }
}
