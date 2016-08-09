package com.connor.myapplication.util;

import android.opengl.GLES20;
import android.util.Log;

import com.connor.myapplication.data.Constant;
import com.connor.myapplication.program.TextureHelper;

import java.util.LinkedList;

/**
 * Created by meitu on 2016/7/9.
 */
public class FBOArrayUtil {
    public  int[] TargetTextureArray = new int[10];
    public  int[] FrameBufferArray = new int[10];

    private int mSurfaceViewWidth;
    private int mSurfaceViewHeight;
    private int mTextureWidth;
    private int mTextureHeight;
    private int mPicWidth;
    private int mPicHeight;
    private static LinkedList<Integer> mFrameBufferQueue;
    private static LinkedList<Integer> mExtraFrameBufferQueue;
    private static LinkedList<Integer> mTextureQueue;
    private static LinkedList<Integer> mExtraTextureQueue;


    public FBOArrayUtil() {
        this.mSurfaceViewWidth = Constant.mSurfaceViewWidth;
        this.mSurfaceViewHeight = Constant.mSurfaceViewHeight;
        this.mTextureWidth = Constant.TextureWidth;
        this.mTextureHeight = Constant.TextureHeight;
        this.mPicWidth = TextureHelper.mOptions.outWidth;
        this.mPicHeight = TextureHelper.mOptions.outHeight;
        //创建一组FBO
        for (int i = 0; i < 9; ++i) {
            int TargetTexture = createTargetTexture(mSurfaceViewWidth, mSurfaceViewHeight);
            int FrameBuffer = createFrameBuffer(TargetTexture);
            TargetTextureArray[i] = TargetTexture;
            FrameBufferArray[i] = FrameBuffer;
        }

        mFrameBufferQueue = new LinkedList<>();
        mTextureQueue = new LinkedList<>();
        mExtraFrameBufferQueue = new LinkedList<>();
        mExtraTextureQueue = new LinkedList<>();
    }

    /**
     * 操作是为了取到新的一个FrameBuffer和将FB、Texture放入队列
     * 无论如何，都是获取FrameBuffer队列尾部的那个
     *
     * @return 新的FB
     */
    public int getFrameBuffer() {
        ClearExtraQueue();
        if (mFrameBufferQueue.size() >= 9) {
            //大于9，重新使用头部的
            mFrameBufferQueue.add(mFrameBufferQueue.pollFirst());//将头的用完再放回尾
            mTextureQueue.add(mTextureQueue.pollFirst());

        } else if (mFrameBufferQueue.size() >= 0 && mFrameBufferQueue.size() < 9) {
            //小于9的，将array里面剩余小的放入队列位置内
            mFrameBufferQueue.add(getFrameBufferMin());
            mTextureQueue.add(getTextureMin());

        }
        Log.d("TAG", "texture  " + mTextureQueue.toString() + " frame  " + mFrameBufferQueue
                .toString
                        ());
        return mFrameBufferQueue.getLast();//每次都返回队列最后那个fb
    }

    public int getNextTexture() {
        int result = -1;
        Log.d("TAG", "texture  " + mTextureQueue.toString() + " frame  " + mFrameBufferQueue
                .toString());
        Log.d("TAG", "extratexture " + mExtraTextureQueue.toString() + " extraframe  " +
                mExtraFrameBufferQueue.toString());
        if (!mExtraFrameBufferQueue.isEmpty()) {
            result = mExtraTextureQueue.getLast();
            mTextureQueue.add(mExtraTextureQueue.pollLast());
            mFrameBufferQueue.add(mExtraFrameBufferQueue.pollLast());
        }
        Log.d("TAG", "extratexture " + mExtraTextureQueue.toString() + " extraframe  " +
                mExtraFrameBufferQueue);
        Log.d("TAG", "texture  " + mTextureQueue.toString() + " frame  " + mFrameBufferQueue
                .toString());
        Log.d("TAG", "result  " + result);
        return result;
    }


    public int getLastTexture() {

        int result = -1;//如果不行，返回-1就不操作
        if (mTextureQueue.size() == 1) {//只有一张就返回原来的
            mExtraFrameBufferQueue.add(mFrameBufferQueue.pollLast());
            mExtraTextureQueue.add(mTextureQueue.pollLast());
            mFrameBufferQueue.pollLast();//去掉最后一个
            result = TargetTextureArray[9];//返回最初那张纹理
        } else if (mTextureQueue.size() > 1) {
            mExtraTextureQueue.add(mTextureQueue.pollLast());//从队列中取出最后一个，加到另外一个队列
            mExtraFrameBufferQueue.add(mFrameBufferQueue.pollLast());
            result = mTextureQueue.getLast();
        }
        Log.d("TAG", "texture  " + mTextureQueue.toString() + " frame  " + mFrameBufferQueue
                .toString());
        Log.d("TAG", "result  " + result);
        if (result == -1) {
            ClearQueue();
        }

        return result;
    }

    /**
     * 保存开始的纹理到数组9
     *
     * @param textureId 最开始的纹理
     */
    public void setTexture(int textureId) {
        TargetTextureArray[9] = textureId;
    }

    /**
     * 返回list里面不包含的array里面的最小
     */
    private int getFrameBufferMin() {
        int result = 0;
        for (int i = 0; i < FrameBufferArray.length; i++) {
            if (!mFrameBufferQueue.contains(FrameBufferArray[i])) {
                result = FrameBufferArray[i];
                break;
            }
        }
        return result;
    }

    /**
     * 返回list里面不包含的array里面的最小
     */
    private int getTextureMin() {
        int result = 0;
        for (int i = 0; i < TargetTextureArray.length; i++) {
            if (!mTextureQueue.contains(TargetTextureArray[i])) {
                result = TargetTextureArray[i];
                break;
            }
        }
        return result;
    }

    /**
     * 每次重画都会触发清空撤销重画里面的备份
     */
    private void ClearExtraQueue() {
        mExtraTextureQueue.clear();
        mExtraFrameBufferQueue.clear();
    }

    /**
     * 当重画到没有时，就要清空
     */
    private void ClearQueue() {
        mTextureQueue.clear();
        mFrameBufferQueue.clear();
        //ClearExtraQueue();//重画到没有的时候是否也清空撤销重画那里
    }

    /**
     * 检查FrameBuffer是否为空
     * @return
     */
    public static int CheckLeft() {
        return mFrameBufferQueue.size();
    }

    public int createTargetTexture(int width, int height) {
        int texture;
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        texture = textures[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height,
                0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_REPEAT);
        return texture;
    }

    public int createFrameBuffer(int targetTextureId) {
        int framebuffer;
        int[] framebuffers = new int[1];
        GLES20.glGenFramebuffers(1, framebuffers, 0);
        framebuffer = framebuffers[0];
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, framebuffer);

        GLES20.glFramebufferTexture2D(
                GLES20.GL_FRAMEBUFFER,
                GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, targetTextureId, 0);
        int status = GLES20
                .glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Framebuffer Object is not complete: "
                    + Integer.toHexString(status));
        }
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        return framebuffer;
    }
}
