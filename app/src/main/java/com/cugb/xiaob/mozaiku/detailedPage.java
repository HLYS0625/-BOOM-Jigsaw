package com.cugb.xiaob.mozaiku;

import android.app.Activity;
import android.app.Notification;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.media.Image;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Looper;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Random;
import android.os.Handler;

import java.util.concurrent.ThreadFactory;
import java.util.logging.LogRecord;

public class detailedPage extends AppCompatActivity implements View.OnClickListener {
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
//      示例图片View
    private ImageView rei_pic;
    //fst为玩家点击的图片，sec始终为黑色图片（空白区块）
    private ImageView Fst;
    private ImageView Sec;
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
            R.drawable.bijyutubu_11,
            R.drawable.original_17
    };
    //存放所有的零碎图片
    private ImageView[] picBlock;
    //被黑色替代的图片以及从相册中传入的原图
    private Bitmap bitmap,originBm;
    //背景音乐
    MediaPlayer player = null;
    //游戏开始时间
    int costTime;
    //登录用户名
    String username;
    //调用相册相关，相机拍照有空补充

//    作弊次数
    int CheatCount=0;
    private static final int SELECT_PHOTO=0;//调用相册照片
    private static final int TAKE_PHOTO=1;//调用相机拍照
    private static final int CROP_PHOTO=2;//裁剪照片



    //____________________________以上为变量部分，以下为函数部分______________________________________




    @Override
    //页面初始化
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.detailed_page);
        setPic();
        player = MediaPlayer.create(this,R.raw.hatukoi);
        player.setLooping(true);
        Button besy = (Button) findViewById(R.id.easy);
        Button bnml = (Button) findViewById(R.id.noomaru);
        Button bhrd = (Button) findViewById(R.id.hard);
        Button bbck = (Button) findViewById(R.id.help);
        Button bmsc = (Button) findViewById(R.id.music);
        rei_pic = (ImageView)findViewById(R.id.rei);
        bbck.setOnClickListener(this);
        bhrd.setOnClickListener(this);
        bnml.setOnClickListener(this);
        besy.setOnClickListener(this);
        bmsc.setOnClickListener(this);
        rei_pic.setOnClickListener(this);
    }
    @Override
    //按钮监测器
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.help:
                final String[] debug = new String[]{
                        "显示R数组",
                        "显示ArrayList内容",
                        "显示游戏状态值state",
                        "显示图片碎片组picBlock[]的Tag",
                        "显示图片所对应的二维数组",
                        "尝试A*算法自动完成",
                        "伪造胜利，直接进入排行榜",
                        "全部显示"
                };
                AlertDialog alert = null;
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(detailedPage.this);
                alert = alertBuilder.setIcon(R.drawable.konosuba_h_01)
                        .setTitle("选择Log输出的内容")
                        .setSingleChoiceItems(debug, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                debug(which);
                            }
                        }).create();
                alert.show();
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
            case R.id.music:
                music();
                break;
            case R.id.rei:
//                作弊次数增加
                CheatCount++;
                break;
            default:
                click(v);
                break;
        }
    }
    //debug用，输出一些信息，完成后删除
    private void debug(int x){
        switch (x){
            case 0:
                for(int i =0;i<r.length;i++){
                    Log.d("De","r"+i+":"+r[i]);
                }
                break;
            case 1:
                for(int i=0;i<mData.size();i++) {
                    Log.d("Help/", "Arraylist<Block>" + i + ":" + mData.get(i).getiBm());
                    Log.d("Help/", "Arraylist<Block>" + i + ":" + mData.get(i).getIno());
                }
                break;
            case 2:
                Log.d("Help/","state:"+state);
                break;
            case 3:
                for(int i=0;i<picBlock.length;i++){
                    Log.d("Help/","picBlock[]" + i + ":" + picBlock[i].getTag());
                }
                break;
            case 4:
                int width = (int)Math.sqrt(r.length);
                int[][] L = getNumber(width);
                for(int i=0;i<width;i++){
                    for(int j=0;j<width;j++){
                        Log.d("Help/","L["+i+"]["+j+"]:"+L[i][j]);
                    }
                }
                break;
            case 5:
                Toast.makeText(detailedPage.this,R.string.unComplete,Toast.LENGTH_SHORT).show();
                break;
            case 6:
                Intent it = new Intent(detailedPage.this,highScore.class);
                it.putExtra("username",username);
                it.putExtra("costTime",50);
                it.putExtra("difficult",0);
                startActivity(it);
                finish();
                break;
            default:
                debug(0);
                debug(1);
                debug(2);
                debug(3);
                debug(4);
        }
    }


//页面初始化相关
    //根据主菜单传入的数值，决定使用的图片。同时接收主菜单传来的用户名
    private void setPic() {
        Intent it = getIntent();
        i = it.getIntExtra("msg", 404);
        username = it.getStringExtra("username");
        if (i == 404) {
            Toast.makeText(detailedPage.this, R.string.getPic_wrong,
                    Toast.LENGTH_SHORT).show();
            it.setClass(detailedPage.this, MainActivity.class);
            startActivity(it);
        } else if(i==15){
            pickImageFromAlbum();
        } else {
            ImageView rei_pic = (ImageView) findViewById(R.id.rei);
            rei_pic.setImageResource(pic_list[i]);
        }
    }
    //从相册调取图片并返回给picList
    private void pickImageFromAlbum(){
        Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent,SELECT_PHOTO);
    }


//从相册调取图片相关
    //接收从相册返回的图片
    public void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode, resultCode,data);
        if(resultCode==RESULT_OK) {
            if (requestCode == SELECT_PHOTO) {
                ContentResolver resolver = getContentResolver();
                //获取图片原始地址
                Uri imguri = data.getData();
                try {
                    startImageZoom(imguri);
                } catch (Exception e) {
                    // TODO: handle exception
                }
            } else if (requestCode == CROP_PHOTO) {
                try {
                    originBm = data.getParcelableExtra("data");
                    setrei();
                } catch (Exception e) {
                    // TODO: handle exception
                }
            }
        }else {
            Toast.makeText(detailedPage.this,R.string.weixuantupian,Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    //从相册调取图片时打开系统的裁剪功能
    public void startImageZoom(Uri uri) {
        int aspectX=350;
        int aspectY=450;
        int outputX=700;
        int outputY=900;
        Intent intent = new Intent("com.android.camera.action.CROP");//调用Android系统自带的一个图片剪裁页面
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");//进行修剪
        intent.putExtra("aspectX", aspectX);
        intent.putExtra("aspectY", aspectY);
        intent.putExtra("outputX",outputX);
        intent.putExtra("outputY",outputY);
        intent.putExtra("return-data", true);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        startActivityForResult(intent, CROP_PHOTO);
    }


//工具函数
    //计时函数
    private int getTime(){
        int Hour,minute,second;
        Calendar c = Calendar.getInstance();
        Hour = c.get(Calendar.HOUR_OF_DAY);
        minute = c.get(Calendar.MINUTE);
        second = c.get(Calendar.SECOND);
        return Hour*3600+minute*60+second;
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
    //播放或暂停音乐
    private void music(){
        if(!player.isPlaying()){
            player.start();
            Toast.makeText(detailedPage.this,getString(R.string.music)+getString(R.string.on),Toast.LENGTH_SHORT).show();
        }else {
            player.pause();
            Toast.makeText(detailedPage.this,getString(R.string.music)+getString(R.string.off),Toast.LENGTH_SHORT).show();
        }
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
                if (r[k] == blbl) {
                    state[i][j] = 0;
                } else state[i][j] = r[k] + 1;
            }
        return state;
    }
    //返回图片正确的序号，供judge()函数和cansolve函数使用
    private int getIndexByTag(String tag){
        String[] split = tag.split("_");
        return Integer.parseInt(split[1]);
    }


//游戏初始化相关的
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
        if(i!=15) {
            bm = BitmapFactory.decodeResource(getResources(), pic_list[x]);
        }else {
            bm = originBm;
        }
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
    //设置示例图，我也不知道为什么写在setPic()里面会抢在调用相册图片前执行
    private void setrei(){
        if(i==15){
            ImageView rei_pic = (ImageView)findViewById(R.id.rei);
            rei_pic.setImageBitmap(originBm);
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



//游戏过程相关
    //按照state的状态决定选择难度后执行的操作
    //state=0，开始一盘新游戏；state=1，选择重新开始游戏或继续游戏；state=3，返回主选单或重新开始游戏。
    private void hint(int s, final int rows, final int cols){
        if(s==1){
            AlertDialog alt ;
            AlertDialog.Builder alb = new AlertDialog.Builder(detailedPage.this);
            alt = alb.setIcon(R.drawable.konosuba_h_01)
                    .setTitle(R.string.hint)
                    .setMessage(getString(R.string.chosedDiffcult)+getString(R.string.coder))
                    .setPositiveButton(R.string.newGame, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            newgame(rows,cols);
                        }
                    })
                    .setNegativeButton(R.string.conti, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(detailedPage.this,R.string.nuli,Toast.LENGTH_SHORT).show();
                        }
                    })
                    .create();
            alt.show();
        }else if(s==2){
            AlertDialog alt ;
            AlertDialog.Builder alb = new AlertDialog.Builder(detailedPage.this);
            alt = alb.setIcon(R.drawable.konosuba_h_01)
                    .setTitle(R.string.congra_title)
                    .setMessage(getString(R.string.congratulation)+getString(R.string.coder))
                    .setPositiveButton(R.string.replay, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            newgame(rows,cols);
                        }
                    })
                    .setNegativeButton(R.string.other_pic, new DialogInterface.OnClickListener() {
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
            newgame(rows,cols);
        }
    }
    //按照所选难度开始一盘新游戏，初始化全局变量，然后调用一遍初始化相关的全部函数
    private void newgame(int rows,int cols){
        state=0;
        blbl=404;
        costTime=0;
        mData.clear();
        if(r!=null) {
            for (int i = 0; i < r.length; i++) {
                r[i] = 0;
            }
        }
        TableLayout t1 = (TableLayout)findViewById(R.id.tbl);
        t1.removeAllViewsInLayout();
        chooseLevel(rows, cols, i);
        setBlack(rows,cols);
        initPic(rows, cols);
        while(!cansolve(rows)) {
            newgame(rows,cols);
        }
        state = 1;
        costTime = getTime();
    }


//游戏过程相关的操作
    //点击图片后执行此函数，按顺序调用moveable函数、exchange函数，isSuccess函数
    public void click(View v) {
        Fst = (ImageView) v;
        Sec = (ImageView)findViewById(R.id.nblock);
        if (moveable()) {
            exchange();
        } else {
            Fst = null;
            Toast.makeText(detailedPage.this, R.string.cant_move, Toast.LENGTH_SHORT).show();
        }
    }
    //判断图片能否移动，若能移动，则通过exchange移动图片到空白位置
    private boolean moveable(){

//        增加有没有作弊次数的判断
        if(CheatCount>0){
            CheatCount--;
            return true;
        }else {
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
    private void judge(){
        if (isSuccess())
        {
            state=2;
            costTime = getTime()-costTime;
            int minute,second;
            minute = costTime/60;
            second = costTime%60;
            Sec.setImageBitmap(bitmap);
            AlertDialog alt ;
            AlertDialog.Builder alb = new AlertDialog.Builder(detailedPage.this);
            String tmMsg = getResources().getString(R.string.costTime);
            tmMsg = String.format(tmMsg,minute,second);
            alt = alb.setIcon(R.drawable.konosuba_h_01)
                    .setTitle(R.string.congra_title)
                    .setMessage(getString(R.string.congratulation)+tmMsg+getString(R.string.coder))
                    .setPositiveButton(R.string.replay, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(detailedPage.this,R.string.chooseDiffcult,Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNeutralButton(R.string.goToHS, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            int difficult = (int)Math.sqrt(picBlock.length)-3;
                            Intent it = new Intent(detailedPage.this,highScore.class);
                            it.putExtra("username",username);
                            it.putExtra("costTime",costTime);
                            it.putExtra("difficult",difficult);
                            it.putExtra("cheat",CheatCount);
                            startActivity(it);
                            finish();
                        }
                    })
                    .setNegativeButton(R.string.other_pic, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .create();
            alt.show();
        }
    }
}




