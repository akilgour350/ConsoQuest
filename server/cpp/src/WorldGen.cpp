#include "WorldGen.h"
#include <cmath>

using namespace std;

WorldGen::WorldGen(int seed) : worldSeed(seed) {}

float WorldGen::randomiser(int x, int y, int seed) {
    int n = x + y * 57 + seed * 131;
    n = (n << 13) ^ n;
    return (1.0f - ((n * (n * n * 15731 + 789221) + 1376312589) & 0x7fffffff) / 1073741824.0f);
}

float WorldGen::fadeGradient(float t) {
    return t * t * t * (t * (t * 6.0f - 15.0f) + 10.0f);
}

float WorldGen::linearGradient(float a, float b, float t) {
    return a + t * (b - a);
}

float WorldGen::noise(float x, float y, int seed) {
    int xi = (int)floor(x);
    int yi = (int)floor(y);
    float xf = x - xi;
    float yf = y - yi;
    float u = fadeGradient(xf);
    float v = fadeGradient(yf);

    float aa = randomiser(xi,     yi,     seed);
    float ab = randomiser(xi,     yi + 1, seed);
    float ba = randomiser(xi + 1, yi,     seed);
    float bb = randomiser(xi + 1, yi + 1, seed);

    float x1 = linearGradient(aa, ba, u);
    float x2 = linearGradient(ab, bb, u);
    return linearGradient(x1, x2, v);
}

float WorldGen::fbm(float x, float y, int seed, int octaves, float persistence, float lacunarity) {
    float value     = 0.0f;
    float amplitude = 1.0f;
    float frequency = 1.0f;
    float maxValue  = 0.0f;

    for (int i = 0; i < octaves; i++) {
        value    += noise(x * frequency, y * frequency, seed + i) * amplitude;
        maxValue += amplitude;
        amplitude *= persistence;
        frequency *= lacunarity;
    }

    return (value / maxValue + 1.0f) * 0.5f;
}

Biome WorldGen::getBiome(float elevation, float temperature, float moisture) {
    if (elevation < 0.35f) return Biome::OCEAN;
    if (elevation < 0.4f)  return Biome::COAST;

    if (elevation > 0.75f) {
        if (temperature < 0.3f) return Biome::SNOWY_HILLS;
        return Biome::MOUNTAINS;
    }

    if (elevation > 0.55f) {
        if (temperature < 0.3f) return Biome::SNOWY_HILLS;
        return Biome::HILLS;
    }

    if (temperature < 0.2f) {
        if (moisture > 0.5f) return Biome::SNOWY_FOREST;
        return Biome::SNOWY_PLAINS;
    }

    if (moisture > 0.6f) {
        if (temperature > 0.65f) return Biome::JUNGLE;
        return Biome::FOREST;
    }

    if (moisture > 0.4f && moisture < 0.6f && temperature < 0.4f) return Biome::SWAMP;

    if (moisture < 0.3f) return Biome::DESERT;

    return Biome::PLAINS;
}

string WorldGen::generateStructure(Biome biome, int x, int y) {
    float roll = fbm(x * 0.05f, y * 0.05f, worldSeed + 3, 2, 0.5f, 2.0f);

    if (roll > 0.92f) {
        switch (biome) {
            case Biome::MOUNTAINS:    return "CAVE";
            case Biome::HILLS:        return "CAVE";
            case Biome::FOREST:       return roll > 0.96f ? "DUNGEON" : "RUINS";
            case Biome::PLAINS:       return "VILLAGE";
            case Biome::DESERT:       return "RUINS";
            case Biome::JUNGLE:       return "DUNGEON";
            case Biome::SNOWY_PLAINS: return "RUINS";
            case Biome::SNOWY_FOREST: return "CAVE";
            case Biome::SWAMP:        return "CAVE";
            default:                  return "NONE";
        }
    }
    return "NONE";
}

string WorldGen::biomeToString(Biome biome) {
    switch (biome) {
        case Biome::OCEAN:        return "OCEAN";
        case Biome::COAST:        return "COAST";
        case Biome::PLAINS:       return "PLAINS";
        case Biome::FOREST:       return "FOREST";
        case Biome::JUNGLE:       return "JUNGLE";
        case Biome::DESERT:       return "DESERT";
        case Biome::HILLS:        return "HILLS";
        case Biome::MOUNTAINS:    return "MOUNTAINS";
        case Biome::SWAMP:        return "SWAMP";
        case Biome::SNOWY_PLAINS: return "SNOWY_PLAINS";
        case Biome::SNOWY_FOREST: return "SNOWY_FOREST";
        case Biome::SNOWY_HILLS:  return "SNOWY_HILLS";
        default:                  return "UNKNOWN";
    }
}

Tile WorldGen::generateTile(int x, int y) {
    float scale = 0.008f;

    float elevation = fbm(x * scale, y * scale, worldSeed, 6, 0.5f, 2.0f);
    float temperature = fbm(x * scale + 1000.0f, y * scale + 1000.0f, worldSeed + 1, 4, 0.6f, 2.0f);
    float moisture = fbm(x * scale + 2000.0f, y * scale + 2000.0f, worldSeed + 2, 4, 0.5f, 2.0f);

    Tile tile;
    tile.x = x;
    tile.y = y;
    tile.biome = getBiome(elevation, temperature, moisture);
    tile.structure = generateStructure(tile.biome, x, y);

    return tile;
}