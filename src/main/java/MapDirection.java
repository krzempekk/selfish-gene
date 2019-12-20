public enum MapDirection {
    NORTH(0),
    NORTHEAST(1),
    EAST(2),
    SOUTHEAST(3),
    SOUTH(4),
    SOUTHWEST(5),
    WEST(6),
    NORTHWEST(7);

    private int order;

    MapDirection(int order) {
        this.order = order;
    }

    public int getOrder() {
        return this.order;
    }

    public MapDirection turn(int angle) {
        return MapDirection.values()[(this.getOrder() + angle) % 8];
    }

    public Vector2D toUnitVector() {
        switch(this) {
            case NORTH: return new Vector2D(0, 1);
            case NORTHEAST: return new Vector2D(1, 1);
            case EAST: return new Vector2D(1, 0);
            case SOUTHEAST: return new Vector2D(1, -1);
            case SOUTH: return new Vector2D(0, -1);
            case SOUTHWEST: return new Vector2D(-1, -1);
            case WEST: return new Vector2D(-1, 0);
            case NORTHWEST: return new Vector2D(-1, 1);
            default: return new Vector2D(0, 0);
        }
    }
}