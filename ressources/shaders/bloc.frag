#version 330 core

// vertexColor est interpolé entre les sommets des triangles
in vec3 vertexColor; // Reçu du Vertex Shader

// fragColor est la couleur de sortie finale du pixel (RGBA)
out vec4 fragColor;

void main() {
    // On définit la couleur finale du pixel
    // Le 1.0 à la fin est l'Alpha (opacité)
    fragColor = vec4(vertexColor, 1.0);
}