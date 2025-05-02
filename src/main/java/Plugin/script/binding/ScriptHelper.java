package Plugin.script.binding;

import Client.inventory.Equip;
import Net.server.MapleItemInformationProvider;
import Net.server.ShutdownServer;
import Packet.MaplePacketCreator;
import Server.channel.ChannelServer;
import Server.world.WorldBroadcastService;
import lombok.extern.slf4j.Slf4j;
import tools.Randomizer;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;

@Slf4j
public class ScriptHelper extends ScriptBase {
    public ScriptHelper() {
    }

    /**
     * 创建一个Point对象
     *
     * @param x
     * @param y
     * @return
     */
    public Point newPoint(int x, int y) {
        return new Point(x, y);
    }

    /**
     * 获取物品名称
     *
     * @param itemId
     * @return
     */
    public String getItemName(int itemId) {
        return MapleItemInformationProvider.getInstance().getName(itemId);
    }

    /**
     * 返回道具id是否存在
     *
     * @param itemId
     * @return
     */
    public boolean itemExists(int itemId) {
        return MapleItemInformationProvider.getInstance().itemExists(itemId);
    }

    /**
     * 返回道具id的Equip
     *
     * @param equipId
     * @return
     */
    public Equip itemEquip(int equipId) {
        return MapleItemInformationProvider.getInstance().getEquipById(equipId);
    }

    /**
     * 格式化當前日期对象
     *
     * @param format
     * @return
     */
    public String formatDate(String format) {
        return new SimpleDateFormat(format).format(new Date());
    }

    /**
     * 格式化時間戳对象
     *
     * @param timestamp
     * @param format
     * @return
     */
    public String formatDate(long timestamp, String format) {
        Date date = new Date(timestamp * 1000);
        return new SimpleDateFormat(format).format(date);
    }

    /**
     * 格式化日期对象
     *
     * @param date
     * @param format
     * @return
     */
    public String formatDate(Date date, String format) {
        return new SimpleDateFormat(format).format(date);
    }

    /**
     * @param str
     * @return
     */
    public byte[] getBytes(String str) {
        return str.getBytes();
    }

    public void shutdown(int time) {
        ShutdownServer server = ShutdownServer.getInstance();
        server.setTime(time * 1000);
        server.run();
    }

    public void worldBroadcastNotice(String notice) {
        WorldBroadcastService.getInstance().broadcastMessage(MaplePacketCreator.serverNotice(0x00, notice));
    }

    public void worldBroadcastMessage(String notice) {
        WorldBroadcastService.getInstance().broadcastMessage(MaplePacketCreator.serverMessage(notice));
    }

    public void channelBroadcastItemMessage(int channelID, String message, int itemId, int time) {
        if (ChannelServer.getInstance(channelID) != null) {
            ChannelServer.getInstance(channelID).startMapEffect(message, itemId, time);
        }
    }

    public void channelBroadcastNotice(int channelID, String notice) {
        log.error(ChannelServer.getInstance(channelID) != null ? "true" : "false");
        if (ChannelServer.getInstance(channelID) != null) {
            ChannelServer.getInstance(channelID).broadcastPacket(MaplePacketCreator.serverNotice(0x00, notice));
        }
    }

    public void channelBroadcastMessage(int channelID, String notice) {
        log.error(ChannelServer.getInstance(channelID) != null ? "true" : "false");
        if (ChannelServer.getInstance(channelID) != null) {
            ChannelServer.getInstance(channelID).broadcastPacket(MaplePacketCreator.serverMessage(notice));
        }
    }

    /***
     * 隨機整數
     * @param arg0
     * @return
     */
    public int randInt(int arg0) {
        return Randomizer.nextInt(arg0);
    }

    //    format --- "yy/MM/dd"
    public String getStringDate(String format) {

        // 获取当前日期和时间
        LocalDateTime currentDateTime = LocalDateTime.now();

        // 定义日期时间格式化模式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);

        // 格式化日期时间
        return currentDateTime.format(formatter);

    }

    public String getStringDate(long timestamp,String format) {

        // 获取当前日期和时间
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.systemDefault());

        // 定义日期时间格式化模式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);

        // 格式化日期时间
        return dateTime.format(formatter);

    }

    public long getWeekStart(long timestamp) {

        // 将时间戳转换为LocalDateTime
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.systemDefault());

        // 获取本周的周一
        LocalDateTime monday = dateTime.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));

        // 格式化日期时间
        return monday.atZone(ZoneId.systemDefault()).toEpochSecond();
    }

    public long getWeekEnd(long timestamp) {

        // 将时间戳转换为LocalDateTime
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.systemDefault());

        // 获取本周的周一
        LocalDateTime sunday = dateTime.with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY));

        // 格式化日期时间
        return sunday.atZone(ZoneId.systemDefault()).toEpochSecond();
    }
}
