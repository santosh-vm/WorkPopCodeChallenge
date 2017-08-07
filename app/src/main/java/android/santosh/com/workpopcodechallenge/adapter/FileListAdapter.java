package android.santosh.com.workpopcodechallenge.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.santosh.com.workpopcodechallenge.FileVO;
import android.santosh.com.workpopcodechallenge.R;
import android.santosh.com.workpopcodechallenge.WorkPopAPI;
import android.santosh.com.workpopcodechallenge.interfaces.FileListClickInterface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Santosh on 8/6/17.
 */

public class FileListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static String TAG = FileListAdapter.class.getSimpleName();

    private static final long MEGABYTE = 1024L * 1024L;

    private List<FileVO> fileList = new ArrayList<>();
    private Context context;
    private FileListClickInterface fileListClickInterface;
    private WorkPopAPI workPopAPI;


    public FileListAdapter(Context context, WorkPopAPI workPopAPI, FileListClickInterface fileListClickInterface) {
        this.context = context;
        this.workPopAPI = workPopAPI;
        this.fileListClickInterface = fileListClickInterface;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_list_item, parent, false);
        return new FileListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof FileListViewHolder) {
            FileListViewHolder fileListViewHolder = (FileListViewHolder) holder;
            FileVO fileVO = fileList.get(position);

            fileListViewHolder.fileNameTextView.setText(fileVO.getName());
            fileListViewHolder.fileSizeTextView.setText(String.format(Locale.US, "%s MB", fileVO.getFileSize() / MEGABYTE));

            //Setting the visibility of all the views based on the file state
            switch (fileVO.getFileState()) {
//                case NOT_EXIST:
//                    fileListViewHolder.downloadView.setVisibility(View.VISIBLE);
//                    fileListViewHolder.openView.setVisibility(View.GONE);
//                    fileListViewHolder.queueView.setVisibility(View.GONE);
//                    fileListViewHolder.downloadedContentSizeTextView.setVisibility(View.GONE);
//                    fileListViewHolder.downloadedPercentageTextView.setVisibility(View.GONE);
//                    break;
                case DOWNLOADED:
                    fileListViewHolder.downloadView.setVisibility(View.GONE);
                    fileListViewHolder.openView.setVisibility(View.VISIBLE);
                    fileListViewHolder.queueView.setVisibility(View.GONE);
                    fileListViewHolder.downloadedContentSizeTextView.setVisibility(View.GONE);
                    fileListViewHolder.downloadedPercentageTextView.setVisibility(View.GONE);
                    break;
//                case DOWNLOADING:
//                    fileListViewHolder.downloadView.setVisibility(View.GONE);
//                    fileListViewHolder.openView.setVisibility(View.GONE);
//                    fileListViewHolder.queueView.setVisibility(View.GONE);
//                    fileListViewHolder.downloadedContentSizeTextView.setVisibility(View.VISIBLE);
//                    fileListViewHolder.downloadedContentSizeTextView.setText(String.format(Locale.US, "%s - ", fileVO.getBytesCompleted() / MEGABYTE));
//                    fileListViewHolder.downloadedPercentageTextView.setVisibility(View.VISIBLE);
//                    int percentageCompleted = (int) ((fileVO.getBytesCompleted() * 100) / fileVO.getFileSize());
//                    fileListViewHolder.downloadedPercentageTextView.setText(String.format(Locale.US, " - %s percent", percentageCompleted));
//                    break;
//                case QUEUED:
//                    fileListViewHolder.downloadView.setVisibility(View.GONE);
//                    fileListViewHolder.openView.setVisibility(View.GONE);
//                    fileListViewHolder.queueView.setVisibility(View.VISIBLE);
//                    fileListViewHolder.downloadedContentSizeTextView.setVisibility(View.GONE);
//                    fileListViewHolder.downloadedPercentageTextView.setVisibility(View.GONE);
//                    break;
//                default:
//                    break;
            }

            fileListViewHolder.downloadView.setOnClickListener(new DownloadViewClickListener(position, fileVO, fileListClickInterface));
            fileListViewHolder.openView.setOnClickListener(new FileOpenClickListener(position, fileVO));
            fileListViewHolder.rootView.setOnClickListener(new RootViewClickListener(position, fileVO, fileListClickInterface));
        }
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    public void setFileList(List<FileVO> fileList) {
        this.fileList = fileList;
        notifyDataSetChanged();
    }


    public class FileListViewHolder extends RecyclerView.ViewHolder {
        TextView fileNameTextView;
        TextView fileSizeTextView;
        TextView downloadedContentSizeTextView;
        TextView downloadedPercentageTextView;
        View downloadView;
        View openView;
        View queueView;
        View rootView;

        public FileListViewHolder(View itemView) {
            super(itemView);
            fileNameTextView = itemView.findViewById(R.id.file_name_text_view);
            fileSizeTextView = itemView.findViewById(R.id.file_size_text_view);

            downloadedContentSizeTextView = itemView.findViewById(R.id.file_downloaded_bytes_text_view);
            downloadedContentSizeTextView.setVisibility(View.GONE);

            downloadedPercentageTextView = itemView.findViewById(R.id.file_downloaded_percentage_text_view);
            downloadedPercentageTextView.setVisibility(View.GONE);

            downloadView = itemView.findViewById(R.id.download_view);
            downloadView.setVisibility(View.GONE);
            openView = itemView.findViewById(R.id.open_view);
            openView.setVisibility(View.GONE);
            queueView = itemView.findViewById(R.id.queue_view);
            queueView.setVisibility(View.GONE);
            rootView = itemView.findViewById(R.id.root_view);

        }
    }

    private class DownloadViewClickListener implements View.OnClickListener {
        private int position;
        private FileVO fileVO;
        private FileListClickInterface fileListClickInterface;

        DownloadViewClickListener(int position, FileVO fileVO, FileListClickInterface fileListClickInterface) {
            this.position = position;
            this.fileVO = fileVO;
            this.fileListClickInterface = fileListClickInterface;
        }

        @Override
        public void onClick(View view) {
            fileListClickInterface.onDownloadFileClicked(position, fileVO);
        }
    }

    private class FileOpenClickListener implements View.OnClickListener {
        private int position;
        private FileVO fileVO;

        FileOpenClickListener(int position, FileVO fileVO) {
            this.position = position;
            this.fileVO = fileVO;
        }

        @Override
        public void onClick(View view) {
            File fileToOpen = workPopAPI.getDiskController().getFileByUrl(fileVO.getUrl());
            if (fileToOpen != null) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(fileToOpen), "text/plain");
                context.startActivity(intent);
            }
        }
    }

    private class RootViewClickListener implements View.OnClickListener {
        private int position;
        private FileVO fileVO;
        private FileListClickInterface fileListClickInterface;

        RootViewClickListener(int position, FileVO fileVO, FileListClickInterface fileListClickInterface) {
            this.position = position;
            this.fileVO = fileVO;
            this.fileListClickInterface = fileListClickInterface;
        }

        @Override
        public void onClick(View view) {
            fileListClickInterface.onRootViewClicked(position, fileVO);
        }
    }
}
