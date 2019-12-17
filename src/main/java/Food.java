public class Food implements IMapElement {
    private Vector2D position;
    private int energy ;

    public Food(Vector2D position, int energy) {
        this.position = position;
        this.energy = energy;
    }

    public int getEnergy() { return this.energy; }

    @Override
    public Vector2D getPosition() { return position; }

    public String toString() {
        return "x";
    }
}
