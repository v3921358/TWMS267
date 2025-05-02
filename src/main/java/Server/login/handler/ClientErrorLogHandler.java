package Server.login.handler;

import Client.MapleClient;
import Client.MapleJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.data.MaplePacketReader;

public class ClientErrorLogHandler {

    private static final Logger log = LoggerFactory.getLogger("PacketErrorLog");

    public static void handlePacket(MaplePacketReader slea, MapleClient c) {
        if (slea == null) {
            return;
        }
        //byte[] packet = slea.read((int) slea.available());
        int stringLen = slea.readInt();
        String sData = slea.readAsciiString(stringLen);
        String CRLF = ("" + ((char) 0x0D) + ((char) 0x0A));
        String[] Elogs = sData.split(CRLF);

        for (String Elog : Elogs) {
            String[] errInfo = Elog.split("|");
            switch (errInfo[1]) {
                case "18":
                    System.out.println("Elog(" + errInfo[1] + ") 封包解析錯誤 Opcode(" + errInfo[34] + ")");
                    break;
                case "10":
                    System.out.println("Elog(" + errInfo[1] + ") 封包異常 Opcode(" + errInfo[27] + ")");
                    break;
                case "15":
                    System.out.println("Elog(" + errInfo[1] + ") ZException ");
                    break;
            }
            log.warn(Elog);
        }


        String AccountName = "null";
        String charName = "null";
        String charLevel = "null";
        String charJob = "null";
        String Map = "null";
        try {
            AccountName = c.getAccountName();
        } catch (Throwable e) {
        }
        try {
            charName = c.getPlayer().getName();
        } catch (Throwable e) {
        }
        try {
            charLevel = String.valueOf(c.getPlayer().getLevel());
        } catch (Throwable e) {
        }
        try {
            charJob = MapleJob.getTrueNameById(c.getPlayer().getJobWithSub()) + "(" + String.valueOf(c.getPlayer().getJob()) + ")";
        } catch (Throwable e) {
        }
        try {
            Map = c.getPlayer().getMap().toString();
        } catch (Throwable e) {
        }
        if ("null".equals(AccountName)) {
            return;
        }
        log.error("\r\n"
                + "帳號:" + AccountName + "\r\n"
                + "角色:" + charName + "(等級:" + charLevel + ")" + "\r\n"
                + "職業:" + charJob + "\r\n"
                + "地圖:" + Map + "\r\n"
                + "錯誤類型: ClientError\r\n"
                + sData + "\r\n");
    }
}
