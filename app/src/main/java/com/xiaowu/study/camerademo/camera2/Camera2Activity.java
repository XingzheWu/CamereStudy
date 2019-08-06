package com.xiaowu.study.camerademo.camera2;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.TextView;

import com.xiaowu.study.camerademo.R;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class Camera2Activity extends AppCompatActivity {

    @BindView(R.id.camera_preview)
    CameraPreviewView cameraPreview;
    @BindView(R.id.capture)
    TextView capture;

    private File mFile;

    private TextureView.SurfaceTextureListener mTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera(width,height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };
    CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCaptureSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {

        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {

        }
    };

    private CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
            super.onCaptureProgressed(session, request, partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
        }
    };

    private CameraDevice cameraDevice;
    private CaptureRequest.Builder captureRequestBuilder;
    private CaptureRequest captureRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera2);
        ButterKnife.bind(this);
        mFile = new File(this.getExternalFilesDir(null), "pic.jpg");
    }

    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();
        if (cameraPreview.isAvailable()) {
            openCamera(cameraPreview.getWidth(), cameraPreview.getHeight());
        } else {
            cameraPreview.setSurfaceTextureListener(mTextureListener);
        }
    }

    private Handler mHandler;
    private HandlerThread mHandlerThread;

    private void startBackgroundThread() {
        mHandlerThread = new HandlerThread("cameraBackground");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeCamera();
        stopBackgroundThread();
    }

    private void closeCamera(){
        if (captureSession != null) {
            captureSession.close();
            captureSession = null;
        }
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }

        if (imageReader != null) {
            imageReader.close();
            imageReader = null;
        }
    }

    private void stopBackgroundThread() {
        mHandlerThread.quitSafely();
        try {
            mHandlerThread.join();
            mHandlerThread = null;
            mHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void openCamera(int width, int height) {
        setUpCamera(width, height);
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        try {
            manager.openCamera(cameraId, stateCallback, mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private CameraCaptureSession captureSession;
    private void createCaptureSession() {
        try {
            SurfaceTexture surfaceTexture = cameraPreview.getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(cameraPreview.getWidth(), cameraPreview.getHeight());

            Surface surface = new Surface(surfaceTexture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);

            cameraDevice.createCaptureSession(Arrays.asList(surface, imageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            captureSession = session;
                            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                            if (isFlashSupported){
                                captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE,CaptureRequest.CONTROL_AE_ANTIBANDING_MODE_AUTO);
                            }
                            captureRequest = captureRequestBuilder.build();
                            try {
                                captureSession.setRepeatingRequest(captureRequest,captureCallback,mHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                        }
                    },null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private String cameraId;
    private ImageReader imageReader;
    private ImageReader.OnImageAvailableListener onImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {

        }
    };

    private boolean isFlashSupported = false;
    private Size mPreviewSize;

    private void setUpCamera(int width, int height) {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        String[] cameraIdList = new String[0];
        try {
            cameraIdList = manager.getCameraIdList();

            for (String camera : cameraIdList) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(camera);
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);

                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT)
                    continue;
                StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map == null) {
                    return;
                }
                Size largest = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)), new CompareSizeByArea());
                imageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(), ImageFormat.JPEG, 2);
                imageReader.setOnImageAvailableListener(onImageAvailableListener, mHandler);


                mPreviewSize = new Size(cameraPreview.getWidth(), cameraPreview.getHeight());
                Boolean available = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                isFlashSupported = available == null ? false : available;
                cameraId = camera;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.capture)
    public void onViewClicked() {
    }

    static class CompareSizeByArea implements Comparator<Size> {

        @Override
        public int compare(Size o1, Size o2) {
            return Long.signum(o1.getWidth() * o1.getHeight() -
                    o2.getWidth() * o2.getHeight());
        }
    }
}
