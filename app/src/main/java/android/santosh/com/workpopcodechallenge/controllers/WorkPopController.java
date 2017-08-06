package android.santosh.com.workpopcodechallenge.controllers;

import android.content.Context;
import android.os.Handler;
import android.santosh.com.workpopcodechallenge.FileFetchListener;
import android.santosh.com.workpopcodechallenge.FileVO;
import android.santosh.com.workpopcodechallenge.R;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
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

public class WorkPopController {
    private static String TAG = WorkPopController.class.getSimpleName();
    private Context context;
    private Handler uiHandler;
    private Gson gson;
    private List<FileVO> fileList = new ArrayList<>();
    private ExecutorService executorService;
    private List<FileFetchListener> fileFetchListeners = Collections.synchronizedList(new ArrayList<FileFetchListener>());

    public WorkPopController(Context context, Handler uiHandler) {
        this.executorService = Executors.newSingleThreadExecutor();
        this.uiHandler = uiHandler;
        this.context = context;
        gson = new Gson();
    }

    public void fetchFileList() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    if (fileList != null && fileList.size() > 0) {
                        Log.d(TAG, "fetchFileList() from memory fileList.size(): " + fileList.size());
                        loadFileSize();
                    } else {
                        String fileListAsJsonString = getFileListAsString();
                        if (TextUtils.isEmpty(fileListAsJsonString)) {
                            notifyFileFetchFailure();
                        } else {
                            JsonArray jsonArray = getFileListJsonArray(fileListAsJsonString);
                            fileList = Arrays.asList(gson.fromJson(jsonArray, FileVO[].class));
                            Log.d(TAG, "fetchFileList() from JSON file fileList.size(): " + fileList.size());
                            loadFileSize();
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
        //Log.d(TAG, "getFileListJsonArray, jsonArray: " + jsonArray);
        return jsonObject.getAsJsonArray("files");

    }

    private void loadFileSize() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    for (FileVO fileVO : fileList) {
                        //TODO: check if the file exists locally and load the file size from that.
                        HttpURLConnection httpUrlConnection = null;
                        try {
                            URL url = new URL(fileVO.getUrl());
                            httpUrlConnection = (HttpURLConnection) url.openConnection();
                            httpUrlConnection.setRequestMethod("HEAD");
                            httpUrlConnection.getInputStream();
                            //fileList.get(fileList.indexOf(fileVO)).setFileSize(httpUrlConnection.getContentLength());
                            fileVO.setFileSize(httpUrlConnection.getContentLength());
                        } catch (IOException e) {
                            Log.e(TAG, "IOException thrown for file: " + fileVO.getName());
                            //fileList.get(fileList.indexOf(fileVO)).setFileSize(0);
                            fileVO.setFileSize(0);
                        } finally {
                            if (httpUrlConnection != null) {
                                httpUrlConnection.disconnect();
                            }
                        }
                    }
                    notifyFileFetchSuccess(fileList);
                }
            });
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

    private void notifyFileFetchFailure() {
        if (fileFetchListeners != null && fileFetchListeners.size() > 0) {
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
