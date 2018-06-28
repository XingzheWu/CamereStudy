package com.xiaowu.study.camerademo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: wumm
 * Date: 2018/6/26/0026
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private Camera camera;
    private SurfaceHolder holder;
    private MediaRecorder mediaRecorder;

    public CameraPreview(Context context) {
        this(context, null);
    }

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);

        holder = getHolder();
        holder.addCallback(this);
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
        if (camera != null) {
            camera.setDisplayOrientation(90);
            Camera.Parameters parameters = camera.getParameters();
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            camera.setParameters(parameters);
            camera.setFaceDetectionListener(new Camera.FaceDetectionListener() {
                @Override
                public void onFaceDetection(Camera.Face[] faces, Camera camera) {
                    List<Camera.Area> areas = new ArrayList<>();
                    if (faces == null || faces.length == 0)
                        return;
                    for (Camera.Face face : faces) {
                        Rect rect = face.rect;
                        Point leftEye = face.leftEye;
                        Point rightEye = face.rightEye;
                        Point mouth = face.mouth;
                        Log.e("onFaceDetection", "---rect = " + rect.left + " , " + rect.top + " , " + rect.right + " , " + rect.bottom);
                        Camera.Area area = new Camera.Area(rect, 1);
                        areas.add(area);
                    }
                    Camera.Parameters parameters1 = camera.getParameters();
                    parameters1.setFocusAreas(areas);
                    camera.setParameters(parameters1);
                }
            });
        }
    }

    public void startPreview() {
        if (camera != null) {
            try {
                camera.setPreviewDisplay(holder);
                camera.startPreview();
                if (camera.getParameters().getMaxNumDetectedFaces() > 0)
                    camera.startFaceDetection();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopPreview() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }


    private Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            savePhoto(data);
        }
    };


    private void savePhoto(byte[] data) {

        File pictureFile = MediaPathUtil.getMediaPath(MediaPathUtil.TYPE_PICTURE);
        if (pictureFile == null)
            return;
        try {
            FileOutputStream outputStream = new FileOutputStream(pictureFile);
            outputStream.write(data);
            outputStream.close();
            getContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    Uri.fromFile(new File(pictureFile.getPath()))));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void takePhoto() {
        if (camera != null) {
            camera.takePicture(null, null, pictureCallback);
            camera.stopPreview();
            camera.startPreview();
        }
    }

    public boolean prepareRecorder() {
        camera.unlock();
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setCamera(camera);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        File mediaFile = MediaPathUtil.getMediaPath(MediaPathUtil.TYPE_VIDEO);
        if (mediaFile != null)
            mediaRecorder.setOutputFile(mediaFile.getPath());
        mediaRecorder.setPreviewDisplay(holder.getSurface());
        mediaRecorder.setOrientationHint(90);
        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    public void startRecorder() {
        if (prepareRecorder())
            mediaRecorder.start();
    }

    public void stopRecorder() {
        mediaRecorder.stop();
        releaseMediaRecorder();
    }

    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
            camera.lock();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        startPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseMediaRecorder();
        stopPreview();
    }
}
