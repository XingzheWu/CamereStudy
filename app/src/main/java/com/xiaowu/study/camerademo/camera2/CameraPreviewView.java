package com.xiaowu.study.camerademo.camera2;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.view.TextureView;

public class CameraPreviewView extends TextureView {
    public CameraPreviewView(Context context) {
        super(context);
    }

    public CameraPreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CameraPreviewView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CameraPreviewView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}
