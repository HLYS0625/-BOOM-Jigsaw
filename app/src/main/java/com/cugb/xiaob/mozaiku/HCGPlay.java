package com.cugb.xiaob.mozaiku;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by 496983022 on 2017/7/11.
 */

public class HCGPlay extends Activity implements View.OnClickListener{
    //黑色方块
    int blbl =404;
    //从页面一传入i的值，用以确定调取哪张图片
    private int i;
    //状态值：0=初始状态，1=已选择难度，游戏中，2=游戏胜利
    private int state=0;
    //图片数组，用来保存分割后的小拼图
    private ArrayList<Block> mData = new ArrayList<>();
    //随机数组，用以打乱拼图的顺序
    int[] r;
    //      示例图片View   貌似可删
    private ImageView rei_pic;
    //fst为玩家点击的图片，sec始终为黑色图片（空白区块）
    private ImageView Fst;
    private ImageView Sec;
    //图片数组，用于调取图片
    private int[] pic_list = {
            R.drawable.hcg_01,
            R.drawable.hcg_02,
            R.drawable.hcg_03,
            R.drawable.hcg_04,
            R.drawable.hcg_05
    };

    //存放所有的零碎图片
    private ImageView[] picBlock;
    //被黑色替代的图片以及从相册中传入的原图
    private Bitmap bitmap;
    int MaxTime=300;
    //游戏开始时间
    int reamainTime;
    //登录用户名
    String username;
    TextView textViewGameTime;
//   是否是第一次调用handler
    int IsFirst=0;
//    获取挑战时间
    String challengeYMD;


    //数据库相关变量
    private HcgDBOpenHelper myDBHelper = new HcgDBOpenHelper(HCGPlay.this,"hcgInfo.db",null,1);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去标题
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.hcg_paly);
        setPic();
        Button buttonchallenge=(Button)findViewById(R.id.hcg_begin);
        textViewGameTime=(TextView)findViewById(R.id.hcg_time);
        buttonchallenge.setOnClickListener(this);
        //时间耗尽监听
        TimeOut();
//        获取挑战时间
        getChallengeYMD();
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.hcg_begin:
                newgame( 2, 2);
                break;
            default:
                click(v);
                break;
        }
    }

    //页面初始化相关
    //根据主菜单传入的数值，决定使用的图片。同时接收主菜单传来的用户名
    private void setPic() {
        Intent it = getIntent();
        i = it.getIntExtra("Imagepos", 404);
        username = it.getStringExtra("username");
        if (i == 404) {
            Toast.makeText(HCGPlay.this, R.string.getPic_wrong,
                    Toast.LENGTH_SHORT).show();
            it.setClass(HCGPlay.this, MainActivity.class);
            startActivity(it);
        } else {
            ImageView rei_pic = (ImageView) findViewById(R.id.hcg_example);
            rei_pic.setImageResource(pic_list[i]);
        }
    }


//    数据库相关
    public void saveChallengeInfo(){
        SQLiteDatabase db = myDBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("userName",username);
        values.put("useTIme",MaxTime-reamainTime);
        values.put("challengeTime",challengeYMD);
        values.put("imagePos",i);
        db.insert("hcgInfo",null,values);
    }


//    获取当前挑战时间
    public void getChallengeYMD(){
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy年MM月dd日HH:mm:ss");
        Date curDate=new Date(System.currentTimeMillis());
        challengeYMD = simpleDateFormat.format(curDate);
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
    //判断游戏是否有解，没有解的话重新生成数组，直到有解为止
    private boolean cansolve(int h) {
        int[][] state = getNumber(h);
        if(h % 2 == 1) { //問題寬度為奇數
            return (getInversions(state) % 2 == 0);
        } else { //問題寬度為偶數
            ImageView iv = (ImageView)findViewById(R.id.nblock);
            String s = (String)iv.getTag();
            String[] str = s.split("_");
            if((h - Integer.decode(str[2])) % 2 == 1) { //從底往上數,空格位于奇數行
                return (getInversions(state) % 2 == 0);
            } else { //從底往上數,空位位于偶數行
                return (getInversions(state) % 2 == 1);
            }
        }
    }
    //计算序列中的逆序数，作为判断拼图是否有解的根据
    private int getInversions(int[][] state) {
        int inversion = 0;
        int temp = 0;
        for(int i=0;i<state.length;i++) {
            for(int j=0;j<state[i].length;j++) {
                int index = i* state.length + j + 1;
                while(index < (state.length * state.length)) {
                    if(state[index/state.length][index%state.length] != 0
                            && state[index/state.length]
                            [index%state.length] < state[i][j]) {
                        temp ++;
                    }
                    index ++;
                }
                inversion = temp + inversion;
                temp = 0;
            }
        }
        return inversion;
    }
    //将图片碎片转化为数组。
    private int[][] getNumber(int wideth) {
        int[][] state = new int[wideth][wideth];
        for (int i = 0, k = 0; i < wideth; i++)
            for (int j = 0; j < wideth; j++, k++) {
                String tag = (String)picBlock[k].getTag();
                String[] tags = tag.split("_");
                int no = Integer.decode(tags[1]);
                if (no == blbl) {
                    state[i][j] = 0;
                } else state[i][j] = no + 1;
            }
        return state;
    }
    //返回图片正确的序号，供judge()函数和cansolve函数使用
    private int getIndexByTag(String tag){
        String[] split = tag.split("_");
        return Integer.parseInt(split[1]);
    }


//    游戏初始化相关的
    //生成1-9/16/25的随机数列（作为拼图随机顺序的依据）
    private void Rdm(int rows,int cols) {
        int a;
        r = new int[rows * cols];
        for (int i = 0; i < rows * cols - 1; i++) {
            Random random = new Random();
            a = random.nextInt(rows * cols);
            if (NExist(r, a)) {
                r[i] = a;
            } else i -= 1;
        }
        r[rows * cols - 1] = 0;
    }
    //判断在数列中有无所给元素，保证上面的Rdm（）函数生成的数列没有重复数字
    public static boolean NExist(int[] arr, int targetValue) {
        for(int a: arr){
            if(a==targetValue)
                return false;
        }
        return true;
    }
    //按照所选难度分割图片，并将分割好的图片储存在ArrayList<Block>中。
    private  void chooseLevel(int rows,int cols,int x){
        int no =0;
        Bitmap bm;
            bm = BitmapFactory.decodeResource(getResources(), pic_list[x]);

        WindowManager wm = (WindowManager) this
                .getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth()-20;
        bm=zoomBitmap(bm,width,width/7*9);
        Rdm(rows,cols);
        int blockw = bm.getWidth()/cols;
        int blockh = bm.getHeight()/rows;
        for(int i=0;i<rows;i++){
            for(int j =0;j<cols;j++,no++){
                Bitmap b = cutBitmap(bm,j*blockw,i*blockh,blockw,blockh);
                mData.add(new Block(b,no,i,j));
                Log.d("de","b"+b+"no"+no);
            }
        }
    }
    //初始化中间的TableLayout（也就是拼图）。按照Rdm生成数组的顺序放入ArrayList<Block>中的图片（达成乱序）
    private void initPic(int rows,int cols) {
        picBlock = new ImageView[rows*cols];
        TableLayout tl = (TableLayout) findViewById(R.id.hcg_tb);
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
                if(r[no]==blbl){
                    curiv.setId(R.id.nblock);
                }else {
                    curiv.setId(no);
                }
                curiv.setTag(no+"_"+mData.get(r[no]).getIno()+"_"+i+"_"+j);
                picBlock[no]=curiv;
                curiv.setOnClickListener(this);
                curRow.addView(curiv);
            }
        }
    }
    //将最后一张图片设置为空白（黑色方块）
    private void setBlack(int rows,int cols){
        int no=rows*cols-1;
        int w=mData.get(no).getiBm().getWidth();
        int h=mData.get(no).getiBm().getHeight();
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.black);
        bm = zoomBitmap(bm,w,h);
        bitmap = zoomBitmap(mData.get(no).getiBm(),w,h);
        mData.get(no).setiBm(bm);
        blbl = mData.get(no).getIno();
    }

//    游戏过程相关
    //按照所选难度开始一盘新游戏，初始化全局变量，然后调用一遍初始化相关的全部函数
    private void newgame(int rows,int cols){
        state=0;//初始状态
        blbl=404;
        mData.clear();
        if(r!=null) {
            for (int i = 0; i < r.length; i++) {
                r[i] = 0;
            }
        }
        reamainTime=MaxTime;
        CountDowm();
        TableLayout t1 = (TableLayout)findViewById(R.id.hcg_tb);
        t1.removeAllViewsInLayout();
        chooseLevel(rows, cols, i);
        setBlack(rows,cols);
        initPic(rows, cols);
        while(!cansolve(rows)) {
            newgame(rows,cols);
        }
        state = 1;//游戏中
    }

//    游戏过程相关的操作
    //点击图片后执行此函数，按顺序调用moveable函数、exchange函数，isSuccess函数
    public void click(View v) {
        Fst = (ImageView) v;
        Sec = (ImageView)findViewById(R.id.nblock);
        if (moveable()) {
            exchange();
        } else {
            Fst = null;
            Toast.makeText(HCGPlay.this, R.string.cant_move, Toast.LENGTH_SHORT).show();
        }
    }
    //判断图片能否移动，若能移动，则通过exchange移动图片到空白位置
    private boolean moveable(){
            String firstTag = (String) Fst.getTag();
            String secondTag = (String) Sec.getTag();
            //得到在list中索引位置
            String[] fIndex = firstTag.split("_");
            String[] sIndex = secondTag.split("_");
            int i1, j1, i2, j2, b1, b2;
            b1 = Integer.parseInt(fIndex[1]);
            i1 = Integer.parseInt(fIndex[2]);
            j1 = Integer.parseInt(fIndex[3]);
            b2 = Integer.parseInt(sIndex[1]);
            i2 = Integer.parseInt(sIndex[2]);
            j2 = Integer.parseInt(sIndex[3]);

            if (b1 == blbl || b2 == blbl) {
                if ((i1 == i2) && (Math.abs(j1 - j2) == 1)) {
                    return true;
                } else if ((j1 == j2) && (Math.abs(i1 - i2) == 1)) {
                    return true;
                } else return false;
            } else return false;
        }
    //交换两个图片
    private void exchange(){
        String firstTag = (String) Fst.getTag();
        String secondTag = (String) Sec.getTag();
        //得到在list中索引位置
        String[] firstImageIndex = firstTag.split("_");
        String[] secondImageIndex = secondTag.split("_");
        Fst.setImageBitmap(mData.get(Integer
                .parseInt(secondImageIndex[1])).getiBm());
        Sec.setImageBitmap(mData.get(Integer
                .parseInt(firstImageIndex[1])).getiBm());
        Fst.setTag(secondImageIndex[0]+"_"+secondImageIndex[1]+"_"+firstImageIndex[2]+"_"+firstImageIndex[3]);
        Sec.setTag(firstImageIndex[0]+"_"+firstImageIndex[1]+"_"+secondImageIndex[2]+"_"+secondImageIndex[3]);
        Sec.setId(Fst.getId());
        Fst.setId(R.id.nblock);
        Sec = Fst;
        Fst = null;
        judge();
    }

    //判断游戏是否胜利，每次移动后都会进行检测
    private boolean isSuccess(){
        boolean isSuccess = true;
        for (int i = 0; i < picBlock.length; i++)
        {
            ImageView first = picBlock[i];
            Log.v("TAG", getIndexByTag((String) first.getTag()) + "");
            if (getIndexByTag((String) first.getTag()) != i)
            {
                isSuccess = false;
            }
        }
        return isSuccess;
    }
    //若isSuccess返回true，则进行弹框
//    将计分函数注释了 之后应该需要调用数据库
    private void judge(){
        if (isSuccess())
        {
            saveChallengeInfo();
            state=2;
            int minute,second;
            minute = (MaxTime-reamainTime)/60;
            second = (MaxTime-reamainTime)%60;
            Sec.setImageBitmap(bitmap);
            AlertDialog alt ;
            AlertDialog.Builder alb = new AlertDialog.Builder(HCGPlay.this);
            String tmMsg = getResources().getString(R.string.costTime);
            tmMsg = String.format(tmMsg,minute,second);
            alt = alb.setIcon(R.drawable.konosuba_h_01)
                    .setTitle(R.string.congra_title)
                    .setMessage(getString(R.string.congratulation)+tmMsg+getString(R.string.coder))
                    .setPositiveButton(R.string.replay, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
//                            record(1);
                            Toast.makeText(HCGPlay.this,R.string.chooseDiffcult,Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNeutralButton(R.string.goToHS, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            saveChallengeInfo();
                            Toast.makeText(HCGPlay.this,"save",Toast.LENGTH_SHORT).show();
//                            record(0);
                        }
                    })
                    .setNegativeButton(R.string.other_pic, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
//                            record(1);
                            finish();
                        }
                    })
                    .create();
            alt.show();
        }
    }

//    倒计时2.0
    public void CountDowm(){
        if(IsFirst!=0){
//            Toast.makeText(HCGPlay.this,"???",Toast.LENGTH_SHORT).show();
        }
        else {
            IsFirst=1;
        Message message=handler.obtainMessage(1);
        handler.sendMessageDelayed(message,0);
        }
    }
    final Handler handler=new Handler(){
        public  void handleMessage(Message mes){
            switch (mes.what){
                case 1:
                    reamainTime--;
                    textViewGameTime.setText("剩余时间："+reamainTime+"秒");
                    if(reamainTime>0){
                        Message message=handler.obtainMessage(1);
                        handler.sendMessageDelayed(message,1000);
                    }
                    else {
                        //时间没了就消失不见了？
                        textViewGameTime.setVisibility(View.GONE);
                        Toast.makeText(HCGPlay.this,"时间耗尽",Toast.LENGTH_SHORT).show();
                    }
            }
        }
    };

//时间耗尽监听函数
    public  void TimeOut(){
        textViewGameTime.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(reamainTime==0){
//                    Toast.makeText(HCGPlay.this,"时间耗尽",Toast.LENGTH_SHORT).show();
                    //时间耗尽 游戏结束 停止计时 弹窗回到上一页
//                    judge();
                    if(!isSuccess()){
                        AlertDialog alt ;
                        AlertDialog.Builder alb = new AlertDialog.Builder(HCGPlay.this);
                        alt = alb.setIcon(R.drawable.konosuba_h_01)
                                .setTitle("sorry")
                                .setMessage("未完成,点击按钮返回上一界面")
                                .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent=new Intent(HCGPlay.this,HCGView.class);
                                        startActivity(intent);
                                    }
                                })

                                .create();
                        alt.show();
                    }
                }
            }
        });
    }

}
