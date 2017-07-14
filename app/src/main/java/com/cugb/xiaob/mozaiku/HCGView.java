package com.cugb.xiaob.mozaiku;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

/**
 * Created by 496983022 on 2017/7/11.
 */

public class HCGView extends Activity implements ViewSwitcher.ViewFactory, View.OnTouchListener {
    //挑战记录数据变量  挑战用时 挑战图片位置 挑战者名字 挑战时间
    int hcgUseTime;
    String hcgUserName;
    String hcgChallengeTime;
    String username;
    int pos;
    int picture[]={
            R.drawable.hcg_01,
            R.drawable.hcg_02,
            R.drawable.hcg_03,
            R.drawable.hcg_04,
            R.drawable.hcg_05,
            R.drawable.hcg_06
    };

    //数据库存取用到的变量
    private HcgDBOpenHelper myDBH ;
    TextView textViewhcgname;
    TextView textViewhcgTime;
    TextView textViewhcguseTime;

    //ImageSwitcher的引用
    private ImageSwitcher mImageSwitcher;
    //当前选中的图片序号
    private int currentPosition;
    //按下的X点的坐标
    private float downX;




    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去标题
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.hcg_view );
        Intent intent=getIntent();
        username = intent.getStringExtra("username");

        //实例化ImageSwitcher
        mImageSwitcher  = (ImageSwitcher) findViewById(R.id.imageSwitcher1);
        //设置Factory
        mImageSwitcher.setFactory(this);
        //设置OnTouchListener，我们通过Touch事件来切换图片
        mImageSwitcher.setOnTouchListener(this);
        currentPosition=0;
        mImageSwitcher.setImageResource(picture[0]);



        //监控FIGHT按钮
        Challenge();
        myDBH= new HcgDBOpenHelper(this,"hcgInfo.db",null,1);
       textViewhcgname=(TextView)findViewById(R.id.hcg_score_name);
        textViewhcgTime=(TextView)findViewById(R.id.hcg_score_time);
        textViewhcguseTime=(TextView)findViewById(R.id.hcg_score_usetime);
//
    }



    @Override
    public View makeView() {
        final ImageView i = new ImageView(this);
        i.setBackgroundColor(0xff000000);
        i.setScaleType(ImageView.ScaleType.CENTER_CROP);
        i.setLayoutParams(new ImageSwitcher.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
        return i ;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:{
                //手指按下的X坐标
                downX = event.getX();
                break;
            }
            case MotionEvent.ACTION_UP:{
                float lastX = event.getX();
                //抬起的时候的X坐标大于按下的时候就显示上一张图片
                if(lastX > downX){
                    if(currentPosition > 0){
                        //设置动画，这里的动画比较简单，不明白的去网上看看相关内容
                        mImageSwitcher.setInAnimation(AnimationUtils.loadAnimation(getApplication(), R.anim.left_in));
                        mImageSwitcher.setOutAnimation(AnimationUtils.loadAnimation(getApplication(), R.anim.right_out));
                        currentPosition --;
                        mImageSwitcher.setImageResource(picture[currentPosition % picture.length]);
                    }else{
                        Toast.makeText(getApplication(), "已经是第一张", Toast.LENGTH_SHORT).show();
                    }
                }

                if(lastX < downX){
                    if(currentPosition < picture.length - 1){
                        mImageSwitcher.setInAnimation(AnimationUtils.loadAnimation(getApplication(), R.anim.right_in));
                        mImageSwitcher.setOutAnimation(AnimationUtils.loadAnimation(getApplication(), R.anim.left_out));
                        currentPosition ++ ;
                        mImageSwitcher.setImageResource(picture[currentPosition]);
                    }else{
                        Toast.makeText(getApplication(), "到了最后一张", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }

        return true;
    }





    //开始挑战
    public void Challenge(){
        Button button=(Button)findViewById(R.id.challenge);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent();
                intent.putExtra("username",username);
                intent.putExtra("Imagepos",pos);
                intent.setClass(HCGView.this,HCGPlay.class);
                startActivity(intent);

            }
        });
    }

    //在数据库中搜索 图片完成记录 KEY 图片位置
    private void searchScoreByDB(int position){
        SQLiteDatabase db=myDBH.getWritableDatabase();
        String tempPos=String.valueOf(position);
        String[] str={tempPos};
        Cursor cursor = db.rawQuery("SELECT * FROM hcgInfo WHERE imagePos = ?  ",str );
        if(cursor.moveToFirst()){
            do {
                hcgChallengeTime = cursor.getString(cursor.getColumnIndex("challengeTime"));
                hcgUserName = cursor.getString(cursor.getColumnIndex("userName"));
                hcgUseTime = cursor.getInt(cursor.getColumnIndex("useTIme"));
            }while (cursor.moveToNext());
            textViewhcgname.setTextSize(30);
            textViewhcgTime.setTextSize(30);
            textViewhcguseTime.setTextSize(30);
            textViewhcgname.setText("挑战者： " + hcgUserName+ " ");
            textViewhcgTime.setText("挑战时间：" + hcgChallengeTime+" " );
            textViewhcguseTime.setText("挑战用时: " + hcgUseTime + "  秒");

        }else {
            textViewhcgname.setText("挑战者： 无 ");
            textViewhcgTime.setText("挑战时间：无 " );
            textViewhcguseTime.setText("挑战用时: 无 ");
        }
        cursor.close();
        db.close();
    }

}
