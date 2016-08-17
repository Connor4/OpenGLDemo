package com.connor.myapplication.program;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import com.connor.myapplication.data.Constant;

import java.io.IOException;
import java.io.InputStream;

import static android.opengl.GLES20.GL_LINEAR;
import static android.opengl.GLES20.GL_LINEAR_MIPMAP_LINEAR;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDeleteTextures;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glGenerateMipmap;
import static android.opengl.GLES20.glTexParameteri;
import static android.opengl.GLUtils.texImage2D;

/**
 * Created by meitu on 2016/7/5.
 */
public class TextureHelper {
    /**
     * 保存照片options，某些地方需要用到
     */
    public static BitmapFactory.Options mOptions;
    /**
     * 通过输入图片，加载纹理
     *
     * @param context
     * @param resourceId
     * @return
     */
    public static int loadTexture(Context context, int resourceId) {
        final int[] textureObjectIds = new int[1];
        glGenTextures(1, textureObjectIds, 0);

        if (textureObjectIds[0] == 0) {
            return 0;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;

        Bitmap bitmapTmp = BitmapFactory.decodeResource(
                context.getResources(), resourceId, options);

//        Bitmap bitmap = getResizedBitmap(bitmapTmp, Constant.ScreenWidth, Constant.ScreenHeight );

        if (bitmapTmp == null) {

            glDeleteTextures(1, textureObjectIds, 0);
            return 0;
        }
        glBindTexture(GL_TEXTURE_2D, textureObjectIds[0]);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        texImage2D(GL_TEXTURE_2D, 0, bitmapTmp, 0);

        glGenerateMipmap(GL_TEXTURE_2D);
        bitmapTmp.recycle();

        glBindTexture(GL_TEXTURE_2D, 0);

        return textureObjectIds[0];
    }

    /**
     * 这个函数跟上面那个是一样的，但是为了保存原始照片的options，
     *需要另外弄一个给它记录
     * @param context
     * @param resourceId
     * @return
     */
    public static int loadOriginalTexture(Context context, int resourceId) {
        final int[] textureObjectIds = new int[1];
        glGenTextures(1, textureObjectIds, 0);

        if (textureObjectIds[0] == 0) {
            return 0;
        }

        mOptions = new BitmapFactory.Options();
        mOptions.inScaled = false;

        Bitmap bitmapTmp = BitmapFactory.decodeResource(
                context.getResources(), resourceId, mOptions);

        Bitmap bitmap = getResizedBitmap(bitmapTmp, Constant.ScreenWidth, Constant.ScreenHeight );

        if (bitmap == null) {

            glDeleteTextures(1, textureObjectIds, 0);
            return 0;
        }
        glBindTexture(GL_TEXTURE_2D, textureObjectIds[0]);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);

        glGenerateMipmap(GL_TEXTURE_2D);
        bitmap.recycle();

        glBindTexture(GL_TEXTURE_2D, 0);

        return textureObjectIds[0];
    }

    /**
     * 缩放bitmap,
     *
     * @param bm        被缩放的bitmap
     * @param newWidth  bitmap.width/4
     * @param newHeight bitmap.height/4
     * @return 新大小的bitmap
     */
    public static Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);

        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }

    /**w
     * 获取照片的比率
     * @return
     */
    public static float getBitmapOptions() {
        return (float)mOptions.outHeight/(float)mOptions.outWidth;
    }

}
