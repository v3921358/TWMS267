package Net.server.life;

import Net.server.maps.MapleMap;
import Packet.MaplePacketCreator;

import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SpawnPoint extends Spawns {

    private final MapleMonsterStats monster;
    private final Point pos;
    private final int mobTime;
    private final int fh;
    private final int f;
    private final int id;
    private final AtomicInteger spawnedMonsters = new AtomicInteger(0);
    private final String msg;
    private final byte carnivalTeam;
    private long nextPossibleSpawn;
    private int carnival = -1;
    private int level = -1;

    public SpawnPoint(MapleMonster monster, Point pos, int mobTime, byte carnivalTeam, String msg) {
        this.monster = monster.getStats();
        this.pos = pos;
        this.id = monster.getId();
        this.fh = monster.getCurrentFH();
        this.f = monster.getF();
        this.mobTime = (mobTime < 0 ? -1 : (mobTime * 1000));
        this.carnivalTeam = carnivalTeam;
        this.msg = msg;
        this.nextPossibleSpawn = System.currentTimeMillis();
    }

    public void setCarnival(int c) {
        this.carnival = c;
    }

    public void setLevel(int c) {
        this.level = c;
    }

    @Override
    public int getF() {
        return f;
    }

    @Override
    public int getFh() {
        return fh;
    }

    @Override
    public Point getPosition() {
        return pos;
    }

    @Override
    public MapleMonsterStats getMonster() {
        return monster;
    }

    @Override
    public byte getCarnivalTeam() {
        return carnivalTeam;
    }

    @Override
    public int getCarnivalId() {
        return carnival;
    }

    @Override
    public boolean shouldSpawn(long time) {
        if (mobTime < 0) {
            return false;
        }
        // regular spawnpoints should spawn a maximum of 3 monsters; immobile spawnpoints or spawnpoints with mobtime a
        // maximum of 1
        return !(((mobTime != 0 || !monster.isMobile()) && spawnedMonsters.get() > 0) || spawnedMonsters.get() > 1) && nextPossibleSpawn <= time;
    }

    @Override
    public MapleMonster spawnMonster(MapleMap map) {
        MapleMonster mob = new MapleMonster(id, monster);
        mob.setPosition(pos);
        mob.setCy(pos.y);
        mob.setRx0(pos.x - 50);
        mob.setRx1(pos.x + 50); //these dont matter for mobs
        mob.setCurrentFh(fh);
        mob.setF(f);
        mob.setCarnivalTeam(carnivalTeam);
        if (level > -1) {
            mob.setForcedMobStat(level);
        }
        spawnedMonsters.incrementAndGet();
        mob.addListener(() -> {
            nextPossibleSpawn = System.currentTimeMillis();

            if (mobTime > 0) {
                nextPossibleSpawn += mobTime;
            }
            spawnedMonsters.decrementAndGet();
        });
        map.spawnMonster(mob, -2);
        if (msg != null) {
            map.broadcastMessage(MaplePacketCreator.serverNotice(6, msg));
        }
        return mob;
    }

    @Override
    public int getMobTime() {
        return mobTime;
    }
}
