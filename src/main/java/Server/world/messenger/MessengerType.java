/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Server.world.messenger;

/**
 * @author PlayDK
 */
public enum MessengerType {

    隨機聊天(2, true),
    隨機多人聊天(6, true),
    好友聊天(6, false);
    public final int maxMembers;
    public final boolean random;

    MessengerType(int maxMembers, boolean random) {
        this.maxMembers = maxMembers;
        this.random = random;
    }

    public static MessengerType getMessengerType(int maxMembers, boolean random) {
        for (MessengerType mstype : MessengerType.values()) {
            if (mstype.maxMembers == maxMembers && mstype.random == random) {
                return mstype;
            }
        }
        return null;
    }
}
