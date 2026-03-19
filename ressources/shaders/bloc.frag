#version 330 core

in vec3 vertexColor; // On reçoit la couleur qui vient du Vertex Shader
out vec4 fragColor;  // C'est la couleur finale affichée

void main() {
    fragColor = vec4(vertexColor, 1.0);
}