package com.cugb.xiaob.mozaiku;

import android.graphics.Bitmap;

/**
 * Created by Riko on 2017/7/4.
 */

public class Block {
    private Bitmap iBm;
    private int ino;
    private int row;
    private int col;

    public Block() {
    }

    public Block(Bitmap iId, int ino,int row,int col) {
        this.iBm = iId;
        this.ino = ino;
        this.row = row;
        this.col = col;
    }

    public Bitmap getiBm() {
        return iBm;
    }

    public int getIno() {
        return ino;
    }

    public int getRow(){
        return row;
    }

    public int getCol(){
        return col;
    }

    public void setRow(int row){
        this.row = row;
    }

    public void setCol(int col){
        this.col=col;
    }
    public void setiBm(Bitmap iBm) {
        this.iBm = iBm;
    }

    public void setino(int inum) {
        this.ino = inum;
    }
}