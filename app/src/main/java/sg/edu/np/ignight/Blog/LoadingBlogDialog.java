package sg.edu.np.ignight.Blog;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;

import sg.edu.np.ignight.R;

public class LoadingBlogDialog {
    Activity activity;
    AlertDialog dialog;


    public LoadingBlogDialog(Activity a){
        activity = a;
    }

    // Creates custom loading bar
    public void startLoadingDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.custom_dialog, null));
        builder.setCancelable(false);
        dialog = builder.create();
        dialog.show();

    }

    // Destroys loading bar
    public void dismissDialog(){
        dialog.dismiss();
    }
}
