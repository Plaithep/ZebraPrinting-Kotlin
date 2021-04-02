package th.co.srichand.zebraprintkotlin;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Handler;
import android.view.LayoutInflater;

import java.util.logging.LogRecord;

public class LoadingDialog {

    Activity activity ;
    AlertDialog dialog;

    LoadingDialog(Activity myActivity){
        activity = myActivity;
    }

    void startLoadingDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.progress_dialog, null));
        builder.setCancelable(false);

        dialog = builder.create();
        dialog.show();
    }






    void  dismissDialog(){
        dialog.dismiss();
    }
}
