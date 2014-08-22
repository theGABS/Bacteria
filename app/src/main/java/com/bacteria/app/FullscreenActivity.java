package com.bacteria.app;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;


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
            /*AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
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

            alertDialog.show();*/
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
                        adView.setVisibility(View.VISIBLE);
                        adView.setEnabled(true);
                    }
                });
            }
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

        Log.e("","start Game");

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

    static class DrawView extends SurfaceView implements SurfaceHolder.Callback, View.OnTouchListener {




        public void DrawMapMenu(Canvas canvas, Paint p){
            canvas.drawBitmap(fonImgB, 0, 0, p);
            for(int i=0; i < myButtons.size() && i < 3; i++) {
                myButton b = myButtons.get(i);
                b.draw(canvas,p);
                if(i != 0) {
                    if (b.click) {
                        gameMod = GameMod.LEVEL_MENU;
                        survival = false;
                        mapLevel = i;
                        b.click = false;
                        needInit = true;
                    }
                }else{
                    if (b.click) {
                        gameMod = GameMod.ACTIVE_GAME;
                        gameLevel = 30;
                        survival = true;
                        b.click = false;
                        needInit = true;
                    }
                }
            }
        }

        public void DrawBetweenLevel(Canvas canvas, Paint p){
            int time = (int) (System.currentTimeMillis() - startTimeInit);
            canvas.drawBitmap(fonImgB, 0, 0, p);


            for (int i = 0; i < bact.size(); i++) {
                Bacteria b = bact.get(i);
                int s = 0;

                Bitmap tmpBitmap = bactImg[b.team][s][b.paintTypeRadius];
                canvas.drawBitmap(tmpBitmap, b.x - tmpBitmap.getWidth() / 2, b.y - tmpBitmap.getWidth() / 2, p);
            }

            for(int i = 0; i < virus.size(); i++){
                Virus l = virus.get(i);
                canvas.drawBitmap(virusImg[l.team][0] , l.x - 6 , l.y - 6 , p);

            }


            p.setAlpha(Math.min(255 , time/3));
            Log.e("", Integer.toString(p.getAlpha()));



            canvas.drawBitmap(borderImg,0,0,p);

            canvas.drawText("Your result " + Long.toString((finishTimeLevel - startTimeLevel)/1000) + ","
                    + Long.toString((finishTimeLevel - startTimeLevel) % 1000) + "s" , (int)(0.5*metrics.widthPixels) , (float) (0.16*metrics.heightPixels), p);

            canvas.drawText("Green level - " + Integer.toString(gameLevels.get(gameLevel).times[0]) + " Blue level - " + Integer.toString(gameLevels.get(gameLevel).times[1]) , (int)(0.5*metrics.widthPixels) , (float) (0.20*metrics.heightPixels), p);


            for (myButton b : myButtons) {
                b.show = true;
                b.draw(canvas, p);
                if (b.click) {
                    b.click = false;
                    gameMod = GameMod.ACTIVE_GAME;
                    if(survival){
                        numberOfAttackBact = 0;
                        timeToAtacBact = 7000;
                    }
                    needInit = true;
                    if(playerWin) {
                        gameLevel++;
                    }
                }
            }

            if(Math.min(255 , time/4) < 255) {
                p.setAlpha(255);
                dv.drawThread.update = true;
            }
        }

        public void DrawPause(Canvas canvas, Paint p){
            int time = timeFromInit;
            canvas.drawBitmap(fonImgB, 0, 0, p);

            for (int i = 0; i < bact.size(); i++) {
                Bacteria b = bact.get(i);
                int s = 0;
                if (b.select || b.tmpSelect) {
                    s = 1;
                }
                Bitmap tmpBitmap = bactImg[b.team][s][b.paintTypeRadius];
                if (b.drawBorder > 0) {
                    p.setColor(Color.argb((int) b.drawBorder * 3, bactColor[b.team][0], bactColor[b.team][1], bactColor[b.team][2]));
                    canvas.drawCircle(b.x, b.y, bactImg[b.team][1][b.paintTypeRadius].getWidth() / 2 + (float) (5 + b.drawBorder * 0.3), p);
                    p.setColor(Color.BLACK);
                }

                canvas.drawBitmap(tmpBitmap, b.x - tmpBitmap.getWidth() / 2, b.y - tmpBitmap.getWidth() / 2, p);
            }

            for(int i = 0; i < virus.size(); i++){
                Virus l = virus.get(i);
                canvas.drawBitmap(virusImg[l.team][0] , l.x - 6 , l.y - 6 , p);

            }



            p.setAlpha(Math.min(255 , time/3));
            Log.e("", Integer.toString(p.getAlpha()) + " " + Long.toString(timeFromInit) + " " + Long.toString(startTimeInit));
            canvas.drawBitmap(borderImg,0,0,p);


            for(int i = 0; i < myButtons.size(); i++) {
                myButton b = myButtons.get(i);
                b.show = true;
                b.draw(canvas,p);
                if(b.click){

                    switch (i){
                        case 0:
                            gameMod = GameMod.ACTIVE_GAME;
                            break;
                        case 1:
                            gameMod = GameMod.ACTIVE_GAME;
                            needInit = true;
                            break;
                        case 2:
                            gameMod = GameMod.MAP_MENU;
                            needInit = true;
                            break;
                    }
                    b.click = false;
                }
            }

            if(Math.min(255 , time/4) < 255) {
                p.setAlpha(255);
                dv.drawThread.update = true;
            }
        }

        public void DrawActiveGame(Canvas canvas, Paint p , float timeLag, int width , int height){
            canvas.drawBitmap(fonImgB, 0, 0, p);

            playerWin = true;
            playerLose = true;
            for(Bacteria b : bact){
                if(b.team != playerIndexBact){
                    playerWin = false;
                }else{
                    playerLose = false;
                }
            }

            for(Virus l : virus){
                if(l.team != playerIndexBact){
                    playerWin = false;
                }else{
                    playerLose = false;
                }
            }


            for (int i = 0; i < bact.size(); i++) {
                Bacteria b = bact.get(i);
                int s = 0;
                if (b.select || b.tmpSelect) {
                    s = 1;
                }
                Bitmap tmpBitmap = bactImg[b.team][s][b.paintTypeRadius];
                if (b.drawBorder > 0) {
                    p.setColor(Color.argb((int)b.drawBorder * 3, bactColor[b.team][0], bactColor[b.team][1], bactColor[b.team][2]));
                    canvas.drawCircle(b.x, b.y, bactImg[b.team][1][b.paintTypeRadius].getWidth() / 2 + (float) (5 + b.drawBorder * 0.3), p);
                    p.setColor(Color.BLACK);
                }

                canvas.drawBitmap(tmpBitmap, b.x - tmpBitmap.getWidth() / 2, b.y - tmpBitmap.getWidth() / 2, p);
                canvas.drawText(Integer.toString((int) b.count), b.x, b.y + WHmin/75 , p);

                float dist;
                float minDist = 9999999;
                int curKey = -1;


                for (int j = 0; j < bact.size(); j++) {
                    if(i == j) continue;
                    Bacteria bj = bact.get(j);
                    dist = distBetween(b.x, b.y, bj.x, bj.y) - (b.radius + bj.radius);
                    if (dist < minDist) {
                        minDist = dist;
                        curKey = j;
                    }

                    timeCorrect += 16*timeLag;
                    while(timeCorrect > 0) {
                        timeCorrect -= 16;
                        if (b.team != playerIndexBact && bj.team != b.team && b.team != xxxReplaceIt) {
                            if (rn.nextInt(1000) < gameHardLevel &&
                                    dist / overWidth < rn.nextInt(1000) * rn.nextInt(1000) / 1000 &&
                                    b.count / b.maxCount * 10000 > 1000 + rn.nextInt(100) * rn.nextInt(100) &&
                                    b.count - bj.count > 10 - (rn.nextInt(20) * rn.nextInt(20) / 20)
                                    ) {
                                b.fire(j, i);
                            }
                        }
                    }
                }


                if (minDist < 0) {
                    myVector2d vec = new myVector2d(bact.get(curKey).x - b.x, bact.get(curKey).y - b.y).norm();

                    b.dx -= vec.x;
                    b.dy -= vec.y;

                    bact.get(curKey).dx += vec.x;
                    bact.get(curKey).dy += vec.y;
                }


                if (b.x + b.radius > width)  b.dx -= 0.5;
                if (b.y + b.radius > height) b.dy -= 0.5;
                if (b.x - b.radius < 0)      b.dx += 0.5;
                if (b.y - b.radius < 0)      b.dy += 0.5;

                b.move(timeLag);

                for (int j = virus.size() - 1; j >= 0; j--) {
                    Virus l = virus.get(j);
                    dist = distBetween(l.x, l.y, b.x ,b.y);
                    if (dist < b.radius) {
                        if (i == l.target && dist < b.radius) {

                            if (b.team == l.team) {
                                b.count++;
                            } else {
                                b.count--;
                                if(b.lastNotif > 32){
                                    b.lastNotif = 0;
                                    floatingNumber tmp = new floatingNumber();
                                    tmp.x = (int)b.x+(rn.nextInt(300)-150)*overWidth;
                                    tmp.y = (int)b.y+(rn.nextInt(300)-150)*overWidth*0;
                                    tmp.team = l.team;
                                    tmp.life = 1500;
                                    tmp.number = (int)b.count+1;
                                    floatingNumbers.add(tmp);
                                }
                                if(b.count < 1) { // capture
                                    b.count = 0;
                                    b.team = l.team;
                                    b.select = b.tmpSelect = false;
                                    b.drawBorder = 60;
                                }
                            }

                            myVector2d vec = new myVector2d(b.x - l.x, b.y - l.y).norm();

                            b.dx += vec.x * 0.01 * (b.radius-dist);  // important
                            b.dy += vec.y * 0.01 * (b.radius-dist);

                            virus.remove(j);

                        } else if (!(l.parent == i || l.target == i)) {
                            myVector2d vec = new myVector2d(b.x - l.x, b.y - l.y).norm();
                            float g = (b.radius + 20 - dist) / 30;
                            l.dx += -vec.x * g;
                            l.dy += -vec.y * g;
                        }
                    }
                }
            }

            for(int i = floatingNumbers.size()-1; i >= 0; i--){
                floatingNumber n = floatingNumbers.get(i);
                p.setColor(Color.rgb(bactColor[n.team][0] , bactColor[n.team][1] , bactColor[n.team][2]));
                p.setAlpha((int) (255*n.life/1500));
                canvas.drawText(Integer.toString(n.number) , n.x , n.y , p);
                n.life -= 16*timeLag;
                n.y -= WHmin/360.0*timeLag;
                if(n.life < 0) floatingNumbers.remove(i);
            }
            p.setAlpha(255);

            p.setStrokeWidth((float) (bactRadius[0] * 0.1));
            p.setColor(Color.WHITE);
            if (canDrawLine) {
                for (Bacteria b : bact) {
                    if (b.select) canvas.drawLine(mouseX, mouseY, b.x, b.y, p);
                }
            }
            p.setColor(Color.BLACK);

            for(int i = 0; i < virus.size(); i++){
                Virus l = virus.get(i);
                l.move(timeLag);
                if(l.drawBorder < 20){l.borderGrow = true;}
                if(l.drawBorder > 234){l.borderGrow = false;}
                if(l.borderGrow){
                    l.drawBorder += rn.nextInt(20)*timeLag;
                }else {
                    l.drawBorder -= rn.nextInt(20)*timeLag;
                }
                l.drawBorder = Math.min(255,Math.max(0,l.drawBorder));



                canvas.drawBitmap(virusImg[l.team][(int)l.drawBorder/16],l.x-virusImg[0][0].getWidth()/2, l.y-virusImg[0][0].getWidth()/2 , p);

            }

            if(gameLevel == 1){
                float t = System.currentTimeMillis() - startTimeLevel;
                int x = (int) (bact.get(0).x + (bact.get(1).x - bact.get(0).x)*( 1 + Math.sin(t*0.001))/2.0 - WHmin/5*256/600.0);
                int y = (int) (bact.get(0).y + (bact.get(1).y - bact.get(0).y)*( 1 + Math.sin(t*0.001))/2.0);
                canvas.drawBitmap(fingerImg,x , y,p);


            /*floatingNumber tmp = new floatingNumber();
            tmp.x = (float) (x + WHmin/5*256/600.0) + rn.nextInt(50)-25;
            tmp.y = y + rn.nextInt(50)-25 ;
            tmp.team = rn.nextInt(3);
            tmp.life = 1500;
            tmp.number = 0;
            floatingNumbers.add(tmp);*/


            }

            if(survival) {

                timeToAtacBact -= 16 * timeLag;
                timeSurvival += 16 * timeLag;
                canvas.drawText(Integer.toString(timeToAtacBact / 1000), 200, 200, p);
                if (timeToAtacBact < 0) {

                    BactАttack attack;
                    if(bactАttacks.size() > numberOfAttackBact) {

                        attack = bactАttacks.get(numberOfAttackBact);
                        numberOfAttackBact++;


                        String[] AAA = attack.all.split(",");

                        timeToAtacBact = Integer.parseInt(AAA[0]);
                        for(int j=1; j < AAA.length; j++) {
                            String[] CCC = AAA[j].split("/");

                            int x = (int) (metrics.widthPixels*(-0.2) + metrics.widthPixels*(Integer.parseInt(CCC[0]) % 2)*1.4);
                            int y = (int) (metrics.heightPixels*(-0.2) + metrics.heightPixels*(Integer.parseInt(CCC[0]) / 2)*1.4);
                            for (int i = 0; i < Integer.parseInt(CCC[1]); i++) {
                                Virus tmp = new Virus();
                                tmp.dx = tmp.dy = 0;
                                tmp.x = x;
                                tmp.y = y;
                                tmp.team = Integer.parseInt(CCC[0]);
                                tmp.target = Integer.parseInt(CCC[2]);
                                tmp.parent = 0;
                                virus.add(tmp);
                            }
                        }
                    }
                }
                if (playerLose) {
                    gameMod = GameMod.BETWEEN_LEVEL;
                    needInit = true;
                }
            }else { // not survival

                if (playerLose || playerWin) {
                    finishTimeLevel = System.currentTimeMillis();
                    gameMod = GameMod.BETWEEN_LEVEL;

                /*if (passageTime.size() <= gameLevel) {
                    passageTime[gameLevel] = ((int) ((finishTimeLevel - startTimeLevel) / 1000));
                } else {
                    int time = passageTime.get(gameLevel);
                    if (time > (int) ((finishTimeLevel - startTimeLevel) / 1000)) {
                        passageTime.set(gameLevel, (int) ((finishTimeLevel - startTimeLevel) / 1000));
                    }
                }*/

                    if(passageTime[gameLevel] > 0){ // it means your once play this level
                        if(passageTime[gameLevel] > (int) ((finishTimeLevel - startTimeLevel) / 1000)){
                            passageTime[gameLevel] = (int) ((finishTimeLevel - startTimeLevel) / 1000);
                        }
                    }else{
                        passageTime[gameLevel] = (int) ((finishTimeLevel - startTimeLevel) / 1000);
                    }

                    activity.sPref = activity.getPreferences(MODE_PRIVATE);
                    SharedPreferences.Editor ed = activity.sPref.edit();

                    String str = "";
                    for (int i = 0; i < 30; i++) {
                        str += Integer.toString(passageTime[i]) + " ";
                    }

                    ed.putString("passage_time_1", str);
                    ed.commit();
                    needInit = true;

                }
            }
        }

        public void myInit(){
            startTimeInit = System.currentTimeMillis();
            switch (gameMod){
                case LEVEL_MENU:
                    activity.swithcAdv(true);
                    myButtons.clear();
                    for(int ii = 0; ii < 15; ii++){
                        int i = ii;
                        if(mapLevel == 2){
                            i = ii + 15;
                        }
                        myButton b = new myButton();
                        b.maxTimePress = 1;

                        b.dx = (int) (-WHmin/4*0.05);
                        b.dy = (int) (-WHmin/4*0.05);

                        if(passageTime[i] > 0){

                            if(passageTime[i] < gameLevels.get(i).times[0]){
                                b.just = iconLevel[0][0];
                                b.active = iconLevel[0][1];
                            }else if(passageTime[i] < gameLevels.get(i).times[1]){
                                b.just = iconLevel[1][0];
                                b.active = iconLevel[1][1];
                            }else{
                                b.just = iconLevel[2][0];
                                b.active = iconLevel[2][1];
                            }
                        }else{
                            b.just = iconLevel[3][0];
                            b.active = iconLevel[3][1];
                        }

                        b.timePress = 0;
                        int width = metrics.widthPixels;
                        int height = heightView;
                        int margin = (int) (width*0.2 - b.width())/2;
                        b.x = margin + (int) (width * 0.2 * (ii % 5)  );
                        b.y = margin + (int) ((ii / 5) * 0.5 * (height-margin*2-b.height()) );

                        myButtons.add(b);
                    }
                    break;
                case MAP_MENU:
                    activity.swithcAdv(true);
                    myButtons.clear();
                    for(int i = 0; i < 3; i++){
                        myButton b =  new myButton();
                        b.maxTimePress = 1;
                        b.just = GameMap[i][0];
                        b.active = GameMap[i][1];

                        switch (i){
                            case 0:
                                b.x = 0;
                                b.y = 0;
                                break;
                            case 1:
                                b.x = 0;
                                b.y = GameMap[0][0].getHeight();
                                break;
                            case 2:
                                b.x = metrics.widthPixels - GameMap[1][0].getWidth();
                                b.y = GameMap[0][0].getHeight();
                                break;
                        }

                        b.timePress = 0;
                        myButtons.add(b);
                    }

                    break;
                case ACTIVE_GAME:
                    Log.e("" , Integer.toString(gameLevel));

                    activity.swithcAdv(false);
                    bact.clear();
                    virus.clear();
                    myButtons.clear();
                    for(int i = 0; i < gameLevels.get(gameLevel).one.size(); i++){
                        Bacteria newbact = new Bacteria();
                        newbact.x = gameLevels.get(gameLevel).one.get(i).x;
                        newbact.y = gameLevels.get(gameLevel).one.get(i).y;
                        newbact.team = gameLevels.get(gameLevel).one.get(i).team;
                        newbact.paintTypeRadius = gameLevels.get(gameLevel).one.get(i).paintTypeRadius;
                        newbact.radius = bactRadius[newbact.paintTypeRadius];
                        newbact.maxCount = 16 + newbact.paintTypeRadius*16;
                        bact.add(newbact);
                    }


                    startTimeLevel = System.currentTimeMillis();
                    break;
                case BETWEEN_LEVEL:
                    activity.swithcAdv(true);
                    myButtons.clear();
                    myButton b =  new myButton();
                    b.just = buttonNextLevel;
                    b.active = buttonNextLevel;
                    b.nWidth = (int) (metrics.widthPixels*0.6);
                    b.nHeight = (int) (metrics.widthPixels*0.6*0.2);
                    if(playerWin) {
                        b.text = "Next level";
                    }else{
                        b.text = "Try again";
                    }
                    b.render();
                    b.click = false;b.show = false;

                    b.x = (int) (metrics.widthPixels*0.20);
                    b.y = (int) (metrics.heightPixels - b.just.getHeight()*2.5);


                    b.timePress = 0;
                    myButtons.add(b);
                    break;
                case PAUSE:
                    myButtons.clear();
                    for(int i = 0; i < 3; i++){
                        b = new myButton();
                        b.just = buttonNextLevel;
                        b.active = buttonNextLevel;
                        b.click = false;b.show = false;
                        b.nWidth = (int) (metrics.widthPixels*0.6);
                        b.nHeight = (int) (metrics.widthPixels*0.6*0.2);

                        switch (i){
                            case 0:
                                b.text = "Continues";
                                break;
                            case 1:
                                b.text = "Restart";
                                break;
                            case 2:
                                b.text = "Go to menu";
                        }

                        b.render();

                        b.x = (int) (metrics.widthPixels*0.20);
                        //b.y = (int) (metrics.heightPixels - b.just.getHeight()*2.5 - b.just.getHeight()*1.5*i);
                        b.y = (int) (metrics.widthPixels*60/800 + b.just.getHeight()*1.3*i);
                        b.timePress = 0;
                        myButtons.add(b);
                    }
            }
            if(dv != null){
                if(dv.drawThread != null) {
                    dv.drawThread.update = true;
                }
            }
        }



        public void pause(){
            Log.e("some1","pauseWork " + gameMod.toString() + " " + Integer.toString(gameMod.hashCode()));
            switch (gameMod){
                case ACTIVE_GAME:
                    gameMod = GameMod.PAUSE;
                    break;
                case LEVEL_MENU:
                    gameMod = GameMod.MAP_MENU;
                    break;
            }
            needInit = true;
        }

        public boolean onTouch(View v, MotionEvent event) {

            float x = event.getX();
            float y = event.getY();
            boolean oneSelect , bactClick;

            mouseX = x;
            mouseY = y;

            if( dv != null) dv.drawThread.update(); // maybe it's don`t need

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    for(myButton b : myButtons){
                        if (!b.show) continue;
                        if (mouseX > b.x && mouseX < b.x + b.width() && mouseY > b.y && mouseY < b.y + b.height()) {
                            if(Color.alpha(b.just.getPixel((int)mouseX - b.x, (int)mouseY - b.y)) > 100 ) b.pressed = true;
                        }
                    }

                    canDrawLine = true;
                    bactClick = false;

                    for( int i = 0; i < bact.size(); i++){
                        Bacteria b = bact.get(i);
                        if( (b.x - x)*(b.x - x) + (b.y - y)*(b.y - y) < b.radius*b.radius ){
                            bactClick = true;
                            if(b.select || b.team != playerIndexBact) {
                                for (int j = 0; j < bact.size(); j++) {
                                    if (bact.get(j).select && bact.get(j).team == playerIndexBact && i!=j) {
                                        bact.get(j).fire(i, j);
                                        b.tmpSelect = b.select = false;
                                        bact.get(j).select = bact.get(j).tmpSelect = false;
                                    }
                                }
                            }else if(b.team == playerIndexBact) { b.select = true;}
                        }
                    }

                    if(!bactClick){
                        for (Bacteria b : bact) {
                            b.select = false;
                        }
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    oneSelect = false;
                    for (Bacteria b : bact) {
                        if (b.select) {
                            oneSelect = true;
                            break;
                        }
                    }

                    for (Bacteria b : bact) {
                        b.tmpSelect = false;
                        if ((b.x - x) * (b.x - x) + (b.y - y) * (b.y - y) < b.radius * b.radius) {
                            if (b.team == playerIndexBact) {
                                b.select = true;
                                b.drawBorder = Math.max(b.drawBorder , 30);
                            } else if (oneSelect){
                                b.tmpSelect = true;
                            }
                        }
                    }

                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    for(myButton b : myButtons){
                        if (mouseX > b.x && mouseX < b.x + b.width() && mouseY > b.y && mouseY < b.y + b.height()) {
                            if(b.pressed && b.show) {
                                if (Color.alpha(b.just.getPixel((int) mouseX - b.x, (int) mouseY - b.y)) > 100){
                                    b.click = true;
                                }
                            }
                        }
                        b.pressed = false;
                    }

                    canDrawLine = false;
                    for( int i = 0; i < bact.size(); i++){
                        Bacteria b = bact.get(i);
                        if( (b.x - x)*(b.x - x) + (b.y - y)*(b.y - y) < b.radius*b.radius ){
                            if(b.team != playerIndexBact){
                                for( int j = 0; j < bact.size(); j++){
                                    if(bact.get(j).select && bact.get(j).team == playerIndexBact){
                                        bact.get(j).fire(i,j);
                                        bact.get(j).select = bact.get(j).tmpSelect = false;
                                        b.tmpSelect = b.select = false;
                                    }
                                }
                            }
                        }
                    }

                    break;
            }
            return true;
        }

        class myButton{
            public int x1 = 14;
            public int y1 = 14;
            public int x2 = 430;
            public int y2 = 62;
            public Bitmap just;
            public Bitmap active;
            public int nWidth, nHeight;
            public int timePress , x , y;
            public int dx,dy;
            public int maxTimePress = 500;
            public boolean pressed = false;
            public boolean click = false;
            public boolean show = true;
            public String text = "";
            public void draw(Canvas canvas , Paint p){
                if(maxTimePress < 16 || true){
                    if(timePress > 0){
                        canvas.drawBitmap(active, x + dx, y + dy , p);
                    }else{
                        canvas.drawBitmap(just,x,y,p);
                    }
                }else {
                    p.setAlpha(255);
                    canvas.drawBitmap(just, x, y, p);
                    if (255 * timePress / maxTimePress > 0) {
                        p.setAlpha(255 * timePress / maxTimePress);
                        canvas.drawBitmap(active, x + dx, y + dy, p);
                    }
                    p.setAlpha(255);
                }

            }
            int width(){
                return just.getWidth();
            }
            int height(){
                return just.getHeight();
            }

            void render(){
                Paint p = new Paint();
                Bitmap tmp = Bitmap.createBitmap(nWidth , nHeight, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(tmp);
                Rect rect = new Rect(0,0,x1,y1);


                int RW = nWidth - width();
                int RH = nHeight - height();


                canvas.drawBitmap(just, rect , rect ,p);

                canvas.drawBitmap(just, new Rect(x1,0,x2,y1) , new Rect(x1,0,x2+RW , y1) ,p);

                canvas.drawBitmap(just, new Rect(x2,0,width(),y1) , new Rect(x2 + RW,0, nWidth , y1) ,p);

                canvas.drawBitmap(just, new Rect(0, y1, x1 ,y2) , new Rect(0, y1, x1 , y2 + RH) ,p);

                canvas.drawBitmap(just, new Rect(x1, y1, x2 ,y2) , new Rect(x1,y1, x2 + RW , y2 + RH) ,p);

                canvas.drawBitmap(just, new Rect(x2, y1, width() ,y2) , new Rect(x2 + RW ,y1, nWidth , y2 + RH) ,p);

                canvas.drawBitmap(just, new Rect(0, y2, x1 ,height()) , new Rect(0, y2 + RH, x1 , nHeight) ,p);

                canvas.drawBitmap(just, new Rect(x1, y2, x2, height()) , new Rect(x1, y2 + RH, x2 + RW , nHeight) ,p);

                canvas.drawBitmap(just, new Rect(x2, y2, width(), height()) , new Rect(x2 + RW, y2 + RH, nWidth , nHeight) ,p);

                p.setTypeface(fontFace);
                p.setFlags(Paint.ANTI_ALIAS_FLAG);
                p.setTextAlign(Paint.Align.CENTER);
                p.setTextSize(WHmin/25);
                canvas.drawText(text , nWidth/2 , nHeight/2 , p);






                Bitmap tmp2 = Bitmap.createBitmap((int) (nWidth*1.1), (int) (nHeight*1.1), Bitmap.Config.ARGB_8888);
                canvas = new Canvas(tmp2);
                rect = new Rect(0,0,x1,y1);



                RW = (int) (nWidth + nHeight*0.1 - width());
                RH = (int) (nHeight*1.1 - height());

                dx = (int) (-nHeight*0.05);
                dy = (int) (-nHeight*0.05);

                nWidth += nHeight*0.1;
                nHeight *= 1.1;




                canvas.drawBitmap(just, rect , rect ,p);

                canvas.drawBitmap(just, new Rect(x1,0,x2,y1) , new Rect(x1,0,x2+RW , y1) ,p);

                canvas.drawBitmap(just, new Rect(x2,0,width(),y1) , new Rect(x2 + RW,0, nWidth , y1) ,p);

                canvas.drawBitmap(just, new Rect(0, y1, x1 ,y2) , new Rect(0, y1, x1 , y2 + RH) ,p);

                canvas.drawBitmap(just, new Rect(x1, y1, x2 ,y2) , new Rect(x1,y1, x2 + RW , y2 + RH) ,p);

                canvas.drawBitmap(just, new Rect(x2, y1, width() ,y2) , new Rect(x2 + RW ,y1, nWidth , y2 + RH) ,p);

                canvas.drawBitmap(just, new Rect(0, y2, x1 ,height()) , new Rect(0, y2 + RH, x1 , nHeight) ,p);

                canvas.drawBitmap(just, new Rect(x1, y2, x2, height()) , new Rect(x1, y2 + RH, x2 + RW , nHeight) ,p);

                canvas.drawBitmap(just, new Rect(x2, y2, width(), height()) , new Rect(x2 + RW, y2 + RH, nWidth , nHeight) ,p);

                p.setTypeface(fontFace);
                p.setFlags(Paint.ANTI_ALIAS_FLAG);
                p.setTextAlign(Paint.Align.CENTER);
                p.setTextSize(WHmin/25);
                canvas.drawText(text , nWidth/2 , nHeight/2 , p);

                just = tmp;
                active = tmp2;

            }
        }

        GameMod gameMod = GameMod.LOAD_GAME;


        public Bitmap scaleToWidth(Bitmap b , int width){
            return Bitmap.createScaledBitmap(b , width, width*b.getHeight()/b.getWidth() , true);
        }

        public Bitmap bitmapLoadRaw(int raw){
            return  BitmapFactory.decodeStream(getResources().openRawResource(raw));
        }
        public float distBetween(float x1, float y1, float x2, float y2){
            return (float) Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
        }

        public int[] passageTime = new int [30];
        CopyOnWriteArrayList<Bacteria> bact = new CopyOnWriteArrayList<Bacteria>();
        CopyOnWriteArrayList<Virus> virus = new CopyOnWriteArrayList<Virus>();
        CopyOnWriteArrayList<GameLevel> gameLevels = new CopyOnWriteArrayList<GameLevel>();
        CopyOnWriteArrayList<myButton> myButtons = new CopyOnWriteArrayList<myButton>();
        CopyOnWriteArrayList<floatingNumber> floatingNumbers= new CopyOnWriteArrayList<floatingNumber>();
        CopyOnWriteArrayList<BactАttack> bactАttacks= new CopyOnWriteArrayList<BactАttack>();

        class BactАttack{
            //public int time;
            public String all;
        }

        class floatingNumber{
            public int number,team;
            public float life,x,y;
        }



        class LoadStruct{
            public float x,y;
            public int paintTypeRadius , team;
        }

        class GameLevel{
            ArrayList<LoadStruct> one = new ArrayList<LoadStruct>();
            int[] times = new int[2];
        }

        class Virus{
            public float x,y,dx,dy,vx,vy;
            public int team,target,parent;
            public float drawBorder = 0;
            public boolean borderGrow;
            public void move(float TL){
                myVector2d vec = new myVector2d(dx,dy);

                Bacteria b = bact.get(target);

                if(vec.dist() > 1){
                    vec.norm();
                }

                myVector2d vec2 = new myVector2d(b.x - x, b.y - y).norm();
                vec.x += vec2.x*0.7;
                vec.y += vec2.y*0.7;

                vec.norm();
                float angle  = (float) ((rn.nextInt(100)-50)/40.0);
                //Log.e("",Float.toString(angle));
                vec.rotation(angle);

                vx += vec.x*0.4;
                vy += vec.y*0.4;



                //x += (vx+(rn.nextInt(100)-50)/50.0)*overWidth*1.5*TL;
                //y += (vy+(rn.nextInt(100)-50)/50.0)*overWidth*1.5*TL;

                x += vx*overWidth*TL;
                y += vy*overWidth*TL;

                //x+= vec.x;
                //y+= vec.y;
                vx *= 0.95;
                vy *= 0.95;

                dx = dy = 0;
            }
        }



        class Bacteria{
            public float x;
            public float y;
            public float dx;
            public float dy;
            public int radius;
            public int paintTypeRadius;
            public int team;
            public boolean select;
            public boolean tmpSelect;
            public float count;
            public float maxCount;
            public float drawBorder = 0;
            public float lastNotif = 0;

            public void move(float TL){
                drawBorder -= 1;
                lastNotif += 16.7*TL;
                x += dx*overWidth*TL;
                y += dy*overWidth*TL;
                dx *= 0.95;
                dy *= 0.95;
                if(team != xxxReplaceIt) {
                    count += TL * 0.1 * Math.cos(Math.min(3.1415, 3.1415 / 2 * count / maxCount));
                }
            }

            public void fire(int target , int parent){
                float tmpCount = bact.get(parent).count/2;
                for(int i=0; i<tmpCount; i++){
                    Virus tmp = new Virus();
                    tmp.drawBorder = 10 + rn.nextInt(200);
                    tmp.dx = tmp.dy = 0;
                    tmp.x = x + rn.nextInt(30) -15;
                    tmp.y = y + rn.nextInt(30) -15;
                    tmp.team = team;
                    tmp.target = target;
                    tmp.parent = parent;
                    virus.add(tmp);
                    count -= 1;
                }
                myVector2d vec = new myVector2d(bact.get(target).x - bact.get(parent).x ,bact.get(target).y - bact.get(parent).y ).norm();
                dx -= vec.x*count*0.05 + vec.x;
                dy -= vec.y*count*0.05 + vec.y;
            }
        }

        Typeface fontFace;

        Bitmap fonImgB , buttonNextLevel , fingerImg;
        Bitmap[][] iconLevel = new Bitmap[4][2];
        Bitmap[][] virusImg = new Bitmap[9][16];
        Bitmap[][] GameMap = new Bitmap[3][2];
        Bitmap[][][] bactImg = new Bitmap[9][2][3];
        Bitmap borderImg;
        int[] bactRadius = new int[3];
        int[][] bactColor = {{100,255,150}  ,  {255,100,100}  ,  {100,150,255} ,
                {255,140,200} , {255,255,0} , {255,255,255} , {0,0,0} , {0,0,0} , {0,0,0}};
        public int gameHardLevel = 100;
        public int gameLevel = 0;
        public int mapLevel = 0;
        public int score = 0;
        public int countAtacBact = 100;
        public int timeToAtacBact = 7000;
        public float timeCorrect = 0;
        public int numberOfAttackBact = 0;
        int timeSurvival = 0;
        public int playerIndexBact = 0;
        public int xxxReplaceIt = 8;
        public int WHmin;
        public float overWidth; // use for different DPI;
        public float mouseX, mouseY;
        public boolean canDrawLine;
        public boolean needInit = false;
        public boolean survival = false;
        public boolean playerWin = true;
        public boolean playerLose = true;
        public int timeFromInit;
        DisplayMetrics metrics = new DisplayMetrics();
        public int heightView;

        Random rn = new Random();
        long startTimeLevel, finishTimeLevel, startTimeInit;
        FullscreenActivity activity;



        Paint p;
        private DrawThread drawThread;
        String loadText = "Load";
        long timeout = System.currentTimeMillis();
        long curTime;


        public DrawView(Context context, FullscreenActivity _activity) {

            super(context);
            setOnTouchListener(this);



            activity = _activity;
            getHolder().addCallback(this);
            getHolder().setFormat(PixelFormat.RGBA_8888);

            Log.e("","its must be only one");


            activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);


            WHmin = Math.min(metrics.widthPixels, metrics.heightPixels);
            overWidth = (float) (WHmin / 1920.0);

            bactRadius[0] = (int) (WHmin / 11.32);
            bactRadius[1] = WHmin / 7;
            bactRadius[2] = WHmin / 5;






            BufferedReader reader = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.level)));
            String line;

            GameLevel tmp = new GameLevel();
            boolean readTime = true;

            try {
                while ((line = reader.readLine()) != null) {
                    char[] charArray = line.toCharArray();
                    if(charArray.length > 0){
                        if(line.toCharArray()[0] == '#') continue;
                    }
                    if (line.length() < 5) {
                        gameLevels.add(tmp);
                        tmp = new GameLevel();
                        readTime = true;
                    } else {
                        String[] number = line.split(" ");
                        if(readTime){
                            tmp.times[0] = Integer.parseInt(number[0]);
                            tmp.times[1] = Integer.parseInt(number[1]);
                            readTime = false;
                        }else {
                            LoadStruct tmpStruct = new LoadStruct();
                            tmpStruct.x = Integer.parseInt(number[0]) * metrics.widthPixels / 1920;
                            tmpStruct.y = Integer.parseInt(number[1]) * metrics.heightPixels / 1080;
                            tmpStruct.team = Integer.parseInt(number[2]);
                            tmpStruct.paintTypeRadius = Integer.parseInt(number[3]);

                            tmp.one.add(tmpStruct);
                        }
                    }
                }
            } catch (IOException e) {}

            gameLevels.add(tmp);

            String ddd = "";

            for(int i = 0; i < 20; i++){
                int a =  10000 + rn.nextInt(2000) - i*400;
                int r = 2+rn.nextInt(5);
                ddd+=Integer.toString(a) + ",";
                for(int j = 0; j < r; j++) {
                    int b = 1+rn.nextInt(3);
                    int c = 25 + rn.nextInt(25) + i;
                    int d = (b-1)*3+rn.nextInt(3);
                    ddd += Integer.toString(b)+"/" + Integer.toString(c) + "/" + Integer.toString(d);
                    if(j != r-1){
                        ddd += ",";
                    }
                }

                if(i != 19){
                    ddd += "_";
                }
            }

            Log.e("" , ddd);

            String bbb = "10000,2/30/1,3/30/2_8000,2/30/1,3/30/2_10000,2/30/1,3/30/2_10000,4/40/1,1/30/2";
            bbb+= "_10000,5/60/1,3/30/2_8000,2/30/1,3/30/2_10000,2/30/1,3/30/2_10000,4/40/1,1/30/2";

            bbb = ddd;

            String[] BBB = bbb.split("_");

            for(int i = 0; i < BBB.length; i++){
                //String[] DDD = BBB[i].split(",");
                BactАttack bactАttack = new BactАttack();
                //bactАttack.time = Integer.parseInt(DDD[0]);
                bactАttack.all = BBB[i];
                bactАttacks.add(bactАttack);
            }


            activity.sPref = activity.getPreferences(MODE_PRIVATE);
            String passage_time_1 = activity.sPref.getString("passage_time_1", "");

            for(int i = 0; i < 30; i++) {
                passageTime[i] = -1;
            }

            if(passage_time_1.length() > 0) {
                String[] number = passage_time_1.split(" ");
                for (int i = 0; i < number.length; i++) {
                    passageTime[i] = Integer.parseInt(number[i]);
                }

            }

            class WorkingClass implements Runnable{
                @Override
                public void run() {

                    fingerImg = scaleToWidth(bitmapLoadRaw(R.raw.finger),WHmin/5);

                    iconLevel[0][0] = scaleToWidth(bitmapLoadRaw(R.raw.icongreen) , WHmin/4);
                    iconLevel[1][0] = scaleToWidth(bitmapLoadRaw(R.raw.icon) , WHmin/4);
                    iconLevel[2][0] = scaleToWidth(bitmapLoadRaw(R.raw.iconred) , WHmin/4);
                    iconLevel[3][0] = scaleToWidth(bitmapLoadRaw(R.raw.icondis) , WHmin/4);

                    iconLevel[0][1] = scaleToWidth(bitmapLoadRaw(R.raw.icongreen) , (int) (WHmin/4*1.1));
                    iconLevel[1][1] = scaleToWidth(bitmapLoadRaw(R.raw.icon) , (int) (WHmin/4*1.1));
                    iconLevel[2][1] = scaleToWidth(bitmapLoadRaw(R.raw.iconred) , (int) (WHmin/4*1.1));
                    iconLevel[3][1] = scaleToWidth(bitmapLoadRaw(R.raw.icondis) , (int) (WHmin/4*1.1));
                    fonImgB = Bitmap.createScaledBitmap(bitmapLoadRaw(R.raw.fon3), metrics.widthPixels ,metrics.heightPixels,true);
                    buttonNextLevel = bitmapLoadRaw(R.raw.buttonnext);



                    borderImg = scaleToWidth(bitmapLoadRaw(R.raw.between),metrics.widthPixels);

                    GameMap[1][0] = scaleToWidth(bitmapLoadRaw(R.raw.easy) , (int) (metrics.widthPixels*0.94*0.5));
                    GameMap[1][1] = scaleToWidth(bitmapLoadRaw(R.raw.easywithout) , (int) (metrics.widthPixels*0.94*0.5));

                    GameMap[2][0] = scaleToWidth(bitmapLoadRaw(R.raw.hard) , (int) (metrics.widthPixels*0.94*0.5));
                    GameMap[2][1] = scaleToWidth(bitmapLoadRaw(R.raw.hardwithout) , (int) (metrics.widthPixels*0.94*0.5));

                    GameMap[0][0] = scaleToWidth(bitmapLoadRaw(R.raw.survival) , metrics.widthPixels);
                    GameMap[0][1] = scaleToWidth(bitmapLoadRaw(R.raw.survival) , metrics.widthPixels);

                    Paint pp = new Paint();
                    pp.setFilterBitmap(true);
                    pp.setFlags(Paint.ANTI_ALIAS_FLAG);

                    Bitmap bm = bitmapLoadRaw(R.raw.bact);

                    for( int j = 2; j != -1; j--){
                        Bitmap bmS = Bitmap.createScaledBitmap(bm, bactRadius[j]*2, bactRadius[j]*2, true);
                        for( int i = 0 ; i < 9; i++){
                            if(i > 5 && i != 8)
                                continue;

                            int r,g,b;
                            r = bactColor[i][0];
                            g = bactColor[i][1];
                            b = bactColor[i][2];

                            //if(j == 2) {
                                bactImg[i][0][j] = Bitmap.createBitmap(bactRadius[j] * 2, bactRadius[j] * 2, Bitmap.Config.ARGB_8888);


                                pp.setStyle(Paint.Style.FILL);
                                Canvas canvasB = new Canvas(bactImg[i][0][j]);
                                canvasB.drawBitmap(bmS, 0, 0, pp);
                                pp.setColor(Color.argb(120, r, g, b));
                                canvasB.drawCircle(bactRadius[j], bactRadius[j], (float) (bactRadius[j] - bactRadius[j] * 0.04), pp);
                            //}else{
                                //bactImg[i][0][j] = Bitmap.createScaledBitmap(bactImg[i][0][2], bactRadius[j]*2, bactRadius[j]*2, true);
                            //}

                            //Canvas canvasB;
                            bactImg[i][1][j] = Bitmap.createBitmap((int)(bactRadius[j]*2.4),(int)(bactRadius[j]*2.4),
                                    Bitmap.Config.ARGB_8888);
                            canvasB = new Canvas((bactImg[i][1][j]));
                            pp.setColor(Color.BLACK);
                            canvasB.drawBitmap(bmS , (int)(bactRadius[j]*0.2) , (int)(bactRadius[j]*0.2) , pp);
                            pp.setColor(Color.argb(200 , r,g,b));
                            canvasB.drawCircle((int)(bactRadius[j]*1.2) , (int)(bactRadius[j]*1.2) , bactRadius[j]-1 , pp);
                            pp.setStyle(Paint.Style.STROKE);
                            pp.setStrokeWidth((float)(bactRadius[j]*0.1));
                            pp.setColor(Color.WHITE);

                            canvasB.drawCircle((int)(bactRadius[j]*1.2) , (int)(bactRadius[j]*1.2) ,
                                    bactRadius[j]+(float)(bactRadius[j]*0.1) ,pp);
                        }
                    }

                    pp.setStyle(Paint.Style.FILL);
                    pp.setAlpha(255);

                    for(int i = 0; i < 9; i++){
                        int r,g,b;
                        r = bactColor[i][0];
                        g = bactColor[i][1];
                        b = bactColor[i][2];

                        float radius = (float) (WHmin/80.0);

                        for(int jj = 0; jj < 16; jj++){
                            virusImg[i][jj] = Bitmap.createBitmap((int)(radius*4) , (int) (radius*4), Bitmap.Config.ARGB_8888);
                            Canvas canvasB = new Canvas(virusImg[i][jj]);

                            float j = (float) (8*( 1 - Math.cos(jj*Math.PI/15)));
                            pp.setColor(Color.argb(255 , r,g,b));
                            canvasB.drawCircle(radius*2, radius*2 , (float) (radius*0.66), pp);

                            pp.setColor(Color.argb((int) (j*15), r, g, b));
                            canvasB.drawCircle(radius*2, radius*2, radius*2/16*j, pp);


                        }
                    }

                    p.setTextSize(WHmin/25);
                    p.setColor(Color.BLACK);
                    p.setTextAlign(Paint.Align.CENTER);
                    gameMod = GameMod.MAP_MENU;
                    myInit();
                }
            }




            fontFace = Typeface.createFromAsset(activity.getAssets(), "fonts/komix.ttf");
            p = new Paint();
            p.setColor(Color.WHITE);
            p.setTypeface(fontFace);
            p.setFlags(Paint.ANTI_ALIAS_FLAG);
            p.setTextSize(WHmin/25*4);
            p.setFilterBitmap(true);

            WorkingClass workingClass = new WorkingClass();
            Thread thread = new Thread(workingClass);
            thread.start();



            //myInit();

        }


        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            drawThread = new DrawThread(getHolder());
            drawThread.setRunning(true);
            drawThread.start();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            boolean retry = true;
            drawThread.setRunning(false);
            while (retry) {
                try {
                    drawThread.join();
                    retry = false;
                } catch (InterruptedException e) {
                }
            }
        }

        class DrawThread extends Thread {

            public boolean running = false;
            public boolean update = true;
            private SurfaceHolder surfaceHolder;

            public DrawThread(SurfaceHolder surfaceHolder) {
                this.surfaceHolder = surfaceHolder;
            }

            public void setRunning(boolean running) {
                this.running = running;
            }
            public void update() {this.update = true;}

            @Override
            public void run() {
                Canvas canvas;
                while (running) {
                    //Log.e("update" , Integer.toString((int) update));
                    curTime = System.currentTimeMillis() - timeout;
                    timeout = System.currentTimeMillis();
                    timeFromInit = (int) (System.currentTimeMillis() - startTimeInit);

                    heightView = dv.getHeight();


                    if (update) {
                        canvas = null;
                        try {
                            canvas = surfaceHolder.lockCanvas(null);
                            /*if(canvas.isHardwareAccelerated()){
                                bact.get(1).x += 10; // KIIIIIIIIIIIIIILLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLL
                            }*/
                            if (canvas == null) continue;

                            float timeLag = curTime/16;
                            timeLag = Math.min(3,(float)Math.max(0.3, timeLag));


                            Log.e("some2",gameMod.toString() + " " + Integer.toString(gameMod.hashCode()));

                            switch (gameMod){
                                case MAP_MENU:

                                    update = false;
                                    DrawMapMenu(canvas,p);
                                    break;


                                case LEVEL_MENU:
                                    update = false;
                                    canvas.drawBitmap(fonImgB, 0, 0, p);
                                    for (int ii = 0; ii < myButtons.size(); ii++) {
                                        myButton b = myButtons.get(ii);
                                        int i = ii + mapLevel*15 - 15;
                                        if(b.click){
                                            b.click = false;
                                            if(i == 0 || i == 15 || passageTime[i-1] > 0 ) {
                                                gameMod = GameMod.ACTIVE_GAME;
                                                gameLevel = i;
                                                needInit = true;
                                            }else{
                                                /*runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Toast toast = Toast.makeText(getApplicationContext(),
                                                                "This level is closed.", Toast.LENGTH_SHORT);
                                                        toast.show();
                                                    }
                                                });*/


                                            }
                                        }
                                        b.draw(canvas,p);
                                        canvas.drawText(Integer.toString(i + 1), b.x+b.width()/2,
                                                b.y+b.height()/2 + WHmin/75, p);
                                    }
                                    break;
                                case BETWEEN_LEVEL:
                                    update = false;
                                    DrawBetweenLevel(canvas, p);
                                    break;
                                case PAUSE:
                                    update = false;
                                    DrawPause(canvas, p);
                                    break;
                                case ACTIVE_GAME:
                                    update = true;
                                    DrawActiveGame(canvas,p,timeLag,getWidth(),getHeight());
                                    break;
                                case LOAD_GAME:
                                    update = true;
                                    canvas.drawText(loadText,WHmin*0.04f,heightView/2,p);
                                    loadText += ".";
                                    break;
                            }

                            for(myButton b : myButtons){
                                int last = b.timePress;
                                if(b.pressed) {
                                    b.timePress = (int) Math.min(b.timePress+curTime , b.maxTimePress);
                                }else{
                                    b.timePress = (int) Math.max(b.timePress-curTime , 0);
                                }
                                if(b.timePress != last) update = true;
                            }

                            canvas.drawText(Long.toString(curTime) , 100 , 100 , p);


                        } finally {
                            if (canvas != null) {
                                surfaceHolder.unlockCanvasAndPost(canvas);
                            }
                        }
                    }else{
                        try {
                            Thread.sleep(16);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if(needInit){
                        needInit = false;
                        myInit();
                    }
                }
            }
        }
    }
}