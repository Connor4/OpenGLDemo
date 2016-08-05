package com.connor.myapplication.util;

import android.content.Context;

import com.connor.myapplication.R;
import com.connor.myapplication.program.OtherTextureShaderProgram;
import com.connor.myapplication.program.TextureHelper;

import java.util.Random;

/**
 * Renderer类的工具类，主要是讲render中一些计算操作放到这里
 */
public class RendererUtil {

    public static int CreateChangeTexture(Context mContext) {
        int mOtherTexture = 0;//选择0是错误的，0代表其他纹理
        Random random = new Random();
        int result = random.nextInt(6) % (6 - 1 + 1) + 1;
        switch (result) {
            case 1:
                mOtherTexture = TextureHelper.loadTexture(mContext, R.drawable.dm_1049_1);
                break;
            case 2:
                mOtherTexture = TextureHelper.loadTexture(mContext, R.drawable.dm_1049_2);
                break;
            case 3:
                mOtherTexture = TextureHelper.loadTexture(mContext, R.drawable.dm_1049_3);
                break;
            case 4:
                mOtherTexture = TextureHelper.loadTexture(mContext, R.drawable.dm_1049_4);
                break;
            case 5:
                mOtherTexture = TextureHelper.loadTexture(mContext, R.drawable.dm_1049_5);
                break;
            case 6:
                mOtherTexture = TextureHelper.loadTexture(mContext, R.drawable.dm_1049_6);
                break;
            default:
                break;
        }
        return mOtherTexture;
    }

    /**
     * 目的是采用不同的脚本
     */
    public static OtherTextureShaderProgram CreateChangeProgram(Context mContext) {
        OtherTextureShaderProgram mOtherProgram = null;
        int source1 = R.raw.other_texture_shader_program;
        int source2 = R.raw.other_texture_shader_program2;
        Random random = new Random();
        int result = random.nextInt(2) % (2 - 1 + 1) + 1;
        switch (result) {
            case 1:
                mOtherProgram = new OtherTextureShaderProgram(mContext,
                        source1);
                break;
            case 2:
                mOtherProgram = new OtherTextureShaderProgram(mContext,
                        source2);
                break;
            default:
                break;

        }
        return mOtherProgram;
    }
    //==============================end=============================

    public static int[] mFireWorkTexture = new int[6];

    public static void CreateFireWorkTexture(Context mContext) {
        int[] resource = new int[]
                {
                        R.drawable.dm_1000_1,
                        R.drawable.dm_1000_2,
                        R.drawable.dm_1000_3,
                        R.drawable.dm_1000_4,
                        R.drawable.dm_1000_5,
                        R.drawable.dm_1000_6
                };
        for (int i = 0; i < 6; i++) {
            mFireWorkTexture[i] = TextureHelper.loadTexture(mContext, resource[i]);
        }
    }


    /**
     * 选择烟花笔的纹理
     *
     * @return
     */
    public static int SelectFireWorkTexture() {
        Random random = new Random();
        int result = random.nextInt(6) % (6 - 1 + 1) + 1;
        switch (result) {
            case 1:
                return mFireWorkTexture[0];
            case 2:
                return mFireWorkTexture[1];
            case 3:
                return mFireWorkTexture[2];
            case 4:
                return mFireWorkTexture[3];
            case 5:
                return mFireWorkTexture[4];
            case 6:
                return mFireWorkTexture[5];
            default:
                break;
        }
        return 0;
    }

    /**
     * 创建给firework用的program
     *
     * @param mContext
     * @return
     */
    public static OtherTextureShaderProgram CreateFireWorkProgram(Context mContext) {
        OtherTextureShaderProgram mOtherProgram = new OtherTextureShaderProgram(mContext,
                R.raw.firework_texture_shader_program);
        return mOtherProgram;
    }
}
