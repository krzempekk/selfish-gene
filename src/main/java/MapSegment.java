public class MapSegment {
    public Vector2D lowerLeft, upperRight;
    public PlaceType type;

    public MapSegment(Vector2D lowerLeft, Vector2D upperRight, PlaceType type) {
        this.lowerLeft = lowerLeft;
        this.upperRight = upperRight;
        this.type = type;
    }

    public boolean positionBelongs(Vector2D position) {
        return lowerLeft.precedes(position) && upperRight.follows(position);
    }
}
