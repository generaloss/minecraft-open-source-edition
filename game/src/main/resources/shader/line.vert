#version 330

in vec4 a_position;

uniform mat4 u_combined;
uniform mat4 u_model;

void main() {
    gl_Position = u_combined * u_model * a_position;
}
