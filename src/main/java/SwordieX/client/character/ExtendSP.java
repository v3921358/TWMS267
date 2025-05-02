package SwordieX.client.character;

import Client.MapleCharacter;
import Config.constants.JobConstants;
import connection.OutPacket;
import SwordieX.util.Util;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class ExtendSP {
    private List<SPSet> spSet;

    public ExtendSP() {
        this(0);
    }

    public ExtendSP(int subJobs) {
        spSet = new ArrayList<>();
        for (int i = 1; i <= subJobs; i++) {
            spSet.add(new SPSet((byte) i, 0));
        }
    }

    public ExtendSP(MapleCharacter chr) {
        spSet = new ArrayList<>();
        boolean isDualBlade = JobConstants.is影武者(chr.getJob());
        int sp1 = 0, sp2 = 0;
        for (int i = 0; i < chr.getRemainingSps().length; i++) {
            if (chr.getRemainingSps()[i] > 0) {
                if (i < 2) {
                    sp1 += chr.getRemainingSp(i);
                } else if (i < 4) {
                    sp2 += chr.getRemainingSp(i);
                }
                if (isDualBlade && i < 4) {
                    if (i == 1 && sp1 > 0) {
                        spSet.add(new SPSet((byte) 1, sp1));
                        spSet.add(new SPSet((byte) 2, sp1));
                    }
                    if (i == 3 && sp2 > 0) {
                        spSet.add(new SPSet((byte) 3, sp2));
                        spSet.add(new SPSet((byte) 4, sp2));
                    }
                } else {
                    spSet.add(new SPSet((byte) (i + 1), chr.getRemainingSps()[i]));
                }
            }
        }
    }

    public int getTotalSp() {
        return spSet.stream().mapToInt(SPSet::getSp).sum();
    }

    public void encode(OutPacket outPacket) {
        outPacket.encodeByte(getSpSet().size());
        for (SPSet spSet : getSpSet()) {
            outPacket.encodeByte(spSet.getJobLevel());
            outPacket.encodeInt(spSet.getSp());
        }
    }

    public void setSpToJobLevel(int jobLevel, int sp) {
        getSpSet().stream().filter(sps -> sps.getJobLevel() == jobLevel).findFirst().ifPresent(spSet -> spSet.setSp(sp));
    }

    public int getSpByJobLevel(byte jobLevel) {
        SPSet spSet = Util.findWithPred(getSpSet(), sps -> sps.getJobLevel() == jobLevel);
        if (spSet != null) {
            return spSet.getSp();
        }
        return -1;
    }
}
