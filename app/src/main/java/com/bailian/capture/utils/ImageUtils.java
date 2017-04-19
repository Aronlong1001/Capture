package com.bailian.capture.utils;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.bailian.capture.bean.Album;
import com.bailian.capture.bean.PhotoItem;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class ImageUtils {

    public static Bitmap createScanImage(String url) {
        try {
            //判断URL合法性
            if (!TextUtils.isEmpty(url)) {
                Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
                hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
                hints.put(EncodeHintType.MARGIN, 1);
                //图像数据转换，使用了矩阵转换
                BitMatrix bitMatrix = new QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, 300, 300, hints);
                int[] pixels = new int[300 * 300];
                //下面这里按照二维码的算法，逐个生成二维码的图片，
                //两个for循环是图片横列扫描的结果
                for (int y = 0; y < 300; y++) {
                    for (int x = 0; x < 300; x++) {
                        if (bitMatrix.get(x, y)) {
                            pixels[y * 300 + x] = 0xff000000;
                        } else {
                            pixels[y * 300 + x] = 0xffffffff;
                        }
                    }
                }
                //生成二维码图片的格式，使用ARGB_8888
                Bitmap bitmap = Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888);
                bitmap.setPixels(pixels, 0, 300, 0, 0, 300, 300);
                //显示到一个ImageView上面
                return bitmap;
            }
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static void saveImageToGallery(Context context, Bitmap bmp) throws Exception {
        // 首先保存图片
        File appDir = new File(Environment.getExternalStorageDirectory() + "/RISO", "picture");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        FileOutputStream fos = new FileOutputStream(file);
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        fos.flush();
        fos.close();
        // 其次把文件插入到系统图库
        MediaStore.Images.Media.insertImage(context.getContentResolver(),
                file.getAbsolutePath(), fileName, null);

        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + file.getAbsolutePath())));

    }

    public static void saveImageToGallery(Context context, String fileName, Bitmap bmp) throws Exception {
        // 首先保存图片
        File appDir = new File(Environment.getExternalStorageDirectory() + "/RISO/picture");
//        File appDir = new File(context.getFilesDir() + "/RISO/picture");
        if (!appDir.exists()) {
            appDir.mkdirs();
        }
        FileOutputStream fos = null;
        try {
            File file = new File(appDir + "/" + fileName);
            fos = new FileOutputStream(file);
            if (fos != null){
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
                // 其次把文件插入到系统图库
                MediaStore.Images.Media.insertImage(context.getContentResolver(),
                        file.getAbsolutePath(), fileName, null);

                context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + file.getAbsolutePath())));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public static List<Album> findGalleries(Context mContext, List<String> paths, long babyId) {
        paths.clear();
        paths.add(FileUtils.getInst().getSystemPhotoPath());
        String[] projection = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DATE_ADDED};//FIXME 拍照时间为新增照片时间
        Cursor cursor = mContext.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,//指定所要查询的字段
                MediaStore.Images.Media.SIZE + ">?",//查询条件
                new String[]{"100000"}, //查询条件中问号对应的值
                MediaStore.Images.Media.DATE_ADDED + " desc");

        //文件夹照片
        Map<String, Album> galleries = new HashMap<String, Album>();

        if (cursor != null) {
            cursor.moveToFirst();

            while (cursor.moveToNext()) {
                String data = cursor.getString(1);
                if (data.lastIndexOf("/") < 1) {
                    continue;
                }
                String sub = data.substring(0, data.lastIndexOf("/"));
                if (!galleries.keySet().contains(sub)) {
                    String name = sub.substring(sub.lastIndexOf("/") + 1, sub.length());
                    if (!paths.contains(sub)) {
                        paths.add(sub);
                    }
                    galleries.put(sub, new Album(name, sub, new ArrayList<PhotoItem>()));
                }

                galleries.get(sub).getPhotos().add(new PhotoItem(data, (long) (cursor.getInt(2)) * 1000));
            }
        }
        //系统相机照片
        ArrayList<PhotoItem> sysPhotos = FileUtils.getInst().findPicsInDir(
                FileUtils.getInst().getSystemPhotoPath());
        if (!sysPhotos.isEmpty()) {
            galleries.put(FileUtils.getInst().getSystemPhotoPath(), new Album("相册", FileUtils
                    .getInst().getSystemPhotoPath(), sysPhotos));
        } else {
            galleries.remove(FileUtils.getInst().getSystemPhotoPath());
            paths.remove(FileUtils.getInst().getSystemPhotoPath());
        }

        List<Album> albums = new ArrayList<>();
        //将Map转为List
        for (Map.Entry<String, Album> entry : galleries.entrySet()) {
            albums.add(entry.getValue());
        }
        return albums;
    }

    /**
     * Load resource bitmap with given name.
     *
     * @param name Name of the resource bitmap file.
     * @param ctx
     * @return Bitmap or 'null' if no such resource found.
     */
    public static Bitmap loadResBitmapWithName(String name, Context ctx) {
        Bitmap b = null;

        int res = ctx.getResources().getIdentifier(name, "drawable", ctx.getPackageName());
        if (res == 0)    // No resource found with given name.
            return b;
        InputStream is = ctx.getResources().openRawResource(res);
        try {
            b = BitmapFactory.decodeStream(is);
        } catch (Exception e) {
            // normally should never come here.
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return b;
    }
}
