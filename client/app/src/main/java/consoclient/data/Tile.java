package consoclient.data;

public class Tile {
    public int x;
    public int y;
    public String biome;
    public String structure;

    public Tile(int x, int y, String biome, String structure) {
        this.x = x;
        this.y = y;
        this.biome = biome;
        this.structure = structure;
    }
}