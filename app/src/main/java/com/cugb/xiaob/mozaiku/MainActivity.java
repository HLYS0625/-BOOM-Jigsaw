package com.cugb.xiaob.mozaiku;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AlertDialogLayout;
import android.text.GetChars;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置无标题栏且全屏、位置必须处于调用资源文件前，onCreate函数后
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        //常用变量
        mContext = MainActivity.this;
        //grid声明
        gridPhoto = (GridView)findViewById(R.id.grid_photo);

        //将图片作为ArrayList存储，以便于Grid使用
        ArrayList<Icon> mData = new ArrayList<>();

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



        mAdapter = new MyAdapter<Icon>(mData,R.layout.gridview_layout) {
            @Override
            public void bindView(ViewHolder holder, Icon obj) {
                holder.setImageResource(R.id.img_icon,obj.getiId());
                holder.setText(R.id.txt_icon,obj.getiName());
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        final LayoutInflater inflater = MainActivity.this.getLayoutInflater();
        final View view_custom = inflater.inflate(R.layout.custom_dialog,null,false);
        builder.setView(view_custom);
        final AlertDialog alert = builder.create();
        view_custom.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.dismiss();
                Toast.makeText(mContext,"请输入用户名后进行游戏",Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        view_custom.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = (EditText)view_custom.findViewById(R.id.nameText);
                String userName = editText.getText().toString();
                if(userName != null){
                    if(!finduser(userName)) {
                        saveuser(userName);
                        Toast.makeText(mContext,"未找到此用户，已新建用户名",Toast.LENGTH_SHORT).show();
                        alert.dismiss();
                    }else{
                        Toast.makeText(mContext,"欢迎回来，"+userName,Toast.LENGTH_SHORT).show();
                        alert.dismiss();
                    }
                }
            }
        });
        alert.show();






        gridPhoto.setAdapter(mAdapter);
        gridPhoto.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent it = new Intent(MainActivity.this,detailedPage.class);
                it.putExtra("msg",position);
                startActivity(it);
            }
        });

        Button b = (Button)findViewById(R.id.help);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
            }
        });


    }

    private Context mContext;
    private GridView gridPhoto;
    private BaseAdapter mAdapter =null;
    private ArrayList<Icon> mData = null;
    private String getStr(int i){
        return getResources().getString(i);
    }


    //在文件中存储用户名
    public void saveuser(String username) {
        try {
            FileOutputStream outStream=this.openFileOutput("user.txt",Context.MODE_APPEND);
            username+=",";
            outStream.write(username.getBytes());
            outStream.close();
            Toast.makeText(MainActivity.this,"User Info Saved",Toast.LENGTH_SHORT).show();
        } catch (IOException e){
            //TODO:handle exception
        }
    }
    //在文件中搜索用户名，找到返回true
    public Boolean finduser(String username){
        try{
            boolean arimasu =false;
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
            for (int i=0;i<users.length;i++){
                if(users[i].equals(username))
                    arimasu =  true;
            }
            return arimasu;
        }catch (FileNotFoundException e){
            return false;
        }catch (IOException e){
            return false;
        }
    }
}

