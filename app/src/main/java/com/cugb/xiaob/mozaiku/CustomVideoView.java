package com.cugb.xiaob.mozaiku;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.VideoView;

/**
 * Created by HLYS on 2017/7/11.
 */

public class CustomVideoView extends VideoView
{
    public CustomVideoView(Context context){
        super(context);
    }
    public CustomVideoView(Context context, AttributeSet attrs){
        super(context,attrs);
    }
    public CustomVideoView(Context context,AttributeSet attrs,int deffStyleAttr){
        super(context,attrs,deffStyleAttr);
    }
    protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec){
        //重新计算高度
        int width=getDefaultSize(0,widthMeasureSpec);
        int height=getDefaultSize(0,heightMeasureSpec);
        setMeasuredDimension(width,height);

    }
    public void setOnPreparedListener(MediaPlayer.OnPreparedListener l){
        super.setOnPreparedListener(l);
    }
    public boolean onKeyDown(int keyCode, KeyEvent event){
        return super.onKeyDown(keyCode,event);
    }

}
