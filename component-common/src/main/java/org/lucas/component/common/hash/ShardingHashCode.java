package org.lucas.component.common.hash;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.LongAdder;

import static com.google.common.base.Preconditions.checkArgument;

public class ShardingHashCode {

    private int realNodeCount;

    private int virtualNodeCount;

    private final HashFunction hash;

    private final SortedMap<Integer, Integer> bucketMap;

    public ShardingHashCode(int realNodeCount, int virtualNodeCount) {
        checkArgument(realNodeCount > 0, "realNodeCount must be positive: %s", realNodeCount);
        checkArgument(virtualNodeCount > 0, "virtualNodeCount must be positive: %s", realNodeCount);

        this.realNodeCount = realNodeCount;
        this.virtualNodeCount = virtualNodeCount;

        this.bucketMap = new TreeMap<>();
        this.hash = Hashing.murmur3_32();

        String virtualNodeNameFormat = "VIRTUAL-%s-NODE-%s";
        for (int i = 0; i < realNodeCount; i++) {
            for (int n = 0; n < virtualNodeCount; n++) {
                String virtualNodeName = String.format(virtualNodeNameFormat, i, n);
                this.bucketMap.put(this.hash.hashUnencodedChars(virtualNodeName).asInt(), i);
            }
        }
    }


    public int hashFor(String v) {
        SortedMap<Integer, Integer> tail = bucketMap.tailMap(hash.hashUnencodedChars(v).asInt());
        if (tail.isEmpty()) {
            return bucketMap.get(bucketMap.firstKey());
        }
        return tail.get(tail.firstKey());
    }

    public static void main(String[] args) throws Exception {
        ShardingHashCode hashCoding = new ShardingHashCode(32, 64);
        int count = 100000;
        Map<String, LongAdder> map = getMap();
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            int v = hashCoding.hashFor(Integer.toString(i));
            LongAdder longAdder = map.get(Integer.toString(v));
            longAdder.increment();
        }
        long endTime = System.currentTimeMillis();
        System.out.println(endTime - startTime);

        long c = 0;
        for (Map.Entry<String, LongAdder> entry : map.entrySet()) {
            System.out.println(String.format("%s=%s", entry.getKey(), entry.getValue().longValue()));
            c += entry.getValue().longValue();
        }
        System.out.println(c);
    }

    static Map<String, LongAdder> getMap() {
        Map<String, LongAdder> map = new HashMap<>();
        for (int i = 0; i < 32; i++) {
            map.put(Integer.toString(i), new LongAdder());
        }
        return map;
    }
}
