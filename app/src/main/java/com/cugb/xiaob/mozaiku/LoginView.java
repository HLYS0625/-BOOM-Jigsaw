package com.cugb.xiaob.mozaiku;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Created by HLYS on 2017/7/12.
 * 登录界面
 */

public class LoginView extends Activity {
    public DBOpenHelper newuserDBHelper=new DBOpenHelper(LoginView.this,2);
    //创建播放视频的控件对象
    private CustomVideoView videoview;
    //


    //_____________________________

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置无标题且全屏、位置必须位于调用资源文件前，onCreate函数后面
        requestWindowFeature(Window.FEATURE_PROGRESS);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.login);
        initView();
        //实例化
        final EditText userName = (EditText) findViewById(R.id.userName);
        final EditText passwordtext = (EditText) findViewById(R.id.passwordText);
        final TextView forget = (TextView) findViewById(R.id.forget);
        final TextView signout = (TextView) findViewById(R.id.signout);
        final Button signin = (Button) findViewById(R.id.signin);

          //点击editText以外的地方键盘消失

        findViewById(R.id.login).setOnClickListener(new View.OnClickListener(){
            public  void onClick(View v){
                InputMethodManager imm = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }

        });

        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转页面  跳转到注册页面
                 Intent it = new Intent(LoginView.this, NewuserView.class);
                 startActivity(it);
            }
        }

        );
          //登录按钮
        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = userName.getText().toString();
                String password = passwordtext.getText().toString();
                TextView toasttext=(TextView)findViewById(R.id.toast);
                if (!username.matches("") && !password.matches("")) {
                    int state = searchByDB(username, password);
                    if (state == 0)//未注册
                    {
                    toasttext.setText("!!!∑(ﾟДﾟノ)ノ:\n"+getStr(R.string.wrong_nm));
                        userName.setText("");
                        passwordtext.setText("");
                    } else if (state == 1)//正确登录
                    {
                        Intent it = new Intent(LoginView.this, MainActivity.class);
                        it.putExtra("username", username);
                        startActivity(it);
                    } else if (state == 2)//密码错误
                    {
                        toasttext.setText("Σ(っ°Д°;)っ:\n"+getStr(R.string.wrong_pw));
                        userName.setText("");
                        passwordtext.setText("");
                    }
                } else{
                    toasttext.setText("(｀・ω・´)：\n"+getStr(R.string.null_nm));
                    passwordtext.setText("");
                }
            }
        });
        //监听登录界面账号输入框输入，重写回车事件，使得回车后切换到密码输入
        userName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
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
                if (actionId == EditorInfo.IME_ACTION_SEND || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    signin.findViewById(R.id.signin).performClick();
                    return true;
                }
                return false;
            }
        });
    }



/*
    //使用Internet收传数据，用户名（username）与密码（password）
    Intent intent=getIntent();
    username=intent.getStringExtra("username");
    password=intent.getStringExtra("password");

    */

//数据库操作相关
    //在数据库中储存用户信息
    private void saveuserByDB(String userName,String password){
        SQLiteDatabase db = newuserDBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("userName",userName);
        values.put("password",password);
        //db的insert函数的参数分别为『要操作的表名』，『要强行插入null值的列名』，『插入的一行数据』
        db.insert("userInfo",null,values);
        Toast.makeText(LoginView.this,R.string.userinfo_saved,Toast.LENGTH_SHORT).show();
    }
    //在数据库中搜索用户信息
    private int searchByDB(String userName,String password){
        SQLiteDatabase db=newuserDBHelper.getReadableDatabase();
        //参数依次是:数据库查询语句
        Cursor cursor = db.rawQuery("SELECT * FROM userInfo WHERE userName = ?",new String[]{userName});
        //存在数据才返回true
        if(cursor.moveToFirst()) {
            cursor.close();
            Cursor newCursor = db.rawQuery("SELECT * FROM userInfo WHERE userName = ? AND password = ?",new String[]{userName,password});
            if(newCursor.moveToFirst()) {
                newCursor.close();
                db.close();
                return 1;//应该进入
            }
            else {
                newCursor.close();
                db.close();
                return 2;//应该是密码错误 或 密码为空
            }
        }else {
            cursor.close();
            db.close();
            return 0;//未注册
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

    //获取存储在string资源文件中的字符串
    private String getStr(int i){
        return getResources().getString(i);
    }

}