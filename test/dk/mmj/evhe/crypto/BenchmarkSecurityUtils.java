package dk.mmj.evhe.crypto;

import dk.mmj.evhe.entities.CipherText;
import dk.mmj.evhe.entities.KeyPair;
import dk.mmj.evhe.entities.PersistedVote;
import dk.mmj.evhe.entities.PublicKey;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static dk.mmj.evhe.crypto.TestUtils.generateKeysFromP2048bitsG2;
import static dk.mmj.evhe.crypto.TestUtils.generateVotes;

@SuppressWarnings("unused, WeakerAccess")
public class BenchmarkSecurityUtils {

    @Benchmark
    public void benchmarkFilterSync(VoteState voteState, Blackhole blackhole) {
        long l = new Date().getTime() - 500;
        List<PersistedVote> collect = voteState.votes
                .stream()
                .filter(v -> v.getTs().getTime() < l).collect(Collectors.toList());
        blackhole.consume(collect);

    }

    @Benchmark
    public void benchmarkFilterAsync(VoteState voteState, Blackhole blackhole) {
        long l = new Date().getTime() - 500;
        List<PersistedVote> collect = voteState.votes
                .parallelStream()
                .filter(v -> v.getTs().getTime() < l).collect(Collectors.toList());
        blackhole.consume(collect);

    }

    @Benchmark
    public void benchmarkSumSync(VoteState voteState, Blackhole blackhole) {
        CipherText oldSum = SecurityUtils.voteSum(voteState.votes, voteState.publicKey);
        blackhole.consume(oldSum);
    }

    @Benchmark
    public void benchmarkSumAsync(VoteState voteState, Blackhole blackhole) {
        CipherText concSum = SecurityUtils.concurrentVoteSum(voteState.votes, voteState.publicKey, voteState.partitionSize);
    }

    @State(Scope.Benchmark)
    public static class VoteState {

        @Param({"100", "500", "1000"})
        public int size;

        public int partitionSize = size / 10;

        public List<PersistedVote> votes;
        public PublicKey publicKey;

        @Setup(Level.Trial)
        public void setUp() {
            KeyPair keyPair = generateKeysFromP2048bitsG2();
            publicKey = keyPair.getPublicKey();
            votes = generateVotes(size, publicKey);
        }
    }

}
