package fr.minecraft.physiques;

public record AABB(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {

    public boolean chevauche(AABB o) {
        return maxX > o.minX && minX < o.maxX
            && maxY > o.minY && minY < o.maxY
            && maxZ > o.minZ && minZ < o.maxZ;
    }

    public AABB translate(float dx, float dy, float dz) {
        return new AABB(minX+dx, minY+dy, minZ+dz, maxX+dx, maxY+dy, maxZ+dz);
    }

    /** Returns clipped dx that won't penetrate `other` (YZ overlap assumed to already be checked outside). */
    public float sweepX(AABB other, float dx) {
        if (maxY <= other.minY || minY >= other.maxY) return dx;
        if (maxZ <= other.minZ || minZ >= other.maxZ) return dx;
        if (dx > 0 && maxX <= other.minX) return Math.min(dx, other.minX - maxX);
        if (dx < 0 && minX >= other.maxX) return Math.max(dx, other.maxX - minX);
        return 0;
    }

    public float sweepY(AABB other, float dy) {
        if (maxX <= other.minX || minX >= other.maxX) return dy;
        if (maxZ <= other.minZ || minZ >= other.maxZ) return dy;
        if (dy > 0 && maxY <= other.minY) return Math.min(dy, other.minY - maxY);
        if (dy < 0 && minY >= other.maxY) return Math.max(dy, other.maxY - minY);
        return 0;
    }

    public float sweepZ(AABB other, float dz) {
        if (maxX <= other.minX || minX >= other.maxX) return dz;
        if (maxY <= other.minY || minY >= other.maxY) return dz;
        if (dz > 0 && maxZ <= other.minZ) return Math.min(dz, other.minZ - maxZ);
        if (dz < 0 && minZ >= other.maxZ) return Math.max(dz, other.maxZ - minZ);
        return 0;
    }
}
