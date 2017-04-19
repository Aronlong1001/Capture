package com.bailian.capture;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    private ImageView img;
    private TextView takeCamera;
    public static final int REQUEST_CAMERA = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        img = (ImageView) findViewById(R.id.img);
        takeCamera = (TextView) findViewById(R.id.take);
        takeCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                startActivityForResult(intent, REQUEST_CAMERA);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CAMERA&& resultCode == RESULT_OK && data != null){
            String path = Environment.getExternalStorageDirectory() + "/RISO/picture/" + data.getStringExtra(CameraActivity.EXTRA_PATH);
            img.setImageURI(Uri.parse("file://" + path));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
