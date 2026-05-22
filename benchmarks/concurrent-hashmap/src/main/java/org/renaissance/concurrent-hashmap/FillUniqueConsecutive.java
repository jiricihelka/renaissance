package org.renaissance.concurrenthashmap;

import org.renaissance.Benchmark;
import org.renaissance.BenchmarkContext;
import org.renaissance.BenchmarkResult;
import org.renaissance.BenchmarkResult.Validators;
import org.renaissance.License;

import static org.renaissance.Benchmark.*;
import java.util.concurrent.ConcurrentHashMap;

@Name("concurrent-hashmap-fill-unique-consecutive")
@Group("concurrent-hashmap")
@Summary("Fills a concurrent hashmap with unique keys (consecutive integers) in parallel.")
@Parameter(name = "thread_count", defaultValue = "$cpu.count")
@Parameter(name = "items_per_thread", defaultValue = "1000000")
@Parameter(name = "prealloc", defaultValue = "false")
@Parameter(name = "load_factor", defaultValue = "0.75")
@Licenses(License.MIT)
@Configuration(name = "serial-1M", settings = {"thread_count = 1", "items_per_thread = 1000000"})
@Configuration(name = "serial-1M-prealloc", settings = {"thread_count = 1", "items_per_thread = 1000000", "prealloc = true"})
@Configuration(name = "parallel-1M", settings = {"thread_count = $cpu.count", "items_per_thread = 1000000"})
@Configuration(name = "parallel-1M-prealloc", settings = {"thread_count = $cpu.count", "items_per_thread = 1000000", "prealloc = true"})
@Configuration(name = "serial-2M", settings = {"thread_count = 1", "items_per_thread = 2000000"})
@Configuration(name = "serial-2M-prealloc", settings = {"thread_count = 1", "items_per_thread = 2000000", "prealloc = true"})
@Configuration(name = "serial-4M", settings = {"thread_count = 1", "items_per_thread = 4000000"})
@Configuration(name = "serial-4M-prealloc", settings = {"thread_count = 1", "items_per_thread = 4000000", "prealloc = true"})
@Configuration(name = "serial-8M", settings = {"thread_count = 1", "items_per_thread = 8000000"})
@Configuration(name = "serial-8M-prealloc", settings = {"thread_count = 1", "items_per_thread = 8000000", "prealloc = true"})
@Configuration(name = "serial-16M", settings = {"thread_count = 1", "items_per_thread = 16000000"})
@Configuration(name = "serial-16M-prealloc", settings = {"thread_count = 1", "items_per_thread = 16000000", "prealloc = true"})
@Configuration(name = "serial-32M", settings = {"thread_count = 1", "items_per_thread = 32000000"})
@Configuration(name = "serial-32M-prealloc", settings = {"thread_count = 1", "items_per_thread = 32000000", "prealloc = true"})
@Configuration(name = "serial-64M", settings = {"thread_count = 1", "items_per_thread = 64000000"})
@Configuration(name = "serial-64M-prealloc", settings = {"thread_count = 1", "items_per_thread = 64000000", "prealloc = true"})
@Configuration(name = "2T-1M", settings = {"thread_count = 2", "items_per_thread = 1000000"})
@Configuration(name = "4T-1M", settings = {"thread_count = 4", "items_per_thread = 1000000"})
@Configuration(name = "8T-1M", settings = {"thread_count = 8", "items_per_thread = 1000000"})
@Configuration(name = "16T-1M", settings = {"thread_count = 16", "items_per_thread = 1000000"})
@Configuration(name = "32T-1M", settings = {"thread_count = 32", "items_per_thread = 1000000"})
@Configuration(name = "64T-1M", settings = {"thread_count = 64", "items_per_thread = 1000000"})
@Configuration(name = "2T-1M-prealloc", settings = {"thread_count = 2", "items_per_thread = 1000000", "prealloc = true"})
@Configuration(name = "4T-1M-prealloc", settings = {"thread_count = 4", "items_per_thread = 1000000", "prealloc = true"})
@Configuration(name = "8T-1M-prealloc", settings = {"thread_count = 8", "items_per_thread = 1000000", "prealloc = true"})
@Configuration(name = "16T-1M-prealloc", settings = {"thread_count = 16", "items_per_thread = 1000000", "prealloc = true"})
@Configuration(name = "32T-1M-prealloc", settings = {"thread_count = 32", "items_per_thread = 1000000", "prealloc = true"})
@Configuration(name = "64T-1M-prealloc", settings = {"thread_count = 64", "items_per_thread = 1000000", "prealloc = true"})
public final class FillUniqueConsecutive implements Benchmark {
  @Override
  public BenchmarkResult run(BenchmarkContext c) {
    int threadCount = c.parameter("thread_count").toPositiveInteger();
    int itemsPerThread = c.parameter("items_per_thread").toPositiveInteger();
    boolean prealloc = c.parameter("prealloc").toBoolean();
    double loadFactor = c.parameter("load_factor").toDouble();
    ConcurrentHashMapFillConsecutiveBenchmarkHelper helper = new ConcurrentHashMapFillConsecutiveBenchmarkHelper(threadCount, itemsPerThread, prealloc, (float) loadFactor);
    helper.fillUnique();

    return Validators.simple("map size", threadCount * itemsPerThread, helper.mapSize());
  }
}

final class ConcurrentHashMapFillConsecutiveBenchmarkHelper {
  private final int threadCount;
  private final int itemsPerThread;
  private final ConcurrentHashMap<Integer, Integer> map;

  public ConcurrentHashMapFillConsecutiveBenchmarkHelper(int threadCount, int itemsPerThread, boolean prealloc, float loadFactor) {
    this.threadCount = threadCount;
    this.itemsPerThread = itemsPerThread;
    this.map = new ConcurrentHashMap<>(prealloc ? threadCount * itemsPerThread : 16, loadFactor, threadCount);
  }

  public void fillUnique() {
    Thread[] threads = new Thread[threadCount];
    for (int i = 0; i < threadCount; i++) {
      final int threadIndex = i;
      threads[i] = new Thread(() -> {
        int startKey = threadIndex * itemsPerThread;
        int endKey = startKey + itemsPerThread;
        for (int key = startKey; key < endKey; key++) {
          map.put(key, key);
        }
      });
      threads[i].start();
    }
    for (Thread thread : threads) {
      try {
        thread.join();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }

  public int mapSize() {
    return map.size();
  }
}


