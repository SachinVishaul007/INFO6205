package edu.neu.coe.info6205.sort.linearithmic;

import edu.neu.coe.info6205.sort.Helper;
import edu.neu.coe.info6205.sort.InstrumentedHelper;
import edu.neu.coe.info6205.sort.SortWithHelper;
import edu.neu.coe.info6205.sort.elementary.InsertionSort;
import edu.neu.coe.info6205.util.Benchmark;
import edu.neu.coe.info6205.util.Benchmark_Timer;
import edu.neu.coe.info6205.util.Config;

import java.util.Arrays;

/**
 * Class MergeSort.
 *
 * @param <X> the underlying comparable type.
 */
public class MergeSort<X extends Comparable<X>> extends SortWithHelper<X> {

    public static final String DESCRIPTION = "MergeSort";

    /**
     * Constructor for MergeSort
     * <p>
     * NOTE this is used only by unit tests, using its own instrumented helper.
     *
     * @param helper an explicit instance of Helper to be used.
     */
    public MergeSort(Helper<X> helper) {
        super(helper);
        insertionSort = new InsertionSort<>(helper);
    }

    /**
     * Constructor for MergeSort
     *
     * @param N      the number elements we expect to sort.
     * @param config the configuration.
     */
    public MergeSort(int N, Config config) {
        super(DESCRIPTION + ":" + getConfigString(config), N, config);
        insertionSort = new InsertionSort<>(getHelper());
    }

    public X[] sort(X[] xs, boolean makeCopy) {
        getHelper().init(xs.length);
        X[] result = makeCopy ? Arrays.copyOf(xs, xs.length) : xs;
        sort(result, 0, result.length);
        return result;
    }

    public void sort(X[] a, int from, int to) {
        // CONSIDER don't copy but just allocate according to the xs/aux interchange optimization
        X[] aux = Arrays.copyOf(a, a.length);
        sort(a, aux, from, to);
    }

    private void sort(X[] a, X[] aux, int from, int to) {
        final Helper<X> helper = getHelper();
        Config config = helper.getConfig();
        boolean insurance = config.getBoolean(MERGESORT, INSURANCE);
        boolean noCopy = config.getBoolean(MERGESORT, NOCOPY);
        if (to <= from + helper.cutoff()) {
            insertionSort.sort(a, from, to);
            return;
        }

        int mid = from + (to - from)/2;
        sort(aux, a, from, mid);
        sort(aux, a, mid, to);
        boolean leftSorted = true, rightSorted = true;

        merge(aux, a, from, mid, to);
    }

    // CONSIDER combine with MergeSortBasic perhaps.
    private void merge(X[] sorted, X[] result, int from, int mid, int to) {
        final Helper<X> helper = getHelper();
        int i = from;
        int j = mid;
        for (int k = from; k < to; k++)
            if (i >= mid) helper.copy(sorted, j++, result, k);
            else if (j >= to) helper.copy(sorted, i++, result, k);
            else if (helper.less(sorted[j], sorted[i])) {
                helper.incrementFixes(mid - i);
                helper.copy(sorted, j++, result, k);
            } else helper.copy(sorted, i++, result, k);
    }

    public static final String MERGESORT = "mergesort";
    public static final String NOCOPY = "nocopy";
    public static final String INSURANCE = "insurance";

    private static String getConfigString(Config config) {
        StringBuilder stringBuilder = new StringBuilder();
        if (config.getBoolean(MERGESORT, INSURANCE)) stringBuilder.append(" with insurance comparison");
        if (config.getBoolean(MERGESORT, NOCOPY)) stringBuilder.append(" with no copy");
        return stringBuilder.toString();
    }

    private final InsertionSort<X> insertionSort;

    public static void main (String[] args) {
        int N = 10000;

        while(N<=256000) {
            InstrumentedHelper<Integer> instrumentedHelper = new InstrumentedHelper<>("MergeSort", Config.setupConfig("true", "0", "0", "", ""));
            MergeSort<Integer> hs = new MergeSort<>(instrumentedHelper);
            int x = N;
            hs.init(x);
            Integer[] xs = instrumentedHelper.random(Integer.class, r -> r.nextInt(x));
            Benchmark<Boolean> benchmark = new Benchmark_Timer<>("Sorting", b -> hs.sort(xs, 0, x));
            double time = benchmark.run(true, 20);
            long compares = instrumentedHelper.getCompares();
            long swaps = instrumentedHelper.getSwaps();
            long hits = instrumentedHelper.getHits();

            System.out.println("When array size is: " + x);
            System.out.println("Compares: " + compares);
            System.out.println("Swaps: " + swaps );
            System.out.println("Hits: " + hits);
            System.out.println("Time: " + time);

            System.out.println("\nFor references:\t" + x + "\t" + compares + "\t" + swaps + "\t" + hits + "\t" + time + "\n");

            N = N*2;
        }
}

}