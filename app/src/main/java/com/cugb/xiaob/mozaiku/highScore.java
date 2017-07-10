package com.cugb.xiaob.mozaiku;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
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
    String[] HS_username = new String[8];//高分榜文件中记录的全部玩家名称
    int[] HS_costTime = new int[8];//高分榜文件中记录的全部最高分数
    int[] yourHighscore = new int[3];//当前登录用户的最高分数
    int difficult;//当前登录用户的难度类型，三种取值为0,1,2,对应简单，普通，困难。5表示没有接收到该项数据


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //设置无标题栏且全屏、位置必须处于调用资源文件前，onCreate函数后
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.high_score);


        Intent it = getIntent();
        curUsername = it.getStringExtra("username");
        curCostTime = it.getIntExtra("costTime",999999999);
        difficult = it.getIntExtra("difficult",5);
        loadHS();
        if(difficult!=5){
            freshHS(curUsername,curCostTime);
        }
        saveHS(HS_username,HS_costTime);
        loadPHS(curUsername);
        freshPHS(curCostTime);
        savePHS(curUsername);
        displayHS();
        displayPHS();
    }

    //在高分榜文件中写入最高分。
    public void saveHS(String[] username,int[] score) {
        try {
            FileOutputStream outStream=this.openFileOutput("HighScore.txt", Context.MODE_PRIVATE);
            outStream.write("".getBytes());
            outStream.close();
            for(int i=0;i<8;i++){
                outStream = this.openFileOutput("HighScore.txt",Context.MODE_APPEND);
                String info = String.valueOf(score[i]);
                info = username[i] + ":" + info + ",";
                outStream.write(info.getBytes());
                outStream.close();
            }
        } catch (IOException e){
            //TODO:handle exception
        }
    }
    //读取高分榜文件的数据到全局变量中
    public void loadHS(){
        try{
            FileInputStream inStream = this.openFileInput("HighScore.txt");
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length =-1;
            while ((length=inStream.read(buffer))!=-1){
                stream.write(buffer,0,length);
            }
            stream.close();
            inStream.close();
            String text = stream.toString();
            String[] score = text.split(",");
            String[][] info = new String[8][2];
            for (int i=0;i<8;i++) {
                if(i<score.length) {
                    info[i] = score[i].split(":");
                }else {
                    info[i][0] = getResources().getString(R.string.nobody);
                    info[i][1] = "0";
                }
            }
            for(int i=0;i<8;i++){
                HS_username[i] = info[i][0];
                HS_costTime[i] = Integer.decode(info[i][1]);
            }
        }catch (FileNotFoundException e){
        }catch (IOException e){
        }
    }
    //更新暂存在全局变量中的高分榜数据
    public void freshHS(String curUsername,int curCostTime){
        for(int i=0;i<8;i++){
            if(curCostTime<HS_costTime[i]||HS_costTime[i]==0){
                int x =i;
                for(int j=i+1;j<8;j++) {
                    HS_username[j] = HS_username[x];
                    HS_costTime[j] = HS_costTime[x];
                    x++;
                }
                HS_username[i] = curUsername;
                HS_costTime[i] = curCostTime;
                break;
            }
        }
    }
    //读取个人最高分表
    public void loadPHS(String curUsername){
        try{
            String filename = curUsername + ".txt";
            File file = new File(filename);
            if (file.exists()) {
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
                for (int i = 0; i < 3; i++) {
                    yourHighscore[i] = Integer.decode(usersScore[i]);
                }
            }else{
                yourHighscore[0] = 0;
                yourHighscore[1] = 0;
                yourHighscore[2] = 0;
                savePHS(curUsername);
            }

        }catch (FileNotFoundException e){
            yourHighscore[0] = 0;
            yourHighscore[1] = 0;
            yourHighscore[2] = 0;
            Toast.makeText(highScore.this,"尚未有您的得分数据",Toast.LENGTH_SHORT).show();
        }catch (IOException e){
        }
    }
    //更新暂存在全局变量中的个人最高分
    public void freshPHS(int curCostTime){
        if(yourHighscore[difficult]==0 || yourHighscore[difficult]>curCostTime)
            yourHighscore[difficult]=curCostTime;
    }
    public void savePHS(String curUsername){
        try {
            String filename = curUsername + ".txt";
            FileOutputStream outStream=this.openFileOutput(filename, Context.MODE_PRIVATE);
            for(int i=0;i<3;i++){
                String info = String.valueOf(yourHighscore[i]);
                info = yourHighscore[i] + ",";
                outStream.write(info.getBytes());
                outStream.close();
            }
        } catch (IOException e){
            //TODO:handle exception
        }
    }
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
}
