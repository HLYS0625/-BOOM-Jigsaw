package com.cugb.xiaob.mozaiku;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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

/**
 * Created by HLYS on 2017/7/14.
 */

public class NewuserView extends Activity {
    public DBOpenHelper newuserDBHelper=new DBOpenHelper(NewuserView.this,2);
    private Context mContext;
    private String userForIntent;


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
        final EditText userName=(EditText)findViewById(R.id.nameText);
        final EditText passWord=(EditText)findViewById(R.id.passwordText);
        final Button btncancel=(Button)findViewById(R.id.cancel);
        final Button btnok=(Button)findViewById(R.id.ok);
        final TextView toast002=(TextView)findViewById(R.id.toast02);
        final String username=userName.getText().toString();
        final String password=passWord.getText().toString();
        Toast.makeText(NewuserView.this,username,Toast.LENGTH_SHORT).show();
        btncancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转页面
                Intent it = new Intent(NewuserView.this, LoginView.class);
                it.putExtra("username", userForIntent);
                startActivity(it);
                finish();
            }
        });

        //
        btnok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(searchByDB(username,password)==1){
                    Toast.makeText(NewuserView.this,username,Toast.LENGTH_SHORT).show();
                    toast002.setText("ヾ(ﾟ∀ﾟゞ):\n已存在该用户！");
                }
                else{
                    saveuserByDB(username,password);
                    if(searchByDB(username,password)==1){
                        toast002.setText("ヾ(ﾟ∀ﾟゞ):\n创建成功！");
                        Intent it = new Intent(NewuserView.this, LoginView.class);
                        it.putExtra("username", userForIntent);
                        startActivity(it);
                        finish();
                    }else toast002.setText("︿(￣︶￣)︿:\n出问题啦~~~");
                }
            }
        });

        }

    //数据库操作相关
    //在数据库中储存用户信息
    private void saveuserByDB(String userName,String password){
        SQLiteDatabase db = newuserDBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("userName",userName);
        values.put("password",password);
        //db的insert函数的参数分别为『要操作的表名』，『要强行插入null值的列名』，『插入的一行数据』
        db.insert("userInfo",null,values);
        Toast.makeText(NewuserView.this,R.string.userinfo_saved,Toast.LENGTH_SHORT).show();
    }
    //在数据库中搜索用户信息
    private int searchByDB(String userName,String password){
        SQLiteDatabase db=newuserDBHelper.getReadableDatabase();
        //参数依次是:数据库查询语句
        Cursor cursor = db.rawQuery("SELECT * FROM userInfo WHERE userName = ?",new String[]{userName});
        while (cursor.moveToNext()){
            String name = cursor.getString(cursor.getColumnIndex("userName"));
            if (userName.equals(name)){
                Toast.makeText(NewuserView.this,"name:"+name,Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(NewuserView.this,"没有",Toast.LENGTH_SHORT).show();
            }
            Log.d("NewuserView",name);
        }

        //存在数据才返回true
//        if(cursor.moveToFirst()) {
//
//            cursor.close();
//            Cursor newCursor = db.rawQuery("SELECT * FROM userInfo WHERE userName = ? AND password = ?",new String[]{userName,password});
//            if(newCursor.moveToFirst()) {
//                newCursor.close();
//                db.close();
//                return 1;
//            }
//            else {
//                newCursor.close();
//                db.close();
//                return 2;
//            }
//        }else {
//            cursor.close();
//            db.close();
//            return 0;
//        }
        return 1;
    }


}
