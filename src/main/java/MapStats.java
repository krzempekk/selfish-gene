import com.sun.xml.internal.ws.policy.EffectiveAlternativeSelector;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

public class MapStats implements PropertyChangeListener {
    public WorldMap map;
    public Animal trackedAnimal;

    public Map<MapStatsType, Integer> mediumStats;
    public Map<MapStatsType, Integer> currentStats;

    public Map<Genome, Integer> genomesLiving;
    public Map<Genome, Integer> genomes;

    public int trackedLifespan = 0;
    public int lifespanSum = 0;
    public int deadAnimals = 0;

    MapStats(WorldMap map) {
        this.map = map;
        map.addPropertyChangeListener(this);
        this.mediumStats = new HashMap<>();
        this.currentStats = new HashMap<>();
        this.genomesLiving = new HashMap<>();
        this.genomes = new HashMap<>();

        for (MapStatsType stat : MapStatsType.values()) {
            currentStats.put(stat, 0);
            mediumStats.put(stat, 0);
        }
    }

    public Object calculateStat(MapStatsType stat) {
        switch (stat) {
            case animalsNumber:
                return map.animals.size();
            case plantsNumber:
                return map.mapElementMap.size() - map.animals.size();
            case averageEnergy:
                if(map.animals.size() == 0) return 0;
                int energySum = 0;
                for(Animal animal: map.animals) {
                    energySum += animal.energy;
                }
                return energySum / map.animals.size();
            case averageLifespan:
                if(deadAnimals == 0) return 0;
                return lifespanSum / deadAnimals;
            case averageChildCount:
                if(map.animals.size() == 0) return 0;
                int childCount = 0;
                for(Animal animal: map.animals) {
                    childCount += animal.childCount;
                }
                return childCount / map.animals.size();
            default:
                return -1;
        }
    }

    public void calculateStats() {
        for (MapStatsType stat : MapStatsType.values()) {
            int statValue = (int) calculateStat(stat);
            int currentValue = mediumStats.get(stat);
            currentStats.put(stat, statValue);
            mediumStats.put(stat, currentValue + statValue);
        }
    }

    public String getDominatingGenome() {
        if(genomes.size() == 0) return null;
        Map.Entry<Genome, Integer> maxEntry = Collections.max(genomes.entrySet(), new Comparator<Map.Entry<Genome, Integer>>() {
            public int compare(Map.Entry<Genome, Integer> e1, Map.Entry<Genome, Integer> e2) {
                return e1.getValue().compareTo(e2.getValue());
            }
        });
        return maxEntry.getKey().toString();
    }

    public int getEpoch() {
        return map.epoch;
    }

    public int getStat(MapStatsType stat) {
        return currentStats.get(stat);
    }

    public int getAvgStat(MapStatsType stat) {
        return mediumStats.get(stat) / map.epoch;
    }

    public boolean isTracking() {
        return trackedAnimal != null;
    }

    public void untrackAnimal() {
        trackedLifespan = 0;
        trackedAnimal.isTracked = false;
        trackedAnimal = null;
        for(Animal animal: map.animals) {
            animal.isSuccessor = false;
        }
        map.tracking = false;
        map.successorCount = 0;
    }

    public void trackAnimal(Vector2D position) {
        if(trackedAnimal != null) {
            untrackAnimal();
        }
        map.tracking = true;
        List<Animal> animals = map.getSortedAnimalsFrom(position);
        if(animals.size() > 0) {
            trackedAnimal = animals.get(0);
            trackedAnimal.isSuccessor = true;
            trackedAnimal.isTracked = true;
        }
    }

    public Vector2D getTrackedPosition() {
        return trackedAnimal == null ? null : trackedAnimal.getPosition();
    }

    public String getTrackedGenome() {
        if(trackedAnimal != null) {
            String genome = "";
            for(int i = 0; i < trackedAnimal.genome.geneCount.length; i++) {
                genome = genome + i + ":" + trackedAnimal.genome.geneCount[i] + ",";
            }
            return genome;
        }
        return null;
    }

    public int getTrackedChildCount() {
        return trackedAnimal.childCount;
    }

    public int getTrackedSuccessorsCount() {
        return map.successorCount;
    }

    public int getTrackedDeathEpoch() {
        return trackedLifespan;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String event = evt.getPropertyName();
        if (event.equals("dayEnd")) {
            calculateStats();
        } else if(event.equals("animalDied")) {
            Animal animal = (Animal) evt.getNewValue();
            if(animal == trackedAnimal) {
                trackedLifespan = map.epoch - trackedAnimal.epochBorn;
            }
            lifespanSum += map.epoch - animal.epochBorn;
            deadAnimals++;
            Integer currentGenomeCount = genomes.get(animal.genome);
            if(currentGenomeCount != null) {
                genomes.put(animal.genome, currentGenomeCount - 1);
            }
        } else if(event.equals("animalBorn")) {
            Animal animal = (Animal) evt.getNewValue();
            Integer currentGenomeCount = genomes.get(animal.genome);
            genomes.put(animal.genome, currentGenomeCount == null ? 1 : currentGenomeCount + 1);
        }
    }
}
