
package tools.json;

final class JSONPointerException
        extends JSONException {
    private static final long serialVersionUID = 8872944667561856751L;

    public JSONPointerException(String message) {
        /* 38 */
        super(message);
    }

    public JSONPointerException(String message, Throwable cause) {
        /* 42 */
        super(message, cause);
    }
}
