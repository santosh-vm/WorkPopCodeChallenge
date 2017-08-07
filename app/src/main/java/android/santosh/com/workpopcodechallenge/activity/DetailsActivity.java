package android.santosh.com.workpopcodechallenge.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.santosh.com.workpopcodechallenge.FileVO;
import android.santosh.com.workpopcodechallenge.R;
import android.santosh.com.workpopcodechallenge.interfaces.FileFetchListener;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.util.List;
import java.util.Locale;

/**
 * Created by Santosh on 8/6/17.
 */

public class DetailsActivity extends BaseActivity implements View.OnClickListener, FileFetchListener {
    private static String TAG = DetailsActivity.class.getSimpleName();
    private static final long MEGABYTE = 1024L * 1024L;

    private FileVO fileVO;
    private String fileUrl;
    private TextView fileNameTextView;
    private TextView fileSizeTextView;
    private TextView downloadedContentSizeTextView;
    private TextView downloadedPercentageTextView;
    private Button downloadButton;
    private Button openButton;
    private TextView errorMessageTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        workPopAPI.getWorkPopController().addFileFetchListener(this);
        bindUIElements();
    }

    @Override
    protected void onResume() {
        super.onResume();
        downloadButton.setOnClickListener(this);
        openButton.setOnClickListener(this);
        fileUrl = getIntent().getStringExtra(FILE_URL_INTENT_KEY);
        if (TextUtils.isEmpty(fileUrl)) {
            errorMessageTextView.setVisibility(View.VISIBLE);
        } else {
            refreshViews();
        }
    }

    private void bindUIElements() {
        fileNameTextView = (TextView) findViewById(R.id.file_name_text_view);
        fileSizeTextView = (TextView) findViewById(R.id.file_size_text_view);

        downloadedContentSizeTextView = (TextView) findViewById(R.id.file_downloaded_bytes_text_view);
        downloadedContentSizeTextView.setVisibility(View.GONE);

        downloadedPercentageTextView = (TextView) findViewById(R.id.file_downloaded_percentage_text_view);
        downloadedPercentageTextView.setVisibility(View.GONE);

        downloadButton = (Button) findViewById(R.id.download_button);
        downloadButton.setVisibility(View.GONE);
        openButton = (Button) findViewById(R.id.open_button);
        openButton.setVisibility(View.GONE);

        errorMessageTextView = (TextView) findViewById(R.id.error_message_text_view);
        errorMessageTextView.setVisibility(View.GONE);
    }

    private void refreshViews() {
        fileVO = workPopAPI.getWorkPopController().getFileVOFromFileList(fileUrl);
        if (fileVO != null) {
            fileNameTextView.setText(fileVO.getName());
            fileSizeTextView.setText(String.format(Locale.US, "%s MB", fileVO.getFileSize() / MEGABYTE));
            toggleViews();
        } else {
            errorMessageTextView.setVisibility(View.VISIBLE);
        }
    }

    private void toggleViews() {
        //Setting the visibility of all the views based on the file state
        switch (fileVO.getFileState()) {
            case NOT_EXIST:
                downloadButton.setVisibility(View.VISIBLE);
                openButton.setVisibility(View.GONE);
                downloadedContentSizeTextView.setVisibility(View.GONE);
                downloadedPercentageTextView.setVisibility(View.GONE);
                break;
            case DOWNLOADING:
                downloadButton.setVisibility(View.GONE);
                openButton.setVisibility(View.GONE);
                downloadedContentSizeTextView.setVisibility(View.VISIBLE);
                downloadedContentSizeTextView.setText(String.format(Locale.US, "%s - ", fileVO.getBytesCompleted() / MEGABYTE));
                downloadedPercentageTextView.setVisibility(View.VISIBLE);
                int percentageCompleted = (int) ((fileVO.getBytesCompleted() * 100) / fileVO.getFileSize());
                downloadedPercentageTextView.setText(String.format(Locale.US, " - %s percent", percentageCompleted));
                break;
            case DOWNLOADED:
                downloadButton.setVisibility(View.GONE);
                openButton.setVisibility(View.VISIBLE);
                downloadedContentSizeTextView.setVisibility(View.GONE);
                downloadedPercentageTextView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        downloadButton.setOnClickListener(null);
        openButton.setOnClickListener(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        workPopAPI.getWorkPopController().removeFileFetchListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.open_button:
                File fileToOpen = workPopAPI.getDiskController().getFileByUrl(fileVO.getUrl());
                if (fileToOpen != null) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(fileToOpen), "text/plain");
                    startActivity(intent);
                }
                break;
            case R.id.download_button:
                workPopAPI.getDiskController().downloadFile(fileVO);
                break;
            default:
                break;
        }
    }

    @Override
    public void onFileFetchSuccess(List<FileVO> fileList) {
        refreshViews();
    }

    @Override
    public void onFileFetchFailure() {
        refreshViews();
    }
}
