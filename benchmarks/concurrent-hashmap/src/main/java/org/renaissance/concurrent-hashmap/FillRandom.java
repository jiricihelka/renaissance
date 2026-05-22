package org.renaissance.concurrenthashmap;

import org.renaissance.Benchmark;
import org.renaissance.BenchmarkContext;
import org.renaissance.BenchmarkResult;
import org.renaissance.BenchmarkResult.Validators;
import org.renaissance.License;

import static org.renaissance.Benchmark.*;
import java.util.concurrent.ConcurrentHashMap;

@Name("concurrent-hashmap-fill-random")
@Group("concurrent-hashmap")
@Summary("Fills a concurrent hashmap with random keys in parallel.")
@Parameter(name = "thread_count", defaultValue = "$cpu.count")
@Parameter(name = "total_items", defaultValue = "1000000")
@Parameter(name = "key_range", defaultValue = "1000000")
@Parameter(name = "load_factor", defaultValue = "0.75")
@Configuration(name = "serial-16M", settings = {"thread_count = 1", "total_items = 16000000"})
@Configuration(name = "parallel-16M", settings = {"thread_count = $cpu.count", "total_items = 16000000"})
@Configuration(name = "2T-16M", settings = {"thread_count = 2", "total_items = 16000000"})
@Configuration(name = "4T-16M", settings = {"thread_count = 4", "total_items = 16000000"})
@Configuration(name = "8T-16M", settings = {"thread_count = 8", "total_items = 16000000"})
@Configuration(name = "16T-16M", settings = {"thread_count = 16", "total_items = 16000000"})
@Configuration(name = "32T-16M", settings = {"thread_count = 32", "total_items = 16000000"})
@Configuration(name = "64T-16M", settings = {"thread_count = 64", "total_items = 16000000"})
@Licenses(License.MIT)
public final class FillRandom implements Benchmark {
  @Override
  public BenchmarkResult run(BenchmarkContext c) {
    int threadCount = c.parameter("thread_count").toPositiveInteger();
    int totalItems = c.parameter("total_items").toPositiveInteger();
    int itemsPerThread = totalItems / threadCount;
    int keyRange = c.parameter("key_range").toPositiveInteger();
    double loadFactor = c.parameter("load_factor").toDouble();
    ConcurrentHashMapFillRandomBenchmarkHelper helper = new ConcurrentHashMapFillRandomBenchmarkHelper(threadCount, itemsPerThread, (float) loadFactor);
    helper.fillRandom(keyRange);

    // We can't validate the map size since keys are random and may collide, but we can at least check that it's not larger than the total number of inserted items.

    return Validators.simple("map size", 1, threadCount * itemsPerThread >= helper.mapSize() ? 1 : 0);
  }
}

final class ConcurrentHashMapFillRandomBenchmarkHelper {
  private final int threadCount;
  private final int itemsPerThread;
  private final ConcurrentHashMap<Integer, Integer> map;

  public ConcurrentHashMapFillRandomBenchmarkHelper(int threadCount, int itemsPerThread, float loadFactor) {
    this.threadCount = threadCount;
    this.itemsPerThread = itemsPerThread;
    this.map = new ConcurrentHashMap<>(16, loadFactor, threadCount);
  }

  public void fillRandom(int keyRange) {
    Thread[] threads = new Thread[threadCount];
    for (int i = 0; i < threadCount; i++) {
      threads[i] = new Thread(() -> {
        for (int j = 0; j < itemsPerThread; j++) {
          int key = (int) (Math.random() * keyRange);
          map.put(key, key);
        }
      });
      threads[i].start();
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