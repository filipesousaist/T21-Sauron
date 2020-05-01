package pt.tecnico.sauron.util;

import java.util.*;

public class VectorTS {
    private Map<Integer, Integer> map;

    public VectorTS() {
        this.map = new HashMap<>();
    }

    public VectorTS(Map<Integer, Integer> map) {
        this.map = new HashMap<>(map);
    }

    public Map<Integer, Integer> getMap() {
        return map;
    }

    public int get(int index) {
        return map.getOrDefault(index, 0);
    }

    public void set(int index, int value) {
        map.put(index, value);
    }

    public void incr(int index) {
        map.put(index, get(index) + 1);
    }

    public boolean isZero() { return map.isEmpty(); }

    public void update(VectorTS v) {
        v.map.forEach((key, otherValue) ->
                map.put(key, Integer.max(get(key), otherValue))
        );
    }

    public boolean happensBefore(VectorTS v) {
        return happensBefore(v, false);
    }

    public boolean happensBeforeOrEquals(VectorTS v) {
        return happensBefore(v, true);
    }

    private boolean happensBefore(VectorTS v, boolean orEquals) {
        boolean hasSmaller = false;

        for (int key : getJoinedKeys(v)) {
            int myValue = get(key);
            int otherValue = v.get(key);

            if (myValue < otherValue)
                hasSmaller = true;
            else if (myValue > otherValue)
                return false;
        }

        return orEquals || hasSmaller;
    }

    private Set<Integer> getJoinedKeys(VectorTS v) {
        Set<Integer> keys = new HashSet<>();
        keys.addAll(map.keySet());
        keys.addAll(v.map.keySet());
        return keys;
    }

    @Override
    public String toString() {
        return map.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VectorTS vectorTS = (VectorTS) o;
        return Objects.equals(map, vectorTS.map);
    }

    @Override
    public int hashCode() {
        return Objects.hash(map);
    }
}