package gb.cloud.common;

//For the JTree
public class FileTreeElement {
    private final String name;
    private final boolean isFolder;
    private final long size;

    private static final double KB = 1024;
    private static final double MB = KB * 1024;
    private static final double GB = MB * 1024;
    private static final double TB = GB * 1024;

    public FileTreeElement(String name, boolean isFolder, long size){
        this.name = name;
        this.isFolder = isFolder;
        this.size = size;
    }

    public FileTreeElement(String name, boolean isFolder){
        this.name = name;
        this.isFolder = isFolder;
        this.size = 0;
    }

    public boolean isFolder(){
        return isFolder;
    }

    public String getName(){
        return name;
    }

    private String getSizeString(long size){
        if(size > TB){
            return String.format("%.2f TB", (double)size / TB);
        }
        if(size > GB){
            return String.format("%.2f GB", (double)size / GB);
        }
        if(size > MB){
            return String.format("%.2f MB", (double)size / MB);
        }
        if(size > KB){
            return String.format("%.2f kB", (double)size / KB);
        }
        return Long.toString(size);
    }

    @Override
    public String toString() {
        if(isFolder){
            return "(D) " + name;
        }else{
            return "(f) " + name + " [" + getSizeString(size) + "]";
        }
    }
}
