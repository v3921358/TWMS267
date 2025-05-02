package Client;

import java.lang.ref.WeakReference;
import java.util.concurrent.ScheduledFuture;

public class MapleCoolDownValueHolder {

    public final int skillId;
    public final long startTime;
    public final int length;
    private ScheduledFuture schedule;

    public MapleCoolDownValueHolder(int skillId, int length, ScheduledFuture schedule) {
        this.skillId = skillId;
        this.startTime = System.currentTimeMillis();
        this.length = length;
        this.schedule = schedule;
    }

    public int getLeftTime() {
        return Math.max(0, (int) (this.length - (System.currentTimeMillis() - this.startTime)));
    }

    public final void cancel() {
        if (this.schedule != null) {
            this.schedule.cancel(true);
            this.schedule = null;
        }
    }

    public static class CancelCooldownAction implements Runnable {
        private final int skillID;
        private final WeakReference<MapleCharacter> chr;

        public CancelCooldownAction(int skillID, MapleCharacter chr) {
            this.skillID = skillID;
            this.chr = new WeakReference<>(chr);
        }

        @Override
        public void run() {
            MapleCharacter chr = this.chr.get();
            if (chr != null) {
                chr.cancelSkillCooldown(this.skillID);
            }
        }
    }
}
