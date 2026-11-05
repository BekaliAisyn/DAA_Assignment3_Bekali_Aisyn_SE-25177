package algorithms;

import java.util.ArrayList;
import java.util.List;

public final class SccResult {
    private final int[] component;
    private final int count;

    public SccResult(int[] component, int count) {
        this.component = component;
        this.count = count;
    }

    public int[] component() {
        return component;
    }

    public int count() {
        return count;
    }

    public List<List<Integer>> groups() {
        List<List<Integer>> out = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            out.add(new ArrayList<>());
        }
        for (int v = 0; v < component.length; v++) {
            out.get(component[v]).add(v);
        }
        return out;
    }
}
