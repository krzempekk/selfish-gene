import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

public class MapStats implements PropertyChangeListener {
    private WorldMap map;

    private Map<MapStatsType, Integer> mediumStats;
    private Map<MapStatsType, Integer> currentStats;

    private Map<Genome, Integer> genomes;
    private Map<Genome, Integer> allGenomes;

    private boolean showDominatingGenome;

    private int deadAnimals;
    private int deadAnimalsLifespanSum;

    private Animal trackedAnimal;
    private Map<TrackedAnimalStatsType, Integer> trackedAnimalStats;

    MapStats(WorldMap map) {
        this.map = map;
        this.map.addPropertyChangeListener(this);

        this.mediumStats = new HashMap<>();
        this.currentStats = new HashMap<>();

        this.genomes = new HashMap<>();
        this.allGenomes = new HashMap<>();

        this.showDominatingGenome = false;

        this.deadAnimals = 0;
        this.deadAnimalsLifespanSum = 0;

        this.trackedAnimalStats = new HashMap<>();

        for (MapStatsType stat: MapStatsType.values()) {
            this.currentStats.put(stat, 0);
            this.mediumStats.put(stat, 0);
        }

        this.resetTrackedAnimalStats();
    }

    private void resetTrackedAnimalStats() {
        for(TrackedAnimalStatsType stat: TrackedAnimalStatsType.values()) {
            this.trackedAnimalStats.put(stat, 0);
        }
    }

    public int getEpoch() {
        return this.map.getEpoch();
    }

    private int calculateStat(MapStatsType stat) {
        List<Animal> mapAnimals = this.map.getAnimals();
        switch (stat) {
            case animalsNumber:
                return mapAnimals.size();
            case plantsNumber:
                return this.map.getMapElementMap().size() - mapAnimals.size();
            case averageEnergy:
                if(mapAnimals.size() == 0) return 0;
                int energySum = 0;
                for(Animal animal: mapAnimals) {
                    energySum += animal.getEnergy();
                }
                return energySum / mapAnimals.size();
            case averageLifespan:
                if(this.deadAnimals == 0) return 0;
                return this.deadAnimalsLifespanSum / this.deadAnimals;
            case averageChildCount:
                if(mapAnimals.size() == 0) return 0;
                int childCount = 0;
                for(Animal animal: mapAnimals) {
                    childCount += animal.getChildCount();
                }
                return childCount / mapAnimals.size();
            default:
                return -1;
        }
    }

    private void calculateStats() {
        for (MapStatsType stat : MapStatsType.values()) {
            int statValue = this.calculateStat(stat);
            int currentValue = this.mediumStats.get(stat);
            this.currentStats.put(stat, statValue);
            this.mediumStats.put(stat, currentValue + statValue);
        }
    }

    public int getStat(MapStatsType stat) {
        return this.currentStats.get(stat);
    }

    public int getAvgStat(MapStatsType stat) {
        return this.mediumStats.get(stat) / this.map.getEpoch();
    }

    public boolean isTracking() {
        return this.trackedAnimal != null;
    }

    public void untrackAnimal() {
        this.trackedAnimal.setTracked(false);
        this.trackedAnimal = null;
        this.resetTrackedAnimalStats();
        for(Animal animal: this.map.getAnimals()) {
            animal.setSuccessorOfTracked(false);
        }
    }

    public void trackAnimal(Vector2D position) {
        if(this.trackedAnimal != null) {
            this.untrackAnimal();
        }
        List<Animal> animals = this.map.getSortedAnimalsFrom(position);
        if(animals.size() > 0) {
            this.trackedAnimal = animals.get(0);
            this.trackedAnimal.setTracked(true);
        }
    }

    public Animal getTrackedAnimal() { return this.trackedAnimal; }

    public Vector2D getTrackedAnimalPosition() {
        if(this.trackedAnimal == null) return null;
        return this.trackedAnimal.getPosition();
    }

    public int getTrackedAnimalStat(TrackedAnimalStatsType stat) {
        return this.trackedAnimalStats.get(stat);
    }

    public Map.Entry<Genome, Integer> getDominatingGenome() {
        if(this.genomes.size() == 0) return null;
        return Collections.max(this.genomes.entrySet(), new Comparator<Map.Entry<Genome, Integer>>() {
            public int compare(Map.Entry<Genome, Integer> e1, Map.Entry<Genome, Integer> e2) {
                return e1.getValue().compareTo(e2.getValue());
            }
        });
    }

    public void setShowDominatingGenome(boolean show) {
        this.showDominatingGenome = show;
    }

    public List<Vector2D> getAnimalsWithDominatingGenomePositions() {
        if(!this.showDominatingGenome) return new ArrayList<>();
        List<Vector2D> positions = new ArrayList<>();
        Genome genome = this.getDominatingGenome().getKey();
        for(Animal animal: this.map.getAnimals()) {
            if(animal.getGenome().equals(genome)) {
                positions.add(animal.getPosition());
            }
        }
        return positions;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String event = evt.getPropertyName();
        switch (event) {
            case "dayEnd":
                this.calculateStats();
                break;
            case "animalDied": {
                Animal animal = (Animal) evt.getNewValue();
                if (animal == this.trackedAnimal) {
                    this.trackedAnimalStats.put(TrackedAnimalStatsType.deathEpoch, this.map.getEpoch());
                }

                this.deadAnimalsLifespanSum += this.map.getEpoch() - animal.getEpochBorn();
                this.deadAnimals++;

                Integer currentGenomeCount = this.genomes.get(animal.getGenome());
                if (currentGenomeCount != null && currentGenomeCount > 1) {
                    this.genomes.put(animal.getGenome(), currentGenomeCount - 1);
                } else {
                    this.genomes.remove(animal.getGenome());
                }
                break;
            }
            case "animalBorn": {
                Animal animal = (Animal) evt.getNewValue();

                Integer currentGenomeCount = this.genomes.get(animal.getGenome());
                this.genomes.put(animal.getGenome(), currentGenomeCount == null ? 1 : currentGenomeCount + 1);
                if (animal.isChildOfTracked()) {
                    this.trackedAnimalStats.put(TrackedAnimalStatsType.childenCount, this.trackedAnimalStats.get(TrackedAnimalStatsType.childenCount) + 1);
                }

                Integer currentAllGenomeCount = this.allGenomes.get(animal.getGenome());
                this.allGenomes.put(animal.getGenome(), currentAllGenomeCount == null ? 1 : currentAllGenomeCount + 1);
                if (animal.isSuccessorOfTracked()) {
                    this.trackedAnimalStats.put(TrackedAnimalStatsType.successorsCount, this.trackedAnimalStats.get(TrackedAnimalStatsType.successorsCount) + 1);
                }
                break;
            }
        }
    }
}
