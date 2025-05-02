package Plugin.script.binding;

import Client.MapleCharacter;
import Client.MapleReward;
import Client.inventory.Item;
import Config.constants.enums.UserChatMessageType;
import Database.tools.SqlTool;
import Net.server.MapleItemInformationProvider;
import Packet.MaplePacketCreator;
import Packet.UIPacket;
import Server.channel.ChannelServer;
import Server.world.WorldBroadcastService;
import lombok.extern.slf4j.Slf4j;
import tools.DateUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ScriptBase {

    private final Map<String, Object> props = new ConcurrentHashMap<String, Object>();

    public ScriptBase() {

    }

    /**
     * 全服公告 带前缀[公告事项]
     *
     * @param message
     */
    public void broadcastNotice(String message) {
        broadcastPlayerNotice(9, "[公告事項]" + message);
    }

    /**
     * 全服公告 不带前缀
     *
     * @param message
     */
    public void broadcastNoticeWithoutPrefix(String message) {
        broadcastPlayerNotice(9, message);
    }

    /**
     * 全服 广播玩家聊天窗口信息
     *
     * @param type
     * @param message
     */
    public void broadcastPlayerNotice(int type, String message) {
        WorldBroadcastService.getInstance().broadcastMessage(MaplePacketCreator.serverNotice(type, message));
        WorldBroadcastService.getInstance().broadcastMessage(MaplePacketCreator.spouseMessage(UserChatMessageType.getByType(type), message));
    }

    /**
     * 全服广播 右下角气泡文字
     *
     * @param npcid
     * @param time
     * @param msg
     * @param sound
     */
    public void broadcastPopupSay(int npcid, int time, String msg, String sound) {
        for (ChannelServer cserv : ChannelServer.getAllInstances()) {
            for (MapleCharacter mch : cserv.getPlayerStorage().getAllCharacters()) {
                mch.getClient().announce(UIPacket.addPopupSay(npcid, time, msg, sound));
            }
        }
    }

    /**
     * 全服广播 道具喇叭消息
     *
     * @param name
     * @param message
     * @param item
     * @param color
     */
    public void broadcastItemMessage(String name, String message, Item item, int color) {
        String itemName = item.getName();
        String prefix = "";
        if (name != null) {
            prefix = name;
        }
        WorldBroadcastService.getInstance().broadcastMessage(MaplePacketCreator.gachaponMsg(prefix + message + "[" + itemName + "]", item));
    }

    /***
     * 数据库插入操作
     * @param sql
     * @param values
     */
    public void customSqlInsert(String sql, Object... values) {
        SqlTool.update(sql, values);
    }

    /***
     * 数据库更新操作
     * @param sql
     * @param values
     * @return
     */
    public int customSqlUpdate(final String sql, final Object... values) {
        return SqlTool.executeUpdate(sql, values);
    }

    /**
     * 数据库获取操作
     *
     * @param sql
     * @param values
     * @return
     */
    public List<Map<String, Object>> customSqlResult(final String sql, final Object... values) {
        return SqlTool.customSqlResult(sql, values);
    }

    /***
     *  在線獎勵
     * @param itemId
     * @param quantity
     * @param message
     * @param duration
     */
    public void addItemReward(int itemId, int quantity, String message, int duration) {
        int type = MapleReward.道具;
        if (MapleItemInformationProvider.getInstance().isCash(itemId)) {
            type = MapleReward.現金道具;
        }
        for (ChannelServer cserv : ChannelServer.getAllInstances()) {
            for (MapleCharacter mch : cserv.getPlayerStorage().getAllCharacters()) {
                mch.addReward(true, DateUtil.getNextDayTime(0), duration <= 0 ? 0 : (DateUtil.getNextDayTime(duration) - 60000), type, quantity, itemId, message);
                mch.updateReward();
                mch.dropMessage(1, "收到管理員發來的禮物，請到左側獎勵箱領收。");
            }
        }
    }

    /**
     * 设置全局共享变量
     *
     * @param key
     * @param value
     */
    public void setGlobalVariable(String key, Object value) {
        if (value == null || "".equals(value)) {
            props.remove(key);
        } else {
            props.put(key, value);
        }
    }

    /**
     * 获取全局共享变量
     *
     * @param key
     * @return
     */
    public Object getGlobalVariable(String key) {
        return props.get(key);
    }

    /**
     * 在服务端打印调试信息
     *
     * @param s
     */
    public void debug(Object s) {
        log.info(s.toString());
    }

}
