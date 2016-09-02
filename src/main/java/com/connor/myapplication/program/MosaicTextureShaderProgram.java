package com.connor.myapplication.program;

import android.content.Context;

import com.connor.myapplication.R;

import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE1;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1i;

/**
 * Created by meitu on 2016/8/3.
 */
public class MosaicTextureShaderProgram extends ShaderProgram {
    private static final String U_POINT_TEXTURE_UNIT = "u_PointTextureUnit";
    private static final String A_POINT_TEXTURE_COORDINATES = "a_PointTextureCoordinates";
    private int aPositionLocation;
    private int uTextureUnitLocation;
    private int uPointTextureUnitLocation;
    private int aTextureCoordinatesLocation;
    private int aPointTextureCoordinatesLocation;

    public MosaicTextureShaderProgram(Context context, int glsl) {
        super(context, R.raw.multiple_texture_vertex_shader, glsl);

        aPositionLocation = glGetAttribLocation(program, A_POSITION);

        uTextureUnitLocation = glGetUniformLocation(program, U_TEXTURE_UNIT);
        uPointTextureUnitLocation = glGetUniformLocation(program, U_POINT_TEXTURE_UNIT);

        aTextureCoordinatesLocation =
                glGetAttribLocation(program, A_TEXTURE_COORDINATES);
        aPointTextureCoordinatesLocation = glGetAttribLocation(program,
                A_POINT_TEXTURE_COORDINATES);
    }

    public void setUniforms(int textureId, int pointId) {
        glUniform1i(uTextureUnitLocation, 0);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureId);

        glUniform1i(uPointTextureUnitLocation, 1);
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, pointId);

    }

    public int getPositionAttributeLocation() {
        return aPositionLocation;
    }

    public int getTextureCoordinatesAttributeLocation() {
        return aTextureCoordinatesLocation;
    }

    public int getPointTextureCoordinatesAttributeLocation() {
        return aPointTextureCoordinatesLocation;
    }
}
