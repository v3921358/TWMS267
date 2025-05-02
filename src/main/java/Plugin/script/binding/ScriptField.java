package Plugin.script.binding;

import Client.MapleCharacter;
import Net.server.life.MapleLifeFactory;
import Net.server.life.MapleMonster;
import Net.server.maps.MapleMap;
import Opcode.Headler.OutHeader;
import Packet.MaplePacketCreator;
import Server.BossEventHandler.*;
import Server.BossEventHandler.Demian.Demian;
import Server.BossEventHandler.Dusk.Dusk;
import Server.BossEventHandler.Jin.JinHillah;
import connection.packet.FieldPacket;
import SwordieX.field.ClockPacket;
import SwordieX.field.fieldeffect.FieldEffect;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import tools.data.MaplePacketLittleEndianWriter;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class ScriptField {

    @Getter
    private final MapleMap map;

    private List<MapleCharacter> characters;

    public ScriptField(MapleMap map) {
        this.map = map;
        this.characters = new ArrayList<>(map.getCharacters());  // 初始化角色列表
    }

    public void reset() {
        getMap().killAllMonsters(false);
        getMap().reloadReactors();
        getMap().resetNPCs();
        getMap().resetSpawns();
        getMap().resetPortals();
        getMap().removeDrops();
        getMap().setUserFirstEnter(false);
    }

    public int getMonsterSize() {
        return getMap().getMonsters().size();
    }

    public void startDojangRandTimer(int sec, int wait) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.CTX_Event_Field_Timer.getValue());
        mplew.write(7);
        mplew.write(1);
        mplew.writeInt(sec);
        mplew.writeInt(wait);
        getMap().broadcastMessage(mplew.getPacket());
    }

    public void spawnRune(int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_RuneStoneAppear.getValue());
        mplew.writeInt(1);
        mplew.writeInt(0);
        mplew.writeInt(2);
        mplew.writeInt(type); // ERuneStoneType
        mplew.writeInt((int) getMap().getAllChracater().getFirst().getPosition().getX());
        mplew.writeInt((int) getMap().getAllChracater().getFirst().getPosition().getY());
        mplew.write(0);
        getMap().broadcastMessage(mplew.getPacket());
    }


    public void reset(int level) {
        getMap().resetPQ(level);
    }

    public void overrideFieldLimit(int var) {
        getMap().setFieldLimit(var);
    }

    public void showWeatherEffectNotice(String msg, int type, int duration) {
        getMap().showWeatherEffectNotice(msg, type, duration);
    }

    public void changeBGM(String name) {
        FieldPacket.fieldEffect(FieldEffect.changeBGM(name, 0, 0, 0));
    }

    public void clearMobs() {
        getMap().killAllMonsters(true);
    }

    public void clearDrops() {
        getMap().removeDrops();
    }

    public void createObtacleAtom(int count, int type1, int type2, int DamageRang, int SpeedRang) {
        getMap().getAllChracater().forEach(chr -> chr.send(MaplePacketCreator.createObtacleAtom(count, type1, type2, getMap())));
//        getMap().getAllChracater().forEach(chr -> chr.send(MaplePacketCreator.createObtacleAtom(count, type1, type2, DamageRang, SpeedRang, getMap())));
    }

    public void destroyTempNpc(int npcId) {
        getMap().removeNpc(npcId);
    }

    public void spawnTempNpc(int npcId, int x, int y) {
        getMap().spawnNpc(npcId, new Point(x, y));
    }

    public int getId() {
        return getMap().getId();
    }

    public int getInstanceId() {
        return getMap().getInstanceId();
    }

    public int getNumPlayersInArea(int id) {
        return getMap().getNumPlayersInArea(id);
    }

    public int getPlayerCount() {
        return getMap().getAllChracater().size();
    }

    public ScriptMob makeMob(int mobId) {
        MapleMonster Mob = MapleLifeFactory.getMonster(mobId);
        return new ScriptMob(Mob);
    }

    public void spawnMob(int mobId, int x, int y) {
        MapleMonster Mob = MapleLifeFactory.getMonster(mobId);
        getMap().spawnMonsterOnGroundBelow(Mob, new Point(x, y));
    }

    public void spawnMob(ScriptMob Mob, int x, int y) {

        getMap().spawnMonsterOnGroundBelow(Mob.getMob(), new Point(x, y));
    }
    public void portalEffect(String name, int i) {
        getMap().showPortalEffect(name, i);
    }

    public void resetMobsSpawns() {
        getMap().resetSpawns();
    }

    public void screenEffect(String name) {
        getMap().showScreenEffect(name);
    }

    public void scriptProgressMessage(String msg) {
        getMap().showScriptProgressMessage(msg);
    }

    public void setNoSpawn(boolean value) {
        getMap().setSpawns(value);
    }

    public void showTimer(double seconds) {
        long sec = (long) Math.ceil(seconds * 1000);
        getMap().getAllChracater().forEach(chr -> chr.getClient().announce(FieldPacket.clock(ClockPacket.secondsClock(sec))));
    }

    public void closeTimer() {
        getMap().getAllChracater().forEach(chr -> chr.getClient().announce(FieldPacket.clock(ClockPacket.secondsClock(-1))));
    }

    public List<ScriptPlayer> getPlayers() {
        return getMap().getAllChracater().stream()
                .map(ScriptPlayer::new)
                .collect(Collectors.toList());
    }


    public void endFieldEvent() {
        getMap().endFieldEvent();
    }

    public void getName() {
        getMap().getMapName();
    }

    public void setReactorState(String name, byte state) {
        getMap().setReactorState(name, state);
    }

    public int getReactorStateId(String var1) {
        return getMap().getReactorStat(var1);
    }

    public int getEventMobCount() {
        return getMap().getMonsters().size();

    }

    public int getEventMobCountById(int mobId) {
        List<MapleMonster> monsters = getMap().getMonsters();
        int count = 0;
        for (MapleMonster monster : monsters) {
            if (monster.getId() == mobId) count++;
        }
        return count;
    }

    public void blowWeather(int itemId, String msg, int time) {
        getMap().startMapEffect(msg, itemId, time);
    }

    public void blowWeather(int itemId, String msg) {
        getMap().startMapEffect(msg, itemId, 3000);
    }

    public ScriptEvent getEvent() {
        return getMap().getEvent();
    }

    public void setEvent(ScriptEvent event) {
        getMap().setEvent(event);
    }

    public List<MapleCharacter> getCharacters() {
        return this.getMap().getCharacters();
    }

    public void startFieldEvent() {
        for (MapleCharacter chr : map.getAllCharactersThreadsafe()) {
            chr.getMap().startFieldEvent();
        }
    }

    public void startDemianField() {
        for (MapleCharacter chr : this.getCharacters()) {
            Demian.start(chr);
        }
    }

    public void startLucidField() {
        for (MapleCharacter chr : this.getCharacters()) {
            Lucid.start(chr);
        }
    }

    public void startDuskField() {
        for (MapleCharacter chr : this.getCharacters()) {
            Dusk.start(chr);
        }
    }

    public void startJinField() {
        for (MapleCharacter chr : this.getCharacters()) {
            JinHillah.start(chr);
        }
    }

    public void startAngelField() {
        for (MapleCharacter chr : this.getCharacters()) {
            Angel.start(chr);
        }
    }

    public void startWillField() {
        for (MapleCharacter chr : this.getCharacters()) {
            Will.start(chr);
        }
    }

    public void startSerenField() {
        for (MapleCharacter chr : this.getCharacters()) {
            Seren.start(chr, chr.getMap().getMonsters().getFirst());
        }
    }

    public void startKalosField() {
        for (MapleCharacter chr : this.getCharacters()) {
            kalos.start(chr);
        }
    }

    public void startBlackMageField() {
        for (MapleCharacter chr : this.getCharacters()) {
            BlackMage.start(chr);
        }
    }

    public void startBlackMageField_II() {
        for (MapleCharacter chr : this.getCharacters()) {
            BlackMage.start2(chr);
        }
    }

    public void startBlackMageField_III() {
        for (MapleCharacter chr : this.getCharacters()) {
            BlackMage.start3(chr);
        }
    }

    public void startBlackMageField_IV() {
        for (MapleCharacter chr : this.getCharacters()) {
            BlackMage.start4(chr);
        }
    }

    public void StartKarNingField(){
        for (MapleCharacter player : this.getCharacters()) {
            Caning.startEvent_Field(player);
        }
    }
}
