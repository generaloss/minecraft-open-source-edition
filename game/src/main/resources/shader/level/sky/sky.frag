#version 330

in vec4 fragCoord;

uniform vec4 u_skyColor;

void main(){
    vec4 color = u_skyColor;

    // Fog
    float hypot = sqrt(fragCoord.x * fragCoord.x + fragCoord.y * fragCoord.y + fragCoord.z * fragCoord.z);
    float angle = asin(fragCoord.y / hypot) / 3.1415 * 2;

    float fogMinAngle = 0;
    float fogMaxAngle = 0.5;
    float fogFactor = pow(1 - (fogMaxAngle - angle) / (fogMaxAngle - fogMinAngle), 1);

    color.a = fogFactor;

    // Result
    gl_FragColor = color;
}