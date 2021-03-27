package org.lucas.component.common.hash.sharding;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import java.util.SortedMap;
import java.util.TreeMap;

public class ShardingHashCode {

    private final HashFunction hash;

    private final SortedMap<Integer, Integer> bucketMap;

    public ShardingHashCode(int realNodeCount, int virtualNodeCount) {
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
}
