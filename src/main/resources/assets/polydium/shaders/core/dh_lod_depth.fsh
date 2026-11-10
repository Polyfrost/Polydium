#version 330
#extension GL_ARB_separate_shader_objects : require

uniform sampler2D DhDepthSampler;

layout(location = 0) out vec4 FragColor;

void main() {
    float depth = texelFetch(DhDepthSampler, ivec2(gl_FragCoord.xy), 0).r;
    if (depth <= 0.0 || depth >= 1.0) {
        discard;
    }

    FragColor = vec4(0.0);
#ifdef REVERSED_DEPTH
    gl_FragDepth = 4e-6;
#else
    gl_FragDepth = 1.0 - 4e-6;
#endif
}
