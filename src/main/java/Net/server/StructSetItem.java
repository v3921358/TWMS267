package Net.server;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/*
 * 套裝設置
 */
public class StructSetItem {

    //[激活屬性的數量] [激活後的套裝加成屬性]
    public final Map<Integer, StructSetItemStat> setItemStat = new LinkedHashMap<>();
    public final List<Integer> itemIDs = new ArrayList<>();
    public int setItemID; //套裝ID
    public byte completeCount; //套裝總數
    public String setItemName; //套裝名稱

    public Map<Integer, StructSetItemStat> getSetItemStats() {
        return new LinkedHashMap<>(setItemStat);
    }
}
