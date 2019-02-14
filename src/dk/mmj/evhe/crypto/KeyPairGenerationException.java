package dk.mmj.evhe.crypto;

/**
 * Thrown when key pair could not be generated
 */
public class KeyPairGenerationException extends Exception {
    KeyPairGenerationException(String message) {
        super(message);
    }
}
