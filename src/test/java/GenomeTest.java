import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class GenomeTest {

    @Test
    void CreatedSequencesAreValid() {
        Genome g = new Genome();

        Genome g1 = new Genome(g.sequence);

        Map<Genome, Integer> m = new HashMap<>();

        Map<Integer, Long> counts = g.sequence.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        List<Integer> allGenes = IntStream.range(0, 8).boxed().collect(Collectors.toList());
        assertTrue(allGenes.stream().allMatch(counts::containsKey));
    }

    @Test
    void MixedSequencesAreValid() {
        Genome g1 = new Genome();
        Genome g2 = new Genome();

        Genome g = g1.mix(g2);

        Map<Integer, Long> counts = g.sequence.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        List<Integer> allGenes = IntStream.range(0, 8).boxed().collect(Collectors.toList());
        assertTrue(allGenes.stream().allMatch(counts::containsKey));
    }

}