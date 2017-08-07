package android.santosh.com.workpopcodechallenge.controllers;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.santosh.com.workpopcodechallenge.FileVO;
import android.santosh.com.workpopcodechallenge.interfaces.DownloadFileListener;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Santosh on 8/6/17.
 */

public class DiskController {
    private static String TAG = DiskController.class.getSimpleName();

    private Context context;
    private Handler uiHandler;
    private ExecutorService executorService;
    private List<DownloadFileListener> downloadFileListeners = Collections.synchronizedList(new ArrayList<DownloadFileListener>());

    //Used in restoring state of fileList in WorkPopController when user backs out of the app.
    private long currentBytesCompleted = 0;
    private String currentFileDownloadUrl = null;

    private static String MAIN_FOLDER_PATH = Environment.getExternalStorageDirectory() + File.separator + "WORKPOP";

    public DiskController(Context context, Handler uiHandler) {
        this.executorService = Executors.newSingleThreadExecutor();
        this.context = context;
        this.uiHandler = uiHandler;
        createMainDirectory();
    }

    private void createMainDirectory() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    File mainDirectory = new File(MAIN_FOLDER_PATH);
                    Log.d(TAG, "createMainDirectory() mainDirectory.exists()?: " + mainDirectory.exists());
                    if (!mainDirectory.exists()) {
                        mainDirectory.mkdir();
                    }
                }
            });
        }
    }

    public boolean doesFileExist(String url) {
        boolean doesFileExist = false;
        String fileName = url.substring(url.lastIndexOf('/') + 1, url.length());
        File file = new File(MAIN_FOLDER_PATH + File.separator + fileName);
        //Log.d(TAG, "Does file: " + MAIN_FOLDER_PATH + File.separator + fileName + ", exist?: " + file.exists());
        if (file.exists()) {
            doesFileExist = true;
        } else {
            doesFileExist = false;
        }

        return doesFileExist;
    }

    public File getFileByUrl(String url) {
        String fileName = url.substring(url.lastIndexOf('/') + 1, url.length());
        File file = new File(MAIN_FOLDER_PATH + File.separator + fileName);
        if (file.exists()) {
            return file;
        }
        return null;
    }

    public void downloadFile(final FileVO fileVO) {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    String fileName = fileVO.getUrl().substring(fileVO.getUrl().lastIndexOf('/') + 1, fileVO.getUrl().length());
                    File file = new File(MAIN_FOLDER_PATH + File.separator + fileName);
                    if (file.exists()) {
                        file.delete();
                    }
                    Log.d(TAG, "downloadFile, url:" + fileVO.getUrl());
                    try {
                        URL url = new URL(fileVO.getUrl());
                        URLConnection connection = url.openConnection();
                        connection.connect();
                        InputStream input = new BufferedInputStream(url.openStream());

                        OutputStream output = new FileOutputStream(file);

                        byte data[] = new byte[1024];
                        int count;
                        currentBytesCompleted = 0;
                        currentFileDownloadUrl = fileVO.getUrl();

                        notifyDownloadProgress(fileVO, currentBytesCompleted);
                        while ((count = input.read(data)) != -1) {
                            currentBytesCompleted += count;

                            notifyDownloadProgress(fileVO, currentBytesCompleted);
                            output.write(data, 0, count);
                        }

                        // Close connection
                        output.flush();
                        output.close();
                        input.close();

                        currentBytesCompleted = 0;
                        currentFileDownloadUrl = null;
                        notifyDownloadFinished(fileVO);

                    } catch (IOException iex) {
                        Log.e(TAG, "downloadFile exception caught: " + iex.getMessage());
                        iex.printStackTrace();
                    }
                }
            });
        }
    }

    public void clearFiles() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    File mainDirectory = new File(MAIN_FOLDER_PATH);
                    if (mainDirectory.exists() &&
                            mainDirectory.listFiles() != null &&
                            mainDirectory.listFiles().length > 0) {
                        for (File file : mainDirectory.listFiles()) {
                            file.delete();
                        }
                        notifyFileClear();
                    }
                }
            });
        }
    }

    public long getCurrentBytesCompleted() {
        return currentBytesCompleted;
    }

    public String getCurrentFileDownloadUrl() {
        return currentFileDownloadUrl;
    }

    private void notifyDownloadProgress(FileVO fileVO, long currentBytesCompleted) {
        if (downloadFileListeners != null && downloadFileListeners.size() > 0) {
            for (DownloadFileListener downloadFileListener : downloadFileListeners) {
                downloadFileListener.onDownloadProgress(fileVO, currentBytesCompleted);
            }
        }
    }

    private void notifyDownloadFinished(FileVO fileVO) {
        if (downloadFileListeners != null && downloadFileListeners.size() > 0) {
            for (DownloadFileListener downloadFileListener : downloadFileListeners) {
                downloadFileListener.onDownloadFinished(fileVO);
            }
        }
    }

    private void notifyFileClear(){
        if (downloadFileListeners != null && downloadFileListeners.size() > 0) {
            for (DownloadFileListener downloadFileListener : downloadFileListeners) {
                downloadFileListener.onFilesCleared();
            }
        }
    }

    public void addDownloadFileListener(DownloadFileListener downloadFileListener) {
        if (downloadFileListeners != null && !downloadFileListeners.contains(downloadFileListener)) {
            downloadFileListeners.add(downloadFileListener);
        }
    }

    public void removeDownloadFileListener(DownloadFileListener downloadFileListener) {
        if (downloadFileListeners != null && downloadFileListeners.contains(downloadFileListener)) {
            downloadFileListeners.remove(downloadFileListener);
        }
    }
}
