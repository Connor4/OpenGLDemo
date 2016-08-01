precision highp float;
uniform sampler2D u_TextureUnit;

varying vec2 v_TextureCoordinates;//灰度图的传过来的顶点
void main()
{
       vec4 backcolor = texture2D(u_TextureUnit, v_TextureCoordinates);
       gl_FragColor = vec4(0.0, 0.0, 0.0, backcolor.r);
}