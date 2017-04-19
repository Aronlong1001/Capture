package com.bailian.capture.compatibility;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceHolder;


import com.bailian.capture.BaseApplication;
import com.bailian.capture.utils.PhoneUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;


public class LowCamera implements CameraInterface {

    private SurfaceHolder mHolder;

    private boolean bOpenSlash = false;
    private int mCameraFace = Camera.CameraInfo.CAMERA_FACING_BACK;

    private Camera mCamera;
    private Camera.Size adapterSize;
    private Camera.Size previewSize;

    /**
     * 最大宽高比差
     */
    private static final double MAX_ASPECT_DISTORTION = 0.15;
    /**
     * 最小预览界面的分辨率
     */
    private static final int MIN_PREVIEW_PIXELS = 480 * 320;

    @Override
    public void create(SurfaceHolder holder) {
        if (mCamera == null) {
            try {
                mCamera = Camera.open(mCameraFace);
                if (holder != null) {
                    mHolder = holder;
                }
                mCamera.setPreviewDisplay(mHolder);
                initCamera();
                mCamera.startPreview();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void autofocus() {
        new Thread() {
            @Override
            public void run() {
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (mCamera == null) {
                    return;
                }
                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        if (success) {
                            initCamera();//实现相机的参数初始化
                        }
                    }
                });
            }
        };
    }

    @Override
    public void takePicture(final Callback callback) {
        try {
            mCamera.takePicture(null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    if (callback != null) {
                        callback.onCapturePicture();
                    }
                    new SavePicThread(callback, data).start();
                }
            });
        } catch (Throwable t) {
            t.printStackTrace();
            if (callback != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onFailTakePicture();
                    }
                });
            }
            try {
                mCamera.startPreview();
            } catch (Throwable e) {

            }
        }
    }

    @Override
    public void changeFace() {
        if (mCameraFace == Camera.CameraInfo.CAMERA_FACING_BACK) {
            mCameraFace = Camera.CameraInfo.CAMERA_FACING_FRONT;
        } else {
            mCameraFace = Camera.CameraInfo.CAMERA_FACING_BACK;
        }
        release();
        create(null);
    }

    @Override
    public void restart() {
        //重新恢复预览状态
        if (mCamera != null) {
            mCamera.startPreview();
        }
    }

    @Override
    public void changeSlash() {
        if (mCamera == null) {
            return;
        }
        this.bOpenSlash = !bOpenSlash;
        setSlash();
    }

    @Override
    public boolean isSlashOpen() {
        return bOpenSlash;
    }

    private void setSlash() {
        if (mCamera == null) {
            return;
        }
        try {
            Camera.Parameters parameters = mCamera.getParameters();
            if (parameters != null) {
                if (bOpenSlash) {
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                } else {
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                }
            }
            mCamera.setParameters(parameters);
        } catch (Exception e) {

        }
    }

    private class SavePicThread extends Thread {

        private byte[] data;
        private Callback callback;

        SavePicThread(Callback callback, byte[] data) {
            this.data = data;
            this.callback = callback;
        }

        @Override
        public void run() {
            Log.e("result","成功");
            final Bitmap result = arrayToBitmap(data);
            Log.e("result","成功矫正");
            if (callback != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onSuccessTakePicture(result, data);
                    }
                });
            }
        }
    }

    private Handler mHandler = new Handler(Looper.getMainLooper());


    public Bitmap arrayToBitmap(byte[] data) {
//        Bitmap bmpOrigin = BitmapUtils.byteToBitmap(data);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;  //这里表示原来图片的1/4
        Bitmap bmpOrigin = BitmapFactory.decodeByteArray(data, 0,
                data.length, options);
        Log.e("result","转换成功");

        /**
         * 需要将拍下的图旋转90度
         */
        Matrix matrix = new Matrix();
        if (mCameraFace == Camera.CameraInfo.CAMERA_FACING_BACK) {
            /**
             * 正拍
             */
            matrix.setRotate(90);
        } else {
            /**
             * 自拍
             */
            matrix.setRotate(-90);
            matrix.postScale(-1, 1);
        }
        Bitmap bmp = Bitmap.createBitmap(bmpOrigin, 0, 0,
                bmpOrigin.getWidth(), bmpOrigin.getHeight(), matrix,
                true);
        return bmp;
    }

    @Override
    public void release() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    private void initCamera() {
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPictureFormat(PixelFormat.JPEG);

        setUpPicSize(parameters);
        setUpPreviewSize(parameters);
        if (adapterSize != null) {
            parameters.setPictureSize(adapterSize.width, adapterSize.height);
        }
        if (previewSize != null) {
            parameters.setPreviewSize(previewSize.width, previewSize.height);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);//1连续对焦
        } else {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }
        if (bOpenSlash) {
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
        } else {
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        }
        setDispaly(parameters);
        try {
            mCamera.setParameters(parameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mCamera.startPreview();
        mCamera.cancelAutoFocus();// 2如果要实现连续的自动对焦，这一句必须加上
    }

    //控制图像的正确显示方向
    private void setDispaly(Camera.Parameters parameters) {
        if (Build.VERSION.SDK_INT >= 8) {
            setDisplayOrientation(mCamera, 90);
        } else {
            parameters.setRotation(90);
        }
    }

    //实现的图像的正确显示
    private void setDisplayOrientation(Camera camera, int i) {
        Method downPolymorphic;
        try {
            downPolymorphic = camera.getClass().getMethod("setDisplayOrientation",
                    new Class[]{int.class});
            if (downPolymorphic != null) {
                downPolymorphic.invoke(camera, new Object[]{i});
            }
        } catch (Exception e) {
            Log.e("Came_e", "图像出错");
        }
    }

    private void setUpPicSize(Camera.Parameters parameters) {
        if (adapterSize != null) {
            return;
        } else {
            adapterSize = findBestPictureResolution();
            return;
        }
    }

    private void setUpPreviewSize(Camera.Parameters parameters) {
        if (previewSize != null) {
            return;
        } else {
            previewSize = findBestPreviewResolution();
        }
    }

    /**
     * 找出最适合的预览界面分辨率
     *
     * @return
     */
    private Camera.Size findBestPreviewResolution() {
        Camera.Parameters cameraParameters = mCamera.getParameters();
        Camera.Size defaultPreviewResolution = cameraParameters.getPreviewSize();

        List<Camera.Size> rawSupportedSizes = cameraParameters.getSupportedPreviewSizes();
        if (rawSupportedSizes == null) {
            return defaultPreviewResolution;
        }

        // 按照分辨率从大到小排序
        List<Camera.Size> supportedPreviewResolutions = new ArrayList<Camera.Size>(rawSupportedSizes);
        Collections.sort(supportedPreviewResolutions, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size a, Camera.Size b) {
                int aPixels = a.height * a.width;
                int bPixels = b.height * b.width;
                if (bPixels < aPixels) {
                    return -1;
                }
                if (bPixels > aPixels) {
                    return 1;
                }
                return 0;
            }
        });


        // 移除不符合条件的分辨率
        double screenAspectRatio = (double) PhoneUtils.getWindowWidth(BaseApplication.getContext())
                / (double) (PhoneUtils.getWindowHeight(BaseApplication.getContext()));
        Iterator<Camera.Size> it = supportedPreviewResolutions.iterator();
        while (it.hasNext()) {
            Camera.Size supportedPreviewResolution = it.next();
            int width = supportedPreviewResolution.width;
            int height = supportedPreviewResolution.height;

            // 移除低于下限的分辨率，尽可能取高分辨率
            if (width * height < MIN_PREVIEW_PIXELS) {
                it.remove();
                continue;
            }

            // 在camera分辨率与屏幕分辨率宽高比不相等的情况下，找出差距最小的一组分辨率
            // 由于camera的分辨率是width>height，我们设置的portrait模式中，width<height
            // 因此这里要先交换然preview宽高比后在比较
            boolean isCandidatePortrait = width > height;
            int maybeFlippedWidth = isCandidatePortrait ? height : width;
            int maybeFlippedHeight = isCandidatePortrait ? width : height;
            double aspectRatio = (double) maybeFlippedWidth / (double) maybeFlippedHeight;
            double distortion = Math.abs(aspectRatio - screenAspectRatio);
            if (distortion > MAX_ASPECT_DISTORTION) {
                it.remove();
                continue;
            }

            // 找到与屏幕分辨率完全匹配的预览界面分辨率直接返回
            if (maybeFlippedWidth == PhoneUtils.getWindowWidth(BaseApplication.getContext())
                    && maybeFlippedHeight == PhoneUtils.getWindowHeight(BaseApplication.getContext())) {
                return supportedPreviewResolution;
            }
        }

        // 如果没有找到合适的，并且还有候选的像素，则设置其中最大比例的，对于配置比较低的机器不太合适
        if (!supportedPreviewResolutions.isEmpty()) {
            Camera.Size largestPreview = supportedPreviewResolutions.get(0);
            return largestPreview;
        }

        // 没有找到合适的，就返回默认的

        return defaultPreviewResolution;
    }

    private Camera.Size findBestPictureResolution() {
        Camera.Parameters cameraParameters = mCamera.getParameters();
        List<Camera.Size> supportedPicResolutions = cameraParameters.getSupportedPictureSizes(); // 至少会返回一个值

        Camera.Size defaultPictureResolution = cameraParameters.getPictureSize();

        // 排序
        List<Camera.Size> sortedSupportedPicResolutions = new ArrayList<Camera.Size>(
                supportedPicResolutions);
        Collections.sort(sortedSupportedPicResolutions, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size a, Camera.Size b) {
                int aPixels = a.height * a.width;
                int bPixels = b.height * b.width;
                if (bPixels < aPixels) {
                    return -1;
                }
                if (bPixels > aPixels) {
                    return 1;
                }
                return 0;
            }
        });

        // 移除不符合条件的分辨率
        double screenAspectRatio = (double) PhoneUtils.getWindowWidth(BaseApplication.getContext())
                / (double) (PhoneUtils.getWindowHeight(BaseApplication.getContext()));
        Iterator<Camera.Size> it = sortedSupportedPicResolutions.iterator();
        while (it.hasNext()) {
            Camera.Size supportedPreviewResolution = it.next();
            int width = supportedPreviewResolution.width;
            int height = supportedPreviewResolution.height;

            // 在camera分辨率与屏幕分辨率宽高比不相等的情况下，找出差距最小的一组分辨率
            // 由于camera的分辨率是width>height，我们设置的portrait模式中，width<height
            // 因此这里要先交换然后在比较宽高比
            boolean isCandidatePortrait = width > height;
            int maybeFlippedWidth = isCandidatePortrait ? height : width;
            int maybeFlippedHeight = isCandidatePortrait ? width : height;
            double aspectRatio = (double) maybeFlippedWidth / (double) maybeFlippedHeight;
            double distortion = Math.abs(aspectRatio - screenAspectRatio);
            if (distortion > MAX_ASPECT_DISTORTION) {
                it.remove();
                continue;
            }
        }

        // 如果没有找到合适的，并且还有候选的像素，对于照片，则取其中最大比例的，而不是选择与屏幕分辨率相同的
        if (!sortedSupportedPicResolutions.isEmpty()) {
            return sortedSupportedPicResolutions.get(0);
        }

        // 没有找到合适的，就返回默认的
        return defaultPictureResolution;
    }


}
