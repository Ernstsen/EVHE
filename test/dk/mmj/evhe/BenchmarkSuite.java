package dk.mmj.evhe;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class BenchmarkSuite {

    public static void main(String[] args) throws Exception {
        Options opt = new OptionsBuilder()
                .include(".").
                        warmupIterations(1).
                        measurementIterations(5).
                        threads(1).
                        forks(1).
                        build();
        new Runner(opt).run();
    }
}
