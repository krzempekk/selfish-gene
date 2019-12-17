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

    public Genome(List<Integer> sequence) {
        this.sequence = sequence;
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
}

//public class Genome {
//    List<List<Integer>> sequences = new ArrayList<>();
//
//    final static int SEQ_COUNT = 1; // how many sequences will be
//    final static int[] SEQ_LEN = {32}; // how long is each of sequences
//    final static int[][] SEQ_RANGE = {{0, 7}}; // range of values in each of sequences
//    final static double MUTATION_RATIO = 0.001; // mutation chance for each element
//
//
//    public Genome() {
//        for(int i = 0; i < SEQ_COUNT; i++) {
//            List<Integer> sequence = new ArrayList<>();
//            for(int j = 0; j < SEQ_LEN[i]; j++) {
//                sequence.add(Utils.randomInt(SEQ_RANGE[i][0], SEQ_RANGE[i][1]));
//            }
//            Collections.sort(sequence);
//            sequences.add(sequence);
//        }
//        this.mutate();
//        this.fixSequences();
//    }
//
//    public Genome(List<List<Integer>> sequences) {
//        this.sequences = sequences;
//    }
//
//    public List<Integer> getSequence(int sequenceIndex) {
//        return sequences.get(sequenceIndex);
//    }
//
//    public List<Integer> getSequencePart(int sequenceIndex, int start, int stop) {
//        return sequences.get(sequenceIndex).subList(start, stop);
//    }
//
//    private void fixSequences() {
//        for(int i = 0; i < SEQ_COUNT; i++) {
//            List<Integer> sequence = sequences.get(i);
//
//            SortedSet<Map.Entry<Integer, Integer>> counts = new TreeSet<>((v1, v2) -> {
//                if (v1.getValue() < v2.getValue() || (v1.getValue().equals(v2.getValue()) && v1.getKey() < v2.getKey())) {
//                    return -1;
//                } else if (v1.getValue().equals(v2.getValue()) && v1.getKey().equals(v2.getKey())) {
//                    return 0;
//                }
//                return 1;
//            });
//
//            for(int j = SEQ_RANGE[i][0]; j <= SEQ_RANGE[i][1]; j++) {
//                counts.add(new AbstractMap.SimpleEntry<>(j, Collections.frequency(sequence, j)));
//            }
//
//            while(counts.first().getValue() == 0) {
//                Map.Entry<Integer, Integer> first = counts.first(), last = counts.last();
//                counts.remove(first); counts.remove(last);
//                sequence.remove(last.getKey()); sequence.add(first.getKey());
//                first.setValue(1); last.setValue(last.getValue() - 1);
//                counts.add(first); counts.add(last);
//            }
//        }
//    }
//
//    private void mutate() {
//        // not implemented yet
//    }
//
//    public Genome mix(Genome other) {
//        List<List<Integer>> newSequences = new ArrayList<>();
//
//        for(int seq_index = 0; seq_index < SEQ_COUNT; seq_index++) {
//            List<Integer> newSequence = new ArrayList<>();
//
//            List<Integer> partitionIndexes = Arrays.asList(0, Utils.randomInt(1, SEQ_LEN[seq_index] - 1), Utils.randomInt(1, SEQ_LEN[seq_index] - 1), SEQ_LEN[seq_index]);
//            Collections.sort(partitionIndexes);
//
//            List<Genome> genomes = Arrays.asList(this, other);
//            Collections.shuffle(genomes);
//
//            List<Integer> partsOrigin = Arrays.asList(0, 0, 1);
//            Collections.shuffle(partsOrigin);
//
//            for(int part_index = 0; part_index < 3; part_index++) {
//                Genome g = genomes.get(partsOrigin.get(part_index));
//                newSequence.addAll(g.getSequencePart(seq_index, partitionIndexes.get(part_index), partitionIndexes.get(part_index + 1)));
//            }
//
//            newSequences.add(newSequence);
//        }
//
//        Genome newGenome = new Genome(newSequences);
//
//        newGenome.mutate();
//        newGenome.fixSequences();
//
//        return newGenome;
//    }
//}