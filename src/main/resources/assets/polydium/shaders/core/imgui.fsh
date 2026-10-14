#version 330

uniform sampler2D Sampler0;

in vec4 vertexColor;
in vec2 texCoord;

out vec4 fragColor;

void main() {
    fragColor = vertexColor * texture(Sampler0, texCoord);
}
