package android.santosh.com.workpopcodechallenge;

/**
 * Created by Santosh on 8/5/17.
 */

public class WorkPopAPI {
    private WorkPopController workPopController;

    public WorkPopAPI(WorkPopController workPopController){
        this.workPopController = workPopController;
    }

    public WorkPopController getWorkPopController(){
        return workPopController;
    }
}
