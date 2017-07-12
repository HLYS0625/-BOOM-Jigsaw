package com.cugb.xiaob.mozaiku;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by xiaob on 2017/7/12.
 */

public class DBOpenHelper extends SQLiteOpenHelper {

    //构造函数，创建一个新的数据库
    public DBOpenHelper(Context context,int version) {
        super(context, "my.db", null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE userInfo(userName VARCHAR(20) PRIMARY KEY ,password VARCHAR(20))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
