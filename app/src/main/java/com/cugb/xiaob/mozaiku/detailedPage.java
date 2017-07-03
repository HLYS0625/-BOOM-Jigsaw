package com.cugb.xiaob.mozaiku;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;

public class detailedPage extends AppCompatActivity implements View.OnClickListener{

    private int[] pic_list = {R.drawable.akunohana_08,R.drawable.bijyutubu_11,R.drawable.bleach_h_23, R.drawable.demichan_08,
            R.drawable.gaburiiru_vina_13,R.drawable.hibike_nakayoshi_17,R.drawable.typemoon_shiki_15,R.drawable.touhou_hakugyokurou_youmu_h_34};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detailed_page);
        setPic();
        Button besy = (Button)findViewById(R.id.easy);
        Button bnml = (Button)findViewById(R.id.noomaru);
        Button bhrd = (Button)findViewById(R.id.hard);
        Button bbck = (Button)findViewById(R.id.back);
        bbck.setOnClickListener(this);
        bhrd.setOnClickListener(this);
        bnml.setOnClickListener(this);
        besy.setOnClickListener(this);
    }

    private void setPic(){
        Intent it = getIntent();
        int i = it.getIntExtra("msg",404);
        if(i==404) {
            Toast.makeText(detailedPage.this, "Something Wrong,Can't finding the picture",
                    Toast.LENGTH_SHORT).show();
            it.setClass(detailedPage.this, MainActivity.class);
            startActivity(it);
        }
        else {
            ImageView rei_pic = (ImageView)findViewById(R.id.rei);
            ImageView main_pic = (ImageView)findViewById(R.id.main_pic);
            rei_pic.setImageResource(pic_list[i]);
            main_pic.setImageResource(pic_list[i]);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.back:
                Intent i = new Intent(detailedPage.this,MainActivity.class);
                startActivity(i);
                finish();
                break;
            case R.id.easy:
                Toast.makeText(detailedPage.this,"敬请期待QwQ",Toast.LENGTH_SHORT).show();
                break;
            case R.id.noomaru:
                Toast.makeText(detailedPage.this,"敬请期待QwQ",Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(detailedPage.this,"敬请期待QwQ",Toast.LENGTH_SHORT).show();
                break;
        }
    }
}