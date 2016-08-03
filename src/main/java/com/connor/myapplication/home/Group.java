package com.connor.myapplication.home;

import com.connor.myapplication.data.Constant;
import com.connor.myapplication.program.MosaicTextureShaderProgram;
import com.connor.myapplication.program.OtherTextureShaderProgram;
import com.connor.myapplication.program.TextureShaderProgram;

import java.util.LinkedList;

/**
 * 管理需要绘制的点
 */
public class Group extends Mesh {
    private final LinkedList<Mesh> mContainer = new LinkedList<>();//存放在屏幕上操作的对象
    private final LinkedList<Mesh> mOppositeContainer = new LinkedList<>();//存放在FBO上操作的对象

    /**
     * 选择对象的容器去绘制
     *
     * @param type       在哪里画
     * @param program    片段着色器
     * @param resourceId 那张cover图片的纹理
     */
    public void draw(int type, TextureShaderProgram program, int resourceId) {
        int size;
        switch (type) {
            case Constant.OnScreen:
                size = mContainer.size();
                for (int i = 0; i < size; i++) {
                    if (mContainer.peekFirst() != null) {
                        program.useProgram();
                        program.setUniforms(resourceId);
                        mContainer.peekFirst().bindData(program);
                        mContainer.peekFirst().draw();
                        mContainer.pollFirst();
                    }
                }
                break;
            case Constant.OffScreen:
                size = mOppositeContainer.size();
                for (int i = 0; i < size; i++) {
                    if (mOppositeContainer.peekFirst() != null) {
                        program.useProgram();
                        program.setUniforms(resourceId);
                        mOppositeContainer.peekFirst().bindData(program);
                        mOppositeContainer.peekFirst().draw();
                        mOppositeContainer.pollFirst();
                    }
                }
                break;
            default:
                break;
        }
    }

    public void drawMosaic(int type, MosaicTextureShaderProgram program, int resourceId, int pointId) {
        int size;
        switch (type) {
            case Constant.OnScreen:
                size = mContainer.size();
                for (int i = 0; i < size; i++) {
                    if (mContainer.peekFirst() != null) {
                        program.useProgram();
                        program.setUniforms(resourceId, pointId);
                        mContainer.peekFirst().bindData2(program);
                        mContainer.peekFirst().draw();
                        mContainer.pollFirst();
                    }
                }
                break;
            case Constant.OffScreen:
                size = mOppositeContainer.size();
                for (int i = 0; i < size; i++) {
                    if (mOppositeContainer.peekFirst() != null) {
                        program.useProgram();
                        program.setUniforms(resourceId, pointId);
                        mOppositeContainer.peekFirst().bindData2(program);
                        mOppositeContainer.peekFirst().draw();
                        mOppositeContainer.pollFirst();
                    }
                }
                break;
            default:
                break;
        }
    }


    /**
     * 添加对象进入屏幕使用的容器
     */
    public void addObject(Mesh object) {
        mContainer.add(object);
    }

    /**
     * 添加对象进入FBO使用的对象容器
     */
    public void addOppositeObject(Mesh object) {
        mOppositeContainer.add(object);
    }

    /**
     * 清除容器
     */
    public void clear() {
        mContainer.clear();
        mOppositeContainer.clear();
    }

    /**
     * 返回所选对象的大小
     *
     * @param type 选择类型
     * @return
     */
    public int size(int type) {
        switch (type) {
            case Constant.OnScreen:
                return mContainer.size();
            case Constant.OffScreen:
                return mOppositeContainer.size();
            default:
                break;
        }
        return -1;
    }
}