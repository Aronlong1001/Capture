package com.bailian.capture.utils;


import android.os.Environment;


import com.bailian.capture.bean.PhotoItem;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;

public class FileUtils {

    private static String BASE_PATH;
    private static String STICKER_BASE_PATH;

    private static FileUtils mInstance;

    public static FileUtils getInst() {
        if (mInstance == null) {
            synchronized (FileUtils.class) {
                if (mInstance == null) {
                    mInstance = new FileUtils();
                }
            }
        }
        return mInstance;
    }

    public String getSystemPhotoPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/Camera";
    }


    //获取path路径下的图片
    public ArrayList<PhotoItem> findPicsInDir(String path) {
        ArrayList<PhotoItem> photos = new ArrayList<PhotoItem>();
        File dir = new File(path);
        if (dir.exists() && dir.isDirectory()) {
            for (File file : dir.listFiles(new FileFilter() {

                @Override
                public boolean accept(File pathname) {
                    String filePath = pathname.getAbsolutePath();
                    return (filePath.endsWith(".png") || filePath.endsWith(".jpg") || filePath
                            .endsWith(".jepg"));
                }
            })) {
                photos.add(new PhotoItem(file.getAbsolutePath(), file.lastModified()));
            }
        }
        Collections.sort(photos);
        return photos;
    }


}
