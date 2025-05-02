/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Net.server.api;

/**
 * Class to hold information about responses from the Lumiere OAuth Net.server.
 *
 * @author Tyler
 */
public class TokenResponseData {

    private String access_token;
    private String token_type;
    private int expires_in;
    private String refresh_token;
    private String error;
    private boolean success = false;

    /**
     * Returns the access token used to send to the Lumiere API as authentication.
     *
     * @return The API Access Token. Use this in future Authorization headers.
     */
    public String getAccessToken() {
        return access_token;
    }

    /**
     * The token type used for the access token.
     *
     * @return Used in the Authorization header to specify the type of token.
     */
    public String getTokenType() {
        return token_type;
    }

    /**
     * Gets the amount of time the token is valid for in seconds.
     *
     * @return The amount of time the token is valid for.
     */
    public int getExpiresIn() {
        return expires_in;
    }

    /**
     * The refresh token is sent to the API to get a new Net.auth token before the current one expires.
     *
     * @return The Refresh token to send.
     */
    public String getRefreshToken() {
        return refresh_token;
    }

    /**
     * If an error occurred, this will return the error type. If no error occurred it will be null.
     *
     * @return The error that occurred.
     */
    public String getError() {
        return error;
    }

    /**
     * Returns true if the request was successful and an access token was obtained. If not check the return from {@link getError()} to find
     * out what went wrong.
     *
     * @return
     */
    public boolean getSuccess() {
        return success;
    }

    /**
     * Used to set if the request succeeded or not. This should be set after the API returns.
     *
     * @param succ {@code true} if the request succeeded, {@code false} if not.
     */
    public void setSuccess(boolean succ) {
        success = succ;
    }

    @Override
    public String toString() {
        return "TokenData: [" + access_token + ", " + token_type + ", " + expires_in
                + ", " + refresh_token + ", " + error + ", " + success + "]";
    }

}
