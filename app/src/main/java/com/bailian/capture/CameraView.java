package com.bailian.capture;

import android.content.ComponentCallbacks2;
import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.bailian.capture.compatibility.CameraCompatibility;
import com.bailian.capture.compatibility.CameraInterface;


public class CameraView extends SurfaceView implements SurfaceHolder.Callback {

    private CameraInterface mCamera;

    public CameraView(Context context) {
        super(context);
        init();
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.setKeepScreenOn(true);
        setFocusable(true);
        setBackgroundColor(ComponentCallbacks2.TRIM_MEMORY_BACKGROUND);
        surfaceHolder.addCallback(this);//为SurfaceView的句柄添加一个回调函数
        mCamera = CameraCompatibility.getCamera();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mCamera.create(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mCamera.autofocus();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.release();
    }

    public void takePicture( CameraInterface.Callback callback) {
        mCamera.takePicture(callback);
    }

    public void changeSlash() {
        mCamera.changeSlash();
    }

    public boolean isSlashOpen(){
        return mCamera.isSlashOpen();
    }


    public void changeFace() {
        mCamera.changeFace();
    }

    public void restart() {
        mCamera.restart();
    }
}
