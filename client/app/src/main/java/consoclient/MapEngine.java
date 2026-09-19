package consoclient;

import java.util.HashMap;

// this class will be server side eventually, but for now we're doing it here til we know the logic works
// later can be used to cache map tiles
public final class MapEngine {
    private HashMap<Coordinate, MapTile> map;
    public MapEngine() {
        map = new HashMap<>();
    }

    /// Gets or creates a map tile for the given co-ordinates
    /// @param x x-coordinate of the tile
    /// @param y y-coordinate of the tile
    /// @return MapTile object of the retrieved or generated tile
    public MapTile getMapTile(int x, int y) {
        MapTile mt = map.get(new Coordinate(x, y));

        if (mt == null) { // here is where new tiles will be generated
            mt = new MapTile(x, y);
            map.put(mt.coordinate, mt);
        }

        return mt;
    }
}
