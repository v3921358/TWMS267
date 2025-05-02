package SwordieX.field.fieldeffect;

import SwordieX.util.Util;

public enum FieldEffectType {
    Summon(0),
    Tremble(1),
    Object(2),
    ObjectDisable(3),
    Screen(4),
    Unk5(5),
    Unk6(6),
    PlaySound(7),
    MobHPTag(8),
    ChangeBGM(9),
    BGMVolumeOnly(10),
    BGMVolume(11),
    Unk12(12),
    Unk13(13),
    Unk14(14),
    Unk15(15),
    Unk16(16),
    RewardRoulette(17),
    ScreenDelayed(18),
    TopScreen(19),
    TopScreenDelayed(20),
    ScreenAutoLetterBox(21),
    FloatingUI(22),
    Unk23(23),
    Blind(24),
    GreyScale(25),
    OnOffLayer(26),
    Overlap(27),
    OverlapDetail(28),
    RemoveOverlapDetail(29),
    Unk30(30),
    ColorChange(31),
    BLACK_OUT(32),
    StageClear(33),
    TopScreenWithOrigin(34),

    SpineScreen(41),  // SpineScreen ~ unk 46 V262 version change  偏移值 +3
    OffSpineScreen(42),
    Introd(42),
    Unk37(43),
    Unk38(44),
    Unk39(45),
    Unk40(46),
    Unk41(47),
    Unk42(48),
    Unk43(49),
    Unk44(50),
    Unk45(51),
    Unk46(52),
    ;

    private byte val;

    FieldEffectType(int val) {
        this.val = (byte) val;
    }

    public byte getVal() {
        return val;
    }

    public static FieldEffectType getByVal(int val) {
        return Util.findWithPred(values(), v -> v.getVal() == val);
    }
}
