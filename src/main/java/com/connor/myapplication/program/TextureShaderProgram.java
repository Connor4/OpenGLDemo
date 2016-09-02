package com.connor.myapplication.program;

import android.content.Context;

import com.connor.myapplication.R;

import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1i;

/**
 * Created by meitu on 2016/7/5.
 */
public class TextureShaderProgram extends ShaderProgram {
    private int aPositionLocation;
    private int uTextureUnitLocation;
    private int aTextureCoordinatesLocation;

    public TextureShaderProgram(Context context, int glsl) {
        super(context, R.raw.texture_vertex_shader, glsl);

        aPositionLocation = glGetAttribLocation(program, A_POSITION);
        aTextureCoordinatesLocation =
                glGetAttribLocation(program, A_TEXTURE_COORDINATES);
        uTextureUnitLocation = glGetUniformLocation(program, U_TEXTURE_UNIT);
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
