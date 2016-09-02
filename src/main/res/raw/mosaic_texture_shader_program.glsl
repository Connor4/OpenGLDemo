precision highp float;
uniform sampler2D u_TextureUnit;
uniform sampler2D u_PointTextureUnit;

varying vec2 v_TextureCoordinates;
varying vec2 v_PointTextureCoordinates;

void main()
{
     float pixel = 1.0;
     float imageWidthFactor = 0.01;
     float imageHeightFactor = 0.01;
     vec4 pointcolor = texture2D(u_PointTextureUnit, v_PointTextureCoordinates);
     vec2 uv  = v_TextureCoordinates.xy;
     float dx = pixel * imageWidthFactor;
     float dy = pixel * imageHeightFactor;
     vec2 coord = vec2(dx * floor(uv.x / dx), dy * floor(uv.y / dy));
     vec3 tc = texture2D(u_TextureUnit, coord).rgb;
     gl_FragColor = vec4(tc, pointcolor.r);
}
