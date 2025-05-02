package tools.config;

public class TransformationException
        extends RuntimeException {
    private static final long serialVersionUID = -6641235751743285902L;

    public TransformationException() {
    }

    public TransformationException(String message) {
        /* 29 */
        super(message);
    }

    public TransformationException(String message, Throwable cause) {
        /* 39 */
        super(message, cause);
    }

    public TransformationException(Throwable cause) {
        /* 48 */
        super(cause);
    }
}
