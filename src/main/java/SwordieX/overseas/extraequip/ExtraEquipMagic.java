package SwordieX.overseas.extraequip;

public enum ExtraEquipMagic {
    NONE(-1),
    SKILL_INNER_GLARE(-2107976430),
    FAMILIAR_CARDS(-1723358014),
    SKILL_INNER_STORM(-1570672098),
    FAMILIAR_LIFE(-1692623269);
    public final int MagicNumber;

    ExtraEquipMagic(final int magicNumber) {
        MagicNumber = magicNumber;
    }
}
