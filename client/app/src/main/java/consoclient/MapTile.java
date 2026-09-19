package consoclient;

import java.util.Random;

public class MapTile {
    public Coordinate coordinate;
    public Biome biome;
    public Structure structure;
    public NPC npc;

    public MapTile(Coordinate coordinate, Biome biome, Structure structure, NPC npc) {
        this.coordinate = coordinate;
        this.biome = biome;
        this.structure = structure;
        this.npc = npc;
    }

    /// Constructor that randomly generates a map tile (no weighting or logic just yet)
    /// @param x x-coordinate of the tile
    /// @param y y-coordinate of the tile
    public MapTile(int x, int y) {
        this.coordinate = new Coordinate(x, y);

        Random random = new Random();
        this.biome = Biome.values()[random.nextInt(Biome.values().length)];
        this.structure = Structure.values()[random.nextInt(Structure.values().length)];
        this.npc = NPC.values()[random.nextInt(NPC.values().length)];
    }
}