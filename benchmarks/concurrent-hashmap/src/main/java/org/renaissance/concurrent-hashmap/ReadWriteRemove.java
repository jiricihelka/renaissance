package org.renaissance.concurrenthashmap;

import org.renaissance.Benchmark;
import org.renaissance.BenchmarkContext;
import org.renaissance.BenchmarkResult;
import org.renaissance.BenchmarkResult.Validators;
import org.renaissance.License;

import static org.renaissance.Benchmark.*;
import java.util.concurrent.ConcurrentHashMap;

@Name("concurrent-hashmap-read-write-remove")
@Group("concurrent-hashmap")
@Summary("Performs read, write, and remove operations on a concurrent hashmap.")
@Parameter(name = "thread_count", defaultValue = "$cpu.count")
@Parameter(name = "total_items", defaultValue = "1000000")
@Parameter(name = "key_range", defaultValue = "1000000")
@Parameter(name = "write_ratio", defaultValue = "0.1")
@Parameter(name = "remove_ratio", defaultValue = "0.1")
@Parameter(name = "load_factor", defaultValue = "0.75")
@Configuration(name = "2T-16M", settings = {"thread_count = 2", "total_items = 16000000"})
@Configuration(name = "4T-16M", settings = {"thread_count = 4", "total_items = 16000000"})
@Configuration(name = "8T-16M", settings = {"thread_count = 8", "total_items = 16000000"})
@Configuration(name = "16T-16M", settings = {"thread_count = 16", "total_items = 16000000"})
@Configuration(name = "32T-16M", settings = {"thread_count = 32", "total_items = 16000000"})
@Configuration(name = "64T-16M", settings = {"thread_count = 64", "total_items = 16000000"})
@Configuration(name = "serial-16M", settings = {"thread_count = 1", "total_items = 16000000"})
@Licenses(License.MIT)
public final class ReadWriteRemove implements Benchmark {
  @Override
  public BenchmarkResult run(BenchmarkContext c) {
    int threadCount = c.parameter("thread_count").toPositiveInteger();
    int totalItems = c.parameter("total_items").toPositiveInteger();
    int itemsPerThread = totalItems / threadCount;
    int keyRange = c.parameter("key_range").toPositiveInteger();
    double writeRatio = c.parameter("write_ratio").toDouble();
    double removeRatio = c.parameter("remove_ratio").toDouble();
    double loadFactor = c.parameter("load_factor").toDouble();
    ConcurrentHashMapReadWriteRemoveBenchmarkHelper helper = new ConcurrentHashMapReadWriteRemoveBenchmarkHelper(threadCount, itemsPerThread, (float) loadFactor);
    helper.runReadWrite(keyRange, writeRatio, removeRatio);

    // We can't validate the map size since keys are random and may collide, but we can at least check that it's not larger than the total number of inserted items.

    return Validators.simple("map size", 1, threadCount * itemsPerThread >= helper.mapSize() ? 1 : 0);
  }
}

final class ConcurrentHashMapReadWriteRemoveBenchmarkHelper {
    private final int threadCount;
    private final int itemsPerThread;
    private final ConcurrentHashMap<Integer, Integer> map;
    
    public ConcurrentHashMapReadWriteRemoveBenchmarkHelper(int threadCount, int itemsPerThread, float loadFactor) {
        this.threadCount = threadCount;
        this.itemsPerThread = itemsPerThread;
        this.map = new ConcurrentHashMap<>(16, loadFactor, threadCount);
    }
    
    public void runReadWrite(int keyRange, double writeRatio, double removeRatio) {
        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < itemsPerThread; j++) {
                    int key = (int) (Math.random() * keyRange);
                    double op = Math.random();
                    if (op < writeRatio) {
                        map.put(key, key);
                    } else if (op < writeRatio + removeRatio) {
                        map.remove(key);
                    } else {
                        Integer value = map.get(key);
                        if (value != null && value != key) {
                            throw new AssertionError("Incorrect value read from map: expected " + key + " but got " + value);
                        }
                    }
                }
            });
        }
        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
        
    }

    public int mapSize() {
        return map.size();
    }
}