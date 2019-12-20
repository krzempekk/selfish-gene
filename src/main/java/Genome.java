import java.util.*;

public class Genome {
    List<Integer> sequence = new ArrayList<>();
    int[] geneCount = new int[8];

    final static int SEQ_LEN = 32;

    public Genome() {
        for(int j = 0; j < SEQ_LEN; j++) {
            sequence.add(Utils.randomInt(0, 7));
        }
        this.fixSequence();
        Collections.sort(sequence);
        this.countGenes();
    }

    public int hashCode() {
        long hash = 0;
        for(int i = 0; i < 8; i++) {
            hash = ((hash + geneCount[i]) * 32) % Integer.MAX_VALUE;
        }
        return (int) hash;
    }

    public Genome(List<Integer> sequence) {
        this.sequence = sequence;
        this.fixSequence();
        Collections.sort(sequence);
        this.countGenes();
    }

    public void countGenes() {
        for(int i = 0; i < 8; i++)
            geneCount[i] = Collections.frequency(sequence, i);
    }

    public List<Integer> getSequence() {
        return sequence;
    }

    public List<Integer> getSequencePart(int start, int stop) {
        return sequence.subList(start, stop);
    }

    private void fixSequence() {
        Integer[] frequencies = new Integer[8];

        for(int i = 0; i <= 7; i++) {
            frequencies[i] = Collections.frequency(sequence, i);
        }

        for(int i = 0; i <= 7; i++) {
            if(frequencies[i] == 0) {
                int j;
                do {
                    j = Utils.randomInt(0, 7);
                } while (frequencies[j] > 1);
                frequencies[j]--; frequencies[i]++;
                sequence.remove(j); sequence.add(i);
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

        Genome newGenome = new Genome(newSequence);

        newGenome.fixSequence();

        return newGenome;
    }

    public String toString() {
        return sequence.toString();
    }
}
