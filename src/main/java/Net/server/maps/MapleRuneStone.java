package Net.server.maps;

import Client.MapleCharacter;
import Client.MapleClient;
import Client.skills.Skill;
import Client.skills.SkillFactory;
import Net.server.buffs.MapleStatEffect;
import Net.server.life.MapleLifeFactory;
import Net.server.life.MapleMonster;
import Packet.MaplePacketCreator;
import tools.HexTool;
import tools.Randomizer;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MapleRuneStone extends MapleMapObject {

    public int[] getNextDirection(int[] currentDirection) {
        RuneStoneAction action = getRuneStoneAction(currentDirection);
        if (action != null) {
            return action.getAction();
        }
        return null;
    }

    private RuneStoneAction getRuneStoneAction(int[] currentDirection) {
        for (RuneStoneAction action : RuneStoneAction.values()) {
            if (Arrays.equals(action.getAction(), currentDirection)) {
                return action;
            }
        }
        return null;
    }

    public enum RuneStoneAction {
        /* 鍵的指示 : 下 0 上 1 左 2  右 3 */
        /* 鍵的指示 : 下 0 上 1 左 2  右 3 */
        左右下下(new int[]{2, 3, 0, 0}, "93 B8 73 FC 28 00 00 00 BE 55 23 88 EE E5 BF DE E0 08 BF DE 18 64 A8 16 5A 01 76 CD B8 97 D0 2D 17 BD 1C 21 FE 97 CA 0F 4C F1 98 D7 A8 A5 14 BC"),
        左右上上(new int[]{2, 3, 1, 1}, "A6 B3 44 26 28 00 00 00 91 F8 B9 53 64 64 88 DA DB D1 E3 4B E2 8D 84 E0 41 D3 71 F2 7C 6E 92 24 60 36 F9 C4 A6 27 3F A6 45 46 69 44 BD B2 4B 34"),
        左右左右(new int[]{2, 3, 2, 3}, "37 3C EC 27 28 00 00 00 11 C0 15 52 D0 61 C7 D9 7A 47 11 B9 03 63 70 DD AD 9F EC 23 27 6D 9F 16 FF BE 7E F8 02 D3 36 81 9A D2 F5 BF 28 DC 68 6B"),
        左下下左(new int[]{2, 0, 0, 2}, "C5 1D 7F AE 28 00 00 00 A6 CB 93 DA A6 A7 25 75 A7 3B C1 14 47 F7 77 39 D4 64 B0 79 39 DD 29 EE 56 4A 66 55 37 EE 24 7D 92 4B D3 5E 78 9B 1C F6"),
        左下右右(new int[]{2, 0, 3, 3}, "54 B1 12 74 28 00 00 00 68 1E 73 01 2B F5 4B 37 57 09 75 67 94 84 AC 20 D1 30 37 DB 4D 73 F8 AD 28 A7 86 41 DE 49 F6 A5 0F 4C 93 CA 17 E3 5A 25"),
        上左右右(new int[]{1, 2, 3, 3}, "44 53 BC 48 28 00 00 00 BC 08 04 3D 4D 39 97 19 64 E5 23 D6 C2 B1 F0 0D F6 4A DF 04 C6 DE C5 1F 5B 91 95 A0 C4 03 77 BD 99 FE A7 1F 4B 4A A0 21"),
        上下右下(new int[]{1, 0, 3, 0}, "AF 17 C5 D6 28 00 00 00 C0 5D 59 A2 7E 51 B7 6F 45 D4 92 F9 B4 F4 26 16 05 9A C0 5B B3 21 EE E1 7C E0 79 EA 07 E4 23 DF A2 E2 BA A2 F8 7B 3E 50"),
        下上下上(new int[]{0, 1, 0, 1}, "72 50 81 93 28 00 00 00 8C 81 A3 E6 2A B3 68 3F 37 AC 50 67 A4 98 D9 18 C9 46 3F 3E F0 B4 8C FA EF 58 87 0B 5E DC 96 B3 37 9B 3D 9B 06 E3 D3 3F"),
        下左右右(new int[]{0, 2, 3, 3}, "49 E4 E3 39 28 00 00 00 9F E8 35 4C 8D AA F1 2F BD C2 9C 3F F3 A8 F1 1A 7A 28 81 CB A3 16 F3 97 8C 7F FF 9D 24 0C 5F 55 63 8A 63 98 CB 68 99 7A"),
        下下左上(new int[]{0, 0, 2, 1}, "54 0B AE BC 28 00 00 00 DC 73 7E C8 85 2E 54 A3 AA F1 57 E4 92 FB 3B 6B CE 50 2E 42 FF 75 07 23 FF AE 08 39 5A C0 FD 85 7E 97 90 35 AD 7C 4A 3E"),
        下下右右(new int[]{0, 0, 3, 3}, "65 84 5C 58 28 00 00 00 F3 9F C5 2D 7F 59 D4 CC A5 6F EE 5A E5 E9 C7 85 CE 45 54 07 6F 16 FE 61 89 A2 6C 06 CA F3 9A F3 3B B6 26 5E FB 6B 2C BD"),
        下右下上(new int[]{0, 3, 0, 1}, "D3 2D 88 2E 28 00 00 00 90 E9 64 5B 50 CA 05 F7 A9 50 6F C5 A7 BB 9D DF 9F 7C F4 03 14 14 71 AD F7 6B AC E7 72 EF F6 78 BE 65 F0 A5 72 1C BB E3"),
        下上右左(new int[]{0, 1, 3, 2}, "AA 5A F5 73 28 00 00 00 43 B3 94 06 F9 88 0D 5E 02 48 BB F3 C7 59 EF C6 26 64 0B 7B FE 9D 86 0E D6 29 0F 99 62 66 3E 4A B6 80 86 41 A7 BC BC 0B"),
        下下左左(new int[]{0, 0, 2, 2}, "25 98 DE 04 28 00 00 00 4B 8F 9E 72 C6 91 F6 87 3A C3 A5 BE 49 75 A4 B3 48 BF A1 54 EF 77 2E A8 BB 5C 96 02 48 1F A9 D3 6C 6B 50 D8 90 DA 57 88"),
        右左左右(new int[]{3, 2, 2, 3}, "25 35 9F 92 28 00 00 00 11 A3 BB E7 E5 29 8D 3B CC 03 11 3E 82 F7 91 83 58 B2 93 70 B5 CC 2B 51 64 94 FC 6B 72 AC 4A A5 D3 AA 24 D1 AA 44 1F CA"),
        右上上左(new int[]{3, 1, 1, 2}, "8A AC 50 86 28 00 00 00 CF 9F 6D F3 BE F8 6C FA 6C B2 39 43 1D E3 AC EE FB D1 2E 5A 7D F0 A9 1B C4 23 F1 04 B5 9B 04 A0 4A B4 02 0C CD D5 24 28"),
        右上下下(new int[]{3, 1, 0, 0}, "1D 7E DD 79 28 00 00 00 BF 67 8B 0C DC 0D 91 6F A8 5D EC 38 95 9C 02 17 EC 23 10 AD 66 A5 AF D4 59 D7 21 AF 9F E8 B7 64 92 70 E5 67 60 7B F8 1D"),
        右上右下(new int[]{3, 1, 3, 0}, "D3 B6 64 AF 28 00 00 00 E2 BD 8F DB AF 34 5E 70 D8 2A 55 67 BD 83 2A 31 BA 4A F3 0B 6A 5D AD 85 88 17 C4 84 C7 AF 28 FA F1 65 01 6D BA 47 2E D3"),
        右下下左(new int[]{3, 0, 0, 2}, "81 29 EA DB 28 00 00 00 CA 29 78 AF DC 1C 41 57 56 05 59 85 AE 3B FC 02 B6 16 13 D7 2B 0F 4A 83 3D D0 CB AE 05 F8 79 C1 E6 09 B6 40 82 2F 63 55"),
        右上右右(new int[]{3, 1, 3, 3}, "D6 90 EE 4F 28 00 00 00 AB 67 47 3A 64 96 3C 12 6E F9 D6 24 2E 15 15 CD 9D 16 13 AF 34 E5 3F 62 CB 6A BD 6A 4C 90 31 E9 3E 8C 2A E5 50 EE 72 78"),
        ;
        int[] action;
        byte[] packet;

        RuneStoneAction(int[] action, String sPacket) {
            this.action = action;
            packet = HexTool.getByteArrayFromHexString(sPacket);
        }

        public int[] getAction() {
            return Arrays.copyOf(action, action.length);
        }

        public byte[] getPacket() {
            return packet;
        }
    }

    private final int type;
    private MapleMap map;
    private final boolean facingLeft;
    private int curseStage;
    private long spawnTime = 0L;

    public MapleRuneStone(int type) {
        this.type = type;
        curseStage = 0;
        facingLeft = false;
    }

    public MapleMap getMap() {
        return map;
    }

    public void setMap(MapleMap map) {
        this.map = map;
    }

    public int getRuneType() {
        return type;
    }

    public final void applyToPlayer(MapleCharacter player) {
        int runeSkillId = 0;
        OUTER:
        switch (getRuneType()) {
            case 0: { // 破滅之輪
                runeSkillId = 80001432; // 破滅之輪行蹤
                break;
            }
            case 1: { // 打雷之輪
                runeSkillId = 80001756; // 解放雷之輪
                break;
            }
            case 2: { // v262 巨人之輪 - 解放
                runeSkillId = 80002887;
                break;
            }
            case 3: { // 黑暗之輪
                MapleMonster elite;
                List<MapleMonster> randomMob = map.getMonsters();
                for (MapleMonster elMob : randomMob) {
                    if (elMob.isEliteMob()) {
                        break OUTER;
                    }
                }
                List<Point> randomSpawn = new ArrayList<>(map.getSpawnPoints());
                for (int i = 0; i < 3; i++) {
                    elite = MapleLifeFactory.getEliteMonster(randomMob.get(Randomizer.nextInt(randomMob.size())).getId());
                    elite.registerKill(300000L);
                    map.spawnMonsterOnGroundBelow(elite, randomSpawn.get(Randomizer.nextInt(randomSpawn.size())));
                }
                break;
            }
            case 4: { // 超越之輪
                runeSkillId = 80001875; // 輪之力解放-超越
                break;
            }
            case 5: { // 淨化之輪
                runeSkillId = 80002888; // 淨化之輪解放
                break;
            }
            case 6: { // 光束之輪
                runeSkillId = 80002889; // 光束之輪解放
                break;
            }
            case 7: { // 轉移之輪
                runeSkillId = 80002890; // 轉移之輪解放
                break;
            }
        }
        player.send(MaplePacketCreator.showRuneEffect(this.getRuneType()));
        Skill skill = SkillFactory.getSkill(80002282);
        MapleStatEffect effect = skill.getEffect(1);
        effect.applyTo(player, 900000);

        if (getMap() != null && getMap().getFieldLevel() > 0 && player.getLevel() >= getMap().getFieldLevel() - 20) {
            if (runeSkillId > 0) {
                skill = SkillFactory.getSkill(runeSkillId);
                effect = skill.getEffect(1);
                effect.applyTo(player);
            }
            skill = SkillFactory.getSkill(80002280); // 解放的輪之力
            effect = skill.getEffect(1);
            effect.applyTo(player);
            if (runeSkillId == 80001432) { // 破滅之輪
                skill = SkillFactory.getSkill(80001431);
                effect = skill.getEffect(1);
                effect.applyTo(player);
            }
            if (runeSkillId < 0) {
                getMap().spawnMonsterWithEffect(Objects.requireNonNull(MapleLifeFactory.getMonster(-runeSkillId)), -5, new Point(getPosition()));
            }
        }

        this.getMap().setRuneTime();
        this.remove(player, false);
    }

    public int getCurseStage() {
        return curseStage;
    }

    public void setCurseStage(int value) {
        curseStage = value;
    }

    public int getCurseRate() {
        int rate = curseStage > 0 ? Math.min(100, 35 + curseStage * 15) : 0;
        return rate >= 95 ? 100 : rate;
    }

    public long getSpawnTime() {
        return spawnTime;
    }

    public void setSpawnTime(long value) {
        spawnTime = value;
    }

    public boolean isFacingLeft() {
        return facingLeft;
    }

    @Override
    public int getRange() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        MapleRuneStone rune = this;
        client.announce(MaplePacketCreator.RuneStoneClearAndAllRegister(new ArrayList<MapleRuneStone>() {{
            add(rune);
        }}));
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        client.announce(MaplePacketCreator.RuneStoneClearAndAllRegister(new ArrayList<>()));
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.RUNE;
    }

    public void remove(MapleCharacter chr, boolean noText) {
        getMap().removeRune(this, chr, noText);
        map = null;
    }
}
