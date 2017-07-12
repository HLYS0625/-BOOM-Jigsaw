package com.cugb.xiaob.mozaiku;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AlertDialogLayout;
import android.text.GetChars;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private Context mContext;
    private GridView gridPhoto;
    private BaseAdapter mAdapter =null;
    private String userForIntent;
    //存储首页的Grid所需要的图片
    private ArrayList<Icon> mData = new ArrayList<>();
    //数据库存取用到的变量
    private DBOpenHelper myDBHelper = new DBOpenHelper(MainActivity.this,1);
    //侧边菜单需要用到的变量
    private DrawerLayout drawer_layout;
    private ListView list_left_drawer;
    private ArrayList<Icon> menuLists;
    private MyAdapter<Icon> myAdapter2 = null;




//______________________________________变量和方法的分割线__________________________________\\

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置无标题栏且全屏、位置必须处于调用资源文件前，onCreate函数后
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        //常用变量,需写在onCreate函数中
        mContext = MainActivity.this;
        //grid声明
        gridPhoto = (GridView)findViewById(R.id.grid_photo);
        initGrid_pic(mData);

        //声明并初始化侧边菜单
        drawer_layout = (DrawerLayout) findViewById(R.id.drawer_Layout);
        list_left_drawer = (ListView) findViewById(R.id.left_drawer);

        menuLists = new ArrayList<Icon>();
        menuLists.add(new Icon(R.mipmap.ic_launcher,getStr(R.string.help)));
        menuLists.add(new Icon(R.mipmap.ic_launcher,getStr(R.string.goToHS)));
        menuLists.add(new Icon(R.mipmap.ic_launcher,getStr(R.string.HCG)));


        //侧滑菜单选择器
        myAdapter2 = new MyAdapter<Icon>(menuLists,R.layout.fg_content) {
            @Override
            public void bindView(ViewHolder holder, Icon obj) {
                holder.setText(R.id.tv_content, obj.getiName());
            }
        };



        //主界面图片选择器
        mAdapter = new MyAdapter<Icon>(mData,R.layout.gridview_layout) {
            @Override
            public void bindView(ViewHolder holder, Icon obj) {
                holder.setImageResource(R.id.img_icon,obj.getiId());
                holder.setText(R.id.txt_icon,obj.getiName());
            }
        };


        //弹出自定义对话框（登录界面）
        tankuang();



        //为主界面图片选择gird建立监听器
        gridPhoto.setAdapter(mAdapter);
        gridPhoto.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent it = new Intent(MainActivity.this,detailedPage.class);
                it.putExtra("msg",position);
                it.putExtra("username",userForIntent);
                startActivity(it);
            }
        });

        //为侧滑菜单建立监听器
        list_left_drawer.setAdapter(myAdapter2);
        list_left_drawer.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                switch (position){
                    case 0:
                        AlertDialog alt ;
                        AlertDialog.Builder alb = new AlertDialog.Builder(mContext);
                        alt = alb.setIcon(R.drawable.konosuba_h_01)
                                .setTitle(R.string.help)
                                .setMessage(getStr(R.string.helpMsg)+getStr(R.string.coder))
                                .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Animation anime = AnimationUtils.loadAnimation(mContext,R.anim.rotate);
                                        gridPhoto.startAnimation(anime);
                                    }
                                }).create();
                        alt.show();
                        break;
                    case 1:
                        Intent it = new Intent(MainActivity.this,highScore.class);
                        it.putExtra("username",userForIntent);
                        startActivity(it);
                        break;
                    case 2:
                        Intent intent=new Intent(MainActivity.this,HCGView.class);
                        intent.putExtra("username",userForIntent);
                        startActivity(intent);
                        break;
                }
            }
        });
    }

//工具函数
    //获取存储在string资源文件中的字符串
    private String getStr(int i){
        return getResources().getString(i);
    }
    //将图片作为ArrayList存储，以便于Grid使用
    private void initGrid_pic(ArrayList<Icon> mData){
        mData.add(new Icon(R.drawable.overwatch_04,getStr(R.string.overWatch)));
        mData.add(new Icon(R.drawable.girls_panzer_rsa_05,getStr(R.string.girls_panzer)));
        mData.add(new Icon(R.drawable.typemoon_shiki_15, getStr(R.string.karaKyokai)));
        mData.add(new Icon(R.drawable.akunohana_08,getStr(R.string.akunohana)));
        mData.add(new Icon(R.drawable.deathnote_08,getStr(R.string.deathNote)));
        mData.add(new Icon(R.drawable.bleach_h_23,getStr(R.string.bleach)));
        mData.add(new Icon(R.drawable.psycho_pass_03,getStr(R.string.pcychoPass)));
        mData.add(new Icon(R.drawable.aido_06,getStr(R.string.aido)));
        mData.add(new Icon(R.drawable.hibike_nakayoshi_17,getStr(R.string.hibike)));
        mData.add(new Icon(R.drawable.touhou_hakugyokurou_youmu_h_34,getStr(R.string.touhou)));
        mData.add(new Icon(R.drawable.suzumiya_05,getStr(R.string.suzumiya)));
        mData.add(new Icon(R.drawable.gaburiiru_vina_13,getStr(R.string.gaburiiru)));
        mData.add(new Icon(R.drawable.demichan_08,getStr(R.string.demichan)));
        mData.add(new Icon(R.drawable.bijyutubu_11,getStr(R.string.bijyutu)));
        mData.add(new Icon(R.drawable.original_17,getStr(R.string.original)));
        mData.add(new Icon(R.drawable.album,getStr(R.string.album)));
    }


//数据库操作相关
    //在数据库中储存用户信息
    private void saveuserByDB(String userName,String password){
        SQLiteDatabase db = myDBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("userName",userName);
        values.put("password",password);
        //db的insert函数的参数分别为『要操作的表名』，『要强行插入null值的列名』，『插入的一行数据』
        db.insert("userInfo",null,values);
        Toast.makeText(MainActivity.this,R.string.userinfo_saved,Toast.LENGTH_SHORT).show();
    }
    //在数据库中搜索用户信息
    private int searchByDB(String userName,String password){
        SQLiteDatabase db=myDBHelper.getReadableDatabase();
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

//自定义弹框
    //自定义弹出框（登录界面）
    private void tankuang(){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        final LayoutInflater inflater = MainActivity.this.getLayoutInflater();
        final View view_custom = inflater.inflate(R.layout.custom_dialog,null,false);
        builder.setView(view_custom);
        final AlertDialog alert = builder.create();
        final EditText editText_name = (EditText)view_custom.findViewById(R.id.nameText);
        final EditText editText_password = (EditText)view_custom.findViewById(R.id.passwordText);
        view_custom.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.dismiss();
                Toast.makeText(mContext,R.string.cancel_nm,Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        view_custom.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String userName = editText_name.getText().toString();
                final String password = editText_password.getText().toString();
                if(!userName.matches("")&&!password.matches("")){
                    int state = searchByDB(userName,password);
                    if(state==0) {
                        AlertDialog alt ;
                        AlertDialog.Builder alb = new AlertDialog.Builder(mContext);
                        alt = alb.setIcon(R.drawable.konosuba_h_01)
                                .setTitle(R.string.help)
                                .setMessage(getStr(R.string.wrong_nm)+getStr(R.string.coder))
                                .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        saveuserByDB(userName,password);
                                        userForIntent = userName;
                                        alert.dismiss();
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
                        a = String.format(a,userName);
                        userForIntent = userName;
                        Toast.makeText(mContext,a,Toast.LENGTH_SHORT).show();
                        alert.dismiss();
                    }else if(state==2){
                        Toast.makeText(mContext,R.string.wrong_pw,Toast.LENGTH_SHORT).show();
                    }
                }else Toast.makeText(mContext,R.string.null_nm,Toast.LENGTH_SHORT).show();
            }
        });
        alert.setCanceledOnTouchOutside(false);
        alert.setCancelable(false);
        alert.show();

        //监听登录界面账号输入框输入，重写回车事件，使得回车后切换到密码输入
        editText_name.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_SEND || (event != null &&event.getKeyCode()==KeyEvent.KEYCODE_ENTER)){
                    editText_password.requestFocus();//密码输入框获得焦点
                    return true;
                }
                return false;
            }
        });
        //监听登录界面密码输入，重写回车事件，使得回车相当于点击ok按钮
        editText_password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_SEND || (event != null &&event.getKeyCode()==KeyEvent.KEYCODE_ENTER)){
                    view_custom.findViewById(R.id.ok).performClick();
                    return true;
                }
                return false;
            }
        });


    }




/*废弃代码，稳定后删除
    //在文件中存储用户名及密码
    public void saveuser(String username,String password) {
        try {
            String userinfo = username + ":" + password + ",";
            FileOutputStream outStream=this.openFileOutput("user.txt",Context.MODE_APPEND);
            outStream.write(userinfo.getBytes());
            outStream.close();
            Toast.makeText(MainActivity.this,"User Info Saved",Toast.LENGTH_SHORT).show();
        } catch (IOException e){
            //TODO:handle exception
        }
    }
    //在文件中搜索用户名，未找到返回0，找到用户名但密码不正确返回2，用户名和密码匹配返回1
    public int finduser(String username,String password){
        try{
            FileInputStream inStream = this.openFileInput("user.txt");
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length =-1;
            while ((length=inStream.read(buffer))!=-1){
                stream.write(buffer,0,length);
            }
            stream.close();
            inStream.close();
            String text = stream.toString();
            String[] users = text.split(",");
            String[] user;
            for (int i=0;i<users.length;i++){
                user = users[i].split(":");
                if(user[0].equals(username)) {
                    if (user[1].equals(password)) {
                        return 1;
                    } else return 2;
                }
            }return 0;
        }catch (FileNotFoundException e){
            return 0;
        }catch (IOException e){
            return 0;
        }
    }
    */
}

