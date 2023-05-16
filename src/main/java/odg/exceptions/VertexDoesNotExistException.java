package odg.exceptions;

import java.io.Serial;

public class VertexDoesNotExistException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;

    public VertexDoesNotExistException() {
        super("ODG initialization failed: one of the vertex does not exist. Please, verify the specification.");
    }
}
