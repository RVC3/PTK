package ru.ppr.cppk.utils.customProgressBar;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import jp.wasabeef.blurry.Blurry;
import ru.ppr.cppk.R;

public class CustomProgress {

    RotateLoading mProgressBar;
    TextView tvTimerSecond;
    LinearLayout fonProgressDialog;
   public static CustomProgress customProgress = null;
   private Dialog mDialog;

   public static CustomProgress getInstance() {
       if (customProgress == null) {
           customProgress = new CustomProgress();
       }
       return customProgress;
   }

   public void showProgress(Context context, String message, boolean cancelable) {
       mDialog = new Dialog(context);
       mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
       WindowManager.LayoutParams lp = mDialog.getWindow().getAttributes();
       int MATCH_PARENT = WindowManager.LayoutParams.MATCH_PARENT;
       lp.width = MATCH_PARENT;
       lp.height = MATCH_PARENT;
       mDialog.getWindow().setAttributes(lp);
       mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
       mDialog.setContentView(R.layout.prograss_bar_dialog);
       mProgressBar = (RotateLoading) mDialog.findViewById(R.id.rl_progress_bar);
       tvTimerSecond = (TextView) mDialog.findViewById(R.id.tv_timer_second);
       fonProgressDialog = (LinearLayout) mDialog.findViewById(R.id.fon_progress_dialog);


       mProgressBar.start();

       Blurry.with(context)
               .radius(10)
               .sampling(8)
               .color(Color.argb(66, 255, 255, 0))
               .async()
               .animate(500)
               .onto(fonProgressDialog);


       TextView progressText = (TextView) mDialog.findViewById(R.id.progress_text);
       progressText.setText("" + message);
       mDialog.setCancelable(cancelable);
       mDialog.setCanceledOnTouchOutside(cancelable);
       mDialog.show();
   }

   public void setTimerSecond(int second){
       tvTimerSecond.setText(String.valueOf(second));
   }

   public void hideProgress() {
       if(mProgressBar != null) {
           mProgressBar.stop();
           mProgressBar = null;
       }

       if (mDialog != null) {
           mDialog.dismiss();
           mDialog = null;
       }
   }
}