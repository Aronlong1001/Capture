package com.bailian.capture.compatibility;

import android.graphics.Bitmap;
import android.view.SurfaceHolder;


public interface CameraInterface {

    public static final int PHOTO_MAX_SIZE = 800;

    public void release();

    public void create(SurfaceHolder holder);

    public void autofocus();

    public void takePicture(Callback callback);

    public void changeFace();

    public void restart();

    public void changeSlash();

    public boolean isSlashOpen();

    public interface Callback {

        /**
         * 拍下的瞬间,还未处理图片
         */
        public void onCapturePicture();

        public void onSuccessTakePicture(Bitmap bitmap, byte[] data);

        public void onFailTakePicture();

    }

}
