package Server.channel.handler;

import Client.MapleCharacter;
import Client.MapleClient;
import Packet.GuildPacket;
import Server.world.WorldAllianceService;
import Server.world.WorldGuildService;
import Server.world.guild.MapleGuild;
import tools.data.MaplePacketReader;

public class AllianceHandler {

    public static void HandleAlliance(MaplePacketReader slea, MapleClient c, boolean denied) {
        if (c.getPlayer().getGuildId() <= 0) {
            c.sendEnableActions();
            return;
        }
        MapleGuild gs = WorldGuildService.getInstance().getGuild(c.getPlayer().getGuildId());
        if (gs == null) {
            c.sendEnableActions();
            return;
        }
        WorldAllianceService allianceService = WorldAllianceService.getInstance();
        //System.out.println("Unhandled GuildAlliance \n" + slea.toString());
        byte op = slea.readByte();
        if (c.getPlayer().getGuildRank() != 1 && op != 1) { //only updating doesn't need guild leader
            return;
        }
        if (op == 22) {
            denied = true;
        }
        int leaderid = 0;
        if (gs.getAllianceId() > 0) {
            leaderid = allianceService.getAllianceLeader(gs.getAllianceId());
        }
        //accept invite, and deny invite don't need allianceid.
        if (op != 4 && !denied) {
            if (gs.getAllianceId() <= 0 || leaderid <= 0) {
                return;
            }
        } else if (leaderid > 0 || gs.getAllianceId() > 0) { //infact, if they have allianceid it's suspicious
            return;
        }
        if (denied) {
            DenyInvite(c, gs);
            return;
        }
        MapleCharacter chr;
        int inviteid;
        switch (op) {
            case 1: //load... must be in world op
                for (byte[] pack : allianceService.getAllianceInfo(gs.getAllianceId(), false)) {
                    if (pack != null) {
                        c.announce(pack);
                    }
                }
                break;
            case 3: //invite
                final int newGuild = WorldGuildService.getInstance().getGuildLeader(slea.readMapleAsciiString());
                if (newGuild > 0 && c.getPlayer().getAllianceRank() == 1 && leaderid == c.getPlayer().getId()) {
                    chr = c.getChannelServer().getPlayerStorage().getCharacterById(newGuild);
                    if (chr != null && chr.getGuildId() > 0 && allianceService.canInvite(gs.getAllianceId())) {
                        chr.send(GuildPacket.sendAllianceInvite(allianceService.getAlliance(gs.getAllianceId()).getName(), c.getPlayer()));
                        WorldGuildService.getInstance().setInvitedId(chr.getGuildId(), gs.getAllianceId());
                    } else {
                        c.getPlayer().dropMessage(1, "請確認要聯盟的公會會長和您是在同一頻道.");
                    }
                } else {
                    c.getPlayer().dropMessage(1, "輸入的公會名字不正確，當前伺服器未找到該公會的信息.");
                }
                break;
            case 4: //accept invite... guildid that invited(int, a/b check) -> guildname that was invited? but we dont care about that
                inviteid = WorldGuildService.getInstance().getInvitedId(c.getPlayer().getGuildId());
                if (inviteid > 0) {
                    if (!allianceService.addGuildToAlliance(inviteid, c.getPlayer().getGuildId())) {
                        c.getPlayer().dropMessage(5, "An error occured when adding guild.");
                    }
                    WorldGuildService.getInstance().setInvitedId(c.getPlayer().getGuildId(), 0);
                }
                break;
            case 2: //leave; nothing
            case 6: //expel, guildid(int) -> allianceid(don't care, a/b check)
                final int gid;
                if (op == 6 && slea.available() >= 4) {
                    gid = slea.readInt();
                    if (slea.available() >= 4 && gs.getAllianceId() != slea.readInt()) {
                        break;
                    }
                } else {
                    gid = c.getPlayer().getGuildId();
                }
                if (c.getPlayer().getAllianceRank() <= 2 && (c.getPlayer().getAllianceRank() == 1 || c.getPlayer().getGuildId() == gid)) {
                    if (!allianceService.removeGuildFromAlliance(gs.getAllianceId(), gid, c.getPlayer().getGuildId() != gid)) {
                        c.getPlayer().dropMessage(5, "An error occured when removing guild.");
                    }
                }
                break;
            case 7: //change leader
                if (c.getPlayer().getAllianceRank() == 1 && leaderid == c.getPlayer().getId()) {
                    if (!allianceService.changeAllianceLeader(gs.getAllianceId(), slea.readInt())) {
                        c.getPlayer().dropMessage(5, "An error occured when changing leader.");
                    }
                }
                break;
            case 8: //title update
                if (c.getPlayer().getAllianceRank() == 1 && leaderid == c.getPlayer().getId()) {
                    String[] ranks = new String[5];
                    for (int i = 0; i < 5; i++) {
                        ranks[i] = slea.readMapleAsciiString();
                    }
                    allianceService.updateAllianceRanks(gs.getAllianceId(), ranks);
                }
                break;
            case 9:
                if (c.getPlayer().getAllianceRank() <= 2) {
                    if (!allianceService.changeAllianceRank(gs.getAllianceId(), slea.readInt(), slea.readByte())) {
                        c.getPlayer().dropMessage(5, "An error occured when changing rank.");
                    }
                }
                break;
            case 10: //notice update
                if (c.getPlayer().getAllianceRank() <= 2) {
                    final String notice = slea.readMapleAsciiString();
                    if (notice.length() > 100) {
                        break;
                    }
                    allianceService.updateAllianceNotice(gs.getAllianceId(), notice);
                }
                break;
            default:
                System.out.println("Unhandled GuildAlliance op: " + op + ", \n" + slea);
                break;
        }
        //c.sendEnableActions();
    }

    public static void DenyInvite(MapleClient c, MapleGuild gs) { //playername that invited -> guildname that was invited but we also don't care
        int inviteid = WorldGuildService.getInstance().getInvitedId(c.getPlayer().getGuildId());
        if (inviteid > 0) {
            int newAlliance = WorldAllianceService.getInstance().getAllianceLeader(inviteid);
            if (newAlliance > 0) {
                final MapleCharacter chr = c.getChannelServer().getPlayerStorage().getCharacterById(newAlliance);
                if (chr != null) {
                    chr.dropMessage(5, "[" + gs.getName() + "] 公會拒絕了聯盟的邀請.");
                }
                WorldGuildService.getInstance().setInvitedId(c.getPlayer().getGuildId(), 0);
            }
        }
        //c.sendEnableActions();
    }
}
