package Net.server;

import Plugin.provider.MapleData;
import Plugin.provider.MapleDataProviderFactory;
import Plugin.provider.MapleDataTool;
import tools.Pair;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemMakerFactory {

    private final static ItemMakerFactory instance = new ItemMakerFactory();
    protected final Map<Integer, ItemMakerCreateEntry> createCache = new HashMap<>();
    protected final Map<Integer, GemCreateEntry> gemCache = new HashMap<>();

    protected ItemMakerFactory() {
        MapleData info = MapleDataProviderFactory.getEtc().getData("ItemMake.img");

        int reqLevel, cost, quantity;
        byte reqMakerLevel, totalupgrades;
        GemCreateEntry ret;
        ItemMakerCreateEntry imt;

        for (MapleData dataType : info.getChildren()) {
            int type = Integer.parseInt(dataType.getName());
            switch (type) {
                case 0: {
                    for (MapleData itemFolder : dataType.getChildren()) {
                        reqLevel = MapleDataTool.getInt("reqLevel", itemFolder, 0);
                        reqMakerLevel = (byte) MapleDataTool.getInt("reqSkillLevel", itemFolder, 0);
                        cost = MapleDataTool.getInt("meso", itemFolder, 0);
                        quantity = MapleDataTool.getInt("itemNum", itemFolder, 0);
                        ret = new GemCreateEntry(cost, reqLevel, reqMakerLevel, quantity);
                        for (MapleData rewardNRecipe : itemFolder.getChildren()) {
                            for (MapleData ind : rewardNRecipe.getChildren()) {
                                if (rewardNRecipe.getName().equals("randomReward")) {
                                    ret.addRandomReward(MapleDataTool.getInt("item", ind, 0), MapleDataTool.getInt("prob", ind, 0));
                                } else if (rewardNRecipe.getName().equals("recipe")) {
                                    ret.addReqRecipe(MapleDataTool.getInt("item", ind, 0), MapleDataTool.getInt("count", ind, 0));
                                }
                            }
                        }
                        gemCache.put(Integer.parseInt(itemFolder.getName()), ret);
                    }
                    break;
                }
                case 1:
                case 2:
                case 4:
                case 8:
                case 16: {
                    for (MapleData itemFolder : dataType.getChildren()) {
                        reqLevel = MapleDataTool.getInt("reqLevel", itemFolder, 0);
                        reqMakerLevel = (byte) MapleDataTool.getInt("reqSkillLevel", itemFolder, 0);
                        cost = MapleDataTool.getInt("meso", itemFolder, 0);
                        quantity = MapleDataTool.getInt("itemNum", itemFolder, 0);
                        totalupgrades = (byte) MapleDataTool.getInt("tuc", itemFolder, 0);
                        int stimulator = MapleDataTool.getInt("catalyst", itemFolder, 0);
                        imt = new ItemMakerCreateEntry(cost, reqLevel, reqMakerLevel, quantity, totalupgrades, stimulator);
                        for (MapleData Recipe : itemFolder.getChildren()) {
                            for (MapleData ind : Recipe.getChildren()) {
                                if (Recipe.getName().equals("recipe")) {
                                    imt.addReqItem(MapleDataTool.getInt("item", ind, 0), MapleDataTool.getInt("count", ind, 0));
                                }
                            }
                        }
                        createCache.put(Integer.parseInt(itemFolder.getName()), imt);
                    }
                    break;
                }
            }
        }
    }

    public static ItemMakerFactory getInstance() {
        return instance;
    }

    public GemCreateEntry getGemInfo(int itemid) {
        return gemCache.get(itemid);
    }

    public ItemMakerCreateEntry getCreateInfo(int itemid) {
        return createCache.get(itemid);
    }

    public static class GemCreateEntry {

        private final int reqLevel;
        private final int reqMakerLevel;
        private final int cost;
        private final int quantity;
        private final List<Pair<Integer, Integer>> randomReward = new ArrayList<>();
        private final List<Pair<Integer, Integer>> reqRecipe = new ArrayList<>();

        public GemCreateEntry(int cost, int reqLevel, int reqMakerLevel, int quantity) {
            this.cost = cost;
            this.reqLevel = reqLevel;
            this.reqMakerLevel = reqMakerLevel;
            this.quantity = quantity;
        }

        public int getRewardAmount() {
            return quantity;
        }

        public List<Pair<Integer, Integer>> getRandomReward() {
            return randomReward;
        }

        public List<Pair<Integer, Integer>> getReqRecipes() {
            return reqRecipe;
        }

        public int getReqLevel() {
            return reqLevel;
        }

        public int getReqSkillLevel() {
            return reqMakerLevel;
        }

        public int getCost() {
            return cost;
        }

        protected void addRandomReward(int itemId, int prob) {
            randomReward.add(new Pair<>(itemId, prob));
        }

        protected void addReqRecipe(int itemId, int count) {
            reqRecipe.add(new Pair<>(itemId, count));
        }
    }

    public static class ItemMakerCreateEntry {

        private final int reqLevel;
        private final int cost;
        private final int quantity;
        private final int stimulator;
        private final byte tuc;
        private final byte reqMakerLevel;
        private final List<Pair<Integer, Integer>> reqItems = new ArrayList<>();

        public ItemMakerCreateEntry(int cost, int reqLevel, byte reqMakerLevel, int quantity, byte tuc, int stimulator) {
            this.cost = cost;
            this.tuc = tuc;
            this.reqLevel = reqLevel;
            this.reqMakerLevel = reqMakerLevel;
            this.quantity = quantity;
            this.stimulator = stimulator;
        }

        public byte getTUC() {
            return tuc;
        }

        public int getRewardAmount() {
            return quantity;
        }

        public List<Pair<Integer, Integer>> getReqItems() {
            return reqItems;
        }

        public int getReqLevel() {
            return reqLevel;
        }

        public byte getReqSkillLevel() {
            return reqMakerLevel;
        }

        public int getCost() {
            return cost;
        }

        public int getStimulator() {
            return stimulator;
        }

        protected void addReqItem(int itemId, int amount) {
            reqItems.add(new Pair<>(itemId, amount));
        }
    }

    public void saveToDat(String dir) {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(dir + "/itemmaker.dat"))) {
            dos.writeInt(createCache.size());
            for (Map.Entry<Integer, ItemMakerCreateEntry> entry : createCache.entrySet()) {
                dos.writeInt(entry.getKey());
                ItemMakerCreateEntry imt = entry.getValue();
                dos.writeInt(imt.getReqLevel());
                dos.writeInt(imt.getCost());
                dos.writeInt(imt.getRewardAmount());
                dos.writeByte(imt.getTUC());
                dos.writeByte(imt.getReqSkillLevel());
                dos.writeInt(imt.getStimulator());
                dos.writeInt(imt.getReqItems().size());
                for (Pair<Integer, Integer> item : imt.getReqItems()) {
                    dos.writeInt(item.getLeft());
                    dos.writeInt(item.getRight());
                }
            }

            dos.writeInt(gemCache.size());
            for (Map.Entry<Integer, GemCreateEntry> entry : gemCache.entrySet()) {
                dos.writeInt(entry.getKey());
                GemCreateEntry gem = entry.getValue();
                dos.writeInt(gem.getReqLevel());
                dos.writeInt(gem.getCost());
                dos.writeInt(gem.getRewardAmount());
                dos.writeByte(gem.getReqSkillLevel());
                dos.writeInt(gem.getRandomReward().size());
                for (Pair<Integer, Integer> reward : gem.getRandomReward()) {
                    dos.writeInt(reward.getLeft());
                    dos.writeInt(reward.getRight());
                }
                dos.writeInt(gem.getReqRecipes().size());
                for (Pair<Integer, Integer> recipe : gem.getReqRecipes()) {
                    dos.writeInt(recipe.getLeft());
                    dos.writeInt(recipe.getRight());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadFromDat(String dir) {
        try (DataInputStream dis = new DataInputStream(new FileInputStream(dir + "/itemmaker.dat"))) {
            int createCacheSize = dis.readInt();
            for (int i = 0; i < createCacheSize; i++) {
                int key = dis.readInt();
                int reqLevel = dis.readInt();
                int cost = dis.readInt();
                int quantity = dis.readInt();
                byte tuc = dis.readByte();
                byte reqMakerLevel = dis.readByte();
                int stimulator = dis.readInt();
                ItemMakerCreateEntry imt = new ItemMakerCreateEntry(cost, reqLevel, reqMakerLevel, quantity, tuc, stimulator);
                int reqItemsSize = dis.readInt();
                for (int j = 0; j < reqItemsSize; j++) {
                    int itemId = dis.readInt();
                    int amount = dis.readInt();
                    imt.addReqItem(itemId, amount);
                }
                createCache.put(key, imt);
            }

            int gemCacheSize = dis.readInt();
            for (int i = 0; i < gemCacheSize; i++) {
                int key = dis.readInt();
                int reqLevel = dis.readInt();
                int cost = dis.readInt();
                int quantity = dis.readInt();
                byte reqMakerLevel = dis.readByte();
                GemCreateEntry gem = new GemCreateEntry(cost, reqLevel, reqMakerLevel, quantity);
                int randomRewardSize = dis.readInt();
                for (int j = 0; j < randomRewardSize; j++) {
                    int itemId = dis.readInt();
                    int prob = dis.readInt();
                    gem.addRandomReward(itemId, prob);
                }
                int reqRecipeSize = dis.readInt();
                for (int j = 0; j < reqRecipeSize; j++) {
                    int itemId = dis.readInt();
                    int count = dis.readInt();
                    gem.addReqRecipe(itemId, count);
                }
                gemCache.put(key, gem);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
