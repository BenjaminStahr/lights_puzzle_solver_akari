package strategies;

import java.util.Comparator;

public class InformedSearchSorter implements Comparator<WeightedIndexPair> {

    // when this returns less than 0 the first object is smaller
    @Override
    public int compare(WeightedIndexPair o1, WeightedIndexPair o2) {
        return o2.weight - o1.weight;
    }
}
