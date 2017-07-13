package com.cugb.xiaob.mozaiku;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

/**
 * Created by 496983022 on 2017/7/13.
 */

public class HcgDBOpenHelper extends SQLiteOpenHelper {
    public static final String CREATE_hcgInfo="create table hcgInfo("
            +"imagePos INTEGER PRIMARY KEY  ,"
            +"userName VARCHAR(20),"
            +"useTIme INTEGER,"
            +"challengeTime VARCHAR(50))";
    private Context mcontext;
    public HcgDBOpenHelper(Context context,String name,SQLiteDatabase.CursorFactory factory,int version){
        super(context,name,factory,version);
        mcontext=context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_hcgInfo);
//        Toast.makeText(this,"创建hcg表ok",Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
