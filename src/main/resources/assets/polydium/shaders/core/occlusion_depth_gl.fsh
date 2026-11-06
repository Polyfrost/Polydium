#version 330
#extension GL_ARB_texture_gather : enable

uniform sampler2D DepthSampler;

out vec4 fragColor;

const ivec2 TARGET_SIZE = ivec2(320, 180);

#ifdef PACKED_DEPTH
#define FARTHEST max
#define NEAREST_DEPTH 0.0
#else
#define FARTHEST min
#define NEAREST_DEPTH 1.0
#endif

void main() {
    ivec2 sourceSize = textureSize(DepthSampler, 0);
    ivec2 target = ivec2(gl_FragCoord.xy);
    ivec2 start = target * sourceSize / TARGET_SIZE;
    ivec2 end = (target + 1) * sourceSize / TARGET_SIZE;
    float farthest = NEAREST_DEPTH;

#ifdef GL_ARB_texture_gather
    vec2 texel = 1.0 / vec2(sourceSize);

    for (int y = start.y; y < end.y; y += 2) {
        for (int x = start.x; x < end.x; x += 2) {
            vec4 depths = textureGather(DepthSampler, (vec2(x, y) + 1.0) * texel);

            farthest = FARTHEST(farthest, FARTHEST(FARTHEST(depths.x, depths.y), FARTHEST(depths.z, depths.w)));
        }
    }
#else
    for (int y = start.y; y < end.y; y++) {
        for (int x = start.x; x < end.x; x++) {
            farthest = FARTHEST(farthest, texelFetch(DepthSampler, ivec2(x, y), 0).r);
        }
    }
#endif

#ifdef PACKED_DEPTH
    uint bits = floatBitsToUint(1.0 - farthest);
    fragColor = vec4(bits & 0xFFu, (bits >> 8) & 0xFFu, (bits >> 16) & 0xFFu, bits >> 24) / 255.0;
#else
    fragColor = vec4(farthest, 0.0, 0.0, 0.0);
#endif
}
