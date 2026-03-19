# 📑 Fiche de Révision : Les Bases d'OpenGL & LWJGL

## 🛠️ Lexique des Termes Techniques

| Terme | Définition simple | Analogie 💡 |
| :--- | :--- | :--- |
| **Buffer** | Un espace de mémoire brute (un "sac") utilisé pour stocker des données. | Un casier à la piscine. |
| **VBO** (Vertex Buffer Object) | Un buffer situé directement sur la **carte graphique** (VRAM) contenant les points (sommets) du jeu. | Un casier ultra-rapide situé juste à côté du bassin. |
| **Vertex** (Sommet) | Un point dans l'espace 3D. Il peut contenir une position, mais aussi une couleur ou une texture. | Un piquet de tente. |
| **Stride** (Pas) | La distance totale (en octets) entre le début d'un sommet et le début du suivant. | La taille d'une enjambée pour passer d'un groupe de données à l'autre. |
| **Offset** (Décalage) | Le nombre d'octets à sauter **au sein d'un même sommet** pour atteindre une information précise. | Le nombre de pas à faire à l'intérieur d'une chambre pour atteindre le lit. |
| **Normalized** | Action de ramener des valeurs dans une échelle standard (souvent entre -1.0 et 1.0 pour les positions). | Graduer une règle pour qu'elle fasse toujours 1 mètre, peu importe sa taille réelle. |



---

## 🚦 Les "Opérateurs" (Constantes & Modes)

Dans ton code, nous utilisons des constantes pour dire à OpenGL comment se comporter :

* **`GL_ARRAY_BUFFER`** : Indique que le buffer contient un tableau de données de sommets (positions, couleurs).
* **`GL_STATIC_DRAW`** : Indique à la carte graphique que nous allons envoyer les données **une seule fois** et les dessiner souvent (très rapide).
* **`GL_TRIANGLES`** : Le mode de dessin de base. OpenGL prend les sommets 3 par 3 pour créer des surfaces.

---

## 🔄 Le Pipeline : Le chemin des données

1.  **Java (CPU)** : On crée un tableau `float[]`.
2.  **Liaison (`glBindBuffer`)** : On "branche" le casier (VBO) pour travailler dessus.
3.  **Envoi (`glBufferData`)** : On pousse les données du CPU vers le GPU.
4.  **Lecture (`glVertexAttribPointer`)** : On donne la "grille de lecture" (Stride et Offset) à la carte graphique.
5.  **Dessin (`glDrawArrays`)** : On donne l'ordre final d'afficher les triangles.

