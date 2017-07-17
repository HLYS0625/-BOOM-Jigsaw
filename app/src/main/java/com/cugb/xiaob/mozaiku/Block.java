package com.cugb.xiaob.mozaiku;

import android.graphics.Bitmap;

/**
 * Created by Riko on 2017/7/4.
 * 拼图使用的碎片图片存储类
 * 其中iBm存储碎片的位图
 *     ino存储碎片从0自增长的正确序号
 * Block类的信息不随拼图中碎片的位置变化而变化
 */

class Block {
    private Bitmap iBm;
    private int ino;

    Block(Bitmap iId, int ino) {
        this.iBm = iId;
        this.ino = ino;
    }

    Bitmap getiBm() {
        return iBm;
    }

    int getIno() {
        return ino;
    }

    void setiBm(Bitmap iBm) {
        this.iBm = iBm;
    }
}