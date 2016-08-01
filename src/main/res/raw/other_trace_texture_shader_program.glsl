precision highp float;
uniform sampler2D u_TextureUnit;
uniform sampler2D u_TraceTextureUnit;

varying vec2 v_TextureCoordinates;

void main()
{
     vec3 texturecolor =  texture2D(u_TextureUnit, v_TextureCoordinates).rgb;
     vec3 tracecolor =  texture2D(u_TraceTextureUnit, v_TextureCoordinates).rgb;
     float r = 0.0;
     float g = 0.0;
     float b = 0.0;
     r = texturecolor.r * 1.0 + tracecolor.r;
     g = texturecolor.g *1.0+ tracecolor.g;
     b = texturecolor.b * 1.0+ tracecolor.b;

     gl_FragColor = vec4(r,g,b, 1.0);
}
