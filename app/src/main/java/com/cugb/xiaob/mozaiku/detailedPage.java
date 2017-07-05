package com.cugb.xiaob.mozaiku;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

public class detailedPage extends AppCompatActivity implements View.OnClickListener {
    //从页面一传入i的值，用以确定调取哪张图片
    private int i;
    //状态值：0=初始状态，1=已选择难度，游戏中，2=游戏胜利
    private int state=0;
    //图片数组，用来保存分割后的小拼图
    private ArrayList<Block> mData = new ArrayList<>();
    //随机数组，用以打乱拼图的顺序
    int[] r = new int[25];
    //图片数组，用于调取图片
    private final  static int[] pic_list = {
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
            R.drawable.bijyutubu_11
    };
    //存放所有的零碎图片
    private ImageView[] picBlock;

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
    //根据主菜单传入的数值，决定使用的图片
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
    //按钮监测器
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.help:
                Toast.makeText(detailedPage.this, "敬请期待QwQ", Toast.LENGTH_SHORT).show();
                break;
            case R.id.easy:
                hint(state,3,3);
                    break;
            case R.id.noomaru:
                hint(state,4,4);
                    break;
            case R.id.hard:
                hint(state,5,5);
                    break;
            default:
                Toast.makeText(detailedPage.this, "敬请期待QwQ", Toast.LENGTH_SHORT).show();
                break;
        }
    }
    //放大/缩小所给的位图
    private Bitmap zoomBitmap(Bitmap Bm, int w, int h) {
        //得到原始位图和要得到的宽高
        int width = Bm.getWidth();
        int height = Bm.getHeight();
        //根据原始位图的宽高和期望的宽高算出缩放倍率
        float wb = ((float) w / width);
        float hb = ((float) h / height);
        //矩阵，位图操作所必须
        Matrix mx = new Matrix();
        mx.postScale(wb, hb);
        return Bitmap.createBitmap(Bm,0,0,width,height,mx,true);
    }
    //切割位图……话说这就一行代码真的要弄个函数出来嘛=。=
    private Bitmap cutBitmap(Bitmap Bm,int x,int y,int w,int h){
        return Bitmap.createBitmap(Bm,x,y,w,h);
    }
    //按照所选难度分割图片，并将分割好的图片储存在ArrayList<Block>中。
    private  void chooseLevel(int rows,int cols,int x){
        int no =0;
        Bitmap bm = BitmapFactory.decodeResource(getResources(), pic_list[x]);
        bm=zoomBitmap(bm,355*3,450*3);
        Rdm(rows,cols);
        int blockw = bm.getWidth()/cols;
        int blockh = bm.getHeight()/rows;
        for(int i=0;i<rows;i++){
            for(int j =0;j<cols;j++,no++){
                Bitmap b = cutBitmap(bm,j*blockw,i*blockh,blockw,blockh);
                mData.add(new Block(b,no));
                Log.d("de",no+"(chooseLevel)");
            }
        }
    }
    //判断在数列中有无所给元素
    public static boolean NExist(int[] arr, int targetValue) {
        for(int a: arr){
            if(a==targetValue)
                return false;
        }
        return true;
    }
    //生成1-9/16/25的随机数列（作为拼图随机顺序的依据）
    private void Rdm(int rows,int cols){
        int a;
        for(int i=0;i<rows*cols-1;i++){
            Random random = new Random();
            a=random.nextInt(rows*cols);
            if(NExist(r,a)){
                r[i] = a;
                Log.d("De0","a:"+a);
            }
            else i-=1;
        }
        r[rows*cols-1]=0;
    }
    //初始化中间的TableLayout（也就是拼图）
    private void initPic(int rows,int cols) {
        picBlock = new ImageView[rows*cols];
        TableLayout tl = (TableLayout) findViewById(R.id.tbl);
        tl.removeAllViewsInLayout();
        TableRow.LayoutParams lpBlock = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        lpBlock.setMargins(5, 5, 0, 0);
        for (int i = 0; i < rows; i++) {
            TableRow curRow = new TableRow(this);
            curRow.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            ));
            tl.addView(curRow);
            for (int j = 0; j < cols; j++) {
                ImageView curiv = new ImageView(this);
                curiv.setLayoutParams(lpBlock);
                curiv.setScaleType(ImageView.ScaleType.FIT_XY);
                int no = i * cols + j;
                curiv.setImageBitmap(mData.get(r[no]).getiBm());
                curiv.setId(no);
                curiv.setTag(no+"_"+mData.get(r[no]).getIno());
                picBlock[no]=curiv;
                curiv.setOnClickListener(this);
                curRow.addView(curiv);
                Log.d("de", no + "(initPic)");
                Log.d("de", r[no] + "(initPicR[no])");
            }
        }
    }
    //state=0，开始一盘新游戏；state=1，选择重新开始游戏或继续游戏；state=3，返回主选单或重新开始游戏。
    private void hint(int s, final int rows, final int cols){
        if(s==1){
            AlertDialog alt ;
            AlertDialog.Builder alb = new AlertDialog.Builder(detailedPage.this);
            alt = alb.setIcon(R.drawable.konosuba_h_01)
                    .setTitle("ヒント")
                    .setMessage("おっど、きみはもう難易度を選択したよ\n\n開発者：理子")
                    .setPositiveButton("新しいゲーム", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            newgame(rows,cols);
                        }
                    })
                    .setNegativeButton("続く", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(detailedPage.this,"もう少し頑張ってください、成功は目の前です",Toast.LENGTH_SHORT).show();
                        }
                    })
                    .create();
            alt.show();
        }else if(s==2){
            AlertDialog alt ;
            AlertDialog.Builder alb = new AlertDialog.Builder(detailedPage.this);
            alt = alb.setIcon(R.drawable.konosuba_h_01)
                    .setTitle("ヒント")
                    .setMessage("おめでとうございます\n\n開発者：理子")
                    .setPositiveButton("もう一度プレーしたい", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            newgame(rows,cols);
                        }
                    })
                    .setNegativeButton("他のピクチャー", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent i = new Intent(detailedPage.this,MainActivity.class);
                            startActivity(i);
                            finish();
                        }
                    })
                    .create();
            alt.show();
        }else if(state==0){
            chooseLevel(rows, cols, i);
            initPic(rows, cols);
            state = 1;
        }
    }
    //按照所选难度开始一盘新游戏。
    private void newgame(int rows,int cols){
        state=0;
        mData.clear();
        for(int i=0;i<25;i++){
            r[i]=0;
        }
        TableLayout t1 = (TableLayout)findViewById(R.id.tbl);
        t1.removeAllViewsInLayout();
        chooseLevel(rows, cols, i);
        initPic(rows, cols);
    }
    private void judge(ImageView V){
        if(state==0){
            Bitmap bm = BitmapFactory.decodeResource(getResources(),R.drawable.black);
            int w = V.getWidth();
            int h = V.getHeight();
            bm = zoomBitmap(bm,w,h);
            V.setImageBitmap(bm);
            V.setId(R.id.nblock);
            state=1;
        }else if(state==2){
            Toast.makeText(detailedPage.this,"已经完成游戏了哟",Toast.LENGTH_SHORT).show();
        }else if(state==1){
                int x = V.getId();
            if(x+1==R.id.nblock){

            }
        }
    }
}