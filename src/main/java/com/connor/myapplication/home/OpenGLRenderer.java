package com.connor.myapplication.home;

import android.content.Context;
import android.graphics.PointF;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;

import com.connor.myapplication.R;
import com.connor.myapplication.data.Constant;
import com.connor.myapplication.program.MosaicTextureShaderProgram;
import com.connor.myapplication.program.TextureHelper;
import com.connor.myapplication.program.TextureShaderProgram;
import com.connor.myapplication.program.TraceTextureShaderProgram;
import com.connor.myapplication.util.FBOArrayUtil;
import com.connor.myapplication.util.RendererUtil;
import com.connor.myapplication.util.SaveUtil;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_BLEND;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_ONE_MINUS_SRC_ALPHA;
import static android.opengl.GLES20.GL_SRC_ALPHA;
import static android.opengl.GLES20.glBlendFunc;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDeleteProgram;
import static android.opengl.GLES20.glDeleteTextures;
import static android.opengl.GLES20.glDisable;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glViewport;

public class OpenGLRenderer implements GLSurfaceView.Renderer, MainActivity.GestureHandleCallback {
    private TextureShaderProgram mTextureProgram;
    private TextureShaderProgram mPointProgram;
    private TextureShaderProgram mEraserProgram;
    private TraceTextureShaderProgram mTraceProgram;
    private MosaicTextureShaderProgram mEffectProgram;
    private BackGround mBackGround;
    private FBOBackGround mFBOBackGround;
    private Group mRoot;
    private Context mContext;
    private int mTexture;
    private int mPointTexture;
    //    private int mFireWorkTexture;
    private int mTargetTexture;
    private int mReturnTexture;

    private int mFramebuffer;
    private FBOArrayUtil mArrayUtil;
    private int mResourceId;

    public boolean mDrawLast;
    public boolean mDrawNext;
    public boolean mSavePic;
    //=======手势部分start======
    private PointF mLastTouchPoint = new PointF();
    private PointF mCurrentTouchPoint = new PointF();
    private int mCurrentViewPortX;
    private int mCurrentViewPortY;
    private int mCurrentViewPortWidth;
    private int mCurrentViewPortHeight;
    //=======手势部分end======


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
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        glViewport(0, 0, width, height);
        //设置视角，改变位置时用
        mCurrentViewPortX = 0;
        mCurrentViewPortY = 0;
        mCurrentViewPortWidth = Constant.mSurfaceViewWidth;
        mCurrentViewPortHeight = Constant.mSurfaceViewHeight;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);

        if (Constant.CURRENT_GESTURE_MODE == Constant.GESTURE_MODE_DRAG) {
            glViewport(mCurrentViewPortX, mCurrentViewPortY, mCurrentViewPortWidth,
                    mCurrentViewPortHeight);
            drawOnscreen();
        } else {
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
                glViewport(mCurrentViewPortX, mCurrentViewPortY, mCurrentViewPortWidth,
                        mCurrentViewPortHeight);
                drawOnscreen();
            }
        }
    }

    /**
     * 离屏渲染
     */
    private void drawOffscreen(int width, int height) {
        glViewport(0, 0, width, height);
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glClear(GL_COLOR_BUFFER_BIT);

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

        glEnable(GL_BLEND);//反正下面全部都要开启的

        switch (Constant.CURRENT_USE_TYPE) {
            case Constant.PAINT:
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                mRoot.draw(mPointProgram, mPointTexture);
                glDisable(GL_BLEND);
                break;

//            case Constant.FIREWORKS:
//                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
//                RendererUtil.CreateFireWorkTexture(mContext);
//                mRoot.draw(RendererUtil.CreateFireWorkProgram(mContext), RendererUtil
//                        .SelectFireWorkTexture());
//
//                glDisable(GL_BLEND);
//                break;

            case Constant.WALLPAPER:
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

                int[] mCurrentOtherTextureIndex = new int[1];
                mCurrentOtherTextureIndex[0] = RendererUtil.CreateChangeTexture(mContext);

                mRoot.draw(RendererUtil.CreateChangeProgram(mContext),
                        mCurrentOtherTextureIndex[0]);

                glDeleteProgram(Constant.CURRENT_OTHER_PROGRAM_INDEX);
                glDeleteTextures(1, mCurrentOtherTextureIndex, 0);

                glDisable(GL_BLEND);
                break;

            case Constant.MOSAIC:
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

                mEffectProgram = new MosaicTextureShaderProgram(mContext,
                        R.raw.mosaic_texture_shader_program);
                mRoot.drawMultiTexture(mEffectProgram, mTexture, mPointTexture);

                glDisable(GL_BLEND);
                break;

            case Constant.ERASER:
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

                mEraserProgram = new TextureShaderProgram(mContext, R.raw
                        .eraer_texture_shader_program);
                mRoot.draw(mEraserProgram, mPointTexture);

                glDisable(GL_BLEND);
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

        //每次将相片跟笔画混合在一起
        mTraceProgram.useProgram();
        if (mDrawLast || mDrawNext) {
            mTraceProgram.setUniforms(mTexture, mReturnTexture);//跟重做的纹理混合
        } else {
            mTraceProgram.setUniforms(mTexture, mTargetTexture);//跟当前屏幕对应的FBO的纹理混合
        }
        mBackGround.bindData(mTraceProgram);
        mBackGround.draw();
    }

    /**
     * 创建所需要的program
     */
    private void initProgram() {
        mTextureProgram = new TextureShaderProgram(mContext, R.raw.texture_fragment_shader);
        mPointProgram = new TextureShaderProgram(mContext, R.raw.point_texture_fragment_shader);

        mTraceProgram = new TraceTextureShaderProgram(mContext, R.raw.trace_texture_shader_program);

    }

    /**
     * 创建所需要的纹理
     */
    private void initTexture() {
        mPointTexture = TextureHelper.loadTexture(mContext, R.drawable.cover);
        mTexture = TextureHelper.loadOriginalTexture(mContext, mResourceId);
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

    public void freeGesturePointF(){
        mCurrentTouchPoint.x  =0;
        mCurrentTouchPoint.y=0;
        mLastTouchPoint.x= 0;
        mLastTouchPoint.y=0;
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

        drawOffscreen(Constant.TextureWidth, Constant.TextureHeight);

        GLES20.glBindFramebuffer(
                GLES20.GL_FRAMEBUFFER, 0);
    }

    @Override
    public boolean handleDragGesture(MotionEvent event) {
        //计算视角偏移,X,Y方向分开算。
        //只要算坐标原点就可以了，用这两个点改变就可以平移
        mCurrentViewPortX += mCurrentTouchPoint.x - mLastTouchPoint.x;
        mCurrentViewPortY -= mCurrentTouchPoint.y - mLastTouchPoint.y;

        mLastTouchPoint.x = mCurrentTouchPoint.x;
        mLastTouchPoint.y = mCurrentTouchPoint.y;
        Log.d("TAG", "LAST " + mLastTouchPoint.x + " LAST " + mLastTouchPoint.y);
        Log.d("TAG", "Current " + mCurrentTouchPoint.x + " Current " + mCurrentTouchPoint.y);


        mCurrentTouchPoint.x = event.getX();
        mCurrentTouchPoint.y = event.getY();

        return true;
    }

    @Override
    public boolean handlePinchGesture(MotionEvent event) {
        return false;
    }


}
