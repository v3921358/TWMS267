package Net.server.life;

import Config.constants.GameConstants;

public final class ForcedMobStat {
    private int PDRate;
    private int watk;
    private int speed;
    private int level;
    private int userCount;
    private int pushed;
    private int MDRate;
    private int eva;
    private int acc;
    private boolean change;
    private int matk;
    private long exp;

    public ForcedMobStat(MapleMonsterStats stats, int newLevel, double r) {
        newLevel = Math.min(newLevel, GameConstants.MAX_LEVEL);
        if (stats.isBoss()) {
            PDRate = stats.getPDRate();
            MDRate = stats.getMDRate();
        } else {
            PDRate = Math.min(50, (int) Math.round(stats.getPDRate() * r));
            MDRate = Math.min(50, (int) Math.round(stats.getMDRate() * r));
        }
        exp = (int) (stats.getExp() * r);
        watk = (int) (stats.getPhysicalAttack() * r);
        matk = (int) (stats.getMagicAttack() * r);
        acc = Math.round(stats.getAcc() + Math.max(0, newLevel - stats.getLevel()) * 2);
        eva = Math.round(stats.getEva() + Math.max(0, newLevel - stats.getLevel()));
        pushed = (int) (stats.getPushed() * r);
        speed = 0;
        level = newLevel;
        userCount = 0;
        change = true;
    }

    public int getUserCount() {
        return this.userCount;
    }

    public void setUserCount(final int userCount) {
        this.userCount = userCount;
    }

    public int getLevel() {
        return this.level;
    }

    public void setLevel(final int level) {
        this.level = level;
    }

    public int getSpeed() {
        return this.speed;
    }

    public void setSpeed(final int speed) {
        this.speed = speed;
    }

    public int getPushed() {
        return this.pushed;
    }

    public void setPushed(final int pushed) {
        this.pushed = pushed;
    }

    public int getMDRate() {
        return this.MDRate;
    }

    public void setMDRate(final int mdRate) {
        this.MDRate = mdRate;
    }

    public int getPDRate() {
        return this.PDRate;
    }

    public void setPDRate(final int pdRate) {
        this.PDRate = pdRate;
    }

    public int getEva() {
        return this.eva;
    }

    public void setEva(final int eva) {
        this.eva = eva;
    }

    public int getAcc() {
        return this.acc;
    }

    public void setAcc(final int acc) {
        this.acc = acc;
    }

    public int getMatk() {
        return this.matk;
    }

    public void setMatk(final int matk) {
        this.matk = matk;
    }

    public int getWatk() {
        return this.watk;
    }

    public void setWatk(final int watk) {
        this.watk = watk;
    }

    public long getExp() {
        return exp;
    }

    public void setExp(long exp) {
        this.exp = exp;
    }

    public boolean isChange() {
        return this.change;
    }

    public void setChange(final boolean change) {
        this.change = change;
    }
}
