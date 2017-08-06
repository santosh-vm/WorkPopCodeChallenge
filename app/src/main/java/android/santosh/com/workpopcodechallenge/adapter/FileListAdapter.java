package android.santosh.com.workpopcodechallenge.adapter;

import android.content.Context;
import android.santosh.com.workpopcodechallenge.FileVO;
import android.santosh.com.workpopcodechallenge.R;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Santosh on 8/6/17.
 */

public class FileListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static String TAG = FileListAdapter.class.getSimpleName();

    private List<FileVO> fileList = new ArrayList<>();
    private Context context;


    public FileListAdapter(Context context) {
        this.context = context;
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
        }
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    public void setFileList(List<FileVO> fileList){
        this.fileList = fileList;
        notifyDataSetChanged();
    }


    public class FileListViewHolder extends RecyclerView.ViewHolder {
        TextView fileNameTextView;

        public FileListViewHolder(View itemView) {
            super(itemView);
            fileNameTextView = itemView.findViewById(R.id.file_name_text_view);
        }
    }
}
