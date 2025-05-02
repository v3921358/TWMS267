/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Client.skills;

import Config.constants.SkillConstants;
import tools.Randomizer;

/**
 * @author PlayDK
 */
public class InnerAbillity {

    private static InnerAbillity instance = null;

    public static InnerAbillity getInstance() {
        if (instance == null) {
            instance = new InnerAbillity();
        }
        return instance;
    }

    public InnerSkillEntry renewSkill(int rank, int position, boolean temp, boolean maxLevel) {
        int posRank = Math.max(0, position == 1 ? rank : (rank - (position == 2 ? 1 : Randomizer.rand(1, 3))));
        int randomRank = Randomizer.rand(0, posRank);
        int randomSkill = 0;
        Skill skill = null;
        while (randomSkill == 0) {
            randomSkill = SkillConstants.getRandomInnerSkill();
            skill = SkillFactory.getSkill(randomSkill);
            if (skill == null || 4 - (skill.getMaxLevel() / 10) > randomRank) {
                randomSkill = 0;
            } else {
                break;
            }
        }
        int nMaxLevel = posRank * 10 - 30 + skill.getMaxLevel();
        if (nMaxLevel > skill.getMaxLevel()) {
            nMaxLevel = skill.getMaxLevel();
        }
        int skillLevel = maxLevel ? nMaxLevel : Randomizer.rand(nMaxLevel - 9, nMaxLevel);
        return new InnerSkillEntry(randomSkill, skillLevel, (byte) position, (byte) posRank, temp);
    }

    /*
     * 現在只有這3個能用
     * 2701000 - 特別還原器 - 擁有神秘力量，可以重置全部「內在能力」的還原器。使用本還原器重置時，最深層的內在能力必定為S級。
     * 2702000 - 內在能力還原器 - #c雙擊#可以對角色的內在能力進行重新設置。內在能力等級可能提高，也可能降低。\n#c無法用於SS級內在能力\n產物最高等級：S級#
     * 2702001 - 內在能力還原器 - #c雙擊#可以對角色的內在能力進行重新設置。內在能力等級可能提高，也可能降低。\n#c無法用於SS級內在能力\n產物最高等級：S級#
     */
    public int getCirculatorRank(int itemId) {
        return ((itemId % 1000) / 100) + 1;
    }

    public boolean isSuccess(int rate) {
        return rate > Randomizer.nextInt(100);
    }
}
