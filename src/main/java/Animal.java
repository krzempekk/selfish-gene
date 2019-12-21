import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class Animal implements IMapElement {
    private Genome genome;
    private Vector2D position;
    private MapDirection direction;
    private WorldMap worldMap;
    private PropertyChangeSupport support;
    private int energy;
    private static final double REPRODUCTION_ENERGY_LOSS = 0.25;
    private int moveEnergy;
    private int epochBorn;
    private int childCount;
    private boolean isSuccessorOfTracked;
    private boolean isChildOfTracked;
    private boolean isTracked;

    public Animal(Vector2D position, WorldMap worldMap, int energy, int moveEnergy) {
        this(new Genome(), position, worldMap, energy, moveEnergy, false, false);
    }

    public Animal(Genome genome, Vector2D position, WorldMap worldMap, int energy, int moveEnergy, boolean isChildOfTracked, boolean isSuccessorOfTracked) {
        this.genome = genome;
        this.position = position;
        this.direction = MapDirection.values()[Utils.randomInt(0, 7)];
        this.worldMap = worldMap;
        this.support = new PropertyChangeSupport(this);
        this.energy = energy;
        this.moveEnergy = moveEnergy;
        this.epochBorn = worldMap.getEpoch();
        this.childCount = 0;
        this.isChildOfTracked = isChildOfTracked;
        this.isSuccessorOfTracked = isSuccessorOfTracked;
    }

    @Override
    public Vector2D getPosition() {
        return this.position;
    }

    public int getEnergy() { return this.energy; }

    public int getChildCount() { return this.childCount; }

    public Genome getGenome() { return this.genome; }

    public int getEpochBorn() { return this.epochBorn; }

    public boolean isChildOfTracked() { return this.isChildOfTracked; }

    public boolean isSuccessorOfTracked() { return this.isSuccessorOfTracked; }

    public void setTracked(boolean tracked) { this.isTracked = tracked; }

    public void setSuccessorOfTracked(boolean successorOfTracked) { this.isSuccessorOfTracked = successorOfTracked; }

    private void turn() {
         int angle = this.genome.getSequence().get(Utils.randomInt(0, Genome.SEQ_LEN - 1));
         this.direction = this.direction.turn(angle);
    }

    public void move() {
        this.turn();
        Vector2D newPosition = this.worldMap.calculateNewPosition(this.position, this.direction.toUnitVector());
        this.support.firePropertyChange("position", this.position, newPosition);
        this.position = newPosition;
        this.energy -= this.moveEnergy;
    }

    public void changeEnergy(int energy) {
        this.energy += energy;
    }

    public Animal reproduceWith(Animal partner) {
        Vector2D newPosition = this.worldMap.getAdjacentFreePosition(this.position);
        if(newPosition == null) newPosition = this.worldMap.getAdjacentPosition(this.position);

        Genome newGenome = this.genome.mix(partner.genome);

        int newEnergy = (int)((this.energy + partner.energy) * REPRODUCTION_ENERGY_LOSS);
        this.energy *= (1 - REPRODUCTION_ENERGY_LOSS);
        partner.energy *= (1 - REPRODUCTION_ENERGY_LOSS);

        this.childCount++;
        partner.childCount++;

        boolean isChildOfTracked = this.isTracked || partner.isTracked;
        boolean isSuccessorOfTracked = isChildOfTracked || this.isSuccessorOfTracked || partner.isSuccessorOfTracked;

        return new Animal(newGenome, newPosition, this.worldMap, newEnergy, this.moveEnergy, isChildOfTracked, isSuccessorOfTracked);
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        this.support.addPropertyChangeListener(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        this.support.removePropertyChangeListener(pcl);
    }
}

