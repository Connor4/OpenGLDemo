precision highp float;
uniform sampler2D u_TextureUnit;

varying vec2 v_TextureCoordinates;
void main()
{
       vec4 textureColor = texture2D(u_TextureUnit, v_TextureCoordinates);
       float gray = dot(textureColor.rgb, vec3(0.299, 0.587, 0.114));
       gl_FragColor = vec4(textureColor.rgb, gray);
}
