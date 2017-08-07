package android.santosh.com.workpopcodechallenge;

/**
 * Created by Santosh on 8/5/17.
 */

public class FileVO {
    public static enum FileState {DOWNLOADED, DOWNLOADING, NOT_EXIST}

    private String name;
    private String url;
    private long fileSize;
    private FileState fileState = FileState.NOT_EXIST;
    private long bytesCompleted;

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

    public void setFileState(FileState fileState) {
        this.fileState = fileState;
    }

    public FileState getFileState(){
        return fileState;
    }

    public void setBytesCompleted(long bytesCompleted) {
        this.bytesCompleted = bytesCompleted;
    }

    public long getBytesCompleted() {
        return bytesCompleted;
    }
}
