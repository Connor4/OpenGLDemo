attribute vec4 a_Position;
attribute vec2 a_TextureCoordinates;
attribute vec2 a_PointTextureCoordinates;

varying vec2 v_TextureCoordinates;
varying vec2 v_PointTextureCoordinates;
void main()
{
    v_TextureCoordinates = a_TextureCoordinates;
    v_PointTextureCoordinates = a_PointTextureCoordinates;

    gl_Position = a_Position;
}