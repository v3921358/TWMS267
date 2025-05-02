package Client.inventory;

import Config.constants.JobConstants;

public enum MapleWeapon {

    沒有武器(1.43f, Attribute.PhysicalMelee, 0),
    閃亮克魯(1.2f, Attribute.Magic, 212), // 21(夜光)
    調節器(1.3f, Attribute.PhysicalMelee, 213), // 213(阿戴爾)
    龍息射手(1.3f, Attribute.PhysicalMelee, 214), // 214(凱殷)
    靈魂射手(1.7f, Attribute.PhysicalRanged, 222), // 22(天使破壞者)
    魔劍(1.3f, Attribute.PhysicalMelee, 232), // 23(惡魔復仇者)
    能量劍(1.3125f, Attribute.PhysicalMelee, 242), // 24(傑諾)
    記憶長杖(1.34f, Attribute.Magic, 252), // 25(琳恩)
    ESP限製器(1.2f, Attribute.Magic, 262), // 26(凱內西斯)
    鎖鍊(1.3f, Attribute.PhysicalMelee, 272), // 27(卡蒂娜)
    魔法護腕(1.3f, Attribute.Magic, 282), // 28(伊利恩)
    仙扇(1.3f, Attribute.PhysicalMelee, 292), // 29(虎影)
    單手劍(1.2f, Attribute.PhysicalMelee, 302), // 30(英雄、米哈逸、聖騎士)
    單手斧(1.2f, Attribute.PhysicalMelee, 312), // 31(惡魔殺手)
    單手棍(1.2f, Attribute.PhysicalMelee, 322),
    短劍(1.3f, Attribute.PhysicalMelee, 332), // 33(影武者、暗影神偷)
    雙刀(1.3f, Attribute.PhysicalMelee, 342), // 34(影武者)
    //副手武器(1.0f, Attribute.PhysicalMelee, 352), // 35(所有副手武器的職業)
    手杖(1.3f, Attribute.PhysicalMelee, 362), // 36(幻影俠盜)
    短杖(1.0f, Attribute.Magic, 372), // 37(冰雷大魔導士、火毒大魔導士、主教、龍魔導士、烈焰巫師、菈菈)
    長杖(1.0f, Attribute.Magic, 382), // 38(煉獄巫師)
    雙手劍(1.34f, Attribute.PhysicalMelee, 402), // 40(聖魂劍士、凱撒)
    武拳(1.7f, Attribute.PhysicalMelee, 403), // 403(墨玄)
    環刃(1.3f, Attribute.PhysicalMelee, 404), // 404(卡莉)
    雙手斧(1.34f, Attribute.PhysicalMelee, 412),
    雙手棍(1.34f, Attribute.PhysicalMelee, 422),
    槍(1.49f, Attribute.PhysicalMelee, 432), // 43(黑騎士)
    矛(1.49f, Attribute.PhysicalMelee, 442), // 44(狂狼)
    弓(1.3f, Attribute.PhysicalRanged, 452), // 45(箭神、破風使者)
    弩(1.35f, Attribute.PhysicalRanged, 462), // 46(狙擊手、狂豹獵人)
    拳套(1.75f, Attribute.PhysicalRanged, 472), // 47(夜使者、暗夜行者)
    指虎(1.7f, Attribute.PhysicalMelee, 482), // 48(隱月、閃雷悍將、亞克、拳霸)
    火槍(1.5f, Attribute.PhysicalRanged, 492),// 49(槍神、機甲戰神)
    雙弩槍(1.3f, Attribute.PhysicalRanged, 522), // 52(精靈遊俠)
    加農砲(1.5f, Attribute.PhysicalRanged, 532), //1.35f // 53(重砲指揮官)
    太刀(1.25f, Attribute.PhysicalMelee, 542), //1.3f // 54(劍豪)
    扇子(1.35f, Attribute.Magic, 552), //1.1f // 55(陰陽師)
    琉(1.49f, Attribute.PhysicalMelee, 562), // 56(神之子)
    璃(1.34f, Attribute.PhysicalMelee, 572), // 57(神之子)
    重拳槍(1.7f, Attribute.PhysicalMelee, 582), // 58(爆拳槍神)
    古代之弓(1.3f, Attribute.PhysicalRanged, 592); // 59(開拓者)

    public enum Attribute {
        PhysicalMelee,
        PhysicalRanged,
        Magic
    }

    private final float damageMultiplier;
    private final Attribute attribute;
    private final int weaponType;

    MapleWeapon(float maxDamageMultiplier, Attribute attribute, int weaponType) {
        this.damageMultiplier = maxDamageMultiplier;
        this.attribute = attribute;
        this.weaponType = weaponType;
    }

    public float getMaxDamageMultiplier(int job) {
        if (this == 沒有武器 && !JobConstants.is冒險家海盜(job)) return 0;
        if ((JobConstants.is冒險家法師(job) || JobConstants.is烈焰巫師(job)) && (this == MapleWeapon.短杖 || this == MapleWeapon.長杖))
            return 1.2f;
        if (JobConstants.is英雄(job) && (this == MapleWeapon.雙手劍 || this == MapleWeapon.雙手斧)) return 1.44f;
        if (JobConstants.is英雄(job) && (this == MapleWeapon.單手劍 || this == MapleWeapon.單手斧)) return 1.3f;
        return damageMultiplier;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public int getBaseMastery() {
        switch (attribute) {
            case PhysicalRanged:
                return 15;
            case PhysicalMelee:
            default:
                return 20;
            case Magic:
                return 25;
        }
    }

    public int getWeaponType() {
        return weaponType;
    }

    public boolean isTwoHand() {
        return weaponType / 100 == 4 || weaponType / 100 == 5;
    }

    public boolean check(int itemId) {
        if (itemId / 1000000 != 1) {
            return false;
        }
        return weaponType == (itemId / 1000) % 1000;
    }

    public static MapleWeapon getByItemID(int itemId) {
        if (itemId / 1000000 != 1) {
            return MapleWeapon.沒有武器;
        }
        int cat = (itemId / 1000) % 1000;
        for (MapleWeapon type : MapleWeapon.values()) {
            if (cat == type.getWeaponType()) {
                return type;
            }
        }
        return MapleWeapon.沒有武器;
    }
}
