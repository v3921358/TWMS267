package Net.server.maps;

public class FieldAttackObjInfo {
    public final String regenObjTag;
    public final int id;
    public final int regenTime;
    public final boolean flip;
    public final int destroyTime;
    public long nextHandleTime;

    public FieldAttackObjInfo(String regenObjTag, int id, int regenTime, boolean flip, int destroyTime) {
        this.regenObjTag = regenObjTag;
        this.id = id;
        this.regenTime = regenTime;
        this.flip = flip;
        this.destroyTime = destroyTime;
    }
}
