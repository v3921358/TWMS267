package Server.channel.handler;

import Client.MapleCharacter;
import Client.MapleClient;
import Client.SecondaryStat;
import Client.SecondaryStatValueHolder;
import Config.constants.skills.菈菈;
import Config.constants.skills.阿戴爾;
import Net.server.MapleStatInfo;
import Net.server.buffs.MapleStatEffect;
import Net.server.maps.ForceAtomObject;
import Opcode.Headler.OutHeader;
import Packet.AdelePacket;
import SwordieX.client.party.PartyMember;
import tools.data.MaplePacketLittleEndianWriter;
import tools.data.MaplePacketReader;

import java.awt.*;
import java.util.Collections;
import java.util.Map;

public class AdeleHandler {

    public static void AdeleChargeRequest(MaplePacketReader slea, MapleClient c, MapleCharacter player) {
        if (player == null || player.getMap() == null) {
            return;
        }
        slea.readInt();
        MapleStatEffect effect = player.getSkillEffect(阿戴爾.乙太);
        if (effect != null && !player.isSkillCooling(阿戴爾.乙太)) {
            player.handleAdeleCharge(effect.getZ());
            player.registerSkillCooldown(effect.getSourceId(), effect.getW() * 1000, true);
            if ((effect = player.getSkillEffect(阿戴爾.創造)) != null) {
                player.handleAdeleObjectSword(effect, null);
            }
            c.announce(AdelePacket.AdeleChargeResult(true));
        } else {
            c.announce(AdelePacket.AdeleChargeResult(false));
        }
    }


    public static void ForceAtomObjectRemove(MaplePacketReader slea, MapleClient c, MapleCharacter player) {
        if (player == null || player.getMap() == null) {
            return;
        }
        int size = slea.readInt();
        int objswordID = slea.readInt();
        int skillid = slea.readInt();
        ForceAtomObject sword = player.getForceAtomObjects().remove(objswordID);
        if (sword == null) {
            sword = new ForceAtomObject(objswordID, 0, 0, 0, 0, 0);
        }
        for (int x = 0; x < size; x++) {
            player.getMap().broadcastMessage(AdelePacket.ForceAtomObjectRemove(c.getPlayer().getId(), objswordID+x, 1), sword.Position);
        }
    }

    public static void ForceAtomObjectMove(MaplePacketReader slea, MapleClient c, MapleCharacter player) {
        if (player == null || player.getMap() == null) {
            return;
        }
        slea.readInt();
        int count = slea.readInt();
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_ForceAtomObjectMove.getValue());
        mplew.writeInt(player.getId());
        mplew.writeInt(count);
        for (int i = 0; i < count; i++) {
            mplew.writeInt(slea.readInt());
            mplew.writeInt(slea.readInt());
            int count2 = slea.readInt();
            mplew.writeInt(count2);
            for (int j = 0; j < count2; j++) {
                mplew.writeInt(slea.readInt());
                mplew.writeInt(slea.readInt());
            }
        }
        c.announce(mplew.getPacket());
    }


    public static void ForceAtomObjectAction(MaplePacketReader slea, MapleClient c, MapleCharacter player) {
        if (player == null || player.getMap() == null) {
            return;
        }
        long pos = slea.getPosition();
        int unk = slea.readInt();
        int idx = slea.readInt();
        ForceAtomObject faObj = null;
        Map<Integer, ForceAtomObject> objsMap = player.getForceAtomObjects();
        for (ForceAtomObject obj : objsMap.values()) {
            if (idx == obj.Idx) {
                faObj = obj;
                break;
            }
        }
        if (faObj != null && 菈菈.發現_江水流動之地 == faObj.SkillId && faObj.DataIndex == 31) {
            int unk1 = slea.readInt();
            int unk2 = slea.readInt();
            int unk3 = slea.readInt();
            int unk4 = slea.readInt();
            int expire = slea.readInt();
            ForceAtomObject obj = null;
            for (ForceAtomObject ob : objsMap.values()) {
                if (32 == ob.DataIndex && 菈菈.發現_江水流動之地 == ob.SkillId && ob.ValueList.isEmpty()) {
                    obj = ob;
                    break;
                }
            }
            if (obj == null) {
                obj = new ForceAtomObject(player.getSpecialStat().gainForceCounter(), 32, 0, player.getId(), 0, 菈菈.發現_江水流動之地);
                obj.Idk1 = 20;
                obj.Position = new Point(0, 1);
                obj.ObjPosition = new Point(player.getPosition().x, player.getPosition().y);
                objsMap.put(obj.Idx, obj);
            }
            obj.Expire = expire;
            obj.addX(idx);
            player.getMap().broadcastMessage(AdelePacket.ForceAtomObject(player.getId(), Collections.singletonList(obj), 0), player.getPosition());
        } else {
            slea.seek(pos);
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
            mplew.writeOpcode(OutHeader.LP_ForceAtomObjectAction);
            mplew.writeInt(player.getId());
            mplew.write(slea.read((int) slea.available()));
            player.getMap().broadcastMessage(player, mplew.getPacket(), player.getPosition());
        }
    }

    public static void UserHealHPBySkillRequest(MaplePacketReader slea, MapleClient c, MapleCharacter player) {
        if (player == null || player.getMap() == null) {
            return;
        }
        slea.readInt();
        int skillID = 阿戴爾.復原;
        MapleStatEffect effect = player.getSkillEffect(skillID);
        SecondaryStatValueHolder holder = player.getBuffStatValueHolder(SecondaryStat.LWRestore);
        if (effect != null && holder != null) {
            // TODO
//            c.announce(EffectPacket.SkillSpecialAffected(-1, skillID, player.isFacingLeft()));
//            player.getMap().broadcastMessage(player, EffectPacket.SkillSpecialAffected(-1, skillID, player.isFacingLeft()), player.getPosition());
            player.addHPMP(0, -effect.getInfo().get(MapleStatInfo.mpRCon));
            if (player.getParty() != null) {
                player.getParty().getMembers().stream()
                        .filter(it -> it.isOnline() && it.getFieldID() == player.getMapId())
                        .map(PartyMember::getChr)
                        .forEach(it -> it.addHPMP(effect.getT(), 0));
            } else {
                player.addHPMP(effect.getT(), 0);
            }
        }
    }
}
