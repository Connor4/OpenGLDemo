attribute vec4 a_Position;
attribute vec2 a_TextureCoordinates;

varying vec2 v_TextureCoordinates;//灰度处理纹理的顶点
void main()
{
    v_TextureCoordinates = a_TextureCoordinates;

    gl_Position =  a_Position;
}