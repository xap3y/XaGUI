package eu.xap3y.xagui.exception;

public class PageOutOfBoundException extends RuntimeException {
    public PageOutOfBoundException(String message) {
        super(message);
    }

    public PageOutOfBoundException() {
        super("Requested page is out of bounds!");
    }
}
