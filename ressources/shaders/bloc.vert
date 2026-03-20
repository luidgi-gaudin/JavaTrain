#version 330 core

// Attributs d'entrée (configurés via glVertexAttribPointer dans le code Java)
layout (location = 0) in vec3 position; // La position (X, Y, Z)
layout (location = 1) in vec3 color;    // La couleur (R, G, B)

// Donnée transmise au Fragment Shader
out vec3 vertexColor;

uniform mat4 model; // Pour la rotation/position

uniform mat4 view; //gerer la vue

uniform mat4 projection; //mat 4 est une matrice en 4 par 4

void main() {
    // gl_Position est une variable intégrée qui définit la position finale du sommet

    //ce calcule permet de rajouter la profondeur a mon 4 2d pour le passer en cube (si java renvoie aucune profondeur le 4 existe pas car 0 de profondeur)
    gl_Position = projection * view * model * vec4(position, 1.0);
    // On transmet la couleur au prochain shader
    vertexColor = color;
}