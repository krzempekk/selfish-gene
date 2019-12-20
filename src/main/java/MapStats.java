import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

public class MapStats implements PropertyChangeListener {
    private WorldMap map;
    private Animal trackedAnimal;

    private Map<MapStatsType, Integer> mediumStats;
    private Map<MapStatsType, Integer> currentStats;

    private Map<Genome, Integer> genomes;

    private int deadAnimals;
    private int deadAnimalsLifespanSum;
    private int trackedAnimalDeathEpoch;
    private int successorCount;

    MapStats(WorldMap map) {
        this.map = map;
        this.map.addPropertyChangeListener(this);
        this.mediumStats = new HashMap<>();
        this.currentStats = new HashMap<>();
        this.genomes = new HashMap<>();

        this.trackedAnimalDeathEpoch = 0;
        this.deadAnimals = 0;
        this.deadAnimalsLifespanSum = 0;
        this.successorCount = 0;

        for (MapStatsType stat : MapStatsType.values()) {
            this.currentStats.put(stat, 0);
            this.mediumStats.put(stat, 0);
        }
    }

    public int calculateStat(MapStatsType stat) {
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

    public void calculateStats() {
        for (MapStatsType stat : MapStatsType.values()) {
            int statValue = this.calculateStat(stat);
            int currentValue = this.mediumStats.get(stat);
            this.currentStats.put(stat, statValue);
            this.mediumStats.put(stat, currentValue + statValue);
        }
    }

    public Genome getDominatingGenome() {
        if(this.genomes.size() == 0) return null;
        Map.Entry<Genome, Integer> maxEntry = Collections.max(this.genomes.entrySet(), new Comparator<Map.Entry<Genome, Integer>>() {
            public int compare(Map.Entry<Genome, Integer> e1, Map.Entry<Genome, Integer> e2) {
                return e1.getValue().compareTo(e2.getValue());
            }
        });
        return maxEntry.getKey();
    }

    public List<Animal> getAnimalsWithDominatingGenome() {
        List<Animal> animals = new ArrayList<>();
        Genome genome = this.getDominatingGenome();
        for(Animal animal: this.map.getAnimals()) {
            if(animal.getGenome().equals(genome)) {
                animals.add(animal);
            }
        }
        return animals;
    }

    public int getEpoch() {
        return this.map.getEpoch();
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
        this.trackedAnimalDeathEpoch = 0;
        for(Animal animal: this.map.getAnimals()) {
            animal.setSuccessor(false);
        }
    }

    public void trackAnimal(Vector2D position) {
        if(this.trackedAnimal != null) {
            this.untrackAnimal();
        }
        this.successorCount = 0;
        List<Animal> animals = this.map.getSortedAnimalsFrom(position);
        if(animals.size() > 0) {
            this.trackedAnimal = animals.get(0);
            this.trackedAnimal.setSuccessor(true);
            this.trackedAnimal.setTracked(true);
        }
    }

    public Vector2D getTrackedPosition() {
        return this.trackedAnimal == null ? null : this.trackedAnimal.getPosition();
    }

//    public String formatGenome(Genome genome) {
//        StringBuilder formattedGenome = new StringBuilder();
//        int[] geneCount = genome.getGeneCount();
//        for(int i = 0; i < geneCount.length; i++) {
//            formattedGenome.append(i).append(":").append(geneCount[i]).append(",");
//        }
//        return formattedGenome.toString();
//    }
//
//    public String getDominatingGenomeString() {
//        return this.formatGenome(this.getDominatingGenome());
//    }
//
//    public String getTrackedGenomeString() {
//        if(this.trackedAnimal != null) {
//            return this.formatGenome(this.trackedAnimal.getGenome());
//        }
//        return null;
//    }

    public Animal getTrackedAnimal() { return this.trackedAnimal; }

    public int getTrackedChildCount() {
        return this.trackedAnimal.getChildCount();
    }

    public int getTrackedSuccessorsCount() {
        return this.successorCount;
    }

    public int getTrackedDeathEpoch() {
        return this.trackedAnimalDeathEpoch;
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
                    this.trackedAnimalDeathEpoch = this.map.getEpoch();
                }
                this.deadAnimalsLifespanSum += this.map.getEpoch() - animal.getEpochBorn();
                this.deadAnimals++;
                Integer currentGenomeCount = this.genomes.get(animal.getGenome());
                if (currentGenomeCount != null) {
                    this.genomes.put(animal.getGenome(), currentGenomeCount - 1);
                }
                break;
            }
            case "animalBorn": {
                Animal animal = (Animal) evt.getNewValue();
                Integer currentGenomeCount = this.genomes.get(animal.getGenome());
                this.genomes.put(animal.getGenome(), currentGenomeCount == null ? 1 : currentGenomeCount + 1);
                if (animal.getSuccessor()) {
                    this.successorCount++;
                }
                break;
            }
        }
    }
}
