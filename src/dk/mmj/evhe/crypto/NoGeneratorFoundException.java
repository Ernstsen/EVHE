package dk.mmj.evhe.crypto;

/**
 * Thrown when no generator g was found for the cyclic group Gq
 */
public class NoGeneratorFoundException extends Exception {
    NoGeneratorFoundException(String message) {
        super(message);
    }
}
