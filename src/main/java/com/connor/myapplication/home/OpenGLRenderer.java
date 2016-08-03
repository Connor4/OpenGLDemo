package com.connor.myapplication.home;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.connor.myapplication.R;
import com.connor.myapplication.data.Constant;
import com.connor.myapplication.program.MosaicTextureShaderProgram;
import com.connor.myapplication.program.OtherTextureShaderProgram;
import com.connor.myapplication.program.TextureHelper;
import com.connor.myapplication.program.TextureShaderProgram;
import com.connor.myapplication.program.TraceTextureShaderProgram;
import com.connor.myapplication.util.FBOArrayUtil;
import com.connor.myapplication.util.SaveUtil;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_BLEND;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_ONE;
import static android.opengl.GLES20.GL_ONE_MINUS_SRC_ALPHA;
import static android.opengl.GLES20.GL_SRC_ALPHA;
import static android.opengl.GLES20.GL_ZERO;
import static android.opengl.GLES20.glBlendFunc;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDisable;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glViewport;

public class OpenGLRenderer implements GLSurfaceView.Renderer {
    private TextureShaderProgram mTextureProgram;
    private TextureShaderProgram mPointProgram;
    private TextureShaderProgram mEraserProgram;
    private TraceTextureShaderProgram mTraceProgram;
    private OtherTextureShaderProgram mOtherProgram;
    private MosaicTextureShaderProgram mEffectProgram;
    private BackGround mBackGround;
    private FBOBackGround mFBOBackGround;
    private Group mRoot;
    private Context mContext;
    private int mTexture;
    private int mPointTexture;
    private int mTargetTexture;
    private int mReturnTexture;
    private int mStarTexture;
    private int mDownStarTexture;
    private int mFramebuffer;
    private FBOArrayUtil mArrayUtil;
    private int mResourceId;

    public boolean mDrawLast;
    public boolean mDrawNext;
    public boolean mSavePic;


    public OpenGLRenderer(Context mContext, int resourceId) {
        this.mContext = mContext;
        Group group = new Group();
        this.mRoot = group;
        this.mResourceId = resourceId;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

        initProgram();
        initTexture();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        glViewport(0, 0, width, height);
        Constant.mSurfaceViewWidth = width;
        Constant.mSurfaceViewHeight = height;

        //因为SurfaceView布局没有设置SurfaceView的宽高，所以只能在这里创建
        mBackGround = new BackGround();//屏幕渲染使用
        mFBOBackGround = new FBOBackGround();//离屏渲染使用
        //给撤销的用
        mArrayUtil = new FBOArrayUtil();
        //独立创建一个FBO给当前的用
        mTargetTexture = mArrayUtil.createTargetTexture(Constant.TextureWidth, Constant
                .TextureHeight);
        mFramebuffer = mArrayUtil.createFrameBuffer(mTargetTexture);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        //缩放用
//        glViewport(0, 0, Constant
//                .mSurfaceViewWidth * 2, Constant.mSurfaceViewHeight * 2);
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);

        if (mSavePic) {
            drawOnscreen();
            SaveUtil.takeScreenShot(gl);
            mSavePic = false;
        } else {
            if (mDrawLast) {
                mReturnTexture = mArrayUtil.getLastTexture();
                if (mReturnTexture == -1) {
                    mDrawLast = false;
                }
            }

            if (mDrawNext) {
                mReturnTexture = mArrayUtil.getNextTexture();
                if (mReturnTexture == -1) {
                    mDrawNext = false;
                }
            }

            drawInFrameBuffer(mFramebuffer);
            //因为在FBO画的时候改变了视角，需要重新改变视角
            glViewport(0, 0, Constant.mSurfaceViewWidth, Constant.mSurfaceViewHeight);

            drawOnscreen();

        }

    }

    /**
     * 离屏渲染
     */
    private void drawOffscreen(int x, int y, int width, int height) {
        glViewport(x, y, width, height);
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glClear(GL_COLOR_BUFFER_BIT);

        //第一次之后都要使用FBO的那个纹理
        mTextureProgram.useProgram();
        if (mDrawLast || mDrawNext) {
            mTextureProgram.setUniforms(mReturnTexture);
            mDrawLast = false;
            mDrawNext = false;
        } else {
            mTextureProgram.setUniforms(mTargetTexture);
        }
        mFBOBackGround.bindData(mTextureProgram);
        mFBOBackGround.draw();

        switch (Constant.CURRENT_USE_TYPE) {

            case Constant.PAINT:
                glEnable(GL_BLEND);//开启混合
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                mRoot.draw(Constant.OffScreen, mPointProgram, mPointTexture);
                glDisable(GL_BLEND);//关闭混合
                break;

            case Constant.WALLPAPER:
                glEnable(GL_BLEND);//开启混合
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                mRoot.draw(Constant.OffScreen, mOtherProgram, mDownStarTexture);
                glDisable(GL_BLEND);//关闭混合
                break;

            case Constant.MOSAIC:
                glEnable(GL_BLEND);//开启混合
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                mRoot.drawMosaic(Constant.OffScreen, mEffectProgram, mTexture, mPointTexture);
                glDisable(GL_BLEND);//关闭混合
                break;

            case Constant.ERASER:
                glEnable(GL_BLEND);//开启混合
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                mRoot.draw(Constant.OffScreen, mEraserProgram, mPointTexture);
                glDisable(GL_BLEND);//关闭混合
                break;
            default:
                break;

        }
    }

    /**
     * 在屏幕上面绘制
     */
    private void drawOnscreen() {
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);
        glEnable(GL_BLEND);//开启混合
        glBlendFunc(GL_ONE, GL_ZERO);
        //每次将相片跟笔画混合在一起
        mTraceProgram.useProgram();
        if (mDrawLast || mDrawNext) {
            mTraceProgram.setUniforms(mTexture, mReturnTexture);//跟重做的纹理混合
        } else {
            mTraceProgram.setUniforms(mTexture, mTargetTexture);//跟当前屏幕对应的FBO的纹理混合
        }
        mBackGround.bindData(mTraceProgram);
        mBackGround.draw();
        glDisable(GL_BLEND);//关闭混合
    }

    /**
     * 创建所需要的program
     */
    private void initProgram() {
        mTextureProgram = new TextureShaderProgram(mContext, R.raw.texture_fragment_shader);
        mPointProgram = new TextureShaderProgram(mContext, R.raw.point_texture_fragment_shader);
        mEraserProgram = new TextureShaderProgram(mContext, R.raw
                .eraer_texture_shader_program);
        mTraceProgram = new TraceTextureShaderProgram(mContext, R.raw.trace_texture_shader_program);
        mOtherProgram = new OtherTextureShaderProgram(mContext, R.raw.other_texture_shader_program);
        mEffectProgram = new MosaicTextureShaderProgram(mContext, R.raw.mosaic_texture_shader_program);
    }

    /**
     * 创建所需要的纹理
     */
    private void initTexture() {
        mStarTexture = TextureHelper.loadTexture(mContext, R.drawable.images);
        mDownStarTexture = TextureHelper.loadTexture(mContext, R.drawable.downimages);
        mPointTexture = TextureHelper.loadTexture(mContext, R.drawable.cover);
        mTexture = TextureHelper.loadTexture(mContext, mResourceId);
    }


    /**
     * 点击事件up的时候对在FBO数组里的保存备份,
     * 主要作用是调用FBOUtil保存当前笔画
     */
    public void drawInBackupFBO() {
        drawInFrameBuffer(mFramebuffer);
        drawInFrameBuffer(mArrayUtil.getFrameBuffer());
        mRoot.clear();
    }

    /**
     * 添加给屏幕用的对象
     */
    public void addMesh(Mesh mesh) {
        mRoot.addObject(mesh);
    }

    /**
     * 添加给离屏渲染用的对象
     */
    public void addOppositeMesh(Mesh mesh) {
        mRoot.addOppositeObject(mesh);
    }

    /**
     * 在选定的FB上面绘制
     */
    private void drawInFrameBuffer(int FrameBuffer) {
        //调用FBO
        GLES20.glBindFramebuffer(
                GLES20.GL_FRAMEBUFFER, FrameBuffer);

        drawOffscreen(0, 0, Constant.TextureWidth, Constant.TextureHeight);

        GLES20.glBindFramebuffer(
                GLES20.GL_FRAMEBUFFER, 0);
    }
}
