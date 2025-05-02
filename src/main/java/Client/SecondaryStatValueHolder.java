package Client;

import Net.server.Timer;
import Net.server.buffs.MapleStatEffect;
import Net.server.buffs.MapleStatEffect.CancelEffectAction;

import java.util.concurrent.ScheduledFuture;

public class SecondaryStatValueHolder {

    public MapleStatEffect effect;
    public long startTime;
    public int localDuration, fromChrID, value, sourceID, x, z;
    public int DropRate = 0, BDR = 0, AttackBossCount = 0, NormalMobKillCount = 0;
    private final long startChargeTime;

    public CancelEffectAction CancelAction;
    public ScheduledFuture<?> schedule;

    public SecondaryStatValueHolder(int value, int sourceID) {
        this.value = value;
        this.sourceID = sourceID;
        startChargeTime = 0;
        startTime = System.currentTimeMillis();
    }

    public SecondaryStatValueHolder(final int chrID, final int value, final int z, final long startTime, final long startChargeTime, final int localDuration, final MapleStatEffect effect, final CancelEffectAction cancelAction) {
        this.effect = effect;
        this.startTime = startTime;
        this.value = value;
        this.localDuration = localDuration;
        this.fromChrID = chrID;
        this.startChargeTime = startChargeTime;
        this.sourceID = effect.getSourceId();
        this.x = effect.getX();
        this.z = z;
        this.CancelAction = cancelAction;
        if (cancelAction != null) {
            this.schedule = Timer.BuffTimer.getInstance().schedule(cancelAction, localDuration);
        }
    }

    public final long getStartChargeTime() {
        return this.startChargeTime;
    }

    public int getLeftTime() {
        if (this.localDuration < 2100000000) {
            return Math.max((int) (startTime + localDuration - System.currentTimeMillis()), 0);
        }
        return 2100000000;
    }

    public void cancel() {
        if (this.schedule != null) {
            this.schedule.cancel(false);
            this.schedule = null;
        }
    }
}
