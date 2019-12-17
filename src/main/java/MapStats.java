public class MapStats {
    public WorldMap map;

    MapStats(WorldMap map) {
        this.map = map;
    }

    public int getAnimalsNumber() {
        return map.animals.size();
    }

    public int getPlantsNumber() {
        return map.mapElementMap.size() - map.animals.size();
    }

    public int getDominatingGene() {
        int[] geneCount = new int[8];
        for(Animal animal: map.animals) {
            for(int i = 0; i < 8; i++) {
                geneCount[i] += animal.genome.geneCount[i];
            }
        }
        int max = 0;
        for(int i = 0; i < 8; i++){
            if(geneCount[max] < geneCount[i]) max = i;
        }
        return max;
    }

    public int getAverageEnergyLevel() {
        int energySum = 0;
        for(Animal animal: map.animals) {
            energySum += animal.energy;
        }
        return energySum / map.animals.size();
    }

    public int getAverageLifespan() {
        int lifespanSum = 0;
        for(Animal animal: map.animals) {
            lifespanSum += (map.epoch - animal.epochBorn);
        }
        return lifespanSum / map.animals.size();
    }

    public int getAverageChildCount() {
        int childCount = 0;
        for(Animal animal: map.animals) {
            childCount += animal.childCount;
        }
        return childCount / map.animals.size();
    }
}
