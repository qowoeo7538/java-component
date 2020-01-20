package org.lucas.component.common;

import org.junit.jupiter.api.Test;
import org.lucas.component.common.hash.ShardingHashCode;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

public class ShardingHashCodeTests {

    @Test
    public void testHashFor() {
        ShardingHashCode hashCoding = new ShardingHashCode(32, 64);
        int count = 100000;
        Map<String, LongAdder> map = new HashMap<>();
        for (int i = 0; i < 32; i++) {
            map.put(Integer.toString(i), new LongAdder());
        }
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

}
