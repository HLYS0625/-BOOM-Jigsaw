package com.cugb.xiaob.mozaiku;

import android.graphics.Bitmap;

/**
 * Created by Riko on 2017/7/4.
 */

public class Block {
    private Bitmap iBm;
    private int ino;

    public Block() {
    }

    public Block(Bitmap iId, int ino) {
        this.iBm = iId;
        this.ino = ino;
    }

    public Bitmap getiBm() {
        return iBm;
    }

    public int getIno() {
        return ino;
    }

    public void setiBm(Bitmap iBm) {
        this.iBm = iBm;
    }

    public void setino(int inum) {
        this.ino = inum;
    }
}