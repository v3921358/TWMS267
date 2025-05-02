package tools.json;

class JSONException extends RuntimeException {
    private static final long serialVersionUID = 0L;

    public JSONException(String message) {
        /* 7 */
        super(message);
    }

    public JSONException(String message, Throwable cause) {
        /* 11 */
        super(message, cause);
    }

    public JSONException(Throwable cause) {
        /* 15 */
        super(cause.getMessage(), cause);
    }
}
