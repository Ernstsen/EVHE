package dk.mmj.evhe.crypto;

import dk.mmj.evhe.crypto.exceptions.UnableToDecryptException;
import dk.mmj.evhe.crypto.keygeneration.KeyGenerationParameters;
import dk.mmj.evhe.crypto.zeroknowledge.VoteProofUtils;
import dk.mmj.evhe.entities.*;
import org.junit.Test;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import static dk.mmj.evhe.crypto.TestUtils.*;
import static org.junit.Assert.*;

public class TestSecurityUtils {

    @Test
    public void shouldCreateCorrectVote1() {
        KeyPair keyPair = generateKeysFromP2048bitsG2();
        String id = "TESTID";
        VoteDTO voteDTO = SecurityUtils.generateVote(1, id, keyPair.getPublicKey());

        boolean verified = VoteProofUtils.verifyProof(voteDTO, keyPair.getPublicKey());
        assertTrue("Unable to verify generated vote", verified);

        try {
            int message = ElGamal.homomorphicDecryption(keyPair, voteDTO.getCipherText(), 1000);
            assertEquals("Decrypted message to wrong value", 1, message);
        } catch (UnableToDecryptException e) {
            fail("Unable to decrypt generated ciphertext");
        }
    }

    @Test
    public void shouldCreateCorrectVote0() {
        KeyPair keyPair = generateKeysFromP2048bitsG2();
        String id = "TESTID";
        VoteDTO voteDTO = SecurityUtils.generateVote(0, id, keyPair.getPublicKey());

        boolean verified = VoteProofUtils.verifyProof(voteDTO, keyPair.getPublicKey());
        assertTrue("Unable to verify generated vote", verified);

        try {
            int message = ElGamal.homomorphicDecryption(keyPair, voteDTO.getCipherText(), 1000);
            assertEquals("Decrypted message to wrong value", 0, message);
        } catch (UnableToDecryptException e) {
            fail("Unable to decrypt generated ciphertext");
        }
    }

    @Test
    public void shouldReturn3AsLagrangeCoefficientForIndex1WithSParams() {
        int[] authorityIndexes = new int[]{1, 2, 3};

        BigInteger lagrangeCoefficient = SecurityUtils.generateLagrangeCoefficient(authorityIndexes, 1, BigInteger.valueOf(5));

        assertEquals("Lagrange coefficient incorrect", BigInteger.valueOf(3), lagrangeCoefficient);
    }

    @Test
    public void shouldReturn2AsLagrangeCoefficientForIndex2WithSParams() {
        int[] authorityIndexes = new int[]{1, 2, 3};

        BigInteger lagrangeCoefficient = SecurityUtils.generateLagrangeCoefficient(authorityIndexes, 2, BigInteger.valueOf(5));

        assertEquals("Lagrange coefficient incorrect", BigInteger.valueOf(2), lagrangeCoefficient);
    }

    @Test
    public void shouldReturn1AsLagrangeCoefficientForIndex3WithSParams() {
        int[] authorityIndexes = new int[]{1, 2, 3};

        BigInteger lagrangeCoefficient = SecurityUtils.generateLagrangeCoefficient(authorityIndexes, 3, BigInteger.valueOf(5));

        assertEquals("Lagrange coefficient incorrect", BigInteger.valueOf(1), lagrangeCoefficient);
    }

    private void testRecoveringOfSecretKey(KeyGenerationParameters params, int[] authorityIndexes, int excludedIndex) {
        BigInteger[] polynomial = SecurityUtils.generatePolynomial(1, params.getPrimePair().getQ());
        Map<Integer, BigInteger> secretValues = SecurityUtils.generateSecretValues(polynomial, 3, params.getPrimePair().getQ());

        BigInteger acc = BigInteger.ZERO;
        for (Map.Entry<Integer, BigInteger> e : secretValues.entrySet()) {
            if (e.getKey() != excludedIndex) {
                BigInteger lagrangeCoefficient = SecurityUtils.generateLagrangeCoefficient(authorityIndexes, e.getKey(), params.getPrimePair().getQ());
                acc = acc.add(e.getValue().multiply(lagrangeCoefficient));
            }
        }

        assertEquals("Secret keys did not match", polynomial[0], acc.mod(params.getPrimePair().getQ()));
    }

    @Test
    public void shouldRecoverSecretKeyWithSecrets123WhenNIs3WithSParams() {
        // No index is excluded
        testRecoveringOfSecretKey(getKeyGenParamsFromP11G2(), new int[]{1, 2, 3}, 0);
    }

    @Test
    public void shouldRecoverSecretKeyWithSecrets12WhenNIs3WithSParams() {
        testRecoveringOfSecretKey(getKeyGenParamsFromP11G2(), new int[]{1, 2}, 3);
    }

    @Test
    public void shouldRecoverSecretKeyWithSecrets13WhenNIs3WithSParams() {
        testRecoveringOfSecretKey(getKeyGenParamsFromP11G2(), new int[]{1, 3}, 2);
    }

    @Test
    public void shouldRecoverSecretKeyWithSecrets23WhenNIs3WithSParams() {
        testRecoveringOfSecretKey(getKeyGenParamsFromP11G2(), new int[]{2, 3}, 1);
    }

    @Test
    public void shouldRecoverSecretKeyWithSecrets123WhenNIs3WithLParams() {
        // No index is excluded
        testRecoveringOfSecretKey(getKeyGenParamsFromP2048bitsG2(), new int[]{1, 2, 3}, 0);
    }

    @Test
    public void shouldRecoverSecretKeyWithSecrets12WhenNIs3WithLParams() {
        testRecoveringOfSecretKey(getKeyGenParamsFromP2048bitsG2(), new int[]{1, 2}, 3);
    }

    @Test
    public void shouldRecoverSecretKeyWithSecrets13WhenNIs3WithLParams() {
        testRecoveringOfSecretKey(getKeyGenParamsFromP2048bitsG2(), new int[]{1, 3}, 2);
    }

    @Test
    public void shouldRecoverSecretKeyWithSecrets23WhenNIs3WithLParams() {
        testRecoveringOfSecretKey(getKeyGenParamsFromP2048bitsG2(), new int[]{2, 3}, 1);
    }

    private void testRecoveringOfPublicKey(List<Integer> excludedIndexes, boolean positiveTest) {
        KeyGenerationParameters params = getKeyGenParamsFromP2048bitsG2();
        BigInteger p = params.getPrimePair().getP();
        BigInteger q = params.getPrimePair().getQ();
        BigInteger g = params.getGenerator();
        BigInteger[] polynomial = SecurityUtils.generatePolynomial(1, q);
        BigInteger h = g.modPow(polynomial[0], p);

        Map<Integer, BigInteger> secretValues = SecurityUtils.generateSecretValues(polynomial, 3, q).entrySet().stream()
                .filter(e -> !excludedIndexes.contains(e.getKey())).collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
        Map<Integer, BigInteger> publicValues = SecurityUtils.generatePublicValues(secretValues, g, p);

        BigInteger hFromPartials = SecurityUtils.combinePartials(publicValues, p);

        if (positiveTest) {
            assertEquals("Public keys did not match", h, hFromPartials);
        } else {
            assertNotEquals("Public keys match; they should not match", h, hFromPartials);
        }
    }

    @Test
    public void shouldBeAbleToRecoverPublicKeyWithSecrets123WhenNIs3() {
        testRecoveringOfPublicKey(Collections.singletonList(0), true);
    }

    @Test
    public void shouldBeAbleToRecoverPublicKeyWithSecrets12WhenNIs3() {
        testRecoveringOfPublicKey(Collections.singletonList(3), true);
    }

    @Test
    public void shouldBeAbleToRecoverPublicKeyWithSecrets13WhenNIs3() {
        testRecoveringOfPublicKey(Collections.singletonList(2), true);
    }

    @Test
    public void shouldBeAbleToRecoverPublicKeyWithSecrets23WhenNIs3() {
        testRecoveringOfPublicKey(Collections.singletonList(1), true);
    }

    @Test
    public void shouldNotBeAbleToRecoverPublicKeyWithSecret1WhenNIs2() {
        testRecoveringOfPublicKey(Arrays.asList(2, 3), false);
    }

    @Test
    public void shouldNotBeAbleToRecoverPublicKeyWithSecret2WhenNIs2() {
        testRecoveringOfPublicKey(Arrays.asList(1, 3), false);
    }

    @Test
    public void shouldNotBeAbleToRecoverPublicKeyWithSecret3WhenNIs2() {
        testRecoveringOfPublicKey(Arrays.asList(1, 2), false);
    }

    @Test
    public void shouldBeSameSumNoWrongProof() {
        KeyPair keyPair = generateKeysFromP2048bitsG2();
        PublicKey publicKey = keyPair.getPublicKey();

        ArrayList<VoteDTO> votes = new ArrayList<>();
        int amount = 200;
        for (int i = 0; i < amount; i++) {
            votes.add(SecurityUtils.generateVote(i % 2, "ID" + 1, publicKey));
        }

        long time = new Date().getTime();
        CipherText oldSum = SecurityUtils.voteSum(votes, publicKey);
        long elapsedOld = new Date().getTime() - time;
        System.out.println("Did old sum of " + amount + " votes in " + elapsedOld + "ms");

        time = new Date().getTime();
        CipherText concSum = SecurityUtils.concurrentVoteSum(votes, publicKey, amount / 10);
        long elapsedConc = new Date().getTime() - time;
        System.out.println("Did concurrent sum of " + amount + " votes in " + elapsedConc + "ms");

        assertEquals("Sums did not match.", oldSum, concSum);
    }

    @Test
    public void shouldBeSameSumSOMEWrongProof() {
        KeyPair keyPair = generateKeysFromP2048bitsG2();
        PublicKey publicKey = keyPair.getPublicKey();

        int amount = 2000;
        List<? extends VoteDTO> votes = generateVotes(amount, publicKey);

        CipherText oldSum = SecurityUtils.voteSum(votes, publicKey);

        CipherText concSum = SecurityUtils.concurrentVoteSum(votes, publicKey, amount / 10);

        assertEquals("Sums did not match.", oldSum, concSum);
    }

    @Test
    public void benchmarkFilter() {
        KeyPair keyPair = generateKeysFromP2048bitsG2();
        PublicKey publicKey = keyPair.getPublicKey();
        long endTime = new Date().getTime() + 5000;

        int amount = 2000;
        List<PersistedVote> votes = generateVotes(amount, publicKey);

        List<PersistedVote> collect = votes.stream().filter(v -> v.getTs().getTime() < endTime).collect(Collectors.toList());

        List<PersistedVote> collectConc = votes.stream().filter(v -> v.getTs().getTime() < endTime).collect(Collectors.toList());

        assertEquals("Filters did not match in results", collect.size(), collectConc.size());
    }
}