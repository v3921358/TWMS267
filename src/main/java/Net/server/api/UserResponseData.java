/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Net.server.api;

/**
 * Class to hold information received from the Lumiere account Net.server after being authenticated. Holds general info about the user account.
 *
 * @author Tyler
 */
public class UserResponseData {

    private int id;
    private String name;
    private String email;
    private int ban_flag;
    private String ban_time;
    private String profile_name;
    private boolean purchase_access;
    private int auth_level;
    private int lumiere_point;
    private String ip;
    private int newsletter;
    private int verified;

    /**
     * Returns the ID for the user on the Lumiere Network. This is a unique id.
     *
     * @return The account ID for the user.
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the name of the account on Lumiere. This is a unique string.
     *
     * @return The account name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the email address linked to this users Lumiere account.
     *
     * @return The users email address.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Returns if the user is able to purchase Lumiere Points.
     *
     * @return Boolean if the user can purchase.
     */
    public boolean getPurchaseAccess() {
        return purchase_access;
    }

    /**
     * Returns the ban flag on the users account. These should correspond to the reasons in the WZ files.
     *
     * @return
     */
    public int getBanFlag() {
        return ban_flag;
    }

    /**
     * Returns an ISO formatted string for the date and time a user is network banned until.
     *
     * @return ISO Date Time formatted string.
     */
    public String getBanTime() {
        return ban_time;
    }

    /**
     * Returns the users profile name for Lumiere network. This is ment to be a public name used in the forums, etc for the user.
     *
     * @return
     */
    public String getProfileName() {
        return profile_name;
    }

    /**
     * Returns the authentication level of the user on the Lumiere network. Currently this corresponds to the GM levels on Rien.
     *
     * @return int of the users authentication level. Corresponds to GM level.
     */
    public int getAuthLevel() {
        return auth_level;
    }

    /**
     * Returns the amount of Lumiere Points the user currently has. These are network wide.
     *
     * @return The amount of LP the user currently has.
     */
    public int getLumierePoints() {
        return lumiere_point;
    }

    /**
     * Gets the users last IP address used on the web site.
     *
     * @return String representation of the users IP.
     */
    public String getIP() {
        return ip;
    }

    /**
     * Returns if the user has verified their email or not.
     *
     * @return {@code true} if the email is verified.
     */
    public boolean isVerified() {
        return verified == 1;
    }

    /**
     * Returns if the user opted to receive the newsletter for the network.
     *
     * @return {@code true} if the user wants the newsletters.
     */
    public int getNewsletter() {
        return newsletter;
    }
}
