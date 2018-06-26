package com.xiaowu.study.camerademo;

import android.Manifest;
import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks{

    public static final int REQUEST_CAMERA_CODE = 1000;

    @BindView(R.id.camera_preview)
    CameraPreview cameraPreview;
    @BindView(R.id.shot_btn)
    Button shotBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraPreview.setCamera(initCamera());
        cameraPreview.startPreview();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraPreview.stopPreview();
    }

    @AfterPermissionGranted(REQUEST_CAMERA_CODE)
    private Camera initCamera() {
        Camera camera = null;
        String[] perms = new String[]{Manifest.permission.CAMERA};
        if (EasyPermissions.hasPermissions(this,perms)){
            camera = Camera.open();
        } else {
            EasyPermissions.requestPermissions(this,"该功能需要相机权限",REQUEST_CAMERA_CODE,perms);
        }
        return camera;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,this);
    }

    private boolean isRecordering;
    @OnClick(R.id.shot_btn)
    public void onViewClicked() {
//        takePhoto();
        doRecorder();
    }

    @AfterPermissionGranted(REQUEST_AUDIO_CODE)
    private void doRecorder(){
        String[] perms = new String[]{Manifest.permission.RECORD_AUDIO};
        if (!EasyPermissions.hasPermissions(this,perms)){
            EasyPermissions.requestPermissions(this,"此功能需要录音权限",REQUEST_STORAGE_CODE,perms);
            return;
        }
        if (isRecordering){
            cameraPreview.stopRecorder();
            shotBtn.setText("开始");
        } else {
            cameraPreview.startRecorder();
            shotBtn.setText("结束");
        }
        isRecordering = !isRecordering;
    }
    public static final int REQUEST_STORAGE_CODE = 1001;
    public static final int REQUEST_AUDIO_CODE = 1002;

    @AfterPermissionGranted(REQUEST_STORAGE_CODE)
    private void takePhoto(){
        String[] perms = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (!EasyPermissions.hasPermissions(this,perms)){
            EasyPermissions.requestPermissions(this,"拍照需要读取存储权限",REQUEST_STORAGE_CODE,perms);
            return;
        }
        cameraPreview.takePhoto();

    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionDenied(this,Manifest.permission.CAMERA)){
            new AppSettingsDialog.Builder(this).build().show();
        }
    }
}
