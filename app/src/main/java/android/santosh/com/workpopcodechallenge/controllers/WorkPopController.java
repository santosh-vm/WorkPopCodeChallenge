package android.santosh.com.workpopcodechallenge.controllers;

import android.content.Context;
import android.os.Handler;
import android.santosh.com.workpopcodechallenge.interfaces.DownloadFileListener;
import android.santosh.com.workpopcodechallenge.interfaces.FileFetchListener;
import android.santosh.com.workpopcodechallenge.FileVO;
import android.santosh.com.workpopcodechallenge.R;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Santosh on 8/5/17.
 */

public class WorkPopController implements DownloadFileListener {
    private static String TAG = WorkPopController.class.getSimpleName();
    private Context context;
    private Handler uiHandler;
    private Gson gson;
    private List<FileVO> fileList = new ArrayList<>();
    private ExecutorService executorService;
    private final List<FileFetchListener> fileFetchListeners = Collections.synchronizedList(new ArrayList<FileFetchListener>());
    private DiskController diskController;

    public WorkPopController(Context context, DiskController diskController, Handler uiHandler) {
        this.executorService = Executors.newSingleThreadExecutor();
        this.diskController = diskController;
        diskController.addDownloadFileListener(this);
        this.uiHandler = uiHandler;
        this.context = context;
        gson = new Gson();
    }

    public synchronized FileVO getFileVOFromFileList(String fileURl) {
        if (fileList != null && fileList.size() > 0) {
            for (FileVO fileVO : fileList) {
                if (fileVO.getUrl().equalsIgnoreCase(fileURl)) {
                    return fileVO;
                }
            }
        }
        return null;
    }

    public void fetchFileList() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    if (fileList != null && fileList.size() > 0) {
                        //Log.d(TAG, "fetchFileList() from memory fileList.size(): " + fileList.size());
                        loadFileDetails();
                    } else {
                        String fileListAsJsonString = getFileListAsString();
                        if (TextUtils.isEmpty(fileListAsJsonString)) {
                            notifyFileFetchFailure();
                        } else {
                            JsonArray jsonArray = getFileListJsonArray(fileListAsJsonString);
                            fileList = Arrays.asList(gson.fromJson(jsonArray, FileVO[].class));
                            //Log.d(TAG, "fetchFileList() from JSON file fileList.size(): " + fileList.size());
                            loadFileDetails();
                        }
                    }
                }
            });
        }
    }

    private String getFileListAsString() {
        InputStream inputStream = context.getResources().openRawResource(R.raw.file_list);

        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
            inputStream.close();
        } catch (IOException iex) {
            Log.e(TAG, "getFileListAsString() IOException Caught, iex: " + iex);
        }

        String jsonString = writer.toString();
        //Log.d(TAG, "getFileListAsString() jsonString: " + jsonString);
        return jsonString;
    }

    private JsonArray getFileListJsonArray(String fileListAsJsonString) {
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = (JsonObject) jsonParser.parse(fileListAsJsonString);
        return jsonObject.getAsJsonArray("files");

    }

    private synchronized void loadFileDetails() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    for (FileVO fileVO : fileList) {
                        loadFileState(fileVO);
                        loadFileSize(fileVO);
                    }
                    notifyFileFetchSuccess(fileList);
                }
            });
        }
    }

    private void loadFileState(FileVO fileVO) {
        if (diskController.doesFileExist(fileVO.getUrl())) {
            if (diskController.getCurrentFileDownloadUrl() != null && fileVO.getUrl().equalsIgnoreCase(diskController.getCurrentFileDownloadUrl())) {
                fileVO.setFileState(FileVO.FileState.DOWNLOADING);
                fileVO.setBytesCompleted(diskController.getCurrentBytesCompleted());
            } else {
                fileVO.setFileState(FileVO.FileState.DOWNLOADED);
            }
        } else if (diskController.isFileVOInDownloadQueue(fileVO)) {
            fileVO.setFileState(FileVO.FileState.QUEUED);
        } else {
            fileVO.setFileState(FileVO.FileState.NOT_EXIST);
        }
    }

    private void loadFileSize(FileVO fileVO) {
        if (diskController.doesFileExist(fileVO.getUrl())) {
            if (diskController.getCurrentFileDownloadUrl() != null && fileVO.getUrl().equalsIgnoreCase(diskController.getCurrentFileDownloadUrl())) {
                fetchFileSizeFromWeb(fileVO);
            }else {
                //File exists, get the actual file size;
                File localFile = diskController.getFileByUrl(fileVO.getUrl());
                if (localFile != null) {
                    fileVO.setFileSize(localFile.length());
                }
            }
        } else {
            fetchFileSizeFromWeb(fileVO);
        }
    }

    private void fetchFileSizeFromWeb(FileVO fileVO) {
        //Doing this to avoid repeated network calls when refreshing the FileList.
        if (fileVO.getFileSize() == 0) {
            HttpURLConnection httpUrlConnection = null;
            try {
                URL url = new URL(fileVO.getUrl());
                httpUrlConnection = (HttpURLConnection) url.openConnection();
                httpUrlConnection.setRequestMethod("HEAD");
                httpUrlConnection.getInputStream();
                fileVO.setFileSize(httpUrlConnection.getContentLength());
            } catch (IOException e) {
                Log.e(TAG, "IOException thrown for file: " + fileVO.getName());
                fileVO.setFileSize(0);
            } finally {
                if (httpUrlConnection != null) {
                    httpUrlConnection.disconnect();
                }
            }
        }
    }

    public void addFileFetchListener(FileFetchListener fileFetchListener) {
        if (fileFetchListeners != null && !fileFetchListeners.contains(fileFetchListener)) {
            fileFetchListeners.add(fileFetchListener);
        }
    }

    public void removeFileFetchListener(FileFetchListener fileFetchListener) {
        if (fileFetchListeners != null && fileFetchListeners.contains(fileFetchListener)) {
            fileFetchListeners.remove(fileFetchListener);
        }
    }

    private void notifyFileFetchSuccess(final List<FileVO> fileList) {
        synchronized (fileFetchListeners) {
            if (fileFetchListeners != null && fileFetchListeners.size() > 0) {
                for (final FileFetchListener fileFetchListener : fileFetchListeners) {
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            fileFetchListener.onFileFetchSuccess(fileList);
                        }
                    });
                }
            }
        }
    }

    private void notifyFileFetchFailure() {
        synchronized (fileFetchListeners) {
            if (fileFetchListeners.size() > 0) {
                for (final FileFetchListener fileFetchListener : fileFetchListeners) {
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            fileFetchListener.onFileFetchFailure();
                        }
                    });
                }
            }
        }
    }

    @Override
    public void onDownloadProgress(final FileVO fileVO, final long bytesCompleted) {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    if (fileList != null && fileList.contains(fileVO)) {
                        fileList.get(fileList.indexOf(fileVO)).setBytesCompleted(bytesCompleted);
                        fileList.get(fileList.indexOf(fileVO)).setFileState(FileVO.FileState.DOWNLOADING);
                        notifyFileFetchSuccess(fileList);
                    }
                }
            });
        }
    }

    @Override
    public void onDownloadFinished(final FileVO fileVO) {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    if (fileList != null && fileList.contains(fileVO)) {
                        fileList.get(fileList.indexOf(fileVO)).setBytesCompleted(0);
                        fileList.get(fileList.indexOf(fileVO)).setFileState(FileVO.FileState.DOWNLOADED);
                        notifyFileFetchSuccess(fileList);
                    }
                }
            });
        }
    }

    @Override
    public void onFilesCleared() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    fetchFileList();
                }
            });
        }
    }

    @Override
    public void onDownloadFileEnqueued(final FileVO fileVO) {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    if (fileList != null && fileList.contains(fileVO)) {
                        fileList.get(fileList.indexOf(fileVO)).setFileState(FileVO.FileState.QUEUED);
                        notifyFileFetchSuccess(fileList);
                    }
                }
            });
        }
    }

    @Override
    public void onFileAlreadyInQueue(FileVO fileVO) {
        Log.d(TAG, "onFileAlreadyInQueue File Name: " + fileVO.getName());
    }
}
