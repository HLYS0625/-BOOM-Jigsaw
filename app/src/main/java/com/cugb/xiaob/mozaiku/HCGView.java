package com.cugb.xiaob.mozaiku;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by 496983022 on 2017/7/11.
 */

public class HCGView extends FragmentActivity {
    //挑战记录数据变量  挑战用时 挑战图片位置 挑战者名字 挑战时间
    int hcgUseTime;
    String hcgUserName;
    String hcgChallengeTime;
    String username;
    int picture[]={
            R.drawable.hcg_06,//为了在ViewPager首项可以向左划到最后一项，数组内添加这一项目
            R.drawable.hcg_01,
            R.drawable.hcg_02,
            R.drawable.hcg_03,
            R.drawable.hcg_04,
            R.drawable.hcg_05,
            R.drawable.hcg_06,
            R.drawable.hcg_01//为了在ViewPager末项可以划到第一项，添加这个衔接
    };

    //数据库存取用到的变量
    private HcgDBOpenHelper myDBH ;
    TextView textViewhcgname;
    TextView textViewhcgTime;
    TextView textViewhcguseTime;

    //ViewPager的引用
    private ViewPager mPager;




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

        //实例化ViewPager
        mPager = (ViewPager) findViewById(R.id.pager);
        //实例化ViewPager的监听器
        PictureSlidePagerAdapter mPagerAdapter = new PictureSlidePagerAdapter(getSupportFragmentManager());
        //为ViewPager添加监听器
        mPager.setAdapter(mPagerAdapter);
        //划动改变事件
        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int arg0) {
                //划动结束后搜索数据库，并更新UI
                searchScoreByDB(arg0);

            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
                //页面划动中事件，空置中
            }

            @Override
            //页面状态改变时调用此算法，目前用以实现循环
            public void onPageScrollStateChanged(int state) {
                // 当到第一张时切换到倒数第二张，当到最后一张时切换到第二张
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    int curr = mPager.getCurrentItem();
                    int lastReal = mPager.getAdapter().getCount() - 2;
                    if (curr == 0) {
                        mPager.setCurrentItem(lastReal, false);
                    } else if (curr > lastReal) {
                        mPager.setCurrentItem(1, false);
                    }
                }
            }
        });



        //监控FIGHT按钮
        Challenge(mPager.getCurrentItem());
        myDBH= new HcgDBOpenHelper(this,"hcgInfo.db",null,1);
       textViewhcgname=(TextView)findViewById(R.id.hcg_score_name);
        textViewhcgTime=(TextView)findViewById(R.id.hcg_score_time);
        textViewhcguseTime=(TextView)findViewById(R.id.hcg_score_usetime);
        //搜索一次
        searchScoreByDB(0);
//
    }


    //ViewPager 的适配器
    private class PictureSlidePagerAdapter extends FragmentStatePagerAdapter {

        public PictureSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return PictureSlideFragment.newInstance(picture[position]);
        }

        @Override
        public int getCount() {
            return picture.length;
        }
    }




    //开始挑战
    public void Challenge(int position){
        Button button=(Button)findViewById(R.id.challenge);
        /**
         * 由于为了实现ViewPager的首位循环衔接
         * 更改了picture[]数组
         * 这是为了抵消其影响而进行的加工
         */
        final int p;
        //由于数组第一项插入了hcg06，所以目前的position0是原先的position5
        if(position==0) p=5;
            //由于数组最后一项插入了hcg01，所以目前的position7是原先的position0
        else if(position==7) p=0;
            //由于数组第一项出入了hcg06，所以所有数组应该向前减一操作
        else p = position - 1;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent();
                intent.putExtra("username",username);
                intent.putExtra("Imagepos",p);
                intent.setClass(HCGView.this,HCGPlay.class);
                startActivity(intent);

            }
        });
    }

    //在数据库中搜索 图片完成记录 KEY 图片位置
    private void searchScoreByDB(int position){
        SQLiteDatabase db=myDBH.getWritableDatabase();

        /**
         * 由于为了实现ViewPager的首位循环衔接
         * 更改了picture[]数组
         * 这是为了抵消其影响而进行的加工
         */
        //由于数组第一项插入了hcg06，所以目前的position0是原先的position5
        if(position==0) position=5;
        //由于数组最后一项插入了hcg01，所以目前的position7是原先的position0
        else if(position==7) position=0;
        //由于数组第一项出入了hcg06，所以所有数组应该向前减一操作
        else position -= 1;


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
