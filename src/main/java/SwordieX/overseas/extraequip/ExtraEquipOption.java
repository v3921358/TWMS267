package SwordieX.overseas.extraequip;

import Client.MonsterFamiliar;
import Net.server.movement.LifeMovementFragment;
import SwordieX.util.Position;
import lombok.Getter;
import lombok.Setter;
import tools.Pair;

import java.awt.*;
import java.util.List;
import java.util.Map;

@Setter
@Getter
public class ExtraEquipOption {

    private ExtraEquipType type;
    private int userID;
    private int cid;
    private int intVal;
    private int size;
    private Map<Integer, MonsterFamiliar> familiars;
    private int index = 0;
    private List<Short> shortList;
    private List<Integer> buffIds;
    private List<Pair<Integer, Integer>> familiarids;
    private MonsterFamiliar familiar;
    private Position position;
    private Map<Integer, Integer> intMap;
    private int anInt;
    private Map<Integer, List<Integer>> intListMap;
    private int gatherDuration, nVal1;
    private Point oPos, mPos;
    private List<LifeMovementFragment> moveRes;
    private boolean success;
    private int sn;
    private boolean lock;

    public ExtraEquipOption(ExtraEquipType type) {
        this.type = type;
    }
}
