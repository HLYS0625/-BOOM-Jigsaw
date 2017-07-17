package com.cugb.xiaob.mozaiku;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaCodec;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by HLYS on 2017/7/14.
 */

public class NewuserView extends Activity {
    public DBOpenHelper newuserDBHelper=new DBOpenHelper(NewuserView.this,2);

    //账号,密码,邮箱字符串
    String username;
    String password;
    String address;

    //_______________________
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置无标题且全屏、位置必须位于调用资源文件前，onCreate函数后面
        requestWindowFeature(Window.FEATURE_PROGRESS);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.newuser_page);
        //
        //点击editText以外的地方键盘消失

        findViewById(R.id.newuser).setOnClickListener(new View.OnClickListener(){
            public  void onClick(View v){
                InputMethodManager imm = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        });

        // 实例化
         final EditText ETuserName=(EditText)findViewById(R.id.nameText);
         final EditText ETpassWord=(EditText)findViewById(R.id.passwordText);
         final EditText ETmail = (EditText)findViewById(R.id.mailText);
         Button btnCancel=(Button)findViewById(R.id.cancel);
         Button btnOk=(Button)findViewById(R.id.ok);
         final TextView toast002=(TextView)findViewById(R.id.toast02);


        //取消按钮 点击监听
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转页面
                Intent it = new Intent(NewuserView.this, LoginView.class);
                startActivity(it);
                finish();
            }
        });

        //ok按钮 点击监听
        btnOk.setOnClickListener(new View.OnClickListener() {
            //获取代注册的账号密码
            @Override
            public void onClick(View v) {
                //获取待注册的账号密码
                username=ETuserName.getText().toString();
                password=ETpassWord.getText().toString();
                address=ETmail.getText().toString();
                //添加非空检测，以及邮箱的正则检测
                if(!(username.equals("")||password.equals("")||!isEmail(address))) {
                    switch (searchByDB(username, address)) {
                        case 1:
                            toast002.setText("ヾ(ﾟ∀ﾟゞ):\n"+getStr(R.string.user_exists));
                            break;
                        case 2:
                            saveuserByDB(username, password, address);
                            if (searchByDB(username, password) == 1) {
                                toast002.setText("_(:зゝ∠)_:\n" + getStr(R.string.userinfo_saved));
                                finish();
                            } else toast002.setText("︿(￣︶￣)︿:\n" + getStr(R.string.fill_problem));
                            break;
                        case 0:
                            toast002.setText("∠( °ω°)／ :\n" + getStr(R.string.mail_exist));
                            break;
                    }
                }else toast002.setText("Σ(ﾟдﾟlll)/：\n " + getStr(R.string.fill_problem));
            }
        });
    }


    //从资源文件中读取字符串
    private String getStr(int i){
        return getResources().getString(i);
    }


    //数据库操作相关
    //在数据库中储存用户信息
    private void saveuserByDB(String userName,String password,String address){
        SQLiteDatabase db = newuserDBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("userName",userName);
        values.put("password",password);
        values.put("address",address);
        //db的insert函数的参数分别为『要操作的表名』，『要强行插入null值的列名』，『插入的一行数据』
        db.insert("userInfo",null,values);
        db.close();
        Toast.makeText(NewuserView.this,R.string.userinfo_saved,Toast.LENGTH_SHORT).show();
    }
    //在数据库中搜索用户信息 用户已存在返回 1  创建成功返回2  邮箱已注册返回0
    private int searchByDB(String userName,String mail){
        SQLiteDatabase db=newuserDBHelper.getReadableDatabase();
//        //参数依次是:数据库查询语句  查询数据库中是否有 该用户名
        Cursor cursor1 = db.rawQuery("SELECT * FROM userInfo WHERE userName = ?",new String[]{userName});
        //参数依次是:数据库查询语句  查询数据库中是否有 该邮箱
        Cursor cursor2 = db.rawQuery("SELECT * FROM userInfo WHERE address = ?",new String[]{mail});
        //存在数据才返回true  存在该用户名 返回1
        if(cursor1.moveToFirst()) {
            cursor1.close();
            cursor2.close();
            db.close();
            return 1;
         }else if(cursor2.moveToFirst()) {
            cursor1.close();
            cursor2.close();
            db.close();
            return 0;
        }else {
            cursor1.close();
            cursor2.close();
            db.close();
            return 2;
        }
    }
    //正则检测邮件地址是否符合规则
    private boolean isEmail(String address){
        String str = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
        Pattern p = Pattern.compile(str);
        Matcher m = p.matcher(address);
        return m.matches();
    }

}
