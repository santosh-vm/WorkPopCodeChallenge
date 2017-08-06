package android.santosh.com.workpopcodechallenge.activity;

import android.santosh.com.workpopcodechallenge.FileFetchListener;
import android.santosh.com.workpopcodechallenge.FileVO;
import android.santosh.com.workpopcodechallenge.R;
import android.santosh.com.workpopcodechallenge.adapter.FileListAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends BaseActivity implements FileFetchListener{
    private static String TAG = MainActivity.class.getSimpleName();

    private ProgressBar progressBar;
    private TextView errorMessageTextView;

    private FileListAdapter fileListAdapter;
    private RecyclerView fileListRecyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getSupportActionBar()!=null){
            getSupportActionBar().setTitle("File List");
        }
        setContentView(R.layout.activity_main);
        bindUIElements();
        workPopAPI.getWorkPopController().addFileFetchListener(this);
        workPopAPI.getWorkPopController().fetchFileList();
    }

    private void bindUIElements(){
        //Progress Bar
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        //TextView
        errorMessageTextView = (TextView) findViewById(R.id.error_message_text_view);
        errorMessageTextView.setVisibility(View.GONE);

        //RecyclerView
        fileListAdapter = new FileListAdapter(this);
        fileListRecyclerView = (RecyclerView) findViewById(R.id.file_list_recycler_view);
        LinearLayoutManager fileListLinearLayoutManger = new LinearLayoutManager(this);
        fileListLinearLayoutManger.setOrientation(LinearLayoutManager.VERTICAL);
        fileListRecyclerView.setLayoutManager(fileListLinearLayoutManger);
        fileListRecyclerView.setAdapter(fileListAdapter);
        fileListRecyclerView.setVisibility(View.GONE);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        workPopAPI.getWorkPopController().removeFileFetchListener(this);
    }

    @Override
    public void onFileFetchSuccess(List<FileVO> fileList) {
        Log.d(TAG,"onFileFetchSuccess(), fileList.size(): "+fileList.size());
        progressBar.setVisibility(View.GONE);
        errorMessageTextView.setVisibility(View.GONE);

        fileListRecyclerView.setVisibility(View.VISIBLE);
        fileListAdapter.setFileList(fileList);

    }

    @Override
    public void onFileFetchFailure() {
        Log.d(TAG,"onFileFetchFailure()");
        progressBar.setVisibility(View.GONE);
        errorMessageTextView.setVisibility(View.VISIBLE);

        fileListRecyclerView.setVisibility(View.GONE);
    }
}
