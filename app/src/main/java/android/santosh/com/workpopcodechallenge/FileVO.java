package android.santosh.com.workpopcodechallenge;

/**
 * Created by Santosh on 8/5/17.
 */

public class FileVO {
    private String name;
    private String url;
    private long fileSize;

    public String getName(){
        return name;
    }

    public String getUrl() {
        return url;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
}
