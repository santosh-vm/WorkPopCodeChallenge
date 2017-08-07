package android.santosh.com.workpopcodechallenge.interfaces;

import android.santosh.com.workpopcodechallenge.FileVO;

/**
 * Created by Santosh on 8/6/17.
 */

public interface FileListClickInterface {
    void onDownloadFileClicked(int position, FileVO fileVO);
    void onRootViewClicked(int position, FileVO fileVO);
}
