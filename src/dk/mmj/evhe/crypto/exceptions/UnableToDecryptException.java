package dk.mmj.evhe.crypto.exceptions;

/**
 * Exception thrown when unable to decrypt a given ciphertext
 */
public class UnableToDecryptException extends Exception {
    public UnableToDecryptException(String message) {
        super(message);
    }
}
