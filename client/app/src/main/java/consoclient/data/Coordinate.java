package consoclient.data;

import java.util.Objects;

public class Coordinate {
    public int x;
    public int y;

    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) { // overrides equals so we can compare coordinates based on values rather than just object references
        if (this == o) { // if the object references match, return true
            return true;
        } else if (!(o instanceof Coordinate)) { // in case the passed object isn't a coordinate object
            return false;
        }

        // the new logic
        Coordinate coord = (Coordinate) o;
        return this.x == coord.x && this.y == coord.y;
    }

    @Override
    public int hashCode() { // has to be overridden so hashmap.get knows where to look
        return Objects.hash(x, y);
    }
}
