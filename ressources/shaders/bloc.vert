#version 330 core

// Attributs d'entrée (configurés via glVertexAttribPointer dans le code Java)
layout (location = 0) in vec3 position; // La position (X, Y, Z)
layout (location = 1) in vec3 color;    // La couleur (R, G, B)

// Donnée transmise au Fragment Shader
out vec3 vertexColor;

void main() {
    // gl_Position est une variable intégrée qui définit la position finale du sommet
    gl_Position = vec4(position, 1.0);
    // On transmet la couleur au prochain shader
    vertexColor = color;
}