package org.renaissance.concurrenthashmap;

import org.renaissance.Benchmark;
import org.renaissance.BenchmarkContext;
import org.renaissance.BenchmarkResult;
import org.renaissance.BenchmarkResult.Validators;
import org.renaissance.License;

import static org.renaissance.Benchmark.*;
import java.util.concurrent.ConcurrentHashMap;

@Name("concurrent-hashmap-read-write")
@Group("concurrent-hashmap")
@Summary("Performs read and write operations on a concurrent hashmap.")
@Parameter(name = "thread_count", defaultValue = "$cpu.count")
@Parameter(name = "items_per_thread", defaultValue = "1000000")
@Parameter(name = "key_range", defaultValue = "1000000")
@Parameter(name = "write_ratio", defaultValue = "0.1")
@Parameter(name = "load_factor", defaultValue = "0.75")
@Configuration(name = "2T-1M", settings = {"thread_count = 2", "items_per_thread = 1000000"})
@Configuration(name = "4T-1M", settings = {"thread_count = 4", "items_per_thread = 1000000"})
@Configuration(name = "8T-1M", settings = {"thread_count = 8", "items_per_thread = 1000000"})
@Configuration(name = "16T-1M", settings = {"thread_count = 16", "items_per_thread = 1000000"})
@Configuration(name = "32T-1M", settings = {"thread_count = 32", "items_per_thread = 1000000"})
@Configuration(name = "64T-1M", settings = {"thread_count = 64", "items_per_thread = 1000000"})
@Configuration(name = "serial-1M", settings = {"thread_count = 1", "items_per_thread = 1000000"})
@Configuration(name = "serial-2M", settings = {"thread_count = 1", "items_per_thread = 2000000"})
@Configuration(name = "serial-4M", settings = {"thread_count = 1", "items_per_thread = 4000000"})
@Configuration(name = "serial-8M", settings = {"thread_count = 1", "items_per_thread = 8000000"})
@Configuration(name = "serial-16M", settings = {"thread_count = 1", "items_per_thread = 16000000"})
@Configuration(name = "serial-32M", settings = {"thread_count = 1", "items_per_thread = 32000000"})
@Configuration(name = "serial-64M", settings = {"thread_count = 1", "items_per_thread = 64000000"})
@Licenses(License.MIT)
public final class ReadWrite implements Benchmark {
  @Override
  public BenchmarkResult run(BenchmarkContext c) {
    int threadCount = c.parameter("thread_count").toPositiveInteger();
    int itemsPerThread = c.parameter("items_per_thread").toPositiveInteger();
    int keyRange = c.parameter("key_range").toPositiveInteger();
    double writeRatio = c.parameter("write_ratio").toDouble();
    double loadFactor = c.parameter("load_factor").toDouble();
    ConcurrentHashMapReadWriteBenchmarkHelper helper = new ConcurrentHashMapReadWriteBenchmarkHelper(threadCount, itemsPerThread, (float) loadFactor);
    helper.runReadWrite(keyRange, writeRatio);

    // We can't validate the map size since keys are random and may collide, but we can at least check that it's not larger than the total number of inserted items.

    return Validators.simple("map size", 1, threadCount * itemsPerThread >= helper.mapSize() ? 1 : 0);
  }
}

final class ConcurrentHashMapReadWriteBenchmarkHelper {
    private final int threadCount;
    private final int itemsPerThread;
    private final ConcurrentHashMap<Integer, Integer> map;
    
    public ConcurrentHashMapReadWriteBenchmarkHelper(int threadCount, int itemsPerThread, float loadFactor) {
        this.threadCount = threadCount;
        this.itemsPerThread = itemsPerThread;
        this.map = new ConcurrentHashMap<>(16, loadFactor, threadCount);
    }
    
    public void runReadWrite(int keyRange, double writeRatio) {
        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
        threads[i] = new Thread(() -> {
            for (int j = 0; j < itemsPerThread; j++) {
            int key = (int) (Math.random() * keyRange);
            if (Math.random() < writeRatio) {
                map.put(key, key);
            } else {
                Integer value = map.get(key);
                if (value != null && value != key) {
                throw new AssertionError("Incorrect value read from map: expected " + key + " but got " + value);
                }
            }
            }
        });
        }
        for (Thread t : threads) {
        t.start();
        }
        for (Thread t : threads) {
        try {
            t.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        }
    }

    public int mapSize() {
        return map.size();
    }
}