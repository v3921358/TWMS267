package SwordieX.overseas.extraequip;

import lombok.Getter;

@Getter
public enum ExtraEquipType {

    OwnerInfo(15),

    // 內面耀光
    InnerGlareSkills(20),

    // 萌獸卡
    FamiliarSpawnedValue(19),
    FamiliarCardsData(20),
    FamiliarTeamStatSelected(22),
    FamiliarTeamStats(23),
    FamiliarGainExp(23),
    FamiliarUpgrade(25),
    FamiliarUseCardPack(26),
    FamiliarCardLock(41),

    // 內面暴風
    InnerStormSkillValue(19),
    InnerStormSkillEffect(20),

    // 萌獸實例
    FamiliarLifeUnk7(7),
    FamiliarLifeUnk8(8),
    FamiliarLifeUnk9(9),
    FamiliarLifeUnk10(10),
    FamiliarLifeUnk11(11),
    FamiliarLifeUnk12(12),
    FamiliarLifeUnk13(13),
    FamiliarLifeUnk14(14),
    FamiliarLifeUnk16(16),
    FamiliarLifeUnk17(17),
    FamiliarLifeUnk18(18),
    FamiliarLifeData(19),
    FamiliarLifePosition(20),
    FamiliarLifeHp(21),
    FamiliarLifeMp(22),
    FamiliarMove(18),
    FamiliarAttack(24),
    ;

    private final int val;

    ExtraEquipType(int val) {
        this.val = val;
    }

}
