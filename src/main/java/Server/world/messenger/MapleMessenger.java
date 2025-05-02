package Server.world.messenger;

import Client.MapleCharacter;
import Server.channel.ChannelServer;
import Server.world.WorldFindService;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;

/*
 * 聊天招待
 */
public class MapleMessenger implements Serializable {

    private static final long serialVersionUID = 9179541993413738569L;
    private final MapleMessengerCharacter[] members; //聊天招待的信息
    private final String[] silentLink; //難道是離線的玩家名字信息
    private final MessengerType type; //聊天招待的類型
    private final boolean hide; //是否是GM創建的聊天室
    private int id; //聊天招待的工作ID

    /*
     * 生成創建1個聊天室
     */
    public MapleMessenger(int id, MapleMessengerCharacter chrfor, MessengerType type, boolean gm) {
        this.id = id;
        this.type = type;
        this.hide = gm;
        members = new MapleMessengerCharacter[type.maxMembers]; //設置聊天招待的最大人
        silentLink = new String[type.maxMembers]; //設置聊天招待空閒位置的人數 也是是離線的人數
        members[0] = chrfor; //設置玩家的位置為 0 也是第1個位置
    }

    public MessengerType getType() {
        return type;
    }

    public boolean isHide() {
        return hide;
    }

    public void addMembers(int pos, MapleMessengerCharacter chrfor) {
        if (members[pos] != null) {
            return;
        }
        members[pos] = chrfor;
    }

    public boolean containsMembers(MapleMessengerCharacter member) {
        return getPositionByName(member.getName()) != -1;
    }

    public void addMember(MapleMessengerCharacter member) {
        int position = getLowestPosition();
        if (position != -1) {
            addMembers(position, member);
        }
    }

    public void removeMember(MapleMessengerCharacter member) {
        int position = getPositionByName(member.getName());
        if (position != -1) {
            members[position] = null;
        }
    }

    public void silentRemoveMember(MapleMessengerCharacter member) {
        int position = getPositionByName(member.getName());
        if (position != -1) {
            members[position] = null;
            silentLink[position] = member.getName();
        }
    }

    public void silentAddMember(MapleMessengerCharacter member) {
        for (int i = 0; i < silentLink.length; i++) {
            if (silentLink[i] != null && silentLink[i].equalsIgnoreCase(member.getName())) {
                addMembers(i, member);
                silentLink[i] = null;
                return;
            }
        }
    }

    /*
     * 更新聊天招待中的成員
     */
    public void updateMember(MapleMessengerCharacter member) {
        for (int i = 0; i < members.length; i++) {
            MapleMessengerCharacter chr = members[i];
            if (chr != null && chr.equals(member)) {
                members[i] = null;
                addMembers(i, member);
                return;
            }
        }
    }

    /*
     * 獲取聊天招待中在線的總人數
     */
    public int getMemberSize() {
        int ret = 0;
        for (MapleMessengerCharacter member : members) {
            if (member != null) {
                ret++;
            }
        }
        return ret;
    }

    /*
     * 獲取聊天招待中最小的位置
     */
    public int getLowestPosition() {
        for (int i = 0; i < members.length; i++) {
            if (members[i] == null) {
                return i;
            }
        }
        return -1;
    }

    /*
     * 獲取玩家在聊天招待中的位置
     * 找不到返回 -1
     */
    public int getPositionByName(String name) {
        for (int i = 0; i < members.length; i++) {
            MapleMessengerCharacter messengerchar = members[i];
            if (messengerchar != null && messengerchar.getName().equalsIgnoreCase(name)) {
                return i;
            }
        }
        return -1;
    }

    public MapleMessengerCharacter getMemberByPos(int pos) {
        return members[pos];
    }

    /*
     * 獲取聊天招待的工作ID
     */
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        return 31 + id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MapleMessenger other = (MapleMessenger) obj;
        return id == other.id;
    }

    public Collection<MapleMessengerCharacter> getMembers() {
        return Arrays.asList(members);
    }

    /*
     * 聊天招待中的玩家是否是監視狀態
     */
    public boolean isMonitored() {
        int ch = -1;
        for (MapleMessengerCharacter member : members) {
            if (member != null) {
                ch = WorldFindService.getInstance().findChannel(member.getName());
                if (ch != -1) {
                    MapleCharacter player = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(member.getName());
                    if (player != null && player.getClient() != null && player.getClient().isMonitored()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public String getMemberNamesDEBUG() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < members.length; i++) {
            if (members[i] != null) {
                sb.append(members[i].getName());
                if (i != members.length - 1) {
                    sb.append(',');
                }
            }
        }
        return sb.toString();
    }
}
