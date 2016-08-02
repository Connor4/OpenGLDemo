precision highp float;
uniform sampler2D u_TextureUnit;
uniform sampler2D u_TraceTextureUnit;

varying vec2 v_TextureCoordinates;

void main()
{
     vec3 textureColor =  texture2D(u_TextureUnit, v_TextureCoordinates).rgb;
     vec3 traceColor =  texture2D(u_TraceTextureUnit, v_TextureCoordinates).rgb;
//     float gray = dot(tracecolor.rgb, vec3(0.299, 0.587, 0.114));
     float r = 0.0;
     float g = 0.0;
     float b = 0.0;
     //tracecolor.r为1.0，一出现笔画就会只有tracecolor
     r = textureColor.r * (1.0 - traceColor.r) + traceColor.r;
     g = textureColor.g * (1.0 - traceColor.r)+ traceColor.g;
     b = textureColor.b * (1.0 - traceColor.r)+ traceColor.b;

     gl_FragColor = vec4(r,g,b, 1.0);
}
