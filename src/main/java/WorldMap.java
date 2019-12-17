import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.stream.Collectors;

import com.google.common.collect.ArrayListMultimap;

public class WorldMap implements PropertyChangeListener {
    public List<Animal> animals;
    public ArrayListMultimap<Vector2D,IMapElement> mapElementMap = ArrayListMultimap.create();
    public HashMap<Vector2D, MapSegment> freePositions;
    public List<MapSegment> mapSegments;

    public Vector2D lowerLeft, upperRight;
    public int width, height;
    public int epoch;

    private int plantsGrowth;

    public WorldMap(int width, int height, int initialAnimals, int initialPlants, int plantsGrowth, double jungleRatio, int startEnergy, int moveEnergy, int plantEnergy) {
        this.lowerLeft = new Vector2D(0, 0);
        this.upperRight = new Vector2D(width - 1, height - 1);
        this.width = width; this.height = height;
        this.plantsGrowth = plantsGrowth;
        this.epoch = 0;

        this.animals = new ArrayList<>();
        this.freePositions = new HashMap<>();
        this.mapSegments = new ArrayList<>();

        int jungleSize = (int) Math.sqrt((width * height) * jungleRatio);
        Vector2D jungleLowerLeft = new Vector2D((lowerLeft.x + upperRight.x)/2 - jungleSize/2, (lowerLeft.y + upperRight.y)/2 - jungleSize/2);
        Vector2D jungleUpperRight = new Vector2D((lowerLeft.x + upperRight.x)/2 + jungleSize/2, (lowerLeft.y + upperRight.y)/2 + jungleSize/2);
        mapSegments.add(new MapSegment(jungleLowerLeft, jungleUpperRight, PlaceType.JUNGLE));

//        mapSegments.add(new MapSegment(new Vector2D(width/8, height/8), new Vector2D(3*width/8, 3*height/8), PlaceType.JUNGLE));
//        mapSegments.add(new MapSegment(new Vector2D(5*width/8, 5*height/8), new Vector2D(7*width/8, 7*height/8), PlaceType.JUNGLE));
//        mapSegments.add(new MapSegment(new Vector2D(width/8, 5*height/8), new Vector2D(3*width/8, 7*height/8), PlaceType.JUNGLE));
//        mapSegments.add(new MapSegment(new Vector2D(5*width/8, height/8), new Vector2D(7*width/8, 3*height/8), PlaceType.JUNGLE));

        for(int i = lowerLeft.x; i <= upperRight.x; i++) {
            for(int j = lowerLeft.y; j <= upperRight.y; j++) {
                freePositions.put(new Vector2D(i, j), getMapSegment(new Vector2D(i, j)));
            }
        }

        for(int i = 0; i < initialAnimals; i++) {
            this.place(new Animal(this.getRandomFreePosition(), this, startEnergy, moveEnergy));
        }

        for(int i = 0; i < initialPlants; i++) {
            this.place(new Food(this.getRandomFreePosition(), plantEnergy));
        }

    }

    public boolean place(IMapElement mapElement) {
        if(mapElement instanceof Animal) {
            Animal animal = (Animal) mapElement;
            animal.addPropertyChangeListener(this);
            animals.add(animal);
        }

        this.add(mapElement);

        return true;
    }

    public void add(IMapElement mapElement) {
        Vector2D position = mapElement.getPosition();

        mapElementMap.put(position, mapElement);
        freePositions.remove(position);
    }

    public void move(IMapElement mapElement, Vector2D oldPosition, Vector2D newPosition) {
        mapElementMap.remove(oldPosition, mapElement);
        if(mapElementMap.get(oldPosition).size() == 0) freePositions.put(oldPosition, getMapSegment(oldPosition));

        if(newPosition != null) {
            mapElementMap.put(newPosition, mapElement);
            freePositions.remove(newPosition);
        }
    }

    public void remove(IMapElement mapElement) {
        Vector2D position = mapElement.getPosition();

        mapElementMap.remove(position, mapElement);
        if(mapElementMap.get(position).size() == 0) freePositions.put(position, getMapSegment(position));
    }

    public boolean isOccupied(Vector2D position) {
        return objectAt(position).size() != 0;
    }

    public Collection objectAt(Vector2D position) {
        return mapElementMap.get(position);
    }

    public Vector2D getRandomFreePosition() {
        int count = freePositions.size();
        if(count == 0) return null;

        return (Vector2D) freePositions.keySet().toArray()[Utils.randomInt(0, count - 1)];
    }

    public Vector2D getRandomFreePositionFromSegment(MapSegment segment) {
        PlaceType type = segment == null ? PlaceType.NORMAL : segment.type;
        List<Vector2D> positions = freePositions.entrySet().stream().filter(entry -> type.equals(entry.getValue())).map(Map.Entry::getKey).collect(Collectors.toList());
        int count = positions.size();
        if(count == 0) return null;

        return positions.get(Utils.randomInt(0, count - 1));
    }

    public Vector2D getAdjacentFreePosition(Vector2D position) {
        for(int i = 0; i < 8; i++) {
            Vector2D step = MapDirection.values()[i].toUnitVector();
            Vector2D adjPosition = calculateNewPosition(position, step);
            if(!isOccupied(adjPosition)) {
                return adjPosition;
            }
        }
        return null;
    }

    public MapSegment getMapSegment(Vector2D position) {
        for(MapSegment mapSegment: mapSegments) {
            if(mapSegment.positionBelongs(position)) return mapSegment;
        }
        return null;
    }

    public PlaceType getMapSegmentType(Vector2D position) {
        for(MapSegment mapSegment: mapSegments) {
            if(mapSegment.positionBelongs(position)) return mapSegment.type;
        }
        return PlaceType.NORMAL;
    }

    private void growPlants() {
        for(int i = 0; i < plantsGrowth; i++) {
            Vector2D newPosition = this.getRandomFreePositionFromSegment(null);
            if(newPosition == null) return;
            Food newPlant = new Food(newPosition, 50);
            this.place(newPlant);
        }

        for(MapSegment segment: mapSegments) {
            for(int i = 0; i < plantsGrowth; i++) {
                Vector2D newPosition = this.getRandomFreePositionFromSegment(segment);
                if(newPosition == null) return;
                Food newPlant = new Food(newPosition, 50);
                this.place(newPlant);
            }
        }
    }

    private void removeDeadAnimals() {
        Iterator<Animal> iter = animals.iterator();
        while(iter.hasNext()) {
            Animal animal = iter.next();
            if(animal.getEnergy() <= 0) {
                animal.removePropertyChangeListener(this);
                this.remove(animal);
                iter.remove();
            }
        }
    }

    private void animalsMove() {
        for(Animal animal: animals) {
            animal.move();
        }
    }

    private void animalsAct() {
        ArrayList<Vector2D> positionsProcessed = new ArrayList<>();
        ArrayList<Animal> newAnimals = new ArrayList<>();

        for(Animal animal: animals) {
            Vector2D currentPosition = animal.getPosition();
            List<IMapElement> elementsOnPosition = mapElementMap.get(currentPosition);
            if(!positionsProcessed.contains(currentPosition) && elementsOnPosition.size() != 0) {

                List<Animal> animalsOnPosition = new ArrayList<>();
                Food foodOnPosition = null;

                for(IMapElement element: elementsOnPosition) {
                    if(element instanceof Food) foodOnPosition = (Food) element;
                    else if(element instanceof Animal) animalsOnPosition.add((Animal) element);
                }

                animalsOnPosition.sort((Animal t1, Animal t2) -> {
                    int energy1 = t1.getEnergy(), energy2 = t2.getEnergy();
                    if (energy1 < energy2) return 1;
                    else if (energy1 == energy2) return 0;
                    return -1;
                });

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
                    Animal newAnimal = animalsOnPosition.get(0).reproduceWith(animalsOnPosition.get(1));
                    if(newAnimal != null) newAnimals.add(newAnimal);
                }

                positionsProcessed.add(currentPosition);
            }
        }

        for(Animal newAnimal: newAnimals) {
            this.place(newAnimal);
        }
    }

    public void run() {
        this.growPlants();
        this.removeDeadAnimals();
        this.animalsMove();
        this.animalsAct();
        this.epoch++;
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

    public Vector2D calculateNewPosition(Vector2D position, Vector2D step) {
        Vector2D newPosition = position.add(step);

        if(newPosition.x < lowerLeft.x) {
            newPosition = newPosition.add(new Vector2D(width, 0));
        } else if(newPosition.x > upperRight.x) {
            newPosition = newPosition.add(new Vector2D(-width, 0));
        }

        if(newPosition.y < lowerLeft.y) {
            newPosition = newPosition.add(new Vector2D(0, height));
        } else if(newPosition.y > upperRight.y) {
            newPosition = newPosition.add(new Vector2D(0, -height));
        }

        return newPosition;
    }
}
