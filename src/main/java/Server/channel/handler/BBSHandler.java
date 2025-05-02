package Server.channel.handler;

import Client.MapleClient;
import Packet.GuildPacket;
import Server.world.WorldGuildService;
import Server.world.guild.MapleBBSThread;
import tools.data.MaplePacketReader;

import java.util.List;

public class BBSHandler {

    private static String correctLength(String in, int maxSize) {
        if (in.length() > maxSize) {
            return in.substring(0, maxSize);
        }
        return in;
    }

    public static void BBSOperation(MaplePacketReader slea, MapleClient c) {
        if (c.getPlayer().getGuildId() <= 0) {
            return; // expelled while viewing bbs or hax
        }
        int localthreadid = 0;
        byte action = slea.readByte();
        switch (action) {
            case 0: // 0 開放公布欄/ 1目錄(公告)/ 2我的內容
                if (slea.available() == 8) {
                    slea.readInt();
                    slea.readInt(); // 1 and 2
                } else {
                    slea.readInt();
                }
                break;
            case 1: // 新增內容
                // bool
                // if (bool) int
                // byte
                // String
                // String
                // Int
                // ---------
                // byte 1
                // int
                // int
                // String
                // ---------
//                slea.readByte(); // 0
//                slea.readInt(); // 0
//                String text = slea.readMapleAsciiString();
//                newBBSThread(c, "", text, 1, false);
                break;
            case 2:
                slea.readInt();
                break;
            case 4:
                if (slea.available() == 9) {
                    slea.readInt();
                    slea.readByte();
                    slea.readInt();
                } else {
                    slea.readInt();
                }
                break;
            case 5:
                slea.readInt();
                slea.readMapleAsciiString();
                break;
            case 6:
                if (slea.available() == 12) {
                    slea.readInt();
                    slea.readInt();
                    slea.readInt();
                } else {
                    slea.readInt();
                    slea.readInt();
                }
                break;
            case 8:
                slea.readInt();
                break;
        }
        /*  switch (action) {
            case 0: // start a new post
                boolean bEdit = slea.readByte() > 0; //是否編輯
                if (bEdit) {
                    localthreadid = slea.readInt();
                }
                boolean bNotice = slea.readByte() > 0; //是否為公告
                String title = correctLength(slea.readMapleAsciiString(), 25);
                String text = correctLength(slea.readMapleAsciiString(), 600);
                int icon = slea.readInt(); //是否有圖標
                if (icon >= 0x64 && icon <= 0x6a) {
                    if (!c.getPlayer().haveItem(5290000 + icon - 0x64, 1, false, true)) {
                        return; // hax, using an nx icon that s/he doesn't have
                    }
                } else if (icon < 0 || icon > 2) {
                    return; // hax, using an invalid icon
                }
                if (!bEdit) {
                    newBBSThread(c, title, text, icon, bNotice);
                } else {
                    editBBSThread(c, title, text, icon, localthreadid);
                }
                break;
            case 1: // 刪除發佈的信息
                localthreadid = slea.readInt();
                deleteBBSThread(c, localthreadid);
                break;
            case 2: // 刷新BBS信息
                int start = slea.readInt();
                listBBSThreads(c, start * 10);
                break;
            case 3: // 瀏覽公告或留言
                localthreadid = slea.readInt();
                displayThread(c, localthreadid);
                break;
            case 4: // 發佈留言
                localthreadid = slea.readInt();
                text = correctLength(slea.readMapleAsciiString(), 25);
                newBBSReply(c, localthreadid, text);
                break;
            case 5: // 刪除留言
                localthreadid = slea.readInt();
                int replyid = slea.readInt();
                deleteBBSReply(c, localthreadid, replyid);
                break;
        }*/
    }

    private static void listBBSThreads(MapleClient c, int start) {
        if (c.getPlayer().getGuildId() <= 0) {
            return;
        }
        c.announce(GuildPacket.BBSThreadList(WorldGuildService.getInstance().getBBS(c.getPlayer().getGuildId()), start));
    }

    private static void newBBSReply(MapleClient c, int localthreadid, String text) {
        if (c.getPlayer().getGuildId() <= 0) {
            return;
        }
        WorldGuildService.getInstance().addBBSReply(c.getPlayer().getGuildId(), localthreadid, text, c.getPlayer().getId());
        displayThread(c, localthreadid);
    }

    private static void editBBSThread(MapleClient c, String title, String text, int icon, int localthreadid) {
        if (c.getPlayer().getGuildId() <= 0) {
            return; // expelled while viewing?
        }
        WorldGuildService.getInstance().editBBSThread(c.getPlayer().getGuildId(), localthreadid, title, text, icon, c.getPlayer().getId(), c.getPlayer().getGuildRank());
        displayThread(c, localthreadid);
    }

    private static void newBBSThread(MapleClient c, String title, String text, int icon, boolean bNotice) {
        if (c.getPlayer().getGuildId() <= 0) {
            return; // expelled while viewing?
        }
        displayThread(c, WorldGuildService.getInstance().addBBSThread(c.getPlayer().getGuildId(), title, text, icon, bNotice, c.getPlayer().getId()));
        listBBSThreads(c, 0);
    }

    private static void deleteBBSThread(MapleClient c, int localthreadid) {
        if (c.getPlayer().getGuildId() <= 0) {
            return;
        }
        WorldGuildService.getInstance().deleteBBSThread(c.getPlayer().getGuildId(), localthreadid, c.getPlayer().getId(), c.getPlayer().getGuildRank());
    }

    private static void deleteBBSReply(MapleClient c, int localthreadid, int replyid) {
        if (c.getPlayer().getGuildId() <= 0) {
            return;
        }

        WorldGuildService.getInstance().deleteBBSReply(c.getPlayer().getGuildId(), localthreadid, replyid, c.getPlayer().getId(), c.getPlayer().getGuildRank());
        displayThread(c, localthreadid);
    }

    private static void displayThread(MapleClient c, int localthreadid) {
        if (c.getPlayer().getGuildId() <= 0) {
            return;
        }
        List<MapleBBSThread> bbsList = WorldGuildService.getInstance().getBBS(c.getPlayer().getGuildId());
        if (bbsList != null) {
            for (MapleBBSThread t : bbsList) {
                if (t != null && t.localthreadID == localthreadid) {
                    c.announce(GuildPacket.showThread(t));
                }
            }
        }
    }
}
