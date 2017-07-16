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
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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

        forget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginView.this);
                final LayoutInflater inflater = LoginView.this.getLayoutInflater();
                final View view_custom = inflater.inflate(R.layout.custom_dialog,null,false);
                builder.setView(view_custom);
                final AlertDialog alert = builder.create();
                Button B_verification = (Button)view_custom.findViewById(R.id.send_verification);
                Button B_OK = (Button)view_custom.findViewById(R.id.ok);
                final EditText ETaddress = (EditText)view_custom.findViewById(R.id.mailText);
                final EditText ETverification = (EditText)view_custom.findViewById(R.id.verificationText);
                final EditText ETnewPW = (EditText)view_custom.findViewById(R.id.passwordText);
                B_verification.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String mail = ETaddress.getText().toString();
                        //保证邮箱格式正确且不为空
                        if(isEmail(mail)||!mail.equals("")){
                            //查验数据库中是否有这个邮箱
                            if(searchByDB(mail,"1")!=0){
                                ETaddress.setFocusable(false);
                                ETaddress.setFocusableInTouchMode(false);
                                sendVerification(mail);
                                Toast.makeText(LoginView.this,getStr(R.string.mail_success),Toast.LENGTH_SHORT).show();
                            }else Toast.makeText(LoginView.this,getStr(R.string.no_such_mail),Toast.LENGTH_SHORT).show();
                        }else Toast.makeText(LoginView.this,getStr(R.string.wrong_address),Toast.LENGTH_SHORT).show();
                    }
                });
                B_OK.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String mail = ETaddress.getText().toString();
                        String verification = ETverification.getText().toString();
                        String password = ETnewPW.getText().toString();
                        //非空校验
                        if (!(mail.equals("") || verification.equals("") || password.equals(""))) {
                            int ver_code = Integer.decode(verification);
                            if (searchByDB(mail, ver_code)) {
                                resetPW(mail, password);
                            } else
                                Toast.makeText(LoginView.this, getStr(R.string.verification_wrong), Toast.LENGTH_SHORT).show();
                        }else Toast.makeText(LoginView.this, getStr(R.string.fill_problem), Toast.LENGTH_SHORT).show();
                    }
                });
                alert.show();
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
                        finish();
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




    //数据库操作相关
    //在数据库中重置用户密码
    private void resetPW(String mail,String password){
        SQLiteDatabase db = newuserDBHelper.getWritableDatabase();
        db.execSQL("UPDATE userInfo SET password = ? WHERE address = ?",new String[]{password,mail});
        Toast.makeText(LoginView.this,R.string.reset_success,Toast.LENGTH_SHORT).show();
    }
    //在数据库中搜索用户信息
    private int searchByDB(String userName,String password){
        Cursor cursor;
        SQLiteDatabase db=newuserDBHelper.getReadableDatabase();
        //增加逻辑：检测用户是否使用邮箱登录，使得用户名和邮箱均可用于登录
        if(!isEmail(userName)) {
            cursor = db.rawQuery("SELECT * FROM userInfo WHERE userName = ?", new String[]{userName});
        }else cursor = db.rawQuery("SELECT * FROM userInfo WHERE address = ?", new String[]{userName});
        //存在数据才返回true
        if(cursor.moveToFirst()) {
            cursor.close();
            Cursor newCursor;
            //增加逻辑：检测用户是否使用邮箱登录，使得用户名和邮箱均可用于登录
            if(!isEmail(userName)) {
                newCursor = db.rawQuery("SELECT * FROM userInfo WHERE userName = ? AND password = ?", new String[]{userName, password});
            }else newCursor = db.rawQuery("SELECT * FROM userInfo WHERE address = ? AND password = ?", new String[]{userName, password});
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
    //重载，在数据库中搜索用户验证码并比对
    private boolean searchByDB(String mail,int verification) {
        String verCode = String.valueOf(verification);
        SQLiteDatabase db = newuserDBHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM userInfo WHERE address = ? AND verification = ?", new String[]{mail, verCode});
        if (cursor.moveToFirst()) {
            cursor.close();
            db.close();
            return true;//账号和验证码均正确
        } else {
            cursor.close();
            db.close();
            return false;//账号或验证码不正确
        }
    }
    //检测登录使用的是用户名还是邮箱
    private boolean isEmail(String address){
        String str = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
        Pattern p = Pattern.compile(str);
        Matcher m = p.matcher(address);
        return m.matches();
    }
    //向用户邮箱发送随机的验证码，并写入该邮箱的数据库
    private void sendVerification(String mail){
        String verification = String.valueOf((int)((Math.random()*9+1)*100000));
        SQLiteDatabase db = newuserDBHelper.getWritableDatabase();
        db.execSQL("UPDATE userInfo SET verification = ? WHERE address = ?",new String[]{verification,mail});
        db.close();
        SendMailUtil.send(mail,getStr(R.string.mail_content_op)+"\n\n"+verification+"\n\n"+getStr(R.string.mail_content_ed));
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