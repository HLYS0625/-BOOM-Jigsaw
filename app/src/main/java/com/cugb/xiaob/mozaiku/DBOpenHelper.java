package com.cugb.xiaob.mozaiku;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;

/**
 * Created by xiaob on 2017/7/12.
 */

public class DBOpenHelper extends SQLiteOpenHelper {

    //构造函数，创建一个新的数据库
    public DBOpenHelper(Context context, int version) {
        super(context, "my.db", null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE scoreInfo(recordNo INTEGER PRIMARY KEY AUTOINCREMENT,userName VARCHAR(20) NOT NULL," +
                "difficult INTEGER NOT NULL,record INTEGER NOT NULL);");
        db.execSQL("CREATE TABLE userInfo(userName VARCHAR(20) PRIMARY KEY ,password VARCHAR(20) NOT NULL,address VARCHAR(40) NOT NULL," +
                "head INTEGER DEFAULT 0,easyHS INTEGER DEFAULT 0,normalHS INTEGER DEFAULT 0,hardHS INTEGER DEFAULT 0,verification INTEGER DEFAULT 0);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        rebuild(db);
    }

    public void rebuild(SQLiteDatabase db) {
        db.execSQL("DROP TABLE scoreInfo;");
        db.execSQL("DROP TABLE userInfo");
        db.execSQL("CREATE TABLE scoreInfo(recordNo INTEGER PRIMARY KEY AUTOINCREMENT,userName VARCHAR(20) NOT NULL," +
                "difficult INTEGER NOT NULL,record INTEGER NOT NULL);");
        db.execSQL("CREATE TABLE userInfo(userName VARCHAR(20) PRIMARY KEY ,password VARCHAR(20) NOT NULL,address VARCHAR(40) NOT NULL," +
                "head INTEGER DEFAULT 0,easyHS INTEGER DEFAULT 0,normalHS INTEGER DEFAULT 0,hardHS INTEGER DEFAULT 0,verification INTEGER DEFAULT 0);");
    }

    public void cleanScore(SQLiteDatabase db) {
        db.execSQL("DROP TABLE scoreInfo;");
        db.execSQL("CREATE TABLE scoreInfo(recordNo INTEGER PRIMARY KEY AUTOINCREMENT,userName VARCHAR(20) NOT NULL," +
                "difficult INTEGER NOT NULL,record INTEGER NOT NULL);");
    }

    public void cleanUser(SQLiteDatabase db) {
        db.execSQL("DROP TABLE userInfo");
        db.execSQL("CREATE TABLE userInfo(userName VARCHAR(20) PRIMARY KEY ,password VARCHAR(20) NOT NULL," +
                "head INTEGER DEFAULT 0,easyHS INTEGER DEFAULT 0,normalHS INTEGER DEFAULT 0,hardHS INTEGER DEFAULT 0);");
    }
}
