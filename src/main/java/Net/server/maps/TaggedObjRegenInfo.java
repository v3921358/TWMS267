package Net.server.maps;

public class TaggedObjRegenInfo {
    private String tag;
    private int removeTime;
    private int regenTime;
    public int footHoldOffY;
    private boolean visible = true;
    public int akb = 1;
    public long akd;
    public long ake;

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setRemoveTime(int removeTime) {
        this.removeTime = removeTime;
    }

    public void setRegenTime(int regenTime) {
        this.regenTime = regenTime;
    }

    public int getRemoveTime() {
        return removeTime;
    }

    public int getRegenTime() {
        return regenTime;
    }

    public int getFootHoldOffY() {
        return footHoldOffY;
    }

    public void setFootHoldOffY(int footHoldOffY) {
        this.footHoldOffY = footHoldOffY;
    }
}
