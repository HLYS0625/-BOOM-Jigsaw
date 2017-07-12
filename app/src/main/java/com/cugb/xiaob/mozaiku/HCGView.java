package com.cugb.xiaob.mozaiku;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
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
import android.widget.Toast;

/**
 * Created by 496983022 on 2017/7/11.
 */

public class HCGView extends Activity {
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


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hcg_view );
        Intent intent=getIntent();
        username = intent.getStringExtra("username");
        //监控画廊图片选择
        ChooseImage();
        //监控FIGHT按钮
        Challenge();

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
                pos=position;
                linearLayout.setBackgroundResource(picture[position]);
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
}
