package dk.mmj.evhe.crypto;

import dk.mmj.evhe.crypto.zeroknowledge.VoteProofUtils;
import dk.mmj.evhe.entities.PersistedVote;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class BenchmarkVoteProofProofUtils {

    @Benchmark
    public void benchmarkValidationAsync(BenchmarkSecurityUtils.VoteState voteState, Blackhole blackhole) {
        long l = new Date().getTime() - 500;
        List<PersistedVote> collect = voteState.votes
                .parallelStream()
                .filter(v -> VoteProofUtils.verifyProof(v, voteState.publicKey)).collect(Collectors.toList());
        blackhole.consume(collect);
    }

    @Benchmark
    public void benchmarkValidationSync(BenchmarkSecurityUtils.VoteState voteState, Blackhole blackhole) {
        long l = new Date().getTime() - 500;
        List<PersistedVote> collect = voteState.votes
                .stream()
                .filter(v -> VoteProofUtils.verifyProof(v, voteState.publicKey)).collect(Collectors.toList());
        blackhole.consume(collect);
    }
}
