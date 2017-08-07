package android.santosh.com.workpopcodechallenge;

import android.app.Application;
import android.os.Handler;
import android.santosh.com.workpopcodechallenge.controllers.DiskController;
import android.santosh.com.workpopcodechallenge.controllers.WorkPopController;

/**
 * Created by Santosh on 8/5/17.
 */

public class WorkPopApplication extends Application {
    private WorkPopAPI workPopAPI;

    @Override
    public void onCreate() {
        super.onCreate();
        DiskController diskController = new DiskController(this, new Handler());
        WorkPopController workPopController = new WorkPopController(this, diskController, new Handler());
        workPopAPI = new WorkPopAPI(workPopController,diskController);
    }

    public WorkPopAPI getWorkPopAPI() {
        return workPopAPI;
    }
}
