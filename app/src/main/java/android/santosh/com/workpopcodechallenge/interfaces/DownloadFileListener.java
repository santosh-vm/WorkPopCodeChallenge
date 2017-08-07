package android.santosh.com.workpopcodechallenge.interfaces;

import android.santosh.com.workpopcodechallenge.FileVO;

/**
 * Created by Santosh on 8/6/17.
 */

public interface DownloadFileListener {
    void onDownloadProgress(FileVO fileVO, long bytesCompleted);
    void onDownloadFinished(FileVO fileVO);
    void onFilesCleared();
}
