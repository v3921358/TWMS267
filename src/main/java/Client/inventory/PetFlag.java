/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Client.inventory;

/**
 * @author PlayDK
 */
public enum PetFlag {

    PET_PICKUP_ITEM(0x01, 5190000, 5191000), // 撿道具技能
    PET_LONG_RANGE(0x02, 5190002, 5191002), // 擴大移動範圍技能
    PET_DROP_SWEEP(0x04, 5190003, 5191003), // 範圍自動撿起功能
    PET_IGNORE_PICKUP(0x08, 5190005, -1), // 勿撿特定道具技能
    PET_PICKUP_ALL(0x10, 5190004, 5191004), // 撿起無所有權道具&楓幣技能
    PET_CONSUME_HP(0x20, 5190001, 5191001), // 自動服用HP藥水技能
    PET_CONSUME_MP(0x40, 5190006, -1), // 自動服用MP藥水技能
    PET_RECALL(0x80, 5190007, -1), // 寵物召喚
    PET_AUTO_SPEAKING(0x100, 5190008, -1), // 自言自語
    LP_PET_AUTO_BUFF(0x200, 5190010, -1), // 寵物自動加持技能
    PET_CONSUME_HEALING(0x400, 5190009, -1), // 寵物萬能療傷藥自動使用技能
    PET_SMART(0x800, 5190011, -1), // 寵物訓練技能
    PET_GIANT(0x1000, 5190012, 5190014), // 寵物巨大技能
//    PET_SHOP(0x2000, 5190013, -1), // 開寵物商店技能
    ;
    private final int i;
    private final int item;
    private final int remove;

    PetFlag(int i, int item, int remove) {
        this.i = i;
        this.item = item;
        this.remove = remove;
    }

    public static PetFlag getByAddId(int itemId) {
        for (PetFlag flag : PetFlag.values()) {
            if (flag.item == itemId) {
                return flag;
            }
        }
        return null;
    }

    public static PetFlag getByDelId(int itemId) {
        for (PetFlag flag : PetFlag.values()) {
            if (flag.remove == itemId) {
                return flag;
            }
        }
        return null;
    }

    public int getValue() {
        return i;
    }

    public boolean check(int flag) {
        return (flag & i) == i;
    }
}
