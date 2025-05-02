/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Client.inventory;

import Config.constants.ItemConstants;

/**
 * @author PlayDK
 */
public class ModifyInventory {

    private final int mode;
    private Item item;
    private short oldPos;
    private short indicator;
    private boolean switchSrcDst = false;

    /*
     * 0 = 獲得道具
     * 1 = 更新道具數量
     * 2 = 移動道具
     * 3 = 刪除道具
     * 4 = 刷新裝備經驗
     * 5 = 移動道具小背包到背包
     * 6 = 小背包更新道具
     * 7 = 小背包刪除道具
     * 8 = 移動位置小背包裡面的道具
     * 9 = 小背包獲得道具
     */
    public ModifyInventory(int mode, Item item) {
        this.mode = mode;
        this.item = item.copy();
    }

    public ModifyInventory(int mode, Item item, short oldPos) {
        this.mode = mode;
        this.item = item.copy();
        this.oldPos = oldPos;
    }

    public ModifyInventory(int mode, Item item, short oldPos, short indicator, boolean switchSrcDst) {
        this.mode = mode;
        this.item = item.copy();
        this.oldPos = oldPos;
        this.indicator = indicator;
        this.switchSrcDst = switchSrcDst;
    }

    public int getMode() {
        if ((getInventoryType() == 2 || getInventoryType() == 3 || getInventoryType() == 4) && item.getPosition() > 10000) { //其他欄目的道具
            switch (mode) {
                case 0:
                    return 9;
                case 1:
                    return 6;
                case 2:
                    return 5;
                case 3:
                    return 7;
            }
        }
        return mode;
    }

    public int getInventoryType() {
        return ItemConstants.getInventoryType(item.getItemId()).getType();
    }

    public short getPosition() {
        return item.getPosition();
    }

    public short getOldPosition() {
        return oldPos;
    }

    public short getIndicator() {
        return indicator;
    }

    public boolean switchSrcDst() {
        return switchSrcDst;
    }

    public short getQuantity() {
        return item.getQuantity();
    }

    public Item getItem() {
        return item;
    }

    public void clear() {
        this.item = null;
    }
}
