package pt.tecnico.sauron.silo.client;

import java.util.LinkedHashMap;
import java.util.Map;

public class SiloCache<K, V> extends LinkedHashMap<K, V> {
    private final int maxSize;

    public SiloCache(int maxSize){
        this.maxSize = maxSize;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxSize;
    }
}
