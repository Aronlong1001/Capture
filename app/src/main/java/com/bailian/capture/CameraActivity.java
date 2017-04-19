package com.bailian.capture;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.bailian.capture.compatibility.CameraInterface;
import com.bailian.capture.utils.ImageUtils;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class CameraActivity extends AppCompatActivity implements View.OnClickListener {

    public static String EXTRA_PATH = "extra_path";
    private LinearLayout back;
    private TextView finish;
    private CameraView cameraView;
    private ImageButton change_camera, flcker, camera;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        requestCodeQRCodePermissions();
        initView();
        initListener();
    }

    private void initView() {
        back = (LinearLayout) findViewById(R.id.lly_back);
        finish = (TextView) findViewById(R.id.finish);
        cameraView = (CameraView) findViewById(R.id.area_sv);
        change_camera = (ImageButton) findViewById(R.id.change_camera_ib);
        flcker = (ImageButton) findViewById(R.id.flicker_ib);
        camera = (ImageButton) findViewById(R.id.camera);
    }

    @AfterPermissionGranted(1)
    private void requestCodeQRCodePermissions() {
        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE};
        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this, "需要打开相机和散光灯的权限", 1, perms);
        }
    }

    public void btnBackClick(View view) {
        if (isTakePick) {
            cameraView.restart();
            isTakePick = false;
            finish.setVisibility(View.GONE);
        } else {
            finish();
        }
    }



    protected void initListener() {
        change_camera.setOnClickListener(this);
        camera.setOnClickListener(this);
        flcker.setOnClickListener(this);
        finish.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.camera) {
            startTakePhoto();
        } else if (view.getId() == R.id.change_camera_ib) {
            changeCamera();
        } else if (view.getId() == R.id.flicker_ib) {
            changeFlicker();
        } else if (view.getId() == R.id.finish) {
        }
    }


    boolean isTakePick = false;

    @Override
    public void onBackPressed() {
        if (isTakePick) {
            cameraView.restart();
            isTakePick = false;
            finish.setVisibility(View.GONE);
        } else {
            finish();
        }
    }

    private void startTakePhoto() {
        isTakePick = true;
        cameraView.takePicture(new CameraInterface.Callback() {
            @Override
            public void onCapturePicture() {
                Log.i("onCapturePicture","onCapturePicture");
            }

            @Override
            public void onSuccessTakePicture(Bitmap bitmap, byte[] data) {
                try {
                    String fileName = System.currentTimeMillis() + ".jpg";
                    ImageUtils.saveImageToGallery(CameraActivity.this, fileName, bitmap);
                    Intent intent = new Intent();
                    intent.putExtra(EXTRA_PATH, fileName);
                    setResult(RESULT_OK, intent);
                    finish();
                } catch (Exception e) {
                    Log.e("error---","保存失败");
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailTakePicture() {
                Log.e("onFailTakePicture","onFailTakePicture");
            }
        });
    }


    private void changeCamera() {
        cameraView.changeFace();
    }

    private boolean isOpenFlicker;

    private void changeFlicker() {
        isOpenFlicker = !isOpenFlicker;
        cameraView.changeSlash();
        if (isOpenFlicker) {
            flcker.setBackgroundResource(R.drawable.icon_flicker);
        } else {
            flcker.setBackgroundResource(R.drawable.icon_no_flicker);
        }
    }
}
