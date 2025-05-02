/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Net.server.api;

/**
 * @author Tyler
 */
public final class ApiConstants {

    public static String PRODUCT_ID = "100";//110
    public static final String BASE_API_URL = "https://api.cellion.org";
    public static String CLIENT_ID = "100";//110
    public static String CLIENT_SECRET = "X#bcoY79FY#KU0NL9M#Kqr8j7v$9iy4C";//fyYj57*z2R0fFDLjSzv&&jv%9lV9i14C
    public static final String SDK_VERSION = "0.1"; //currently unused
    public static final String DISCORD_NOTIFICATION_URL = "https://discordapp.com/api/webhooks/271060725873508352/EAvU-IIWz_OEFwi794xkUMgEI5Q7DLDVgpSf2WMi_ddBdk2Z9Ss0coUNOSeTrAG96Ole";
    public static final String HIPCHAT_COLOR = "green";
    public static final boolean HIPCHAT_NOTIFY = true;

    public static final String FEATURED_URL = BASE_API_URL + "/cms/100/all/featured";
    public static final String PING_URL = BASE_API_URL + "/info";
    public static final String AUTH_URL = BASE_API_URL + "/oauth/token";
    public static final String USER_ACCESS_LOG_URL = BASE_API_URL + "/log/access";
    public static final String USER_INFO_URL = BASE_API_URL + "/users/me";
    public static final String USER_INFO_EX_URL = BASE_API_URL + "/users/%s";
    public static final String USER_ACCESS_CHECK_URL = USER_INFO_EX_URL + "/access?product_id=%s";
    public static final String USER_GRANT_LP_ACCESS = "/activate_purchase";
    public static final String BEST_ITEMS_URL = BASE_API_URL + "/shop/100/bestitems";

    public static final String MESSAGE_URL = BASE_API_URL + "/messages/";
    public static final String MESSAGE_LIST_URL = BASE_API_URL + "/list";
    public static final String MESSAGE_PROCESS_URL = "/process";
    public static final String MESSAGE_DELAY_URL = "/delay";
    public static final String MESSAGE_TRASH_URL = "/trash";

    public static final String HOOK_USER_ONLINE_URL = BASE_API_URL + "/hooks/%s/user_count";

    public static final String GET_CP_URL = BASE_API_URL + "/users/%s/lumierepoints";
    public static final String PURCHASE_ITEM_CP_URL = BASE_API_URL + "/shop/%s/purchase/U%d/%d";
    public static final String LOG_PURCHASE_EVENT = BASE_API_URL + "/shop/%s/log_purchase";

    public static final String JIRA_ISSUE_URL = "http://jira.twdtwd.com/rest/api/2/issue";
    public static final String JIRA_USERNAME = "Twdtwd";
    public static final String JIRA_PASSWORD = "kx6z9mpJEN6W";
    public static final String JIRA_PROJECT = "10000";
    public static final String JIRA_ISSUE_TYPE = "10001";

    public static long LUMIERE_FEATURE_REFRESH_RATE = 60 * 60 * 1000;
}
