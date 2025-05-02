/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Net.server.api;

/**
 * Handles API Exceptions.
 *
 * @author William
 */
public class ApiException extends Exception {

    private final ApiErrorCode _code;
    private String _message;

    @Override
    public String getMessage() {
        if (this._message != null) {
            return this._message;
        }
        if (this._code != null) {
            return this._code.getMessage();
        }
        return super.getMessage();
    }

    public ApiErrorCode getApiErrorCode() {
        return this._code;
    }

    public ApiException(final ApiErrorCode code) {
        this(code, (String) null);
    }

    public ApiException(final ApiErrorCode code, final Throwable cause) {
        this(code, null, cause);
    }

    public ApiException(final ApiErrorCode code, final String message) {
        this(code, message, null);
    }

    public ApiException(final ApiErrorCode code, final String message, final Throwable cause) {
        super(code.getMessage(), cause);
        this._code = code;
        this._message = code.getMessage();
        if (message != null) {
            this._message = this._message + "(" + message + ")";
        }
    }
}
