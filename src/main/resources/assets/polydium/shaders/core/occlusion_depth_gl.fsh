#version 330

uniform sampler2D DepthSampler;

out vec4 fragColor;

const ivec2 TARGET_SIZE = ivec2(320, 180);

void main() {
    ivec2 sourceSize = textureSize(DepthSampler, 0);
    ivec2 target = ivec2(gl_FragCoord.xy);
    ivec2 start = target * sourceSize / TARGET_SIZE;
    ivec2 end = (target + 1) * sourceSize / TARGET_SIZE;

#ifdef PACKED_DEPTH
    float farthest = 0.0;

    for (int y = start.y; y < end.y; y++) {
        for (int x = start.x; x < end.x; x++) {
            farthest = max(farthest, texelFetch(DepthSampler, ivec2(x, y), 0).r);
        }
    }

    uint bits = floatBitsToUint(1.0 - farthest);
    fragColor = vec4(bits & 0xFFu, (bits >> 8) & 0xFFu, (bits >> 16) & 0xFFu, bits >> 24) / 255.0;
#else
    float farthest = 1.0;

    for (int y = start.y; y < end.y; y++) {
        for (int x = start.x; x < end.x; x++) {
            farthest = min(farthest, texelFetch(DepthSampler, ivec2(x, y), 0).r);
        }
    }

    fragColor = vec4(farthest, 0.0, 0.0, 0.0);
#endif
}
