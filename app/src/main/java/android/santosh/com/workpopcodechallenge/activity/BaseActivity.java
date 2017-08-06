package android.santosh.com.workpopcodechallenge.activity;

import android.os.Bundle;
import android.santosh.com.workpopcodechallenge.WorkPopAPI;
import android.santosh.com.workpopcodechallenge.WorkPopApplication;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Santosh on 8/5/17.
 */

public class BaseActivity extends AppCompatActivity {
    protected WorkPopAPI workPopAPI;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        workPopAPI = ((WorkPopApplication)getApplication()).getWorkPopAPI();
    }
}
