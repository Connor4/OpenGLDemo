package com.connor.myapplication.util;

import android.graphics.Bitmap;
import android.os.Environment;

import com.connor.myapplication.data.Constant;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.IntBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by meitu on 2016/7/15.
 */
public class SaveUtil {
    /**
     * 建立目录，用于保存照片
     */
    private static void makeDir(File f) {
        if (!f.exists()) {
            if (!f.mkdirs()) {
                return;
            }
        }
    }

    /**
     * 保存相片到目录
     */
    public static File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment
                .DIRECTORY_PICTURES), "OpenGLDemo");

        makeDir(mediaStorageDir);

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "IMG_" + timeStamp + ".jpg");
        return mediaFile;
    }

    /**
     * 将当前屏幕的橡树通过读取转成bitmap
     */
    private static Bitmap getBitmapFromGL(int w, int h, GL10 gl) {
        int b[] = new int[w * (h)];
        int bt[] = new int[w * h];
        IntBuffer ib = IntBuffer.wrap(b);
        ib.position(0);
        gl.glReadPixels(0, 0, w, h, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, ib);
        for (int i = 0, k = 0; i < h; i++, k++) {
            for (int j = 0; j < w; j++) {
                int pix = b[i * w + j];
                int pb = (pix >> 16) & 0xff;
                int pr = (pix << 16) & 0xffff0000;
                int pix1 = (pix & 0xff00ff00) | pr | pb;
                bt[(h - k - 1) * w + j] = pix1;
            }
        }
        return Bitmap.createBitmap(bt, w, h, Bitmap.Config.ARGB_8888);
    }

    /**
     * 保存当前图片
     */
    public static void takeScreenShot(GL10 gl) {
        Bitmap bitmap = getBitmapFromGL(Constant.TextureWidth, Constant.TextureHeight, gl);
        //需要判断图片大小，排除黑屏的情况 <20K 即为黑屏。
        File imagePath = getOutputMediaFile();
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(imagePath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
                bitmap.recycle();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
