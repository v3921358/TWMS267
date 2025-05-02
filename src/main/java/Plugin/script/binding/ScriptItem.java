package Plugin.script.binding;

import Client.inventory.Equip;
import Client.inventory.FamiliarCard;
import Client.inventory.Item;
import Client.inventory.MaplePet;
import Config.constants.ItemConstants;
import Net.server.MapleItemInformationProvider;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ScriptItem {
    @Getter
    @Setter
    private Item item;

    public ScriptItem(Item item) {
        this.item = item;
    }

    public Item copy() {
        return getItem().copy();
    }

    public int getItemId() {
        return getItem().getItemId();
    }

    public void setItemId(int id) {
        getItem().setItemId(id);
    }

    public byte getItemType() {
        return ItemConstants.getInventoryType(getItem().getItemId()).getType();
    }

    public Equip asEquip() {
        return MapleItemInformationProvider.getInstance().getEquipById(getItem().getItemId());
    }

    public MaplePet asPet() {
        return getItem().getPet();
    }

    public FamiliarCard asFamiliarCard() {
        return getItem().getFamiliarCard();
    }

    public int getAttribute() {
        return getItem().getAttribute();
    }

    public void setAttribute(int attribute) {
        getItem().setAttribute(attribute);
    }

    public short getQuantity() {
        return getItem().getQuantity();
    }

    public void setQuantity(short quantity) {
        getItem().setQuantity(quantity);
    }

    public long getDateExpire() {
        return getItem().getExpiration();
    }

    public void setExpiration(long expire) {
        getItem().setExpiration(expire);
    }

    public int getSN() {
        return getItem().getSN();
    }

    public void setSN(int sn) {
        getItem().setSN(sn);
    }

    public long getInventoryId() {
        return getItem().getInventoryId();
    }

    public void setInventoryId(long ui) {
        getItem().setInventoryId(ui);
    }

//    ItemConstants.getInventoryType(getItem().getItemId()).Trade

    public boolean isTradeAvailable() {
        return MapleItemInformationProvider.getInstance().isTradeAvailable(getItem().getItemId());
    }

    public boolean isTradeBlock() {
        return MapleItemInformationProvider.getInstance().isTradeBlock(getItem().getItemId());
    }

    public boolean isAccountSharable() {
        return MapleItemInformationProvider.getInstance().isAccountShared(getItem().getItemId());
    }

    public String getItemName() {
        return getItem().getName();
    }

    public boolean isCash() {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        return ii.isCash(getItem().getItemId());
    }

    /**
     * 獲取裝備潛能
     * @param pos
     * @return
     */
    public int getOption(int pos) {
        Equip equip = (Equip) getItem();
        return equip.getPotential(pos, pos > 3);
    }

    /**
     * 設置裝備潛能
     * @param pos
     * @param option
     */
    public void setOption(int pos, int option) {
        Equip equip = (Equip) getItem();
        equip.setPotential(option, pos, pos > 3);
    }


}
