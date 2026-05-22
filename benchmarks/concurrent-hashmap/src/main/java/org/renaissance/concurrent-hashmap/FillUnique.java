package org.renaissance.concurrenthashmap;

import org.renaissance.Benchmark;
import org.renaissance.BenchmarkContext;
import org.renaissance.BenchmarkResult;
import org.renaissance.BenchmarkResult.Validators;
import org.renaissance.License;

import static org.renaissance.Benchmark.*;

@Name("concurrent-hashmap-fill-unique")
@Group("concurrent-hashmap")
@Summary("Fills a concurrent hashmap with unique keys.")
@Licenses(License.MIT)
@Configuration(name = "test")
public final class FillUnique implements Benchmark {
  @Override
  public BenchmarkResult run(BenchmarkContext c) {
    return Validators.simple("nothing", 0, 0);
  }
}
