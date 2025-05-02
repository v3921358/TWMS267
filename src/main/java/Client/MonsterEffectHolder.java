package Client;

import Net.server.buffs.MapleStatEffect;

public class MonsterEffectHolder {

    public MapleStatEffect effect;
    public long startTime;
    public int localDuration;
    public int fromChrID;
    public int value;
    public int sourceID;
    public int level;
    public int remain = 0;
    public int z = 0;
    public long nextDotTime;
    public long dotDamage;
    public int moboid;
    public int dotInterval;
    public int dotSuperpos;

    public MonsterEffectHolder(final int sourceID, final int level, final int value) {
        this.sourceID = sourceID;
        this.level = level;
        this.value = value;
    }

    public MonsterEffectHolder(final int chrID, final int value, final long startTime, final int localDuration, final MapleStatEffect effect) {
        this.effect = effect;
        this.startTime = startTime;
        this.value = value;
        this.localDuration = localDuration;
        this.fromChrID = chrID;
        this.sourceID = effect.getSourceId();
        this.level = effect.getLevel();
    }

    public final boolean canNextDot() {
        final long currentTimeMillis = System.currentTimeMillis();
        if (this.nextDotTime <= currentTimeMillis) {
            this.nextDotTime = currentTimeMillis + this.dotInterval;
            return true;
        }
        return false;
    }

    public final long getLeftTime() {
        return Math.max((startTime + localDuration - System.currentTimeMillis()), 0);
    }

    public final long getCancelTime() {
        return startTime + localDuration;
    }
}
