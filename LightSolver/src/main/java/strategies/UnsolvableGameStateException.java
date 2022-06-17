package strategies;

public class UnsolvableGameStateException extends Exception {
    public UnsolvableGameStateException(String message) {
        super(message);
    }

    public UnsolvableGameStateException(UnsolvableGameStateException lastError) {
        super(lastError);
    }
}
