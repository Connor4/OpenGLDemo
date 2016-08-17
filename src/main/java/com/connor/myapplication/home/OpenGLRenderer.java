package com.connor.myapplication.home;

import android.content.Context;
import android.graphics.PointF;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.view.MotionEvent;

import com.connor.myapplication.R;
import com.connor.myapplication.data.Constant;
import com.connor.myapplication.program.MosaicTextureShaderProgram;
import com.connor.myapplication.program.TextureHelper;
import com.connor.myapplication.program.TextureShaderProgram;
import com.connor.myapplication.program.TraceTextureShaderProgram;
import com.connor.myapplication.util.FBOArrayUtil;
import com.connor.myapplication.util.PictureUtil;
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
    private PointF mDragMidPoint = new PointF();
    private PointF mLastDragMidPoint = new PointF();
    private PointF mZoomDragMidPoint = new PointF();
    private PointF mZoomLastDragMidPoint = new PointF();
    private PointF mZoomMidPoint = new PointF();
    private float mNewDist = 0f, mOldDist = 0f;
    private float mZoom = 0f;
    private float mTranslateX = 0, mTranslateY = 0, mScaleX = 1, mScaleY = 1;

    private final float[] projectionMatrix = new float[16];
    private final float[] modelMatrix = new float[16];
    private final float[] temp = new float[16];
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
        Matrix.orthoM(projectionMatrix, 0, -1f, 1f, -1f, 1f, -1f, 1f);
    }


    @Override
    public void onDrawFrame(GL10 gl) {
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

        if (Constant.CURRENT_GESTURE_MODE == Constant.GESTURE_MODE_DRAGANDZOOM) {
            Matrix.setIdentityM(modelMatrix, 0);
            Matrix.translateM(modelMatrix, 0, mTranslateX, mTranslateY, 0);
            Matrix.scaleM(modelMatrix, 0, mScaleX, mScaleY, 0);
            Matrix.multiplyMM(temp, 0, projectionMatrix, 0, modelMatrix, 0);
            System.arraycopy(temp, 0, projectionMatrix, 0, temp.length);

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
                glViewport(0, 0, Constant.mSurfaceViewWidth, Constant.mSurfaceViewHeight);
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

            case Constant.FIREWORKS:
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

                mRoot.draw(RendererUtil.CreateFireWorkProgram(mContext), RendererUtil
                        .SelectFireWorkTexture());

                glDisable(GL_BLEND);
                break;

            case Constant.WALLPAPER:
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

                int[] mCurrentOtherTextureIndex = new int[1];
                mCurrentOtherTextureIndex[0] = RendererUtil.CreateChangeTexture(mContext);

                mRoot.draw(RendererUtil.CreateChangeProgram(mContext),
                        mCurrentOtherTextureIndex[0]);

                glDeleteProgram(Constant.CURRENT_OTHER_PROGRAM_INDEX);//删除program
                glDeleteTextures(1, mCurrentOtherTextureIndex, 0);//删除纹理

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
            mTraceProgram.setUniforms(projectionMatrix, mTexture, mReturnTexture);//跟重做的纹理混合
        } else {
            mTraceProgram.setUniforms(projectionMatrix, mTexture, mTargetTexture);//跟当前屏幕对应的FBO的纹理混合
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
        RendererUtil.CreateFireWorkTexture(mContext);//不应该放这里
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

    /**
     * 释放记录缩放平移位置的数据
     */
    public void freeGestureStatu() {
        //=======缩放======
        mZoom = 0;
        mNewDist = 0;
        mOldDist = 0;
        mScaleX = mScaleY = 1;
        mZoomDragMidPoint.x = mZoomLastDragMidPoint.x = 0;
        mZoomDragMidPoint.y = mZoomLastDragMidPoint.y = 0;
        //=======平移========
        mDragMidPoint.x = mDragMidPoint.y = 0;
        mLastDragMidPoint.x = mLastDragMidPoint.y = 0;
        //传递投影矩阵给这个工具类计算偏移量
        PictureUtil.projectionMatrix = projectionMatrix;
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

    //===================手势部分start========================

    /**
     * 用两指的中点计算偏移量
     */
    @Override
    public boolean handleDragGesture(MotionEvent event) {
        mLastDragMidPoint.x = mDragMidPoint.x;
        mLastDragMidPoint.y = mDragMidPoint.y;
        midPoint(mDragMidPoint, event);

        if (mLastDragMidPoint.x != 0 && mLastDragMidPoint.y != 0) {
            //因为平移变换是针对模型矩阵变换，放大缩小之后移动就会因为手指移动距离跟屏幕上移动的距离不再对应
            //而显得手指移动的距离跟图片移动距离不同，所以除以投影矩阵中当前的缩放大小，变回等价
            //的距离就可以了
            mTranslateX = Xdistance(mDragMidPoint.x, mLastDragMidPoint.x) / projectionMatrix[0];
            mTranslateY = Ydistance(mDragMidPoint.y, mLastDragMidPoint.y) / projectionMatrix[5];
        }
        return true;
    }

    @Override
    public boolean handlePinchGesture(MotionEvent event) {
        mOldDist = mNewDist;
        mNewDist = spacing(event);
        mZoom = mNewDist / mOldDist;

        mZoomLastDragMidPoint.x = mZoomDragMidPoint.x;
        mZoomLastDragMidPoint.y = mZoomDragMidPoint.y;
        midPoint(mZoomDragMidPoint, event);

        if (mZoom != Float.POSITIVE_INFINITY) {//第一次mOldDist = 0时，mZoom会为infinity
            midPoint(mZoomMidPoint, event);
            mScaleX = mScaleY = mZoom;
            //这里还添加一个平移的变化，因为缩放过程中也需要通过平移
            mTranslateX = Xdistance(mZoomDragMidPoint.x, mZoomLastDragMidPoint.x) /
                    projectionMatrix[0];
            mTranslateY = Ydistance(mZoomDragMidPoint.y, mZoomLastDragMidPoint.y) /
                    projectionMatrix[5];
        }
        return true;
    }

    /**
     * 求pointID0和1之间的距离
     */
    private float spacing(MotionEvent event) {
        float x = 0;
        float y = 0;
        try {
            x = event.getX(0) - event.getX(1);
            y = event.getY(0) - event.getY(1);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * 求中点，用于缩放用
     */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    /**
     * 世界坐标换算成OpenGL坐标再计算距离
     */
    private float Xdistance(float p1, float p2) {
        float glX1 = (p1 / (float) Constant.mSurfaceViewWidth) * 2 - 1;
        float glX2 = (p2 / (float) Constant.mSurfaceViewWidth) * 2 - 1;
        return glX1 - glX2;
    }

    /**
     * 世界坐标换算成OpenGL坐标再计算距离
     */
    private float Ydistance(float p1, float p2) {
        float glY1 = 1 - (p1 / (float) Constant.mSurfaceViewHeight) * 2;
        float glY2 = 1 - (p2 / (float) Constant.mSurfaceViewHeight) * 2;
        return glY1 - glY2;
    }


    //===================手势部分end========================

}
