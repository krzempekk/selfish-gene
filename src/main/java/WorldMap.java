import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;

import com.google.common.collect.ArrayListMultimap;

public class WorldMap implements PropertyChangeListener {
    private List<Animal> animals;
    private ArrayListMultimap<Vector2D,IMapElement> mapElementMap;
    private HashMap<Vector2D, MapSegment> freePositions;
    private List<MapSegment> mapSegments;

    private Vector2D lowerLeft, upperRight;
    private int width, height;
    private int epoch;

    private int plantsGrowth;

    private int startEnergy;
    private int plantEnergy;

    private PropertyChangeSupport support;

    public WorldMap(int width, int height, int initialAnimals, int initialPlants, int plantsGrowth, double jungleRatio, int startEnergy, int moveEnergy, int plantEnergy) {
        this.lowerLeft = new Vector2D(0, 0);
        this.upperRight = new Vector2D(width - 1, height - 1);
        this.width = width;
        this.height = height;
        this.plantsGrowth = plantsGrowth;
        this.epoch = 0;

        this.animals = new ArrayList<>();
        this.mapElementMap = ArrayListMultimap.create();
        this.freePositions = new HashMap<>();
        this.mapSegments = new ArrayList<>();
        this.startEnergy = startEnergy;
        this.plantEnergy = plantEnergy;

        this.support = new PropertyChangeSupport(this);

        int jungleSize = (int) Math.sqrt((width * height) * jungleRatio);
        Vector2D jungleLowerLeft = new Vector2D((this.lowerLeft.x + this.upperRight.x)/2 - jungleSize/2, (this.lowerLeft.y + this.upperRight.y)/2 - jungleSize/2);
        Vector2D jungleUpperRight = new Vector2D((this.lowerLeft.x + this.upperRight.x)/2 + jungleSize/2, (this.lowerLeft.y + this.upperRight.y)/2 + jungleSize/2);
        this.mapSegments.add(new MapSegment(jungleLowerLeft, jungleUpperRight, PlaceType.JUNGLE));

        for(int i = this.lowerLeft.x; i <= this.upperRight.x; i++) {
            for(int j = this.lowerLeft.y; j <= this.upperRight.y; j++) {
                this.freePositions.put(new Vector2D(i, j), this.getMapSegment(new Vector2D(i, j)));
            }
        }

        for(int i = 0; i < initialAnimals; i++) {
            this.place(new Animal(this.getRandomFreePosition(), this, startEnergy, moveEnergy));
        }

        for(int i = 0; i < initialPlants; i++) {
            this.place(new Food(this.getRandomFreePosition(), plantEnergy));
        }

    }

    public List<Animal> getAnimals() { return this.animals; }

    public ArrayListMultimap<Vector2D, IMapElement> getMapElementMap() { return this.mapElementMap; }

    public int getEpoch() {
        return this.epoch;
    }

    public int getStartEnergy() {
        return this.startEnergy;
    }

    public void place(IMapElement mapElement) {
        if(mapElement instanceof Animal) {
            Animal animal = (Animal) mapElement;
            animal.addPropertyChangeListener(this);
            this.animals.add(animal);
        }

        this.add(mapElement);
    }

    private void add(IMapElement mapElement) {
        Vector2D position = mapElement.getPosition();

        this.mapElementMap.put(position, mapElement);
        this.freePositions.remove(position);
    }

    private void move(IMapElement mapElement, Vector2D oldPosition, Vector2D newPosition) {
        this.mapElementMap.remove(oldPosition, mapElement);
        if(this.mapElementMap.get(oldPosition).size() == 0) this.freePositions.put(oldPosition, this.getMapSegment(oldPosition));

        if(newPosition != null) {
            this.mapElementMap.put(newPosition, mapElement);
            this.freePositions.remove(newPosition);
        }
    }

    private void remove(IMapElement mapElement) {
        Vector2D position = mapElement.getPosition();

        this.mapElementMap.remove(position, mapElement);
        if(this.mapElementMap.get(position).size() == 0) this.freePositions.put(position, this.getMapSegment(position));
    }

    public Collection<IMapElement> mapElementsFrom(Vector2D position) {
        return this.mapElementMap.get(position);
    }

    public boolean isOccupied(Vector2D position) {
        return this.mapElementsFrom(position).size() != 0;
    }

    public List<Animal> getSortedAnimalsFrom(Vector2D position) {
        Collection<IMapElement> elementsOnPosition = this.mapElementsFrom(position);
        List<Animal> animalsOnPosition = new ArrayList<>();
        for(IMapElement element: elementsOnPosition) {
            if(element instanceof Animal) animalsOnPosition.add((Animal) element);
        }
        animalsOnPosition.sort((Animal t1, Animal t2) -> {
            int energy1 = t1.getEnergy(), energy2 = t2.getEnergy();
            if (energy1 < energy2) return 1;
            else if (energy1 == energy2) return 0;
            return -1;
        });
        return animalsOnPosition;
    }

    public Food getFoodFrom(Vector2D position) {
        for(IMapElement element: this.mapElementsFrom(position)) {
            if(element instanceof Food)
                return (Food) element;
        }
        return null;
    }

    public Vector2D getRandomFreePosition() {
        int count = this.freePositions.size();
        if(count == 0) return null;

        return (Vector2D) this.freePositions.keySet().toArray()[Utils.randomInt(0, count - 1)];
    }

    public Vector2D getRandomFreePositionFromSegment(MapSegment segment) {
        List<Vector2D> positions = new ArrayList<>();
        for(Map.Entry<Vector2D, MapSegment> entry: this.freePositions.entrySet()) {
            if(entry.getValue() == segment) {
                positions.add(entry.getKey());
            }
        }

        int count = positions.size();
        if(count == 0) return null;

        return positions.get(Utils.randomInt(0, count - 1));
    }

    public Vector2D getAdjacentPosition(Vector2D position) {
        Vector2D step = MapDirection.values()[Utils.randomInt(0, 7)].toUnitVector();
        return this.calculateNewPosition(position, step);
    }

    public Vector2D getAdjacentFreePosition(Vector2D position) {
        for(int i = 0; i < 8; i++) {
            Vector2D step = MapDirection.values()[i].toUnitVector();
            Vector2D adjPosition = this.calculateNewPosition(position, step);
            if(!this.isOccupied(adjPosition)) {
                return adjPosition;
            }
        }
        return null;
    }

    public MapSegment getMapSegment(Vector2D position) {
        for(MapSegment mapSegment: this.mapSegments) {
            if(mapSegment.positionBelongs(position)) return mapSegment;
        }
        return null;
    }

    public PlaceType getMapSegmentType(Vector2D position) {
        MapSegment mapSegment = this.getMapSegment(position);
        return mapSegment == null ? PlaceType.NORMAL : mapSegment.getType();
    }

    private void growPlants() {
        int mapSegmentsCount = this.mapSegments.size();

        for(int i = 0; i <= mapSegmentsCount; i++) {
            MapSegment segment = i == mapSegmentsCount ? null : this.mapSegments.get(i);
            for(int j = 0; j < this.plantsGrowth; j++) {
                Vector2D newPosition = this.getRandomFreePositionFromSegment(segment);
                if(newPosition == null) break;
                Food newPlant = new Food(newPosition, this.plantEnergy);
                this.place(newPlant);
            }

        }
    }

    private void removeDeadAnimals() {
        Iterator<Animal> iter = this.animals.iterator();
        while(iter.hasNext()) {
            Animal animal = iter.next();
            if(animal.getEnergy() <= 0) {
                this.support.firePropertyChange("animalDied", null, animal);
                animal.removePropertyChangeListener(this);
                this.remove(animal);
                iter.remove();
            }
        }
    }

    private void animalsMove() {
        for(Animal animal: this.animals) {
            animal.move();
        }
    }

    private void animalsAct() {
        ArrayList<Vector2D> positionsProcessed = new ArrayList<>();
        ArrayList<Animal> newAnimals = new ArrayList<>();

        for(Animal animal: this.animals) {
            Vector2D currentPosition = animal.getPosition();
            if(!positionsProcessed.contains(currentPosition)) {
                List<Animal> animalsOnPosition = this.getSortedAnimalsFrom(currentPosition);
                Food foodOnPosition = this.getFoodFrom(currentPosition);

                if(foodOnPosition != null) {
                    int i = 0;
                    while(i < animalsOnPosition.size() && animalsOnPosition.get(i).getEnergy() == animalsOnPosition.get(0).getEnergy()) i++;

                    int energyPerAnimal = foodOnPosition.getEnergy() / i;

                    for(int j = 0; j < i; j++) {
                        animalsOnPosition.get(j).changeEnergy(energyPerAnimal);
                    }

                    this.remove(foodOnPosition);
                }

                if(animalsOnPosition.size() > 1) {
                    Animal parent1 = animalsOnPosition.get(0), parent2 = animalsOnPosition.get(1);
                    if(parent1.getEnergy() > this.startEnergy / 2 && parent2.getEnergy() > this.startEnergy / 2) {
                        newAnimals.add(parent1.reproduceWith(parent2));
                    }
                }

                positionsProcessed.add(currentPosition);
            }
        }

        for(Animal newAnimal: newAnimals) {
            this.place(newAnimal);
            this.support.firePropertyChange("animalBorn", null, newAnimal);
        }
    }

    public void run() {
        this.growPlants();
        this.removeDeadAnimals();
        this.animalsMove();
        this.animalsAct();
        this.epoch++;
        this.support.firePropertyChange("dayEnd", null, null);
    }

    public Vector2D calculateNewPosition(Vector2D position, Vector2D step) {
        Vector2D newPosition = position.add(step);

        if(newPosition.x < this.lowerLeft.x) {
            newPosition = newPosition.add(new Vector2D(this.width, 0));
        } else if(newPosition.x > this.upperRight.x) {
            newPosition = newPosition.add(new Vector2D(-this.width, 0));
        }

        if(newPosition.y < this.lowerLeft.y) {
            newPosition = newPosition.add(new Vector2D(0, this.height));
        } else if(newPosition.y > this.upperRight.y) {
            newPosition = newPosition.add(new Vector2D(0, -this.height));
        }

        return newPosition;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getPropertyName().equals("position")) {
            Vector2D oldPosition = (Vector2D) evt.getOldValue();
            Vector2D newPosition = (Vector2D) evt.getNewValue();
            IMapElement source = (IMapElement) evt.getSource();

            this.move(source, oldPosition, newPosition);
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        this.support.addPropertyChangeListener(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        this.support.removePropertyChangeListener(pcl);
    }
}
