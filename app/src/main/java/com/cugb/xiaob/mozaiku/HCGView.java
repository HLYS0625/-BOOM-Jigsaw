package com.cugb.xiaob.mozaiku;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import static com.cugb.xiaob.mozaiku.R.color.black;

/**
 * Created by 496983022 on 2017/7/11.
 */

public class HCGView extends Activity {
    //挑战记录数据变量  挑战用时 挑战图片位置 挑战者名字 挑战时间
    int hcgUseTime;
    int hcgImagePos;
    String hcgUserName;
    String hcgChallengeTime;
//  放大图片
    ImageView imageViewForBig;
    String username;
    Gallery gallery;
    int pos;
    LinearLayout linearLayout;
    int picture[]={
            R.drawable.hcg_01,
            R.drawable.hcg_02,
            R.drawable.hcg_03,
            R.drawable.hcg_04,
            R.drawable.hcg_05
    };

    //数据库存取用到的变量
    private HcgDBOpenHelper myDBH ;
    TextView textView;


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
        //监控画廊图片选择
        ChooseImage();
        //监控FIGHT按钮
        Challenge();
        myDBH= new HcgDBOpenHelper(this,"hcgInfo.db",null,1);
       textView=(TextView)findViewById(R.id.hcg_score);
        imageViewForBig=(ImageView)findViewById(R.id.hcg_view_big);
//
    }
    //选择画廊图片
    public void ChooseImage(){
        gallery=(Gallery)findViewById(R.id.ga);
        gallery.setAdapter(new imageAdapter(this));
//        选择画廊里的一张图片 然后将他放大变成背景
        linearLayout=(LinearLayout)findViewById(R.id.bg);
        gallery.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                searchScoreByDB(position);
                pos=position;
                imageViewForBig.setBackgroundResource(picture[position]);
//                linearLayout.setBackgroundResource(picture[position]);
            }
            //        觉得没用的一行代码
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                linearLayout.setBackgroundResource(picture[0]);
            }
        });

    }
    //画廊图片适配
    public class imageAdapter extends BaseAdapter {
        private Context mcontext;
        int mGalleryItemBackground;

        public imageAdapter(Context context){
            mcontext=context;
            TypedArray array= obtainStyledAttributes(R.styleable.GallerH);
            mGalleryItemBackground=array.getResourceId(R.styleable.GallerH_android_galleryItemBackground,0);
            array.recycle();

        }
        @Override
        public int getCount() {
            return picture.length;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            ImageView imageView=new ImageView(mcontext);
            imageView.setImageResource(picture[position]);
            imageView.setId(picture[position]);
            imageView.setLayoutParams(new  Gallery.LayoutParams(120,160));
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setBackgroundResource(mGalleryItemBackground);
            return imageView;
        }
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
        //可能有错误？应该不会吧  没错!
        String tempPos=String.valueOf(position);
        String[] str={tempPos};
        Cursor cursor = db.rawQuery("SELECT * FROM hcgInfo WHERE imagePos = ?  order by useTIme",str );
        if(cursor.moveToFirst()){
            cursor.moveToFirst();
            Toast.makeText(HCGView.this,"提取信息",Toast.LENGTH_SHORT).show();
            hcgImagePos=position;
            textView.setText("好像有数据欸"+hcgImagePos);

                hcgChallengeTime = cursor.getString(cursor.getColumnIndex("challengeTime"));
                hcgUserName = cursor.getString(cursor.getColumnIndex("userName"));
                hcgImagePos = cursor.getInt(cursor.getColumnIndex("imagePos"));
                hcgUseTime = cursor.getInt(cursor.getColumnIndex("useTIme"));
                textView.setTextSize(30);
                textView.setText("挑战者：" + hcgUserName + "\n"+"挑战时间：" + hcgChallengeTime
                    + "\n"+ "挑战用时" + hcgUseTime);

        }else {
            Toast.makeText(HCGView.this,"无数据",Toast.LENGTH_SHORT).show();
            textView.setText("未有人挑战成功");
        }
        cursor.close();
        db.close();
    }

}
