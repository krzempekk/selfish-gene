import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class Animal implements IMapElement {
    public Genome genome;
    private Vector2D position;
    private MapDirection direction;
    private WorldMap worldMap;
    private PropertyChangeSupport support;
    public int energy;
    public int minEnergyToReproduce;
    public static final double REPRODUCTION_ENERGY_LOSS = 0.25;
    private int moveEnergy;
    public int epochBorn;
    public int childCount;
    public boolean isSuccessor;

    public Animal(Vector2D position, WorldMap worldMap, int energy, int moveEnergy) {
        this(new Genome(), position, worldMap, energy, moveEnergy, false);
    }

    public Animal(Genome genome, Vector2D position, WorldMap worldMap, int energy, int moveEnergy, boolean isSuccessor) {
        this.genome = genome;
        this.position = position;
        this.direction = MapDirection.values()[Utils.randomInt(0, 7)];
        this.worldMap = worldMap;
        this.support = new PropertyChangeSupport(this);
        this.energy = energy;
        this.minEnergyToReproduce = energy/2;
        this.moveEnergy = moveEnergy;
        this.epochBorn = worldMap.epoch;
        this.childCount = 0;
        this.isSuccessor = isSuccessor;
    }

    private void turn() {
         int angle = this.genome.sequence.get(Utils.randomInt(0, Genome.SEQ_LEN - 1));
         this.direction = this.direction.turn(angle);
    }

    public void move() {
        this.turn();
        Vector2D newPosition = this.worldMap.calculateNewPosition(this.position, direction.toUnitVector());
        support.firePropertyChange("position", this.position, newPosition);
        this.position = newPosition;
        this.energy -= this.moveEnergy;
    }

    public void changeEnergy(int energy) {
        this.energy += energy;
    }

    public Animal reproduceWith(Animal partner) {
        Vector2D newPosition = worldMap.getAdjacentFreePosition(this.getPosition());
        if(newPosition != null) {
            Genome newGenome = this.genome.mix(partner.genome);

            int newEnergy = (int)((this.energy + partner.energy) * REPRODUCTION_ENERGY_LOSS);
            this.energy *= (1 - REPRODUCTION_ENERGY_LOSS);
            partner.energy *= (1 - REPRODUCTION_ENERGY_LOSS);

            this.childCount++;
            partner.childCount++;

            boolean isChildSuccessor = this.isSuccessor || partner.isSuccessor;

            return new Animal(newGenome, newPosition, worldMap, newEnergy, this.moveEnergy, isChildSuccessor);
        }
        return null;
    }

    @Override
    public Vector2D getPosition() {
        return position;
    }

    public MapDirection getDirection() {
        return direction;
    }

    public int getEnergy() { return energy; }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        support.addPropertyChangeListener(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        support.removePropertyChangeListener(pcl);
    }

    @Override
    public String toString() {
        return "A";
    }
}

