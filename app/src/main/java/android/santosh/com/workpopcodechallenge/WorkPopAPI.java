package android.santosh.com.workpopcodechallenge;

import android.santosh.com.workpopcodechallenge.controllers.DiskController;
import android.santosh.com.workpopcodechallenge.controllers.WorkPopController;

/**
 * Created by Santosh on 8/5/17.
 */

public class WorkPopAPI {
    private WorkPopController workPopController;
    private DiskController diskController;

    public WorkPopAPI(WorkPopController workPopController, DiskController diskController){
        this.workPopController = workPopController;
        this.diskController = diskController;
    }

    public WorkPopController getWorkPopController(){
        return workPopController;
    }

    public DiskController getDiskController(){
        return diskController;
    }
}
