import java.util.*;

public class Genome {
    private List<Integer> sequence = new ArrayList<>();
    private int[] geneCount = new int[8];

    public final static int SEQ_LEN = 32;

    public Genome() {
        for(int j = 0; j < SEQ_LEN; j++) {
            this.sequence.add(Utils.randomInt(0, 7));
        }
        this.countGenes();
        this.fixSequence();
        Collections.sort(this.sequence);
    }

    public Genome(List<Integer> sequence) {
        this.sequence = sequence;
        this.countGenes();
        this.fixSequence();
        Collections.sort(this.sequence);
    }

    public List<Integer> getSequence() { return this.sequence; }

    public int[] getGeneCount() { return this.geneCount; }

    public List<Integer> getSequencePart(int start, int stop) {
        return this.sequence.subList(start, stop);
    }

    private void countGenes() {
        for(int i = 0; i < 8; i++)
            this.geneCount[i] = Collections.frequency(this.sequence, i);
    }

    private void fixSequence() {
        for(int i = 0; i <= 7; i++) {
            if(this.geneCount[i] == 0) {
                int j;
                do {
                    j = Utils.randomInt(0, 7);
                } while (this.geneCount[j] > 1);
                this.geneCount[j]--; this.geneCount[i]++;
                this.sequence.remove(j); this.sequence.add(i);
            }
        }
    }

    public Genome mix(Genome other) {
        List<Integer> newSequence = new ArrayList<>();

        List<Integer> partitionIndexes = Arrays.asList(0, Utils.randomInt(1, SEQ_LEN - 1), Utils.randomInt(1, SEQ_LEN - 1), SEQ_LEN);
        Collections.sort(partitionIndexes);

        List<Genome> genomes = Arrays.asList(this, other);
        Collections.shuffle(genomes);

        List<Integer> partsOrigin = Arrays.asList(0, 0, 1);
        Collections.shuffle(partsOrigin);

        for(int part_index = 0; part_index < 3; part_index++) {
            Genome g = genomes.get(partsOrigin.get(part_index));
            newSequence.addAll(g.getSequencePart(partitionIndexes.get(part_index), partitionIndexes.get(part_index + 1)));
        }

        return new Genome(newSequence);
    }

    public String toString() {
        return this.sequence.toString();
    }

    public int hashCode() {
        long hash = 0;
        for(int i = 0; i < 8; i++) {
            hash = ((hash + this.geneCount[i]) * 32) % Integer.MAX_VALUE;
        }
        return (int) hash;
    }

    public boolean equals(Object other) {
        if(this == other) return true;
        if(!(other instanceof Genome)) return false;
        Genome genome = (Genome) other;
        for(int i = 0; i < 8; i++) {
            if(this.geneCount[i] != genome.geneCount[i]) return false;
        }
        return true;
    }
}
