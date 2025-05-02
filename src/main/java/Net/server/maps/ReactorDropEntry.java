package Net.server.maps;

public class ReactorDropEntry {

    public final int itemId;
    public final int chance;
    public final int questid;
    public final int minimum;
    public final int maximum;

    public ReactorDropEntry(int itemId, int chance, int questid, int minimum, int maximum) {
        this.itemId = itemId;
        this.chance = chance;
        this.questid = questid;
        this.minimum = minimum;
        this.maximum = maximum;
    }
}
