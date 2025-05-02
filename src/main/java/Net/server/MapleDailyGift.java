package Net.server;

import Database.DatabaseConnection;
import Database.tools.SqlTool;
import Opcode.Headler.OutHeader;
import Packet.PacketHelper;
import tools.data.MaplePacketLittleEndianWriter;

import java.sql.ResultSet;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MapleDailyGift {

    private static final Map<Integer, DailyGiftMonth> rewards = new HashMap<>();

    public DailyGiftMonth getRewards() {
        return rewards.get(Calendar.getInstance().get(Calendar.MONTH) + 1);
    }

    public static void initialize() {
        rewards.clear();
        DatabaseConnection.domain(con -> {
            ResultSet rs = SqlTool.query(con, "SELECT * FROM `zdata_dailygifts`");
            while (rs.next()) {
                int id = rs.getInt("id");
                int month = rs.getInt("month");
                int day = rs.getInt("day");
                int itemid = rs.getInt("itemid");
                int count = rs.getInt("count");
                int commodityid = rs.getInt("commodityid");
                int term = rs.getInt("term");

                rewards.computeIfAbsent(month, m -> new DailyGiftMonth(getDaysInMonth(month)))
                        .dailyGifts.put(day, new DailyGiftInfo(id, month, day, itemid, count, commodityid, term));
            }
            return null;
        }, "已經完成重新加載:[HAPPY_DAY].");
    }

    private static int getDaysInMonth(int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, month - 1); // month is 1-based, Calendar.MONTH is 0-based
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    public static class DailyGiftMonth {
        Map<Integer, DailyGiftInfo> dailyGifts;

        public DailyGiftMonth(int daysInMonth) {
            dailyGifts = new HashMap<>(daysInMonth);
        }
    }

    public static class DailyGiftInfo {
        int id, month, day, itemid, count, commodityid, term;

        public DailyGiftInfo(int id, int month, int day, int itemid, int count, int commodityid, int term) {
            this.id = id;
            this.month = month;
            this.day = day;
            this.itemid = itemid;
            this.count = count;
            this.commodityid = commodityid;
            this.term = term;
        }
    }

    public static byte[] dailyGiftResult(final int n, final int n2, final int n3, final int n4) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.SIGIN_INFO.getValue());
        mplew.write(n);
        if (n > 0) {
            if (n > 1) {
                if (n == 2) {
                    mplew.writeInt(n2);
                    mplew.writeInt(n3);
                    if (n2 == 14) {
                        mplew.writeInt(n4);
                    }
                }
            }
            else {
                mplew.writeInt(0);
            }
        }
        return mplew.getPacket();
    }

    private static void addDailyGiftInfo(final MaplePacketLittleEndianWriter mplew, final MapleDailyGift.DailyGiftMonth a1318) {
       // mplew.writeLong(PacketHelper.getTime(a1318.startTime));
       // mplew.writeLong(PacketHelper.getTime(a1318.endTime));
       // mplew.writeInt(a1318.days);
       // mplew.writeInt(0);
       // mplew.writeInt(16700);
       // mplew.writeInt(999);
       // mplew.writeInt(a1318.dailyGifts.size());
       // for (Map.Entry<Integer, MapleDailyGift.DailyGiftInfo> entry : a1318.dailyGifts.entrySet()) {
       //     final MapleDailyGift.DailyGiftInfo gift = entry.getValue();
       //     mplew.writeInt(entry.getKey());
       //     mplew.writeInt(gift.itemid);
       //     mplew.writeInt(gift.count);
       //     mplew.writeInt(gift.commodityid);
       //     mplew.writeInt(gift.term * 24 * 60);
       //     mplew.write(0);
       //     mplew.writeInt(0);
       //     mplew.writeInt(0);
       //     mplew.write(0);
       // }
       // mplew.writeInt(33);
       // mplew.writeInt(0);
//     //   for (final Map.Entry<Integer, Integer> entry : a1318.unknownMap.entrySet()) {
//     //       mplew.writeInt(entry.getKey());
//     //       mplew.writeInt(entry.getValue());
//     //   }
        mplew.writeInt(0);
    }

    public static byte[] getSigninReward(int itemid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.SIGIN_INFO.getValue());

        mplew.write(1);
        mplew.writeInt(SIGNIN_TYPE.領取獎勵.ordinal());
        mplew.writeInt(itemid);

        return mplew.getPacket();
    }

    enum SIGNIN_TYPE {
        UNKNOWN,
        簽到窗口,
        領取獎勵,;
    }

}
