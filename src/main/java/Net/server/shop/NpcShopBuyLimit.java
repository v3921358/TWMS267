package Net.server.shop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NpcShopBuyLimit {
    private final Map<Integer, BuyLimitData> data = new HashMap<>();
    private final int shopID;

    public NpcShopBuyLimit(final int shopID) {
        this.shopID = shopID;
    }

    public final Map<Integer, BuyLimitData> getData() {
        return data;
    }

    public final int getCount(final int n) {
        final BuyLimitData limit = data.get(n);
        if (limit != null) {
            return limit.getCount();
        }
        return 0;
    }

    public final void update(int itemId, final int count, final long now) {
        final BuyLimitData limit = data.getOrDefault(itemId, new BuyLimitData());
        limit.setCount(limit.getCount() + count);
        limit.setDate(now);
        if (!data.containsKey(itemId)) {
            data.put(itemId, limit);
        }
    }

    public final List<Integer> getInfo() {
        List<Integer> list = new ArrayList<Integer>();
        for (Map.Entry<Integer, BuyLimitData> entry : data.entrySet()) {
            final BuyLimitData limit = entry.getValue();
            if (limit.getDate() == 0) {
                continue;
            }
            if (System.currentTimeMillis() >= limit.getDate()) {
                limit.setCount(0);
                limit.setDate(0);
                list.add(entry.getKey());
            }
        }
        return list;
    }
}
