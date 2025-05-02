/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Net.server.api;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * @author William
 */
public enum ApiErrorCode implements ApiReturnCode {
    /* 
        For all Lumeire Network API statuses (including successes)
        Note: A good chunk aren't implemented in both the SDK and the API just yet.
              Most of these are just future proofing. See Confluence docs for more details.
     */
    UNDEFINED(-1, "Undefined error."),
    SUCCESS(0, null), // not implemented in Net.api yet
    PING_FAIL(1, "Could not ping the specified ping endpoint."),
    PING_YES(2, "API status reported to be okay."),
    PING_UH(3, "API status reported to be 'uh...'. Monitor closely."), // not implemented in Net.api yet
    PING_NO(4, "API status reported to be Bad. Abort recommended."), // not implemented in Net.api yet
    INVALID_SDK_VERSION(5, "SDK version is not a valid SDK number reported from the API."),
    UNSUPPORTED_SDK_VERSION(6, "SDK version no longer supported"),
    INVALID_CLIENT_CREDENTIALS(10, "The Client.client credentials were invalid."),
    NULL_SERVER_TOKEN(20, "The Net.server token was null and thus invalid. Consider using DEVMODE."),
    AUTHENTICATION_FAILURE(101, "The authentication system has encountered a failure. Please try again later."),
    INVALID_USER_CREDENTIALS(102, "Your ID or password is invalid. Please try again."),
    ACCOUNT_NOT_VERIFIED_BY_EMAIL(103, "Your account has not been verified yet. Please check your inbox for an email from us to verify your email."),
    ACCOUNT_REQUIRE_REVERIFICATION_BY_EMAIL(103, "Your account needs to be reverified as a result of an administrative account audit. Please check check your inbox from an email for us for more information."),
    ACCOUNT_BLOCKED_BY_ADMIN(110, "Your account has been blocked by an administrator. Please visit our Customer Support center at https://support.cellion.network and submit a ticket."),
    ACCOUNT_BLOCKED_BY_ADMIN_IRREVERISBLE(111, "Your account has been blocked by an administrator. This block is final and cannot be reversed."),
    ACCOUNT_BLOCKED_BY_ADMIN_FRAUD(112, "Your account has been blocked by an administrator for fraudulent purchases and/or charges. These blocks are final and cannot be reversed."),
    ACCOUNT_BLOCKED_BY_ADMIN_SUSPICIOUS(113, "Your account has been blocked by an administrator for suspicious activity. Please visit our Customer Support center at https://support.cellion.org to undergo a reverification process."),
    ACCOUNT_BLOCKED_BY_SYSTEM(114, "Your account has been blocked. Please visit our Customer Support center at https://support.cellion.org and submit a ticket."),
    ACCOUNT_BLOCKED_BY_SYSTEM_IRREVERSIBLE(115, "Your account has been blocked. This block is final and cannot be reversed."),
    ACCOUNT_BLOCKED_BY_SYSTEM_FRAUD(116, "Your account has been blocked for fraudulent Lumiere Point purchases and/or charges. Please visit our Customer Support center at https://support.cellion.org and submit a ticket."),
    ACCOUNT_BLOCKED_BY_SYSTEM_SUSPICIOUS(117, "Your account has been for suspicious activity. Please visit our Customer Support center at https://support.cellion.org to undergo a reverification process."),
    SYSTEM_ERROR(999, "Unknown system error.");

    private final int _value;
    private final String _message;
    private static final Map<Integer, ApiErrorCode> _lookup;

    ApiErrorCode(final int value, final String message) {
        this._value = value;
        this._message = message;
    }

    public int getValue() {
        return this._value;
    }

    public String getMessage() {
        return (this._message == null) ? this.toString() : this._message;
    }

    public static ApiErrorCode parse(final int value) {
        return ApiErrorCode._lookup.get(value);
    }

    public String getType() {
        return this.getClass().getName();
    }

    static {
        _lookup = new HashMap<Integer, ApiErrorCode>();
        for (final ApiErrorCode code : EnumSet.allOf(ApiErrorCode.class)) {
            ApiErrorCode._lookup.put(code.getValue(), code);
        }
    }
}
