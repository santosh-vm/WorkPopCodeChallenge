package android.santosh.com.workpopcodechallenge;

import android.app.Application;
import android.os.Handler;
import android.santosh.com.workpopcodechallenge.controllers.WorkPopController;

/**
 * Created by Santosh on 8/5/17.
 */

public class WorkPopApplication extends Application {
    private WorkPopAPI workPopAPI;

    @Override
    public void onCreate() {
        super.onCreate();
        WorkPopController workPopController = new WorkPopController(this, new Handler());
        workPopAPI = new WorkPopAPI(workPopController);
    }

    public WorkPopAPI getWorkPopAPI() {
        return workPopAPI;
    }
}
