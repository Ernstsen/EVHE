package dk.mmj.evhe.crypto;

/**
 * Exception thrown when unable to decrypt a given ciphertext
 */
public class UnableToDecryptException extends Exception {
    UnableToDecryptException(String message) {
        super(message);
    }
}
