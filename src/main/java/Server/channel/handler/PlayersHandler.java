package Server.channel.handler;

import Client.*;
import Client.anticheat.CheatingOffense;
import Client.anticheat.ReportType;
import Client.inventory.Equip;
import Client.inventory.Item;
import Client.inventory.MapleInventoryType;
import Client.inventory.MapleRing;
import Client.skills.Skill;
import Client.skills.SkillFactory;
import Config.constants.GameConstants;
import Config.constants.ItemConstants;
import Config.constants.JobConstants;
import Config.constants.enums.ScriptType;
import Config.constants.skills.冒險家_技能群組.type_法師.主教;
import Config.constants.skills.菈菈;
import Config.constants.skills.重砲指揮官;
import Net.server.MapleInventoryManipulator;
import Net.server.MapleItemInformationProvider;
import Net.server.buffs.MapleStatEffect;
import Net.server.life.MapleMonster;
import Net.server.life.MobSkill;
import Net.server.life.MobSkillFactory;
import Net.server.maps.*;
import Net.server.quest.MapleQuest;
import Opcode.Headler.OutHeader;
import Opcode.Opcode.EffectOpcode;
import Packet.EffectPacket;
import Packet.MaplePacketCreator;
import Packet.UIPacket;
import Server.world.WorldBroadcastService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.Pair;
import tools.Randomizer;
import tools.data.MaplePacketLittleEndianWriter;
import tools.data.MaplePacketReader;

import javax.script.ScriptException;
import java.awt.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PlayersHandler {
    private static final Logger log = LoggerFactory.getLogger(PlayersHandler.class);
    private static final MapleClient c = new MapleClient();

    public static void GiveFame(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        int who = slea.readInt();
        int mode = slea.readByte();
        int famechange = mode == 0 ? -1 : 1;
        MapleCharacter target = MapleCharacter.getCharacterById(who);
        if (target == null || target == chr) { // faming self
            chr.getCheatTracker().registerOffense(CheatingOffense.FAMING_SELF, "不能對自身操作.");
            return;
        } else if (chr.getLevel() < 15) {
            chr.getCheatTracker().registerOffense(CheatingOffense.FAMING_UNDER_15, "等級小於15級.");
            return;
        }
        switch (mode) {
            case 1:
                if (Math.abs(target.getFame() + famechange) <= 99999) {
                    target.addFame(famechange);
                    target.updateSingleStat(MapleStat.人氣, target.getFame());
                }
                if (!chr.isGm()) {
                    chr.hasGivenFame(target);
                }
                c.announce(MaplePacketCreator.giveFameResponse(mode, target.getName(), target.getFame()));
                target.getClient().announce(MaplePacketCreator.receiveFame(mode, chr.getName()));
                break;
            case 3:
                c.announce(MaplePacketCreator.giveFameErrorResponse(3));
                break;
            case 4:
                c.announce(MaplePacketCreator.giveFameErrorResponse(4));
                break;
        }
    }

    public static void requestEnterTownPortal(MaplePacketReader slea, MapleCharacter chr) {
        int oid = slea.readInt();
        boolean mode = slea.readByte() == 0; // specifies if backwarp or not, 1 town to target, 0 target to town
        for (MapleMapObject obj : chr.getMap().getAllTownPortalsThreadsafe()) {
            TownPortal door = (TownPortal) obj;
            if (door.getOwnerId() == oid) {
                door.warp(chr, mode);
                break;
            }
        }
    }

    public static void EnterRandomPortalRequest(MaplePacketReader slea, MapleCharacter chr) {
        int oid = slea.readInt();
        slea.readByte();
        chr.send(MaplePacketCreator.getRandomPortalTryEnterRequest());
        MapleRandomPortal portal = null;
        for (MapleMapObject obj : chr.getMap().getAllRandomPortalThreadsafe()) {
            if (obj.getObjectId() == oid) {
                portal = (MapleRandomPortal) obj;
                break;
            }
        }
        if (portal == null || portal.getOwerid() != chr.getId()) {
            return;
        }
        chr.getScriptManager().startRandomPortalScript(portal.getOwerid(), portal.getObjectId(), portal.getAppearType().getScript());
    }


    public static void UseMechDoor(MaplePacketReader slea, MapleCharacter chr) {
        if (chr == null || chr.getMap() == null) {
            return;
        }
        int oid = slea.readInt();
        Point pos = slea.readPos();
        int mode = slea.readByte(); // specifies if backwarp or not, 1 town to target, 0 target to town
        chr.getClient().sendEnableActions();
        for (MapleMapObject obj : chr.getMap().getAllMechDoorsThreadsafe()) {
            MechDoor door = (MechDoor) obj;
            if (door == null) {
                continue;
            }
            if (door.getOwnerId() == oid && door.getId() == mode) {
                chr.setPosition(pos);
//                chr.getMap().movePlayer(chr, pos);
                chr.getMap().objectMove(-1, chr, null);
                break;
            }
        }
    }

    public static void UseAffectedArea(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        if (chr == null || chr.getMap() == null) {
            return;
        }
        int mode = slea.readByte();
        int oid = slea.readInt();
        MapleAffectedArea area;
        if ((area = chr.getMap().getAffectedAreaByOid(oid)) == null) {
            return;
        }
        if (area.getSkillID() != slea.readInt()) {
            return;
        }
        Point p;
        if (slea.available() >= 8) {
            p = slea.readPosInt();
        } else {
            p = slea.readPos();
        }
        if (!area.getArea().contains(p)) {
            return;
        }
        switch (area.getSkillID()) {
            case 主教.神聖之泉: {
                if (area.getHealCount() > 0) {
                    if (area.getOwnerId() == chr.getId() || (chr.getParty() != null && chr.getParty().getPartyMemberByID(area.getOwnerId()) != null)) {
                        int healHp = chr.getStat().getCurrentMaxHP() * area.getEffect().getX() / 100;
                        chr.addHP(healHp);
                        area.setHealCount(area.getHealCount() - 1);
                        if (chr.isDebug()) {
                            chr.dropMessage(5, "使用神聖之泉 - 恢復血量: " + healHp + " 百分比: " + area.getEffect().getX() + " 剩餘次數: " + area.getHealCount());
                        }
                        c.announce(EffectPacket.encodeUserEffectLocal(area.getSkillID(), EffectOpcode.UserEffect_SkillAffected, chr.getLevel(), area.getSkillLevel()));
                        chr.getMap().broadcastMessage(chr, EffectPacket.onUserEffectRemote(chr, area.getSkillID(), EffectOpcode.UserEffect_SkillAffected, chr.getLevel(), area.getSkillLevel()), false);
                    }
                } else if (chr.isDebug()) {
                    chr.dropMessage(5, "使用神聖之泉出現錯誤 - 源泉恢復的剩餘次數: " + area.getHealCount() + " 模式: " + mode);
                }
                if (area.getHealCount() <= 0) {
                    area.cancel();
                    chr.getMap().disappearMapObject(area);
                    if (area.getOwner() != null) {
                        area.getOwner().dispelEffect(area.getSkillID());
                    }
                }
                break;
            }
            case 主教.神聖之水: {
                if (area.getOwnerId() == chr.getId() || (chr.getParty() != null && chr.getParty().getPartyMemberByID(area.getOwnerId()) != null)) {
                    chr.addHPMP(area.getHealHPR(), 0);

                    c.announce(EffectPacket.encodeUserEffectLocal(area.getSkillID(), EffectOpcode.UserEffect_SkillAffected, chr.getLevel(), area.getSkillLevel()));
                    chr.getMap().broadcastMessage(chr, EffectPacket.onUserEffectRemote(chr, area.getSkillID(), EffectOpcode.UserEffect_SkillAffected, chr.getLevel(), area.getSkillLevel()), false);

                    area.cancel();
                    chr.getMap().disappearMapObject(area);
                }
                break;
            }
            case 重砲指揮官.精準轟炸_2: {
                if (area.getOwnerId() == chr.getId() || (chr.getParty() != null && chr.getParty().getPartyMemberByID(area.getOwnerId()) != null)) {
                    chr.addHPMP(area.getHealHPR(), 0);

                    Skill skill = SkillFactory.getSkill(重砲指揮官.精準轟炸_3);
                    if (skill != null && area.getOwner() != null && area.getEffect() != null) {
                        MapleStatEffect effect = skill.getEffect(area.getEffect().getLevel());
                        if (effect != null) {
                            effect.applyBuffEffect(area.getOwner(), chr, effect.getBuffDuration(area.getOwner()), false, false, true, null);
                        }
                    }

                    area.cancel();
                    chr.getMap().disappearMapObject(area);

                    c.announce(EffectPacket.encodeUserEffectLocal(area.getSkillID(), EffectOpcode.UserEffect_SkillAffected, chr.getPosition().x, chr.getPosition().y));
                    chr.getMap().broadcastMessage(chr, EffectPacket.onUserEffectRemote(chr, area.getSkillID(), EffectOpcode.UserEffect_SkillAffected, chr.getPosition().x, chr.getPosition().y), false);
                }
                break;
            }
            case 菈菈.發現_風之鞦韆: {
                final MapleCharacter playerObject;
                final MapleStatEffect effect;
                if ((playerObject = chr.getMap().getPlayerObject(area.getOwnerId())) != null
                        && (effect = playerObject.getSkillEffect(菈菈.發現_風之鞦韆_2)) != null
                        && ((playerObject.getParty() != null && playerObject.getParty() == chr.getParty())
                        || playerObject == chr)) {
                    effect.applyTo(chr);
                }
                break;
            }
        }
    }

    public static void TransformPlayer(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        chr.updateTick(slea.readInt());
        short slot = slea.readShort();
        int itemId = slea.readInt();
        String target = slea.readMapleAsciiString();
        Item toUse = c.getPlayer().getInventory(MapleInventoryType.USE).getItem(slot);
        if (toUse == null || toUse.getQuantity() < 1 || toUse.getItemId() != itemId) {
            c.sendEnableActions();
            return;
        }
        if (itemId == 2212000) { //聖誕節組隊藥水
            MapleCharacter search_chr = chr.getMap().getCharacterByName(target);
            if (search_chr != null) {
                MapleItemInformationProvider.getInstance().getItemEffect(2210023).applyTo(search_chr);
                search_chr.dropMessage(6, chr.getName() + " has played a prank on you!");
                MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
            } else {
                chr.dropMessage(1, "在當前地圖中未找到 '" + target + "' 的玩家.");
            }
        }
        c.sendEnableActions();
    }

    public static void HitReactor(MaplePacketReader slea, MapleClient c) {
        int oid = slea.readInt();
        //int charPos = slea.readInt();
        //short stance = slea.readShort();
        MapleReactor reactor = c.getPlayer().getMap().getReactorByOid(oid);
        if (reactor == null || !reactor.isAlive()) {
            return;
        }
        reactor.hitReactor(c);
    }

    public static void TouchReactor(MaplePacketReader slea, MapleClient c) {
        int oid = slea.readInt();
        slea.readInt();
        byte type = slea.readByte();
        MapleCharacter chr = c.getPlayer();
        MapleReactor reactor = chr.getMap().getReactorByOid(oid);
        if (reactor == null) {
            return;
        }
        if (chr.getScriptManager().isActive(ScriptType.Reactor)
                && chr.getScriptManager().getParentIDByScriptType(ScriptType.Reactor) == reactor.getReactorId()) {
            try {
                chr.getScriptManager().getInvocableByType(ScriptType.Reactor).invokeFunction("action", reactor, type);
            } catch (ScriptException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        } else {
            String result = chr.getScriptManager().startReactorScript(reactor);
            if (result == null) return;
        }
        if (type <= 0 || !reactor.isAlive() || reactor.getTouch() == 0) {
            //System.out.println("點擊反應堆出現錯誤 - !touched: " + !touched + " !isAlive: " + !reactor.isAlive() + " Touch: " + reactor.getTouch());
            return;
        }
        if (chr.isAdmin()) {
            chr.dropMessage(5, "反應堆信息 - oid: " + oid + " Touch: " + reactor.getTouch() + " isTimerActive: " + reactor.isTimerActive() + " ReactorType: " + reactor.getReactorType());
        }
        if (reactor.getTouch() == 2) {
            c.getPlayer().getScriptManager().startReactorScript(reactor);
//            ReactorScriptManager.getInstance().act(c, reactor); //not sure how touched boolean comes into play
        } else if (reactor.getTouch() == 1 && !reactor.isTimerActive()) {
            if (reactor.getReactorType() == 100) {
                int itemid = GameConstants.getCustomReactItem(reactor.getReactorId(), reactor.getReactItem().getLeft());
                if (chr.haveItem(itemid, reactor.getReactItem().getRight())) {
                    if (reactor.getArea().contains(chr.getPosition())) {
                        MapleInventoryManipulator.removeById(c, ItemConstants.getInventoryType(itemid), itemid, reactor.getReactItem().getRight(), true, false);
                        reactor.hitReactor(c);
                    } else {
                        chr.dropMessage(5, "距離太遠。請靠近後重新嘗試。");
                    }
                } else {
                    chr.dropMessage(5, "You don't have the item required.");
                }
            } else {
                reactor.hitReactor(c); //just hit it
            }
        }
    }

    public static void ReactorRectInMob(MaplePacketReader slea, MapleClient c) {
        int oid = slea.readInt();
        if (c == null || c.getPlayer() == null || c.getPlayer().getMap() == null) {
            return;
        }
        MapleCharacter chr = c.getPlayer();
        MapleReactor reactor = chr.getMap().getReactorByOid(oid);
        if (reactor == null) {
            return;
        }
        if (chr.getScriptManager().isActive(ScriptType.Reactor)
                && chr.getScriptManager().getParentIDByScriptType(ScriptType.Reactor) == reactor.getReactorId()) {
            try {
                chr.getScriptManager().getInvocableByType(ScriptType.Reactor).invokeFunction("action", reactor, 0);
            } catch (ScriptException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        } else {
            String result = chr.getScriptManager().startReactorScript(reactor);
            if (result == null) return;
        }

        MapleMap map = chr.getMap();
        MapleMonster mob = map.getMobObject(slea.readInt());
        if (mob != null && mob.isAlive()) {
            switch (mob.getId()) {
                case 9390620: // 墮落貝伊之石
                case 9390621: // 墮落貝伊之石
                case 9390622: // 墮落貝伊之石
                case 9390623: // 墮落貝伊之石
                    map.killMonster(mob);
                    MobSkill mSkill = MobSkillFactory.getMobSkill(800, 1);
                    if (mSkill != null) {
                        for (MapleCharacter mc : map.getCharacters()) {
                            mSkill.applyTo(mc);
                        }
                    }
                    break;
            }
        }
    }

    public static void UseRune(MaplePacketReader slea, MapleCharacter chr) {
        if (chr.getRuneStoneAction() != null) {
            return;
        }
        int unk = slea.readInt();
        int RuneType = slea.readInt();
        long cooldown = chr.getRuneCoolDown();
        if (cooldown > 0 && chr.getLevel() >= 30) {
            int l2 = 900000;// 15 min
            if (cooldown > l2) {
                cooldown = l2;
                chr.setRuneNextUseTime(l2);
            }
            chr.send(MaplePacketCreator.RuneAction(2, (int) cooldown));
//            }
        } else if (chr.getLevel() < 30) {
            chr.send(UIPacket.getSpecialTopMsg("使用符文必須等級達到30級以上！", 3, 20, 0, 0));
            chr.sendEnableActions();
        } else {
            MapleRuneStone.RuneStoneAction[] actions = MapleRuneStone.RuneStoneAction.values();
            MapleRuneStone.RuneStoneAction action = actions[Randomizer.nextInt(actions.length)];
            chr.setRuneStoneAction(action.getAction());
            chr.send(MaplePacketCreator.RuneAction(action));
        }
    }

    public static void UseRuneSkillReq(MaplePacketReader slea, MapleCharacter player) {
        if (player.getRuneStoneAction() == null) {
            return;
        }
        int cooldown = player.getRuneCoolDown();
        for (int action : player.getRuneStoneAction()) {
            if (action >= 0) {
                cooldown = 5000;
                player.setRuneNextActionTime(cooldown);
                break;
            }
        }
        player.setRuneStoneAction(null);
        if (cooldown <= 0) {
            if (player.getMap().getAllRuneThreadsafe() != null) {
                player.getMap().getAllRuneThreadsafe().getFirst().applyToPlayer(player);
            }
        }
    }

    public static void UseRuneAction(MaplePacketReader slea, MapleCharacter player) {
        if (player.getRuneStoneAction() == null) {
            return;
        }
        int cooldown = player.getRuneCoolDown();
        if (cooldown > 0) {
            player.send(MaplePacketCreator.RuneAction(2, cooldown));
        }
        int index = slea.readInt();
        int action = slea.readInt();
        if (index >= player.getRuneStoneAction().length || player.getRuneStoneAction()[index] != action) {
            cooldown = 5000;
            player.setRuneNextActionTime(cooldown);
            player.setRuneStoneAction(null);
        } else {
            player.getRuneStoneAction()[index] = -1;
        }
    }


    public static void FollowRequest(MaplePacketReader slea, MapleClient c) {
        MapleCharacter tt = c.getPlayer().getMap().getPlayerObject(slea.readInt());
        if (slea.readByte() > 0) {
            //1 when changing map
            tt = c.getPlayer().getMap().getPlayerObject(c.getPlayer().getFollowId());
            if (tt != null && tt.getFollowId() == c.getPlayer().getId()) {
                tt.setFollowOn(true);
                c.getPlayer().setFollowOn(true);
            } else {
                c.getPlayer().checkFollow();
            }
            return;
        }
        if (slea.readByte() > 0) { //cancelling follow
            tt = c.getPlayer().getMap().getPlayerObject(c.getPlayer().getFollowId());
            if (tt != null && tt.getFollowId() == c.getPlayer().getId() && c.getPlayer().isFollowOn()) {
                c.getPlayer().checkFollow();
            }
            return;
        }
        if (tt != null && tt.getPosition().distance(c.getPlayer().getPosition()) < 100 && tt.getFollowId() == 0 && c.getPlayer().getFollowId() == 0 && tt.getId() != c.getPlayer().getId()) { //estimate, should less
            tt.setFollowId(c.getPlayer().getId());
            tt.setFollowOn(false);
            tt.setFollowInitiator(false);
            c.getPlayer().setFollowOn(false);
            c.getPlayer().setFollowInitiator(false);
            tt.getClient().announce(MaplePacketCreator.followRequest(c.getPlayer().getId()));
        } else {
            c.announce(MaplePacketCreator.serverNotice(1, "距離太遠。"));
        }
    }

    public static void FollowReply(MaplePacketReader slea, MapleClient c) {
        if (c.getPlayer().getFollowId() > 0 && c.getPlayer().getFollowId() == slea.readInt()) {
            MapleCharacter tt = c.getPlayer().getMap().getPlayerObject(c.getPlayer().getFollowId());
            if (tt != null && tt.getPosition().distance(c.getPlayer().getPosition()) < 100 && tt.getFollowId() == 0 && tt.getId() != c.getPlayer().getId()) { //estimate, should less
                boolean accepted = slea.readByte() > 0;
                if (accepted) {
                    tt.setFollowId(c.getPlayer().getId());
                    tt.setFollowOn(true);
                    tt.setFollowInitiator(false);
                    c.getPlayer().setFollowOn(true);
                    c.getPlayer().setFollowInitiator(true);
                    c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.followEffect(tt.getId(), c.getPlayer().getId(), null));
                } else {
                    c.getPlayer().setFollowId(0);
                    tt.setFollowId(0);
                    tt.getClient().announce(MaplePacketCreator.getFollowMsg(5));
                }
            } else {
                if (tt != null) {
                    tt.setFollowId(0);
                    c.getPlayer().setFollowId(0);
                }
                c.announce(MaplePacketCreator.serverNotice(1, "距離太遠."));
            }
        } else {
            c.getPlayer().setFollowId(0);
        }
    }

    /*
     * 1112300 - 月長石戒指1克拉 - 愛情與婚姻的象徵。\n註：結婚人士如果#c離婚#，該戒指將會#c消失#。
     * 1112301 - 月長石戒指2克拉 - 愛情與婚姻的象徵。\n註：結婚人士如果#c離婚#，該戒指將會#c消失#。
     * 1112302 - 月長石戒指3克拉 - 愛情與婚姻的象徵。\n註：結婚人士如果#c離婚#，該戒指將會#c消失#。
     * 1112303 - 閃耀新星戒指1克拉 - 愛情與婚姻的象徵。\n註：結婚人士如果#c離婚#，該戒指將會#c消失#。
     * 1112304 - 閃耀新星戒指2克拉 - 愛情與婚姻的象徵。\n註：結婚人士如果#c離婚#，該戒指將會#c消失#。
     * 1112305 - 閃耀新星戒指3克拉 - 愛情與婚姻的象徵。\n註：結婚人士如果#c離婚#，該戒指將會#c消失#。
     * 1112306 - 金心戒指1克拉 - 愛情與婚姻的象徵。\n註：結婚人士如果#c離婚#，該戒指將會#c消失#。
     * 1112307 - 金心戒指2克拉 - 愛情與婚姻的象徵。\n註：結婚人士如果#c離婚#，該戒指將會#c消失#。
     * 1112308 - 金心戒指3克拉 - 愛情與婚姻的象徵。\n註：結婚人士如果#c離婚#，該戒指將會#c消失#。
     * 1112309 - 銀翼戒指1克拉 - 愛情與婚姻的象徵。\n註：結婚人士如果#c離婚#，該戒指將會#c消失#。
     * 1112310 - 銀翼戒指2克拉 - 愛情與婚姻的象徵。\n註：結婚人士如果#c離婚#，該戒指將會#c消失#。
     * 1112311 - 銀翼戒指3克拉 - 愛情與婚姻的象徵。\n註：結婚人士如果#c離婚#，該戒指將會#c消失#。
     * 1112315 - 恩愛夫妻結婚戒指1克拉 - 愛情與婚姻的象徵。\n註：結婚人士如果#c離婚#，該戒指可能會#c消失#。
     * 1112316 - 恩愛夫妻結婚戒指2克拉 - 愛情與婚姻的象徵。\n註：結婚人士如果#c離婚#，該戒指可能會#c消失#。
     * 1112317 - 恩愛夫妻結婚戒指3克拉 - 愛情與婚姻的象徵。\n註：結婚人士如果#c離婚#，該戒指可能會#c消失#。
     * 1112318 - 鴛鴦夫妻結婚戒指1克拉 - 愛情與婚姻的象徵。\n註：結婚人士如果#c離婚#，該戒指可能會#c消失#。
     * 1112319 - 鴛鴦夫妻結婚戒指2克拉 - 愛情與婚姻的象徵。\n註：結婚人士如果#c離婚#，該戒指可能會#c消失#。
     * 1112320 - 鴛鴦夫妻結婚戒指3克拉 - 愛情與婚姻的象徵。\n註：結婚人士如果#c離婚#，該戒指可能會#c消失#。
     *
     * 2240004 - 月長石戒指 - 用月亮的石頭和鑽石加工而成的求婚戒指。可以用來向心愛的異性求婚。
     * 2240005 - 月長石戒指2克拉 - 用月亮的石頭和2克拉的鑽石加工而成的求婚戒指。可以用來向心愛的異性求婚。
     * 2240006 - 月長石戒指3克拉 - 用月亮的石頭和23克拉的鑽石加工而成的求婚戒指。可以用來向心愛的異性求婚。
     * 2240007 - 閃耀新星戒指 - 用星星的石頭和鑽石加工而成的求婚戒指。可以用來向心愛的異性求婚。
     * 2240008 - 閃耀新星戒指2克拉 - 用星星的石頭和2克拉的鑽石加工而成的求婚戒指。可以用來向心愛的異性求婚。
     * 2240009 - 閃耀新星戒指3克拉 - 用星星的石頭和3克拉的鑽石加工而成的求婚戒指。可以用來向心愛的異性求婚。
     * 2240010 - 金心戒指 - 用黃金和鑽石加工而成的求婚戒指。可以用來向心愛的異性求婚。
     * 2240011 - 金心戒指2克拉 - 用黃金和2克拉的鑽石加工而成的求婚戒指。可以用來向心愛的異性求婚。
     * 2240012 - 金心戒指3克拉 - 用黃金和3克拉的鑽石加工而成的求婚戒指。可以用來向心愛的異性求婚。
     * 2240013 - 銀翼戒指 - 用銀和鑽石加工而成的求婚戒指。可以用來向心愛的異性求婚。
     * 2240014 - 銀翼戒指2克拉 - 用銀和2克拉的鑽石加工而成的求婚戒指。可以用來向心愛的異性求婚。
     * 2240015 - 銀翼戒指3克拉 - 用銀和3克拉的鑽石加工而成的求婚戒指。可以用來向心愛的異性求婚。
     */
    public static void DoRing(MapleClient c, String name, int itemid) {
        int newItemId = getMarriageNewItemId(itemid);
        MapleCharacter chr = c.getChannelServer().getPlayerStorage().getCharacterByName(name);
        /*
         * MarriageId 判斷是否結婚
         * MarriageItemId 判斷求婚狀態
         */
        int errcode = 0x00;
        if (JobConstants.is神之子(c.getPlayer().getJob())) { //該道具不能用於神之子
            errcode = 0x15;
        } else if (c.getPlayer().getMarriageId() > 0) { //您已經是結婚的狀態.
            errcode = 0x1D;
        } else if (c.getPlayer().getMarriageItemId() > 0) { //您已經是訂婚的狀態.
            errcode = 0x1B;
        } else if (!c.getPlayer().haveItem(itemid, 1) || itemid < 2240004 || itemid > 2240015) { //訂婚失敗
            errcode = 0x0F;
        } else if (chr == null) { //當前頻道、地圖找不到該角色或角色名錯誤.
            errcode = 0x16;
        } else if (JobConstants.is神之子(chr.getJob())) { //對方不在同一地圖.
            errcode = 0x15;
        } else if (chr.getMapId() != c.getPlayer().getMapId()) { //對方不在同一地圖.
            errcode = 0x17;
        } else if (chr.getGender() == c.getPlayer().getGender()) { //同性不能結婚.
            errcode = 0x1A;
        } else if (chr.getMarriageId() > 0) { //對方已經是結婚的狀態.
            errcode = 0x1E;
        } else if (chr.getMarriageItemId() > 0) { //對方已經是訂婚的狀態.
            errcode = 0x1C;
        } else if (!MapleInventoryManipulator.checkSpace(c, newItemId, 1, "")) { //道具欄已滿.請整理其他窗口.
            errcode = 0x18;
            //System.err.println("自己是否有位置: " + !MapleInventoryManipulator.checkSpace(c, newItemId, 1, ""));
        } else if (!MapleInventoryManipulator.checkSpace(chr.getClient(), newItemId, 1, "")) { //對方的道具欄已滿.
            errcode = 0x19;
            //System.err.println("對方是否有位置: " + !MapleInventoryManipulator.checkSpace(c, newItemId, 1, ""));
        }
        if (errcode > 0) {
            c.announce(MaplePacketCreator.sendEngagement((byte) errcode, 0, null, null));
            c.sendEnableActions();
            return;
        }
        c.getPlayer().setMarriageItemId(itemid);
        chr.send(MaplePacketCreator.sendEngagementRequest(c.getPlayer().getName(), c.getPlayer().getId()));
    }

    public static void RingAction(MaplePacketReader slea, MapleClient c) {
        byte mode = slea.readByte();
        if (mode == 0) {
            DoRing(c, slea.readMapleAsciiString(), slea.readInt());
        } else if (mode == 1) {
            c.getPlayer().setMarriageItemId(0);
        } else if (mode == 2) { //accept/deny proposal
            boolean accepted = slea.readByte() > 0;
            String name = slea.readMapleAsciiString();
            int id = slea.readInt();
            MapleCharacter chr = c.getChannelServer().getPlayerStorage().getCharacterByName(name);
            if (c.getPlayer().getMarriageId() > 0 || chr == null || chr.getId() != id || chr.getMarriageItemId() <= 0 || !chr.haveItem(chr.getMarriageItemId(), 1) || chr.getMarriageId() > 0 || !chr.isAlive() || chr.checkEvent() || !c.getPlayer().isAlive() || c.getPlayer().checkEvent()) {
                c.announce(MaplePacketCreator.sendEngagement((byte) 0x1F, 0, null, null)); //對方處於無法接受求婚的狀態.
                c.sendEnableActions();
                return;
            }
            if (accepted) {
                int itemid = chr.getMarriageItemId();
                int newItemId = getMarriageNewItemId(itemid);
                if (!MapleInventoryManipulator.checkSpace(c, newItemId, 1, "") || !MapleInventoryManipulator.checkSpace(chr.getClient(), newItemId, 1, "")) {
                    c.announce(MaplePacketCreator.sendEngagement((byte) 0x15, 0, null, null));
                    c.sendEnableActions();
                    return;
                }
                try {
                    int[] ringID = MapleRing.makeRing(newItemId, c.getPlayer(), chr);
                    Equip eq = MapleItemInformationProvider.getInstance().getEquipById(newItemId, ringID[1]);
                    MapleRing ring = MapleRing.loadFromDb(ringID[1]);
                    if (ring != null) {
                        eq.setRing(ring);
                    }
                    MapleInventoryManipulator.addbyItem(c, eq);
                    eq = MapleItemInformationProvider.getInstance().getEquipById(newItemId, ringID[0]);
                    ring = MapleRing.loadFromDb(ringID[0]);
                    if (ring != null) {
                        eq.setRing(ring);
                    }
                    MapleInventoryManipulator.addbyItem(chr.getClient(), eq);
                    MapleInventoryManipulator.removeById(chr.getClient(), MapleInventoryType.USE, chr.getMarriageItemId(), 1, false, false);
                    chr.send(MaplePacketCreator.sendEngagement((byte) 0x0D, newItemId, chr, c.getPlayer())); //恭喜你訂婚成功.
                    chr.setMarriageId(c.getPlayer().getId());
                    c.getPlayer().setMarriageId(chr.getId());
                    chr.fakeRelog();
                    c.getPlayer().fakeRelog();
                    WorldBroadcastService.getInstance().broadcastMessage(MaplePacketCreator.yellowChat("[系統公告] 恭喜：" + c.getPlayer().getName() + " 和 " + chr.getName() + "結為夫妻。 希望你們在 " + chr.getClient().getChannelServer().getServerName() + " 遊戲中玩的愉快!"));
                } catch (Exception e) {
                    log.error("戒指操作錯誤", e);
                }
            } else {
                chr.send(MaplePacketCreator.sendEngagement((byte) 0x20, 0, null, null));
            }
            c.sendEnableActions();
            chr.setMarriageItemId(0);
        } else if (mode == 3) { //drop, only works for ETC
            int itemId = slea.readInt();
            MapleInventoryType type = ItemConstants.getInventoryType(itemId);
            Item item = c.getPlayer().getInventory(type).findById(itemId);
            if (item != null && type == MapleInventoryType.ETC && itemId / 10000 == 421) {
                MapleInventoryManipulator.drop(c, type, item.getPosition(), item.getQuantity());
            }
        }
    }

    private static int getMarriageNewItemId(int itemId) {
        int newItemId;
        if (itemId == 2240004) { //月長石戒指 - 用月亮的石頭和鑽石加工而成的求婚戒指。可以用來向心愛的異性求婚。
            newItemId = 1112300; //月長石戒指1克拉 - 愛情與婚姻的象徵。\n註：結婚人士如果#c離婚#，該戒指將會#c消失#。
        } else if (itemId == 2240005) { //月長石戒指2克拉 - 用月亮的石頭和2克拉的鑽石加工而成的求婚戒指。可以用來向心愛的異性求婚。
            newItemId = 1112301; //月長石戒指2克拉 -  愛情與婚姻的象徵。\n註：結婚人士如果#c離婚#，該戒指將會#c消失#。
        } else if (itemId == 2240006) { //月長石戒指3克拉 - 用月亮的石頭和23克拉的鑽石加工而成的求婚戒指。可以用來向心愛的異性求婚
            newItemId = 1112302; //月長石戒指3克拉 - 愛情與婚姻的象徵。\n註：結婚人士如果#c離婚#，該戒指將會#c消失#。
        } else if (itemId == 2240007) { //閃耀新星戒指 - 用星星的石頭和鑽石加工而成的求婚戒指。可以用來向心愛的異性求婚。
            newItemId = 1112303; //閃耀新星戒指1克拉 - 愛情與婚姻的象徵。\n註：結婚人士如果#c離婚#，該戒指將會#c消失#。
        } else if (itemId == 2240008) { //閃耀新星戒指2克拉 - 用星星的石頭和2克拉的鑽石加工而成的求婚戒指。可以用來向心愛的異性求婚。
            newItemId = 1112304; //閃耀新星戒指2克拉 - 愛情與婚姻的象徵。\n註：結婚人士如果#c離婚#，該戒指將會#c消失#。
        } else if (itemId == 2240009) { //閃耀新星戒指3克拉 - 用星星的石頭和3克拉的鑽石加工而成的求婚戒指。可以用來向心愛的異性求婚。
            newItemId = 1112305; //閃耀新星戒指3克拉 - 愛情與婚姻的象徵。\n註：結婚人士如果#c離婚#，該戒指將會#c消失#。
        } else if (itemId == 2240010) { //金心戒指 - 用黃金和鑽石加工而成的求婚戒指。可以用來向心愛的異性求婚。
            newItemId = 1112306; //金心戒指1克拉 - 愛情與婚姻的象徵。\n註：結婚人士如果#c離婚#，該戒指將會#c消失#。
        } else if (itemId == 2240011) { //金心戒指2克拉 - 用黃金和2克拉的鑽石加工而成的求婚戒指。可以用來向心愛的異性求婚。
            newItemId = 1112307; //金心戒指2克拉 - 愛情與婚姻的象徵。\n註：結婚人士如果#c離婚#，該戒指將會#c消失#。
        } else if (itemId == 2240012) { //金心戒指3克拉 - 用黃金和3克拉的鑽石加工而成的求婚戒指。可以用來向心愛的異性求婚。
            newItemId = 1112308; //金心戒指3克拉 - 愛情與婚姻的象徵。\n註：結婚人士如果#c離婚#，該戒指將會#c消失#。
        } else if (itemId == 2240013) { //銀翼戒指 - 用銀和鑽石加工而成的求婚戒指。可以用來向心愛的異性求婚。
            newItemId = 1112309; //銀翼戒指1克拉 - 愛情與婚姻的象徵。\n註：結婚人士如果#c離婚#，該戒指將會#c消失#。
        } else if (itemId == 2240014) { //銀翼戒指2克拉 - 用銀和2克拉的鑽石加工而成的求婚戒指。可以用來向心愛的異性求婚。
            newItemId = 1112310; //銀翼戒指2克拉 - 愛情與婚姻的象徵。\n註：結婚人士如果#c離婚#，該戒指將會#c消失#。
        } else if (itemId == 2240015) { //銀翼戒指3克拉 - 用銀和3克拉的鑽石加工而成的求婚戒指。可以用來向心愛的異性求婚。
            newItemId = 1112311; //銀翼戒指3克拉 - 愛情與婚姻的象徵。\n註：結婚人士如果#c離婚#，該戒指將會#c消失#。
        } else {
            throw new RuntimeException("Invalid Item Maker id");
        }
        return newItemId;
    }

    public static void Solomon(MaplePacketReader slea, MapleClient c) {
        //2370000
        if (c.getPlayer() != null) {
            c.getPlayer().updateTick(slea.readInt());
        } else {
            c.sendEnableActions();
        }
        Item item = c.getPlayer().getInventory(MapleInventoryType.USE).getItem(slea.readShort());
        if (item == null || item.getItemId() != slea.readInt() || item.getQuantity() <= 0 || c.getPlayer().getGachExp() > 0 || c.getPlayer().getLevel() > 50 || MapleItemInformationProvider.getInstance().getItemEffect(item.getItemId()).getEXP() <= 0) {
            return;
        }
        c.getPlayer().setGachExp(c.getPlayer().getGachExp() + MapleItemInformationProvider.getInstance().getItemEffect(item.getItemId()).getEXP());
        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, item.getPosition(), (short) 1, false);
        c.getPlayer().updateSingleStat(MapleStat.疲勞, c.getPlayer().getGachExp());
    }

    public static void GachExp(MaplePacketReader slea, MapleClient c) {
        c.sendEnableActions();
        c.getPlayer().updateTick(slea.readInt());
        if (c.getPlayer().getGachExp() <= 0) {
            return;
        }
        c.getPlayer().gainExp((long) c.getPlayer().getGachExp() * GameConstants.getExpRate_Quest(c.getPlayer().getLevel()), true, true, false);
        c.getPlayer().setGachExp(0);
        c.getPlayer().updateSingleStat(MapleStat.疲勞, 0);
    }

    public static void Report(MaplePacketReader slea, MapleClient c) {
        //0 = success 1 = unable to locate 2 = once a day 3 = you've been reported 4+ = unknown reason
        MapleCharacter other;
        ReportType type;
        type = ReportType.getById(slea.readByte());
        other = c.getPlayer().getMap().getCharacterByName(slea.readMapleAsciiString());
        //then,byte(?) and string(reason)
        if (other == null || type == null || other.isIntern()) {
            //c.announce(MaplePacketCreator.report(4));
            c.getPlayer().dropMessage(1, "舉報錯誤.");
            c.sendEnableActions();
            return;
        }
        MapleQuestStatus stat = c.getPlayer().getQuestNAdd(MapleQuest.getInstance(GameConstants.REPORT_QUEST));
        if (stat.getCustomData() == null) {
            stat.setCustomData("0");
        }
        long currentTime = System.currentTimeMillis();
        long theTime = Long.parseLong(stat.getCustomData());
        if (theTime + 7200000 > currentTime && !c.getPlayer().isIntern()) {
            c.getPlayer().dropMessage(5, "每2小時才能舉報1次.");
        } else {
            stat.setCustomData(String.valueOf(currentTime));
            other.addReport(type);
            //c.announce(MaplePacketCreator.report(GameConstants.GMS ? 2 : 0));
            c.getPlayer().dropMessage(1, "舉報完成.");
        }
        c.sendEnableActions();
    }

    public static boolean inArea(MapleCharacter chr) {
        for (Rectangle rect : chr.getMap().getAreas()) {
            if (rect.contains(chr.getPosition())) {
                return true;
            }
        }
        for (MapleAffectedArea mist : chr.getMap().getAllAffectedAreasThreadsafe()) {
            if (mist.getOwnerId() == chr.getId() && mist.getAreaType() == 2 && mist.getArea().contains(chr.getPosition())) {
                return true;
            }
        }
        return false;
    }

    /*
     * 測謊機系統
     */
    public static void LieDetector(MaplePacketReader slea, MapleClient c, MapleCharacter chr, boolean isItem) { // Person who used
        if (c == null || chr == null || chr.getMap() == null) {
            return;
        }
        if (!isItem && !chr.isIntern()) {
            return;
        }

        // 偵測角色可測謊狀態處理
        String toAntiChrName = slea.readMapleAsciiString();
        short slot = 0;
        if (isItem) {
            if (!chr.getCheatTracker().canLieDetector()) {
                chr.dropMessage(1, "您已經使用過一次，暫時還無法使用測謊機道具.");
                c.sendEnableActions();
                return;
            }
            slot = slea.readShort(); // 01 00 (first pos in use)
            int itemId = slea.readInt(); // B0 6A 21 00 
            Item toUse = chr.getInventory(MapleInventoryType.USE).getItem(slot);
            if (toUse == null || toUse.getQuantity() <= 0 || toUse.getItemId() != itemId || itemId != 2190000) {
                c.sendEnableActions();
                return;
            }
        } else if (!chr.isIntern()) { // Manager using skill. Lie Detector Skill 
            chr.getClient().disconnect(true, false);
            c.getSession().close();
            return;
        }
        if ((FieldLimitType.NOMOBCAPACITYLIMIT.check(chr.getMap().getFieldLimit()) && isItem) || chr.getMap().getReturnMapId() == chr.getMapId()) {
            chr.dropMessage(5, "當前地圖無法使用測謊機.");
            c.sendEnableActions();
            return;
        }
        MapleCharacter search_chr = chr.getMap().getCharacterByName(toAntiChrName);
        if (search_chr == null || search_chr.getId() == chr.getId() || chr.getGmLevel() < search_chr.getGmLevel()) {
            chr.dropMessage(1, "未找到角色");
//            c.announce(MaplePacketCreator.AntiMacro.cantFindPlayer());
            return;
        }
        if (search_chr.checkEvent() || search_chr.getMapId() == GameConstants.JAIL) {
            chr.dropMessage(5, "當前地圖無法使用測謊機.");
            c.sendEnableActions();
            return;
        }
        if (MapleAntiMacro.startAnti(chr, search_chr, isItem ? MapleAntiMacro.ITEM_ANTI : MapleAntiMacro.GM_SKILL_ANTI, !isItem) && isItem) {
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
        }
    }

    public static void LieDetectorResponse(MaplePacketReader slea, MapleClient c) { // Person who typed
        if (c == null || c.getPlayer() == null || c.getPlayer().getMap() == null) {
            return;
        }
        if (MapleAntiMacro.getCharacterState(c.getPlayer()) != MapleAntiMacro.ANTI_NOW) {
            return;
        }
        String inputCode = slea.readMapleAsciiString();
        if (MapleAntiMacro.verifyCode(c.getPlayer().getName(), inputCode)) {
            MapleAntiMacro.antiSuccess(c.getPlayer());
        } else {
            MapleAntiMacro.antiReduce(c.getPlayer());
        }
    }

    public static void LieDetectorRefresh(MaplePacketReader slea, MapleClient c) {
        if (c.getPlayer() == null || c.getPlayer().getMap() == null) {
            return;
        }
        if (MapleAntiMacro.getCharacterState(c.getPlayer()) != MapleAntiMacro.ANTI_NOW) {
            return;
        }
        MapleAntiMacro.antiReduce(c.getPlayer());
    }

    public static void updateRedLeafHigh(MaplePacketReader slea, MapleClient c) {
        slea.readInt();
        slea.readInt();
        int joejoe = slea.readInt();
        slea.readInt();
        int hermoninny = slea.readInt();
        slea.readInt();
        int littledragon = slea.readInt();
        slea.readInt();
        int ika = slea.readInt();
        slea.readInt();
        int wooden = slea.readInt();
        if (joejoe + hermoninny + littledragon + ika != c.getPlayer().getFriendShipToAdd()) {
            c.sendEnableActions();
            return;
        }
        c.getPlayer().setFriendShipPoints(joejoe, hermoninny, littledragon, ika, wooden);
    }

    public static void PeacemakerHeal(MaplePacketReader slea, MapleCharacter player) {
        int skillID = slea.readInt();
        if (skillID != 主教.和平使者) {
            if (player.isDebug()) {
                player.dropDebugMessage(0, "PeacemakerHeal error " + skillID);
            }
            return;
        }
        MapleStatEffect effect;
        if ((effect = player.getSkillEffect(主教.和平使者)) == null) {
            return;
        }
        slea.readInt();
        slea.readInt(); // 128
        slea.readRect();

        int healHP = player.getStat().getCurrentMaxHP() * effect.getHp() / 100;

        int nCount = slea.readInt();
        for (int i = 0; i < nCount; i++) {
            MapleCharacter chr = player.getMap().getPlayerObject(slea.readInt());
            if (chr != null && (chr == player || chr.getParty() == player.getParty())) {
                chr.addHP(healHP);
                chr.send(EffectPacket.showSkillAffected(-1, skillID, effect.getLevel(), 0));
            }
        }
    }

    public static void MobZoneStateResult(MaplePacketReader slea, MapleCharacter player) {
        List<String> list = ((List<String>) player.getTempValues().getOrDefault("MobZoneState", new LinkedList<>()));
        if (list.size() == 0) {
            return;
        }
        String data = list.remove(0);
        if (slea.readBool()) {
            switch (data) {
                case "CapEffect": {
                    if (player.getBuffStatValueHolder(SecondaryStat.Lapidification) == null) {
                        MobSkill mobskill = MobSkillFactory.getMobSkill(174, 3);
                        if (mobskill != null) {
                            mobskill.applyTo(player);
                        }
                    }
                    break;
                }
                case "DropPerifactionCurse": {
                    if (player.getBuffStatValueHolder(SecondaryStat.Lapidification) == null) {
                        MobSkill mobskill = MobSkillFactory.getMobSkill(174, 5);
                        if (mobskill != null) {
                            mobskill.applyTo(player);
                        }
                    }
                    break;
                }
                case "DropPunchGiantBossL":
                case "DropPunchGiantBossR":
                case "DropStone":
                case "DropStoneGiantBoss1":
                case "DropStoneGiantBoss2":
                case "DropStoneGiantBoss3":
                case "palmAttackGiantBossL":
                case "palmAttackGiantBossR": {
                    MobSkill mobskill = MobSkillFactory.getMobSkill(123, 44);
                    if (mobskill != null) {
                        mobskill.applyTo(player);
                    }
                    break;
                }
                default:
                    player.dropMessage(1, "狀態效果未處理:" + data);
                    break;
            }
        }
    }

    public static void LapidificationStateChange(MaplePacketReader slea, MapleCharacter chr, MapleClient C) {


        int result = slea.readInt();

        Map<SecondaryStat, Pair<Integer, Integer>> cancelList = new HashMap<>();

        if (result == 0
                && /*  818 */ chr.getDiseases(SecondaryStat.Lapidification)) ;
        {
            chr.getDiseases(SecondaryStat.Lapidification);

            cancelList.put(SecondaryStat.Lapidification, new Pair<Integer, Integer>(0, 0));

        }
    }
}
