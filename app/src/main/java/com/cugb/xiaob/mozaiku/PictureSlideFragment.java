package com.cugb.xiaob.mozaiku;

/**
 * Created by xiaob on 2017/7/15.
 */

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class PictureSlideFragment extends Fragment {
    private int draw;
    private ImageView imageView;

    public static PictureSlideFragment newInstance(int draw) {
        PictureSlideFragment f = new PictureSlideFragment();
        Bundle args = new Bundle();
        args.putInt("position", draw);
        f.setArguments(args);
        return f;//获得一个包含图片url的PictureSlideFragmen的实例
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        draw = getArguments() != null ? getArguments().getInt("position") : R.drawable.hcg_01;

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_4_vierpager,container,false);
        imageView= (ImageView) v.findViewById(R.id.iv_main_pic);
        imageView.setImageResource(draw);
        return v;
    }
}