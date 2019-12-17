import java.util.List;

public class MapStats {
    public WorldMap map;

    MapStats(WorldMap map) {
        this.map = map;
    }

    public int getStat(String stat) {
        switch (stat) {
            case "animalsNumber":
                return map.animals.size();
            case "plantsNumber":
                return map.mapElementMap.size() - map.animals.size();
            case "dominatingGene":
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
            case "averageEnergy":
                int energySum = 0;
                for(Animal animal: map.animals) {
                    energySum += animal.energy;
                }
                return energySum / map.animals.size();
            case "averageLifespan":
                int lifespanSum = 0;
                for(Animal animal: map.animals) {
                    lifespanSum += (map.epoch - animal.epochBorn);
                }
                return lifespanSum / map.animals.size();
            case "averageChildCount":
                int childCount = 0;
                for(Animal animal: map.animals) {
                    childCount += animal.childCount;
                }
                return childCount / map.animals.size();
            default:
                return -1;
        }
    }

    public List<Integer> getGenome(int x, int y) {
        return ((Animal)(map.objectAt(new Vector2D(x, y)).toArray()[0])).genome.sequence;
    }
}
