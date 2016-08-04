package com.connor.myapplication.program;

import android.content.Context;

import com.connor.myapplication.data.Constant;

import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE1;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1i;

/**
 * Created by meitu on 2016/8/1.
 */
public class OtherTextureShaderProgram extends TextureShaderProgram {
    private int uTextureUnitLocation;
    private int aPositionLocation;
    private int aTextureCoordinatesLocation;

    public OtherTextureShaderProgram(Context context, int glsl) {
        super(context, glsl);

        aPositionLocation = glGetAttribLocation(program, A_POSITION);
        uTextureUnitLocation = glGetUniformLocation(program, U_TEXTURE_UNIT);

        aTextureCoordinatesLocation =
                glGetAttribLocation(program, A_TEXTURE_COORDINATES);

//        switch (Constant.CURRENT_USE_TYPE) {
//            case Constant.WALLPAPER:
                Constant.CURRENT_OTHERPROGRAM_INDEX = program;
//                break;
//            case Constant.FIREWORKS:
//                Constant.CURRENT_FIREWORKPROGARM_INDEX = program;
//                break;
//            default:
//                break;
//        }

    }

    public void setUniforms(int textureId) {
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureId);
        glUniform1i(uTextureUnitLocation, 0);
    }

    public int getPositionAttributeLocation() {
        return aPositionLocation;
    }

    public int getTextureCoordinatesAttributeLocation() {
        return aTextureCoordinatesLocation;
    }
}
