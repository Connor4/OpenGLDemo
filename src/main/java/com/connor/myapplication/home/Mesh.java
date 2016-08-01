package com.connor.myapplication.home;

import com.connor.myapplication.program.TextureShaderProgram;

/**
 * Created by meitu on 2016/7/8.
 */
abstract class Mesh {
    public void draw() {};

    public void bindData(TextureShaderProgram program) {};

}
