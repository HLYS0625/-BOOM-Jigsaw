package com.cugb.xiaob.mozaiku;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * Created by 496983022 on 2017/7/11.
 */

public class HCGPlay extends Activity {
    Bitmap bitmap;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//          加载视图
        setContentView(R.layout.hcg_paly);
//        信使接受数据
        Intent intent = getIntent();
//        要用来游戏的图片的ID
        final int imageid = intent.getIntExtra("ImageId", R.drawable.hcg_01);
        Toast.makeText(this, " asd+",Toast.LENGTH_SHORT).show();
    }


}
