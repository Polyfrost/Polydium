#version 330
#extension GL_ARB_separate_shader_objects : require
#ifndef TEXEL_FETCH
#extension GL_ARB_texture_gather : require
#endif

uniform sampler2D DepthSampler;

layout(push_constant) uniform PC {
    ivec4 u_Sizes;
};

layout(location = 0) out vec4 FragColor;

void main() {
    ivec2 target = ivec2(gl_FragCoord.xy);
    ivec2 start = target * u_Sizes.xy / u_Sizes.zw;
    ivec2 end = (target + 1) * u_Sizes.xy / u_Sizes.zw;
    float farthest = 1.0;

#ifdef TEXEL_FETCH
    for (int y = start.y; y < end.y; y++) {
        for (int x = start.x; x < end.x; x++) {
            farthest = min(farthest, texelFetch(DepthSampler, ivec2(x, y), 0).r);
        }
    }
#else
    vec2 texel = 1.0 / vec2(u_Sizes.xy);

    for (int y = start.y; y < end.y; y += 2) {
        for (int x = start.x; x < end.x; x += 2) {
            vec4 depths = textureGather(DepthSampler, (vec2(x, y) + 1.0) * texel);

            farthest = min(farthest, min(min(depths.x, depths.y), min(depths.z, depths.w)));
        }
    }
#endif

    FragColor = vec4(farthest, 0.0, 0.0, 0.0);
}
