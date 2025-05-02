package Net.server.collection;

import Plugin.provider.loaders.StringData;

public class MonsterCollection {
    public int bP;
    public int collectionId;
    public int groupId;
    public int type;
    public int mobId;
    public String eliteName;
    public int g0;
    public long me;
    public int gZ;
    public int hj;

    public final String getMobkey() {
        return this.collectionId + ":" + this.bP + ":" + this.groupId + ":" + this.g0;
    }

    @Override
    public final String toString() {
        return this.eliteName + StringData.getMobStringById(this.mobId);
    }
}
