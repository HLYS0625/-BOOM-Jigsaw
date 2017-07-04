package com.cugb.xiaob.mozaiku;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

public class detailedPage extends AppCompatActivity implements View.OnClickListener {

    //从页面一传入i的值，用以确定调取哪张图片
    private int i;
    //图片数组，用来保存分割后的小拼图
    private ArrayList<Block> mData = new ArrayList<>();
    //随机数组，用以打乱拼图的顺序
    int[] r = new int[25];
    //图片数组，用于调取图片
    private int[] pic_list = {
            R.drawable.overwatch_04,
            R.drawable.girls_panzer_rsa_05,
            R.drawable.typemoon_shiki_15,
            R.drawable.akunohana_08,
            R.drawable.deathnote_08,
            R.drawable.bleach_h_23,
            R.drawable.psycho_pass_03,
            R.drawable.aido_06,
            R.drawable.hibike_nakayoshi_17,
            R.drawable.touhou_hakugyokurou_youmu_h_34,
            R.drawable.suzumiya_05,
            R.drawable.gaburiiru_vina_13,
            R.drawable.demichan_08,
            R.drawable.bijyutubu_11};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.detailed_page);

        setPic();
        Button besy = (Button) findViewById(R.id.easy);
        Button bnml = (Button) findViewById(R.id.noomaru);
        Button bhrd = (Button) findViewById(R.id.hard);
        Button bbck = (Button) findViewById(R.id.help);
        bbck.setOnClickListener(this);
        bhrd.setOnClickListener(this);
        bnml.setOnClickListener(this);
        besy.setOnClickListener(this);
    }
    private void setPic() {
        Intent it = getIntent();
        i = it.getIntExtra("msg", 404);
        if (i == 404) {
            Toast.makeText(detailedPage.this, "Something Wrong,Can't finding the picture",
                    Toast.LENGTH_SHORT).show();
            it.setClass(detailedPage.this, MainActivity.class);
            startActivity(it);
        } else {
            ImageView rei_pic = (ImageView) findViewById(R.id.rei);
            rei_pic.setImageResource(pic_list[i]);
        }
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.help:
                Toast.makeText(detailedPage.this, "敬请期待QwQ", Toast.LENGTH_SHORT).show();
                break;
            case R.id.easy:
                chooseLevel(3,3,i);
                break;
            case R.id.noomaru:
                chooseLevel(4,4,i);
                break;
            case R.id.hard:
                chooseLevel(5,5,i);
                break;
            default:
                Toast.makeText(detailedPage.this, "敬请期待QwQ", Toast.LENGTH_SHORT).show();
                break;
        }
    }
    private Bitmap zoomBitmap(Bitmap Bm, int w, int h) {
        //得到原始位图和要得到的宽高
        int width = Bm.getWidth();
        int height = Bm.getHeight();
        float wb = ((float) w / width);
        float hb = ((float) h / height);
        Matrix mx = new Matrix();
        mx.postScale(wb, hb);
        return Bitmap.createBitmap(Bm,0,0,width,height,mx,true);
    }
    private Bitmap cutBitmap(Bitmap Bm,int x,int y,int w,int h){
        return Bitmap.createBitmap(Bm,x,y,w,h);
    }
    private  void chooseLevel(int rows,int cols,int x){
        Bitmap bm = BitmapFactory.decodeResource(getResources(), pic_list[x]);
        bm=zoomBitmap(bm,355*3,450*3);
        Rdm(rows,cols);
        TableLayout tl = (TableLayout)findViewById(R.id.tbl);
        tl.removeAllViewsInLayout();
        TableRow.LayoutParams lpBlock = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,TableRow.LayoutParams.WRAP_CONTENT);
        lpBlock.setMargins(5,5,0,0);
        int blockw = bm.getWidth()/cols;
        int blockh = bm.getHeight()/rows;
        for(int i=0;i<rows;i++){
            TableRow curRow = new TableRow(this);
            curRow.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            ));
            tl.addView(curRow);
            for(int j =0;j<cols;j++){
                ImageView curiv = new ImageView(this);
                curiv.setLayoutParams(lpBlock);
                curiv.setScaleType(ImageView.ScaleType.FIT_XY);
                Bitmap b = cutBitmap(bm,j*blockw,i*blockh,blockw,blockh);
                curiv.setImageBitmap(b);
                mData.add(new Block(b,i*j+i+j));
                curiv.setId((i+1)*(j+1));
                curRow.addView(curiv);
            }
        }
    }
    private void Rdm(int rows,int cols){
        for(int i=0;i<rows*cols;i++){
            HashSet<Integer> intHS = new HashSet<>();
            Random random = new Random();
            r[i] = random.nextInt(25);
            if(!intHS.contains(r[i])){
                intHS.add(r[i]);
            }
        }
    }
    private void refresh(int rows,int cols) {
    }
}