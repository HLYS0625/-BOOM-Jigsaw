package com.cugb.xiaob.mozaiku;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Random;
import android.os.Handler;
import java.util.Stack;

public class detailedPage extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG ="detailPage" ;
    //黑色方块
    int blbl =404;
    //从页面一传入i的值，用以确定调取哪张图片
    private int i;
    //状态值：false = 非游戏中，true = 游戏中
    private boolean state=false;
    //游戏难度类型
    private int type=3;
    //图片数组，用来保存分割后的小拼图
    private ArrayList<Block> mData = new ArrayList<>();
    //随机数组，用以打乱拼图的顺序
    int[] r;
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
    //剩余作弊次数
    int CheatCount=0;
    //已经作弊次数
    int hasCheated = 0;
    //调用相册相关，相机拍照有空补充
    private static final int SELECT_PHOTO=0;//调用相册照片
    private static final int CROP_PHOTO=2;//裁剪照片
    private static final int REQUEST_CODE_REQUEST_PERMISSION = 0;//请求读写权限，用于传递裁剪的照片
    //通过Uri方式存放剪裁后的图片，避免部分手机由于性能不够无法得到返回的data
    private Uri uritempFile;
    //自动拼图待交换碎片
    private int position;
    //装存自动拼图步骤的栈
    Stack<openListEle> stack;
    //自动拼图矩阵所需数组
    int[][] pt;
    int[][] correct;
    int[][] ptnext;
    int[] switchnum=new int[]{-1,-1};
    //自动拼图的线程
    Thread thread;
    //打乱中已经走过的状态
    HashSet<String> steps=new HashSet();
    //打乱步骤，即拼图步骤倒序
    int positions[];

    //____________________________以上为变量部分，以下为函数部分______________________________________

    /**
     * UI更新Handler
     */
    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    // 更新计时器
                    break;
                case 2:
                    if(position>-1){
                        // 交换点击Item与空格的位置
                        picBlock[position].performClick();
                    }
                default:
                    break;
            }
        }
    };


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
        ImageView rei_pic = (ImageView)findViewById(R.id.rei);
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
                if(state) {
                    autoJigsaw();
                }
                break;
            case R.id.easy:
                hint(state,3,3);
                type=3;
                break;
            case R.id.noomaru:
                hint(state,4,4);
                type=4;
                break;
            case R.id.hard:
                hint(state,5,5);
                type=5;
                break;
            case R.id.music:
                music();
                break;
            case R.id.rei:
                //作弊次数增加
                CheatCount++;
                break;
            default:
                click(v);
                break;
        }
    }

    private void autoJigsaw() {
//        if(type*type>10){
//            ;
//        }
//        else{
//            pt=new int[type][type];
//            correct=new int[type][type];
//            ptnext=new int[type][type];
//            //初始化当前位置矩阵和目标位置
//            for(int i=0;i<type;i++){
//                for(int j=0;j<type;j++){
//                    pt[i][j]=getBlock(picBlock[i*type+j]).getIno();
//                    correct[i][j]=i*type+j;
//                }
//            }
//            stack= puzzleAstar(pt,correct,type,type);//取栈
//            //弹出值为空的状态
//            stack.pop();
//            //弹出当前状态
//            stack.pop();
//        }
        //判断是否已完成
        judge();
        // 启动线程
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
//                if(type>3){
                   for(int i=type*type*2;i>0;i--){
                       position=positions[i-1];
                       Message msg = new Message();
                       msg.what = 2;
                       handler.sendMessage(msg);
                       Log.i(TAG, "thread start run");//test
                       try {
                           thread.sleep(200);
                       } catch (InterruptedException e) {
                           e.printStackTrace();
                       }
                   }
//                }
//                else {
//                    while(stack.size()!=0) {
//                        switchstep();
//                        Message msg = new Message();
//                        msg.what = 2;
//                        handler.sendMessage(msg);
//                        Log.i(TAG, "thread start run");//test
//                        try {
//                            thread.sleep(200);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }

            }
        });
        thread.start();

    }

    private Block getBlock(ImageView imageView) {
        String thisTag=(String) imageView.getTag();
        String[] thisImageIndex=thisTag.split("_");
        return mData.get(Integer.parseInt(thisImageIndex[1]));
    }

    private void switchstep() {
            openListEle q=stack.peek();
            long data;
            stack.pop();
            data=q.pts.state;
            Log.i(TAG,"data is"+data);//test
            for (int i = type - 1; i >= 0; --i) {
                for (int j = type - 1; j >= 0; --j) {
                    ptnext[i][j] = (int)(data % 10);
                    data = data / 10;
                    if(pt[i][j]!=ptnext[i][j]){
                        if(ptnext[i][j]==8)
                            position=i*type+j;
                    }
                }
            }
            //赋值给新的当前矩阵
            for(int i=0;i<type;i++){
                for(int j=0;j<type;j++){
                    pt[i][j]=ptnext[i][j];
                }
            }
    }

    //自动拼图相关
    private Stack<openListEle> puzzleAstar(int[][] pt, int[][] correct, int vol, int col) {
        visited.clear();
        //判断是否是正确状态
        long correctStateInt = makeInt(correct,vol,col);
        if(correctStateInt == makeInt(pt,vol,col)){

            return null;
        }
        //初始化头节点
        openListEle openList = new openListEle();
        openList.next = null;
        openList.pre = null;
        //将初始状态加入openlist
        openListEle startEle = new openListEle();
        startEle.pts=new ptstate(0,0,makeInt(pt,vol,col));//?
        openList.addNext(startEle);//?

        openListEle finalEle = null;//?
        while(true) {
            if(finalEle!= null)
                break;
            //找到openlist中代价最小的
            openListEle minEleP = openList.next;
            openListEle minEle = null;
            int minCost = -1;
            //int minElePCount = 0;
            while (minEleP != null) {
                if (minCost == -1 || minEleP.pts.guessCost < minCost) {
                    minCost = minEleP.pts.guessCost;
                    minEle = minEleP;
                }
                //minElePCount++;
                minEleP = minEleP.next;
            }
            if(minEle == null || minEle.pre == null)
                break;
            //计算以当前状态的可变状态的guessCost
            int[][] tmpPt = new int[vol][col];
            int data = (int) minEle.pts.state;
            for (int i = vol - 1; i >= 0; --i) {
                for (int j = col - 1; j >= 0; --j) {
                    tmpPt[i][j] = data % 10;
                    data = data / 10;
                }
            }
            //找空白块的位置
            int zi = 0, zj = 0;
            for (int i = 0; i < vol; ++i) {
                for (int j = 0; j < col; ++j)
                    if (tmpPt[i][j] == vol*col-1) {
                        zi = i;
                        zj = j;
                        break;
                    }
            }
            int[] offseti = new int[]{-1, 0, 1, 0};
            int[] offsetj = new int[]{0, 1, 0, -1};
            for (int i = 0; i < 4; ++i) {
                int ti = zi + offseti[i];
                int tj = zj + offsetj[i];
                if (ti < 0 || ti > vol - 1 || tj < 0 || tj > col - 1)
                    continue;
                int t = tmpPt[ti][tj];
                tmpPt[ti][tj] = tmpPt[zi][zj];
                tmpPt[zi][zj] = t;
                long hashcode = makeInt(tmpPt,vol,col);
                if (!visited.contains(hashcode)) {
                    visited.add(hashcode);
                    openListEle newEle = new openListEle();
                    int tmpguessCost = getGuessCostBetweenTwoState(tmpPt, correct,vol,col);
                    newEle.pts = new ptstate(minEle.pts.currentCost + 1, minEle.pts.currentCost + 1 + tmpguessCost, hashcode);
                    minEle.addNext(newEle);
                    if (hashcode == correctStateInt)
                        finalEle = newEle;
                }
                t = tmpPt[ti][tj];
                tmpPt[ti][tj] = tmpPt[zi][zj];
                tmpPt[zi][zj] = t;
            }
            //从openlist中删除
            deleteOpenListEle(openList,minEle);
        }

        //保存路径
        Stack<openListEle> rightpath=new Stack<openListEle>();
        openListEle pathEle = finalEle;
        while(pathEle != null) {
            rightpath.push(pathEle);
            pathEle = pathEle.pre;
        }
        return rightpath;
    }

    private int getGuessCostBetweenTwoState(int[][] a, int[][] b, int vol, int col) {
        int guessCost = 0;
        for(int ai=0;ai<vol;++ai)
            for(int aj=0;aj<col;++aj){
                boolean found = false ;
                for(int bi=0;bi<vol && !found;++bi)
                    for(int bj=0;bj<col && !found;++bj)
                        if(a[ai][aj] == b[bi][bj]){
                            guessCost += Math.sqrt(1.0*(ai-bi)*(ai-bi)+(aj-bj)*(aj-bj));
                            found = true;
                        }
            }
        return guessCost;
    }

    HashSet<Long> visited=new HashSet();
    private long makeInt(int[][] correct, int vol, int col) {
        long sum = 0;
        for(int i=0;i<vol;++i)
            for(int j=0;j<col;++j){
                sum = 10*sum+correct[i][j];
            }
        return sum;
    }
    //从链表中删除指定节点
    private void deleteOpenListEle(openListEle openList, openListEle minEle) {
        int n=0;
        openListEle p=openList;
        //计算列表长度
        while(p!=null){
            n++;p=p.next;
        }
        //找到待删除结点
        p=openList;
        openListEle q=null;
        for(int i=0;i<n;i++){
            if(p.equals(minEle))
                break;
            else{
                q=p;
                p=p.next;
            }
        }
        if(q==null){
//            openList=openList.next;
        }
        else{
            q.next=p.next;
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
    //从相册调取图片并返回给picList，并申请相关权限
    private void pickImageFromAlbum(){
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            //如果游戏程序拥有读取权限，直接开启相册，进行剪裁，然后进行游戏
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, SELECT_PHOTO);
        }else {
            //游戏程序没有读取权限，请求权限
            boolean shouldShow = ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if(shouldShow) {
                //请求权限
                ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_REQUEST_PERMISSION);
            }
            //权限申请被用户永久关闭，弹出提示，并关闭该页面,开启本程序的设定页面，引导用户开启权限
            else {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getApplicationContext().getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
                Toast.makeText(this,R.string.permission_no_notice,Toast.LENGTH_SHORT).show();
                finish();
            }

        }
    }
   //对申请权限的结果进行响应
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_REQUEST_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //权限申请成功，开启相册
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, SELECT_PHOTO);
            } else {
                //权限申请被拒绝，提示用户，并关闭该页面
                Toast.makeText(this, R.string.permission, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }


//从相册调取图片相关
    //接收从相册返回的图片
    public void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode, resultCode,data);
        if(resultCode==RESULT_OK) {
            if (requestCode == SELECT_PHOTO) {
                //获取图片原始地址
                Uri imguri = data.getData();
                try {
                    startImageZoom(imguri);
                } catch (Exception e) {
                    Log.e("Error","Select Photo from Album Wrong");
                }
            } else if (requestCode == CROP_PHOTO) {
                try {
                    Uri uri =uritempFile;
                    File f = new File(uri.getPath());
                    if(f.exists()) {
                        originBm = BitmapFactory.decodeFile(f.getPath());
                        setrei();
                    }else Toast.makeText(this,"Wrong",Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.e("Error","Crop Photo Wrong");
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
        intent.putExtra("return-data", false);
        //实例化UriTemp，将剪裁后的图片保存到这里
        uritempFile = Uri.parse("file://" + "/" + Environment.getExternalStorageDirectory().getPath() + "/temp/" + "small.jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uritempFile);
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


//游戏初始化相关的
    //生成1-9/16/25的随机数列（作为拼图随机顺序的依据）
    private void Rdm(int rows,int cols) {
        int a;
        r = new int[rows * cols];
//        if(rows*cols>10){
            positions=new int[rows*cols*2];
            int zi = rows-1, zj = cols-1;//空白块的位置
            int[][] tmp=new int[rows][cols];
            String code="";
            steps.clear();
            for(int m=0;m<rows;m++){
                for(int n=0;n<cols;n++){
                    code=code+(m*cols+n)+",";//去掉new String()
                }
            }
            //添加正确拼图的状态入栈
            steps.add(code);
            for(int i=0;i<rows;i++){
                for(int j=0;j<cols;j++){
                    tmp[i][j]=i*cols+j;
                }
            }
            int[] offseti = new int[]{-1, 0, 1, 0};//左下右上
            int[] offsetj = new int[]{0, 1, 0, -1};
            for(int s=0;s<rows*cols*2;s++){
                int ti,tj,t;
                String hashcode="";
                //获得一个随机数
                do{
                    a=new Random().nextInt(4);
                    ti = zi + offseti[a];
                    tj = zj + offsetj[a];
                    if (ti < 0 || ti > rows - 1 || tj < 0 || tj > cols - 1)
                        continue;
                    t = tmp[ti][tj];
                    tmp[ti][tj] = tmp[zi][zj];
                    tmp[zi][zj] = t;
                    hashcode="";
                    for(int m=0;m<rows;m++){
                        for(int n=0;n<cols;n++){
                            hashcode=hashcode+tmp[m][n]+",";//去掉new String()
                        }
                    }
                    t = tmp[ti][tj];
                    tmp[ti][tj] = tmp[zi][zj];
                    tmp[zi][zj] = t;
                   } while (!(ti >= 0 && ti < rows && tj >= 0 && tj < cols)||steps.contains(hashcode));
                   steps.add(hashcode);
                   positions[s]=zi*cols+zj;
                   //交换了空白块的位置
                   t = tmp[ti][tj];
                   tmp[ti][tj] = tmp[zi][zj];
                   tmp[zi][zj] = t;
                   zi=ti;//空白块行数
                   zj=tj;//空白块列数


            }
           for(int i=0;i<rows;i++){
               for(int j=0;j<cols;j++){
                   r[i*cols+j]=tmp[i][j];
               }
           }
//        }
//        else{
//            for (int i = 0; i < rows * cols - 1; i++) {
//                Random random = new Random();
//                a = random.nextInt(rows * cols);
//                if (NExist(r, a)) {
//                    r[i] = a;
//                } else i -= 1;
//            }
//            r[rows * cols - 1] = 0;
//        }


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
                mData.add(new Block(b,no));
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
    private void hint(boolean s, final int rows, final int cols){
        if(s){
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
        }else {
            newgame(rows,cols);
        }
    }
    //按照所选难度开始一盘新游戏，初始化全局变量，然后调用一遍初始化相关的全部函数
    private void newgame(int rows,int cols){
        state=false;
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
        state = true;
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
        if (hasCheated > 0) {
            check();
        }
    }
    //判断图片能否移动，若能移动，则通过exchange移动图片到空白位置
    private boolean moveable() {
        //增加是否在游戏中的检测
        if (state) {
            //增加有没有作弊次数的判断
            if (CheatCount > 0) {
                CheatCount--;
                hasCheated++;
                return true;
            } else {
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
                    if (((i1 == i2) && (Math.abs(j1 - j2) == 1)) || ((j1 == j2) && (Math.abs(i1 - i2) == 1))) {
                        return true;
                    }
                }
            }
        }return false;
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
    //如果调用过作弊方法，移动后检测游戏按照正常方式游玩是否有解，不可解的话弹出提示
    private void check() {
        int width = (int) Math.sqrt(picBlock.length);
        if (!cansolve(width)) {
            Toast.makeText(detailedPage.this, R.string.cant_solve, Toast.LENGTH_SHORT).show();
        }
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
            state=false;
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
                            record(1);
                            //更新游戏状态
                            state=false;
                            Toast.makeText(detailedPage.this,R.string.chooseDiffcult,Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNeutralButton(R.string.goToHS, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            record(0);
                        }
                    })
                    .setNegativeButton(R.string.other_pic, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            record(1);
                            finish();
                        }
                    })
                    .create();
            alt.setCancelable(false);
            alt.setCanceledOnTouchOutside(false);
            alt.show();
        }
    }
    //将分数信息传入高分榜，只有选择前往高分榜页面，noStay值方设为0，表示停留在高分榜页面。
    private void record(int noStay){
        int difficult = (int)Math.sqrt(picBlock.length)-3;
        Intent it = new Intent(detailedPage.this,highScore.class);
        it.putExtra("username",username);
        it.putExtra("costTime",costTime);
        it.putExtra("difficult",difficult);
        it.putExtra("cheat",hasCheated);
        it.putExtra("noStay",noStay);
        startActivity(it);
        if(noStay==0){
            finish();
        }
    }
}




