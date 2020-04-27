package pt.tecnico.sauron.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class VectorTS implements Iterable<Integer> {
    private List<Integer> values;

    public VectorTS(int size) {
        values = new ArrayList<>(Collections.nCopies(size, 0));
    }

    public VectorTS(List<Integer> values) {
        this.values = new ArrayList<>();
        this.values.addAll(values);
    }

    public int get(int index) {
        return values.get(index - 1);
    }

    public void incr(int index) {
        values.set(index - 1, values.get(index - 1) + 1);
    }

    public int getSize() {
        return values.size();
    }

    public void update(VectorTS v) {
        int size = balanceSizes(this, v);

        for (int i = 0; i < size; i ++)
            values.set(i, Integer.max(values.get(i), v.values.get(i)));
    }

    public boolean happensBefore(VectorTS v) {
        int size = balanceSizes(this, v);

        boolean hasSmaller = false;
        for (int i = 0; i < size; i ++) {
            int myVal = values.get(i);
            int otherVal = v.values.get(i);

            if (myVal > otherVal)
                return false;
            else if (myVal < otherVal)
                hasSmaller = true;
        }
        return hasSmaller;
    }

    // Extend this vector until it has
    // as many elements as 'v'
    private static int balanceSizes(VectorTS v1, VectorTS v2) {
        int size1 = v1.values.size();
        int size2 = v2.values.size();

        if (size1 < size2)
            v1.extend0(size2 - size1);
        else if (size2 < size1)
            v2.extend0(size1 - size2);

        return v1.getSize();
    }

    private void extend0(int num) {
        for (int i = 0; i < num; i ++)
            values.add(0);
    }

    @Override
    public Iterator<Integer> iterator() {
        return values.iterator();
    }
}
