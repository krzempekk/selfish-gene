public class MapSegment {
    private Vector2D lowerLeft, upperRight;
    private PlaceType type;

    public MapSegment(Vector2D lowerLeft, Vector2D upperRight, PlaceType type) {
        this.lowerLeft = lowerLeft;
        this.upperRight = upperRight;
        this.type = type;
    }

    public boolean positionBelongs(Vector2D position) {
        return this.lowerLeft.precedes(position) && this.upperRight.follows(position);
    }

    public PlaceType getType() { return this.type; }
}
