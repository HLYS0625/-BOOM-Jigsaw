package com.cugb.xiaob.mozaiku;

import android.content.Context;
        import android.content.DialogInterface;
        import android.content.Intent;
        import android.support.v7.app.AlertDialog;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.support.v7.widget.AlertDialogLayout;
        import android.view.View;
        import android.view.Window;
        import android.view.WindowManager;
        import android.widget.AdapterView;
        import android.widget.BaseAdapter;
        import android.widget.Button;
        import android.widget.GridView;

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
        mData.add(new Icon(R.drawable.overwatch_04,"オーバーウォッチ"));
        mData.add(new Icon(R.drawable.girls_panzer_rsa_05,"ガールズ＆パンツァー"));
        mData.add(new Icon(R.drawable.typemoon_shiki_15,"空の境界"));
        mData.add(new Icon(R.drawable.akunohana_08,"惡の華"));
        mData.add(new Icon(R.drawable.deathnote_08,"デスノート"));
        mData.add(new Icon(R.drawable.bleach_h_23,"ブリーチ"));
        mData.add(new Icon(R.drawable.psycho_pass_03,"パスコード"));
        mData.add(new Icon(R.drawable.aido_06,"アイドルマスター"));
        mData.add(new Icon(R.drawable.hibike_nakayoshi_17,"響けユーフォニアム"));
        mData.add(new Icon(R.drawable.touhou_hakugyokurou_youmu_h_34,"東方妖々夢"));
        mData.add(new Icon(R.drawable.suzumiya_05,"ハルヒの憂鬱"));
        mData.add(new Icon(R.drawable.gaburiiru_vina_13,"ガヴリールドロップ"));
        mData.add(new Icon(R.drawable.demichan_08,"デミちゃん"));
        mData.add(new Icon(R.drawable.bijyutubu_11,"この美術部"));



        mAdapter = new MyAdapter<Icon>(mData,R.layout.gridview_layout) {
            @Override
            public void bindView(ViewHolder holder, Icon obj) {
                holder.setImageResource(R.id.img_icon,obj.getiId());
                holder.setText(R.id.txt_icon,obj.getiName());
            }
        };

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
                        .setTitle("ヘルプ")
                        .setMessage("一つのピクチャーを選びなさい。そして難易度を選択して 、ゲームを始めましょう。\n\n開発者：理子")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

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
}

