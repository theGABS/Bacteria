package com.bacteria.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;


class myVector2d{
    public float x;
    public float y;
    myVector2d(float _x , float _y){
        x = _x;
        y = _y;
    }
    public myVector2d norm(){
        Double d = Math.sqrt(x*x+y*y);
        if(d != 0){
            x /= d;
            y /= d;
        }
        return this;
    }
    public double dist(){
        return Math.sqrt(x*x+y*y);
    }
    public void rotation(float angle){
        float lx = x;
        float ly = y;
        x = (float) (x*Math.cos(angle) - ly * Math.sin(angle));
        y = (float) (lx*Math.sin(angle) + ly * Math.cos(angle));
    }
}





enum GameMod { ACTIVE_GAME, PAUSE, MAP_MENU, LEVEL_MENU, BETWEEN_LEVEL, LOAD_GAME }

public class FullscreenActivity extends Activity {


    AdView adView;
    EditText etText;
    SharedPreferences sPref;
    static DrawView dv;

    //CopyOnWriteArrayList<Integer> passageTime = new CopyOnWriteArrayList<Integer>();




    @Override
    public void onBackPressed() {
        Log.e("","onBackPressed");
        if(dv.gameMod == GameMod.MAP_MENU){
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle("EXIT?");

            alertDialog.setMessage("Do you really want to quit?");

            alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int which) {
                    finish();
                }
            });

            alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            alertDialog.show();
        }
        dv.pause(); }

    public void swithcAdv(boolean show){
        if(adView != null) {
            if (!show) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adView.pause();
                        adView.setVisibility(View.GONE);
                        adView.setEnabled(false);
                    }
                });
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("", "we here");
                        //LinearLayout linearlayout = (LinearLayout)findViewById(R.id.mainLayout);
                        //linearlayout.startAnimation(new ViewAnimation());
                        adView.setVisibility(View.VISIBLE);
                        adView.setEnabled(true);
                    }
                });
            }
        }
    }

    public class ViewAnimation extends Animation {
        int centerX, centerY;
        @Override
        public void initialize(int width, int height, int parentWidth,
                               int parentHeight) {
            super.initialize(width, height, parentWidth, parentHeight);
            setDuration(5000);
            setFillAfter(true);
            setInterpolator(new LinearInterpolator());
            centerX = width / 2;
            centerY = height / 2;
        }
        @Override
        protected void applyTransformation(float interpolatedTime,
                                           Transformation t) {
            final Matrix matrix = t.getMatrix();
            matrix.setScale(interpolatedTime, interpolatedTime);
        }
    }



    @Override
    protected void onResume(){
        super.onResume();
        //gameMod = GameMod.MAP_MENU;
        if(dv != null){
            dv.timeout = System.currentTimeMillis();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.e("","onCreate");

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);




        setContentView(R.layout.activity_fullscreen);

        LinearLayout layout = (LinearLayout) this.findViewById(R.id.mainLayout);
        if(dv == null){
            dv = new DrawView(this,this);
            //dv.setOnTouchListener(this);
            layout.addView(dv);
        }else{
            ViewGroup parent = (ViewGroup) dv.getParent();
            parent.removeView(dv);
            layout.addView(dv);
        }

        adView = (AdView)this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("1FC018C81DA00D37BA952F5798664BED").build();

        adView.loadAd(adRequest); // ITS LOAD MY CPU
    }


}