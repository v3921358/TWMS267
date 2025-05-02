package Client.inventory;

import Config.constants.enums.ScrollIconType;
import Config.constants.enums.ScrollOptionType;
import Config.constants.enums.SpellTraceScrollType;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class EnchantScrollEntry {
    private final String scrollName;
    private final int successRate;
    private final ScrollIconType icon;
    private final SpellTraceScrollType type;
    private final ScrollOptionType option;
    private final int cost;
    private Map<EnchantScrollFlag, Integer> enchantScrollStat = new HashMap<>();

    public EnchantScrollEntry(final String scrollName, final int successRate, final ScrollIconType icon, SpellTraceScrollType type, ScrollOptionType option, int cost, Map<EnchantScrollFlag, Integer> enchantScrollStat) {
        this.scrollName = scrollName;
        this.successRate = successRate;
        this.icon = icon;
        this.type = type;
        this.option = option;
        this.cost = cost;
        this.enchantScrollStat = enchantScrollStat;
    }

    public String getName() {
        return scrollName + successRate + "%";
    }

    public final int getSuccessRate() {
        return this.successRate;
    }

    public final int getIcon() {
        return this.icon.ordinal();
    }

    public final int getType() {
        return this.type.ordinal();
    }

    public final int getOption() {
        return this.option.ordinal();
    }

    public final int getCost() {
        return this.cost;
    }

    public final int getMask() {
        int mask = 0;
        for (EnchantScrollFlag flag : this.enchantScrollStat.keySet()) {
            mask |= flag.getValue();
        }
        return mask;
    }

    public final Map<EnchantScrollFlag, Integer> getEnchantScrollStat() {
        // 排序後輸入
        return new TreeMap<>(this.enchantScrollStat);
    }
}
