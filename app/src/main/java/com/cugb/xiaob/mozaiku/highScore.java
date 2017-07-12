package com.cugb.xiaob.mozaiku;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by xiaob on 2017/7/10.
 */

public class highScore extends AppCompatActivity {
    String curUsername;//当前登录的用户名
    int curCostTime;//刚刚完成的游戏用时
    int[] yourHighscore = {0,0,0};//当前登录用户的最高分数
    int difficult;//当前登录用户的难度类型，三种取值为0,1,2,对应简单，普通，困难。5表示没有接收到该项数据,即非游戏结束后进入此页面
    int CheatCount;//从游戏页面接收的作弊次数计数，若作弊次数大于0，则拒绝将成绩录入排行榜
    int noStay;//从游戏页面接收的停留标识，如果noStay值为1，则记录信息后立刻关闭此页面，不做停留。
    //数据库相关变量
    private DBOpenHelper myDBHelper = new DBOpenHelper(highScore.this,1);


    //____________________________________变量和函数的分割线________________________________________\\


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //设置无标题栏且全屏、位置必须处于调用资源文件前，onCreate函数后
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.high_score);

        //切换按钮监听，按此按钮后可以选择UI中显示不同难度的排行榜。
        Button changeButton = (Button)findViewById(R.id.change) ;
        changeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] change = new String[]{
                        getResources().getString(R.string.change_easy),
                        getResources().getString(R.string.change_normal),
                        getResources().getString(R.string.change_hard)
                };
                AlertDialog alert = null;
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(highScore.this);
                alert = alertBuilder.setIcon(R.drawable.konosuba_h_01)
                        .setTitle(R.string.change_title)
                        .setSingleChoiceItems(change, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                cutover(which);
                            }
                        }).create();
                alert.show();
            }
        });

        //使用Intent接收传入的数据，分别为当前用户名『curUsername』，此次用时『curCostTime』，此次使用的难度『difficult』
        Intent it = getIntent();
        curUsername = it.getStringExtra("username");
        curCostTime = it.getIntExtra("costTime",999999999);
        difficult = it.getIntExtra("difficult",5);
        CheatCount = it.getIntExtra("cheat",0);
        noStay = it.getIntExtra("noStay",0);


        //如果是通关后进入此页面，（依成绩）刷新高分榜和个人最好成绩，并保存在文件中。
        if(difficult!=5 && CheatCount == 0 ){
            saveHSByDatabase(curUsername,curCostTime,difficult);
            savePHSByDatabase(curUsername,curCostTime,difficult);
        }
        //如果通关后没有选择前往高分榜，立刻在存储分数后关闭此页面
        if(noStay==0) {
            //刷新UI显示
            if(difficult!=5) {
                loadHSByDatabase(difficult);
            }else loadHSByDatabase(0);
            loadPHSByDatabase(curUsername);
        }else finish();
    }


//存储相关函数
    //向数据库高分榜文件中写入最高分
    public void saveHSByDatabase(String curUsername,int curCostTime,int difficult){
        SQLiteDatabase db = myDBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("userName",curUsername);
        values.put("record",curCostTime);
        values.put("difficult",difficult);
        db.insert("scoreInfo",null,values);
    }
    //向数据库用户信息文件中更新该用户的个人最高分
    public void savePHSByDatabase(String curUsername,int curCostTime,int difficult){
        loadPHSByDatabase(curUsername);
        if(yourHighscore[difficult]==0 || curCostTime < yourHighscore[difficult]){
            yourHighscore[difficult] = curCostTime;
        }
        String[] s = {
                String.valueOf(yourHighscore[0]),
                String.valueOf(yourHighscore[1]),
                String.valueOf(yourHighscore[2]),
                curUsername
        };
        SQLiteDatabase db = myDBHelper.getWritableDatabase();
        db.execSQL("UPDATE userInfo SET easyHS = ? ,normalHS = ? ,hardHS = ?" +
                "WHERE userName = ?",s);
    }


//读取数据库并刷新UI相关函数
    //从高分榜数据库中读取高分记录并显示在UI上
    public void loadHSByDatabase(int difficult){
        TextView[] username = new TextView[]{
                (TextView)findViewById(R.id.HS_name1),
                (TextView)findViewById(R.id.HS_name2),
                (TextView)findViewById(R.id.HS_name3),
                (TextView)findViewById(R.id.HS_name4),
                (TextView)findViewById(R.id.HS_name5),
                (TextView)findViewById(R.id.HS_name6),
                (TextView)findViewById(R.id.HS_name7),
                (TextView)findViewById(R.id.HS_name8),
        };
        TextView[] userscore = new TextView[]{
                (TextView)findViewById(R.id.HS_score1) ,
                (TextView)findViewById(R.id.HS_score2) ,
                (TextView)findViewById(R.id.HS_score3) ,
                (TextView)findViewById(R.id.HS_score4) ,
                (TextView)findViewById(R.id.HS_score5) ,
                (TextView)findViewById(R.id.HS_score6) ,
                (TextView)findViewById(R.id.HS_score7) ,
                (TextView)findViewById(R.id.HS_score8) ,
        };
        int i =0;//刷新第i条姓名文本和分数文本的显示
        SQLiteDatabase db = myDBHelper.getReadableDatabase();
        String diff = String.valueOf(difficult);
        Cursor cursor = db.rawQuery("SELECT * FROM scoreInfo WHERE difficult = ? " +
                "ORDER BY record",new String[]{diff});
        if(cursor != null){//如果数据库中存在该难度的高分记录
            //逐行读取找到的记录
            for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext(),i++){
                username[i] .setText(cursor.getString(cursor.getColumnIndex("userName")));
                userscore[i].setText(cursor.getString(cursor.getColumnIndex("record")));
                if(i>7) {
                    break; //只显示前8条记录
                }
            }
            cursor.close();
            db.close();
        }
    }
    //从玩家信息数据库中读取个人高分记录并显示在UI上
    public void loadPHSByDatabase(String username){
        TextView[] tv = new TextView[]{
                (TextView)findViewById(R.id.P_score_easy),
                (TextView)findViewById(R.id.P_score_normal),
                (TextView)findViewById(R.id.P_score_hard)
        };
        SQLiteDatabase db = myDBHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM userInfo WHERE userName = ?",new String[]{username});
        if(cursor.moveToFirst()){
            cursor.moveToFirst();
            yourHighscore[0] = cursor.getInt(cursor.getColumnIndex("easyHS"));
            tv[0].setText(String.valueOf(yourHighscore[0]));
            yourHighscore[1] = cursor.getInt(cursor.getColumnIndex("normalHS"));
            tv[1].setText(String.valueOf(yourHighscore[1]));
            yourHighscore[2] = cursor.getInt(cursor.getColumnIndex("hardHS"));
            tv[2].setText(String.valueOf(yourHighscore[2]));
        }
        cursor.close();
        db.close();
    }

//对选择按钮弹出的单选框进行响应
    //根据传进的状态值选择读取的文件，提供给切换按钮刷新数据之用。
    private void cutover(int x){
        switch (x){
            case 0:
                loadHSByDatabase(0);
                break;
            case 1:
                loadHSByDatabase(1);
                break;
            case 2:
                loadHSByDatabase(2);
                break;
        }
    }




/*已废弃
    //写入文件相关函数
        //在高分榜文件中写入最高分,difficult的值代表向哪个文件写入。
        public void saveHS(String[] username,int[] score,int difficult) {
        try {
            String fileName = getFileName(difficult);
            FileOutputStream outStream=this.openFileOutput(fileName, Context.MODE_PRIVATE);
            outStream.write("".getBytes());
            outStream.close();
            for(int i=0;i<8;i++){
                outStream = this.openFileOutput(fileName,Context.MODE_APPEND);
                String info = String.valueOf(score[i]);
                info = username[i] + ":" + info + ",";
                outStream.write(info.getBytes());
                outStream.close();
            }
        } catch (IOException e){
        }
    }
        //在用户名对应的个人文件中写入该用户每个难度所获得的历史最高分
        public void savePHS(String curUsername){
        try {
            String filename = curUsername + ".txt";
            FileOutputStream outStream=this.openFileOutput(filename, Context.MODE_PRIVATE);
            for(int i=0;i<3;i++){
                String info = String.valueOf(yourHighscore[i]);
                info = i + ":" + info + ",";
                outStream.write(info.getBytes());
                outStream.close();
            }
        } catch (IOException e){
        }
    }
    //读取相关函数
        //读取高分榜文件的数据到全局变量中
        public void loadHS(int difficult){
        try {
            String fileName = getFileName(difficult);
            if (fileName.equals("")) fileName = "HighScore_easy.txt";
            //File file = new File(fileName);
            FileInputStream inStream = this.openFileInput(fileName);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length = -1;
            while ((length = inStream.read(buffer)) != -1) {
                stream.write(buffer, 0, length);
            }
            stream.close();
            inStream.close();
            String text = stream.toString();
            String[] score = text.split(",");
            String[][] info = new String[8][2];
            for (int i = 0; i < 8; i++) {
                if (i < score.length) {
                    info[i] = score[i].split(":");
                } else {
                    info[i][0] = getResources().getString(R.string.nobody);
                    info[i][1] = "0";
                }
            }for(int i=0;i<8;i++){
                HS_username[i] = info[i][0];
                HS_costTime[i] = Integer.decode(info[i][1]);
            }
        }catch (FileNotFoundException e) {
            for (int i = 0; i < 8; i++) {
                HS_username[i] = getResources().getString(R.string.nobody);
                HS_costTime[i] = 0;
            }
        }catch (IOException e){
        }
    }
        //读取个人最高分文件的数据到全局变量中
        public void loadPHS(String curUsername){
        try{
            String filename = curUsername + ".txt";
            File file = new File(filename);
            FileInputStream inStream = this.openFileInput(filename);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length = -1;
            while ((length = inStream.read(buffer)) != -1) {
                stream.write(buffer, 0, length);
            }
            stream.close();
            inStream.close();
            String score = stream.toString();
            String[] usersScore = score.split(",");
            for (int i = 0; i < usersScore.length; i++) {
                String[] info = usersScore[i].split(":");
                int no = Integer.decode(info[0]);
                int sco = Integer.decode(info[1]);
                yourHighscore[no] =  sco;
            }
        }catch (FileNotFoundException e){
            yourHighscore[0] = 0;
            yourHighscore[1] = 0;
            yourHighscore[2] = 0;
            savePHS(curUsername);
        }catch (IOException e){
        }
    }
    //更新全局变量函数（以便刷新数据后写入到文件中保存）
        //更新暂存在全局变量中的高分榜数据
        public void freshHS(String curUsername,int curCostTime){
        for(int i=0;i<8;i++){
            if(curCostTime<HS_costTime[i]||HS_costTime[i]==0){
                for(int j=7;j>i;j--) {
                    if(HS_costTime[j-1]==0 ) {
                        continue;
                    }
                    HS_username[j] = HS_username[j-1];
                    HS_costTime[j] = HS_costTime[j-1];
                }
                HS_username[i] = curUsername;
                HS_costTime[i] = curCostTime;
                break;
            }
        }
    }
        //更新暂存在全局变量中的个人最高分
        public void freshPHS(int curCostTime){
        if(yourHighscore[difficult]!=0 && (yourHighscore[difficult]==0 || yourHighscore[difficult]>curCostTime))
            yourHighscore[difficult]=curCostTime;
    }
    //UI显示相关函数
        //刷新UI中的高分榜显示
        private void displayHS(){
        TextView[] username = new TextView[]{
                (TextView)findViewById(R.id.HS_name1),
                (TextView)findViewById(R.id.HS_name2),
                (TextView)findViewById(R.id.HS_name3),
                (TextView)findViewById(R.id.HS_name4),
                (TextView)findViewById(R.id.HS_name5),
                (TextView)findViewById(R.id.HS_name6),
                (TextView)findViewById(R.id.HS_name7),
                (TextView)findViewById(R.id.HS_name8),
        };
        TextView[] userscore = new TextView[]{
                (TextView)findViewById(R.id.HS_score1) ,
                (TextView)findViewById(R.id.HS_score2) ,
                (TextView)findViewById(R.id.HS_score3) ,
                (TextView)findViewById(R.id.HS_score4) ,
                (TextView)findViewById(R.id.HS_score5) ,
                (TextView)findViewById(R.id.HS_score6) ,
                (TextView)findViewById(R.id.HS_score7) ,
                (TextView)findViewById(R.id.HS_score8) ,
        };
        for(int i=0;i<8;i++){
            username[i].setText(HS_username[i]);
            String str = String.valueOf(HS_costTime[i]) + "sec";
            userscore[i].setText(str);
        }
    }
        //刷新UI中的个人高分榜显示
        private void displayPHS(){
        TextView[] tv = new TextView[]{
                (TextView)findViewById(R.id.P_score_easy),
                (TextView)findViewById(R.id.P_score_normal),
                (TextView)findViewById(R.id.P_score_hard)
        };
        for(int i =0;i<3;i++) {
            String costtime = String.valueOf(yourHighscore[i])+"sec";
            tv[i].setText(costtime);
        }
    }
    //工具函数
        //根据传入的difficult的值，决定向哪个文件进行读写
        private String getFileName(int difficult){
        String fileName;
        Button changeButton = (Button)findViewById(R.id.change) ;
        switch (difficult){
            case 0:
                changeButton.setText(R.string.change_easy);
                fileName = "HighScore_easy.txt";
                break;
            case 1:
                changeButton.setText(R.string.change_normal);
                fileName = "HighScore_normal.txt";
                break;
            case 2:
                changeButton.setText(R.string.change_hard);
                fileName = "HighScore_hard.txt";
                break;
            default:
                fileName="";
        }
        return fileName;
    }
        */
}
