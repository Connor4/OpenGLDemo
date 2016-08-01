precision highp float;
uniform sampler2D u_TextureUnit;
uniform sampler2D u_TraceTextureUnit;

varying vec2 v_TextureCoordinates;

void main()
{
     vec3 texturecolor =  texture2D(u_TextureUnit, v_TextureCoordinates).rgb;
     vec3 tracecolor =  texture2D(u_TraceTextureUnit, v_TextureCoordinates).rgb;
      float grey = dot(tracecolor.rgb, vec3(0.299, 0.587, 0.114));
     float r = 0.0;
     float g = 0.0;
     float b = 0.0;
     //tracecolor.r为1.0，一出现笔画就会只有tracecolor
     r = texturecolor.r * (1.0 - tracecolor.r - tracecolor.g - tracecolor.b) + tracecolor.r;
     g = texturecolor.g * (1.0 - tracecolor.r - tracecolor.g - tracecolor.b)+ tracecolor.g;
     b = texturecolor.b * (1.0 - tracecolor.r - tracecolor.g - tracecolor.b)+ tracecolor.b;
//     if(gray>0.1){
//        r = tacecolor.r;
//        g = tracecolor.g;
//        b = tracecolor.b;
//     }else{
//         r = texturecolor.r;
//         g = texturecolor.g;
//         b = texturecolor.b;
//     }

       gl_FragColor = vec4(r,g,b, 1.0);
}
