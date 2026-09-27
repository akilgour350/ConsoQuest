#ifndef SRC_WORLDGEN_H
#define SRC_WORLDGEN_H
#include <string>

#endif //SRC_WORLDGEN_H

using namespace std;

enum class Biome {
    OCEAN, COAST, PLAINS, FOREST, JUNGLE, DESERT, HILLS, MOUNTAINS, SWAMP, SNOWY_PLAINS, SNOWY_FOREST, SNOWY_HILLS
};

struct Tile {
    int x;
    int y;
    Biome biome;
    string structure;
};

struct PlayerStructure {
    int x;
    int y;
    string username;
    string cleared_at;
};

class WorldGen {
    public:
        explicit WorldGen(int seed);
        Tile generateTile(int x, int y);


        static string biomeToString(Biome biome);

    private:
        int worldSeed;

        float randomiser(int x, int y, int seed);
        float fadeGradient(float t);
        float linearGradient(float a, float b, float t);
        float noise(float x, float y, int seed);
        float fbm(float x, float y, int seed, int octaves = 6, float persistence = 0.5f, float lacunarity = 2.0f);
        Biome getBiome(float elevation, float temperature, float moisture);
        string generateStructure(Biome biome, int x, int y);

};