package com.cugb.xiaob.mozaiku;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Created by HLYS on 2017/7/12.
 * 登录界面
 */

public class LoginView extends Activity {
    private Context mContext;
    private String userForIntent;
    String username;//
    String password;//
    //创建播放视频的控件对象
    private CustomVideoView videoview;
    //
    private DBOpenHelper loginDBHelper=new DBOpenHelper(LoginView.this,1);

    //_____________________________

protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    //设置无标题且全屏、位置必须位于调用资源文件前，onCreate函数后面
    requestWindowFeature(Window.FEATURE_PROGRESS);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    setContentView(R.layout.login);

    initView();

    //实例化
    final EditText userName=(EditText)findViewById(R.id.userName);
    final EditText passwordtext=(EditText)findViewById(R.id.passwordText);
    final TextView forget=(TextView)findViewById(R.id.forget);
    final TextView signout=(TextView)findViewById(R.id.signout);

    //登录按钮监听
    final Button signin=(Button)findViewById(R.id.signin);
    signin.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final String username=userName.getText().toString();
            final String password=passwordtext.getText().toString();
            if (!username.matches("")&&!password.matches("")){
                int state= searchByDB(username,password);
                if(state==0){
                    AlertDialog alt ;
                    AlertDialog.Builder alb = new AlertDialog.Builder(mContext);
                    alt = alb.setIcon(R.drawable.konosuba_h_01)
                            .setTitle(R.string.help)
                            .setMessage(getString(R.string.wrong_nm)+getString(R.string.coder))
                            .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    saveuserByDB(username,password);
                                    userForIntent = username;
                                    // alert.dismiss();
                                }
                            })
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .create();
                    alt.show();
                }else if(state==1){
                    String a = getResources().getString(R.string.welcome);
                    a = String.format(a,username);
                    userForIntent = username;
                    Intent it =new Intent(LoginView.this,MainActivity.class);
                    it.putExtra("username",userForIntent);
                    startActivity(it);
                    // alert.dismiss();
                }else if(state==2){
                    Toast.makeText(mContext,R.string.wrong_pw,Toast.LENGTH_SHORT).show();
                }
            }else Toast.makeText(mContext,R.string.null_nm,Toast.LENGTH_SHORT).show();
        }
    });


    //监听登录界面账号输入框输入，重写回车事件，使得回车后切换到密码输入
    userName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if(actionId == EditorInfo.IME_ACTION_SEND || (event != null &&event.getKeyCode()==KeyEvent.KEYCODE_ENTER)){
                passwordtext.requestFocus();//密码输入框获得焦点
                return true;
            }
            return false;
        }
    });
    //监听登录界面密码输入，重写回车事件，使得回车相当于点击ok按钮
    passwordtext.setOnEditorActionListener(new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if(actionId == EditorInfo.IME_ACTION_SEND || (event != null &&event.getKeyCode()==KeyEvent.KEYCODE_ENTER)){
                signin.findViewById(R.id.signin).performClick();
                return true;
            }
            return false;
        }
    });




/*
    //使用Internet收传数据，用户名（username）与密码（password）
    Intent intent=getIntent();
    username=intent.getStringExtra("username");
    password=intent.getStringExtra("password");

    */
}
//数据库操作相关
    //在数据库中储存用户信息
    private void saveuserByDB(String userName,String password){
        SQLiteDatabase db = loginDBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("userName",userName);
        values.put("password",password);
        //db的insert函数的参数分别为『要操作的表名』，『要强行插入null值的列名』，『插入的一行数据』
        db.insert("userInfo",null,values);
        Toast.makeText(LoginView.this,R.string.userinfo_saved,Toast.LENGTH_SHORT).show();
    }
    //在数据库中搜索用户信息
    private int searchByDB(String userName,String password){
        SQLiteDatabase db=loginDBHelper.getReadableDatabase();
        //参数依次是:数据库查询语句
        Cursor cursor = db.rawQuery("SELECT * FROM userInfo WHERE userName = ?",new String[]{userName});
        //存在数据才返回true
        if(cursor.moveToFirst()) {
            cursor.close();
            Cursor newCursor = db.rawQuery("SELECT * FROM userInfo WHERE userName = ? AND password = ?",new String[]{userName,password});
            if(newCursor.moveToFirst()) {
                newCursor.close();
                db.close();
                return 1;
            }
            else {
                newCursor.close();
                db.close();
                return 2;
            }
        }else {
            cursor.close();
            db.close();
            return 0;
        }
    }


private  void initView(){
    //加载视频资源控件
    videoview = (CustomVideoView) findViewById(R.id.videoView);
    //设置播放加载路径
    videoview.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.video));
    //播放
    videoview.start();
    //循环播放
    videoview.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            videoview.start();
        }
    });


}
    //返回重启加载
    @Override
    protected void onRestart() {
        initView();
        super.onRestart();
    }

    //防止锁屏或者切出的时候，音乐在播放
    @Override
    protected void onStop() {
        videoview.stopPlayback();
        super.onStop();
    }

}