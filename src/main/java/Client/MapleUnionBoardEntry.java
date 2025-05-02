package Client;

/**
 * create by Ethan on 20170811
 *
 * @author Ethan
 */
public class MapleUnionBoardEntry {
    private final int xPos;
    private final int yPos;
    private final boolean changeable;
    private final int groupIndex;
    private final int openLevel;

    public MapleUnionBoardEntry(int xPos, int yPos, boolean changeable, int groupIndex, int openLevel) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.changeable = changeable;
        this.groupIndex = groupIndex;
        this.openLevel = openLevel;
    }

    public int getGroupIndex() {
        return this.groupIndex;
    }

    public int getYPos() {
        return this.yPos;
    }

    public int getXPos() {
        return this.xPos;
    }
}
