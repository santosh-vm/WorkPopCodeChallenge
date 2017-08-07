package android.santosh.com.workpopcodechallenge.interfaces;

import android.santosh.com.workpopcodechallenge.FileVO;

import java.util.List;

/**
 * Created by Santosh on 8/6/17.
 */

public interface FileFetchListener {
    void onFileFetchSuccess(List<FileVO> fileList);
    void onFileFetchFailure();
}
