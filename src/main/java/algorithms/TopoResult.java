package algorithms;

import java.util.Arrays;

public final class TopoResult {
    private final int[] order;
    private final int processed;
    private final boolean cycle;

    public TopoResult(int[] order, int processed, boolean cycle) {
        this.order = order;
        this.processed = processed;
        this.cycle = cycle;
    }

    public boolean hasCycle() {
        return cycle;
    }

    public int processed() {
        return processed;
    }

    public int[] order() {
        if (cycle) {
            throw new IllegalStateException("graph has a cycle, there is no topological order");
        }
        return Arrays.copyOf(order, processed);
    }
}
