package com.xiaowu.study.camerademo;

import android.os.Environment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author: wumm
 * @Date: 2018/6/26/0026
 */
public class MediaPathUtil {
    public static final int TYPE_PICTURE = 1;
    public static final int TYPE_VIDEO = 2;


    public static File getMediaPath(int type){
        File mediaDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"MediaDemo");
        if (!mediaDir.exists()){
            if (!mediaDir.mkdirs())
                return null;
        }

        String fileName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;

        if (type == TYPE_PICTURE){
            mediaFile = new File(mediaDir.getPath(),"IMG_" + fileName+ ".jpg");
        } else if (type ==TYPE_VIDEO ){
            mediaFile = new File(mediaDir.getPath(),"VIDEO_" + fileName + ".mp4");
        } else {
            return null;
        }
        return mediaFile;
    }
}
