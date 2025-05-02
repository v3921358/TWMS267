package Net.server.factory;

import Client.MapleCharacter;
import Net.server.collection.MobCollectionGroup;
import Net.server.collection.MobCollectionRecord;
import Net.server.collection.MobCollectionReward;
import Net.server.collection.MonsterCollection;
import Net.server.life.MapleLifeFactory;
import Net.server.life.MapleMonster;
import Packet.MaplePacketCreator;
import Plugin.provider.MapleData;
import Plugin.provider.MapleDataTool;
import connection.packet.FieldPacket;
import SwordieX.field.fieldeffect.FieldEffect;
import tools.Pair;
import tools.Randomizer;

import java.util.*;

public class MobCollectionFactory {
    private static final Map<Integer, MobCollectionRecord> MobCollectionRecordData = new HashMap<>();
    private static final Map<Integer, List<MonsterCollection>> MobCollectionData = new HashMap<>();
    private static final Map<String, MonsterCollection> MobKeyData = new HashMap<>();
    private static final Map<Integer, Map<Integer, MonsterCollection>> hL = new HashMap<>();
    private static final Map<Integer, Map<Integer, List<Pair<Integer, Integer>>>> ExplorationRewardIcons = new HashMap<>();

    public static void init(final MapleData mapleData) {
        for (MapleData data : mapleData) {
            if (data.getName().matches("^\\d+$")) {
                final int no = Integer.valueOf(data.getName());
                final MobCollectionRecord p = new MobCollectionRecord();
                p.recordID = MapleDataTool.getInt("info/recordID", data, 0);
                final MapleData t;
                if ((t = data.getChildByPath("info/clearQuest")) != null) {
                    for (MapleData c1302 : t) {
                        if ((c1302).getName().length() < 2) {
                            p.rewards.add(new Pair<>(MapleDataTool.getInt("clearCount", c1302, 0), MapleDataTool.getInt("rewardID", c1302, 0)));
                        }
                    }
                }
                for (final MapleData mobCollectionInfoData : data) {
                    if (mobCollectionInfoData.getName().matches("^\\d+$")) {
                        final MobCollectionReward mbr = new MobCollectionReward();
                        final int no2 = Integer.valueOf(mobCollectionInfoData.getName());
                        mbr.recordID = MapleDataTool.getInt("info/recordID", mobCollectionInfoData, 0);
                        mbr.rewardCount = MapleDataTool.getInt("info/rewardCount", mobCollectionInfoData, 1);
                        mbr.rewardID = MapleDataTool.getInt("info/rewardID", mobCollectionInfoData, 0);
                        for (final MapleData mobCollectionGroupData : mobCollectionInfoData.getChildByPath("group")) {
                            final MobCollectionGroup mobCollectionGroup = new MobCollectionGroup();
                            final int groupId = Integer.valueOf(mobCollectionGroupData.getName());
                            mobCollectionGroup.exploraionCycle = MapleDataTool.getInt("exploraionCycle", mobCollectionGroupData, 0);
                            mobCollectionGroup.exploraionReward = MapleDataTool.getInt("exploraionReward", mobCollectionGroupData, 0);
                            mobCollectionGroup.recordID = MapleDataTool.getInt("recordID", mobCollectionGroupData, 0);
                            mobCollectionGroup.rewardID = MapleDataTool.getInt("rewardID", mobCollectionGroupData, 0);
                            for (final MapleData mobCollectionData : mobCollectionGroupData.getChildByPath("mob")) {
                                final MonsterCollection mobCollection = new MonsterCollection();
                                final int intValue4 = Integer.valueOf(mobCollectionData.getName());
                                final int id = MapleDataTool.getInt("id", mobCollectionData, 0);
                                List<MonsterCollection> list;
                                if ((list = MobCollectionData.get(id)) == null) {
                                    list = new ArrayList<>();
                                }
                                final int type = MapleDataTool.getInt("type", mobCollectionData, 0);
                                Map<Integer, MonsterCollection> map;
                                if ((map = hL.get(type)) == null) {
                                    map = new HashMap<>();
                                    hL.put(type, map);
                                }
                                MapleDataTool.getInt("starRank", mobCollectionData, 0);
                                final String eliteName = MapleDataTool.getString("eliteName", mobCollectionData, "");
                                mobCollection.collectionId = no;
                                mobCollection.bP = no2;
                                mobCollection.groupId = groupId;
                                mobCollection.g0 = intValue4;
                                mobCollection.mobId = id;
                                mobCollection.eliteName = eliteName;
                                mobCollection.type = type;
                                mobCollection.me = 1L << 31 - intValue4 * 3 % 32;
                                mobCollection.gZ = (int) Math.floor(intValue4 * 3.0 / 32);
                                mobCollection.hj = no2 + 100 * (no + 1000);
                                mobCollectionGroup.mobCollections.put(intValue4, mobCollection);
                                list.add(mobCollection);
                                map.put(id, mobCollection);
                                MobKeyData.put(mobCollection.getMobkey(), mobCollection);
                                MobCollectionData.put(id, list);
                            }
                            mbr.rewardGroup.put(groupId, mobCollectionGroup);
                        }
                        p.mobCollectionRewards.put(no2, mbr);
                    }
                }
                MobCollectionRecordData.put(no, p);
            }
        }
        Optional.ofNullable(mapleData.getChildByPath("ExplorationRewardIcon")).ifPresent(d -> {
            for (MapleData data : d) {
                final int intValue5 = Integer.valueOf((data).getName());
                final HashMap<Integer, List<Pair<Integer, Integer>>> hashMap = new HashMap<>();
                for (final MapleData c1307 : data) {
                    final ArrayList<Pair<Integer, Integer>> list2 = new ArrayList<>();
                    final int intValue6 = Integer.valueOf(c1307.getName());
                    for (final MapleData c1308 : c1307) {
                        list2.add(new Pair<>(MapleDataTool.getInt("item", c1308, 0), MapleDataTool.getInt("count", c1308, 0)));
                    }
                    hashMap.put(intValue6, list2);
                }
                ExplorationRewardIcons.put(intValue5, hashMap);
            }
        });

    }

    public static Map<Integer, Map<Integer, List<Pair<Integer, Integer>>>> getExplorationRewardIcons() {
        return ExplorationRewardIcons;
    }

    public static Map<Integer, MobCollectionRecord> getMobCollectionData() {
        return MobCollectionRecordData;
    }

    public static int getIdByGrade(int n) {
        switch (n) {
            case 0:
                return 20;
            case 1:
                return 21;
            case 2:
                return 22;
            case 3:
                return 23;
            case 4:
                return 24;
            default:
                return 0;
        }
    }

    public static int getCountByGrade(int n) {
        switch (n) {
            case 0:
            case 1:
                return 0;
            case 2:
                return 150;
            case 3:
                return 300;
            case 4: {
                return 600;
            }
            default:
                return -1;
        }
    }

    public static void tryCollect(final MapleCharacter player, final MapleMonster monster) {
        if (!player.getInfoQuest(18821).isEmpty()) {
            player.updateInfoQuest(18821, null);
        }
        final List<MonsterCollection> list = MobCollectionData.get(monster.getId());
        if (list != null) {
            final boolean isBoss = monster.getStats().isBoss();
//            monster.getEliteGrade();
//            monster.getEliteType();
            for (final MonsterCollection mobCollection : list) {
                boolean b = false;
                if (!mobCollection.eliteName.isEmpty()) {
                    if (monster.getEliteMobActive().isEmpty()) {
                        return;
                    }
                    for (int integer : monster.getEliteMobActive()) {
                        if (MapleLifeFactory.getEliteMonEff(integer).equals(mobCollection.eliteName)) {
                            b = true;
                            break;
                        }
                    }
                }
                if (mobCollection.type > 2 && isBoss) {
                    b = true;
                }
                if (Randomizer.nextInt(10000) <= 80 && ((player.getCheatTracker().inMapAttackMinutes >= 3) || b)) {
                    collectionGet(player, mobCollection);
                }
            }
        }
    }

    public static void doneCollection(final MapleCharacter player) {
        if (true) {
            MobCollectionData.values().forEach(list -> list.forEach(mobCollection -> collectionGet(player, mobCollection)));
        }
    }

    public static void registerMobCollection(final MapleCharacter chr, final int n) {
        final List<MonsterCollection> list;
        if ((list = MobCollectionData.get(n)) != null) {
            list.forEach(mobCollection -> collectionGet(chr, mobCollection));
        }
    }

    private static void collectionGet(final MapleCharacter player, final MonsterCollection mobCollection) {
        final int gz = mobCollection.gZ;
        final long me = mobCollection.me;
        final long[] c = getCollectionFlag(player, mobCollection.hj, mobCollection.bP);
        if ((me & c[gz]) == 0x0L) {
            c[gz] |= me;
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; ++i) {
                String s;
                for (int j = (s = Long.toHexString(c[i])).length(); j < 8; ++j) {
                    s = "0" + s;
                }
                sb.append(s);
            }
            int n = 0;
            String countinfo = player.getWorldShareInfo(18821, "count");
            if (countinfo != null && countinfo.length() > 0) {
                try {
                    n = Integer.parseInt(countinfo);
                } catch (Exception ignored) {
                }
            }
            player.updateWorldShareInfo(18821, "count", String.valueOf(n));
            player.updateWorldShareInfo(18821, "lc", mobCollection.getMobkey());
            player.updateMobCollection(mobCollection.hj, String.valueOf(mobCollection.bP), sb.toString());
            player.send(MaplePacketCreator.showMobCollectionComplete(13, null, 0, 0));
            player.send(FieldPacket.fieldEffect(FieldEffect.getFieldEffectFromWz("Effect/BasicEff.img/monsterCollectionGet", 0)));

            // -------------------------------------------------------------------------------------------------------------------
            player.send(MaplePacketCreator.monsterBookMessage(mobCollection + " 已經新增入怪物收藏品中了。")); // v263-4 - op 241
        }
    }

    public static void handleRandCollection(final MapleCharacter player, final int n) {
        final Map<Integer, MonsterCollection> map;
        if ((map = hL.get(n)) != null && !map.isEmpty()) {
            final MonsterCollection[] array = map.values().toArray(new MonsterCollection[0]);
            collectionGet(player, array[Randomizer.nextInt(array.length)]);
        }
    }

    public static boolean checkMobCollection(final MapleCharacter player, final int n) {
        final List<MonsterCollection> list;
        final Iterator<MonsterCollection> iterator;
        return (list = MobCollectionData.get(n)) != null && (iterator = list.iterator()).hasNext() && checkMobCollection(player, iterator.next());
    }

    public static boolean checkMobCollection(final MapleCharacter player, final String s) {
        final MonsterCollection mobCollection;
        return (mobCollection = MobKeyData.get(s)) != null && checkMobCollection(player, mobCollection);
    }

    private static boolean checkMobCollection(final MapleCharacter player, final MonsterCollection mobCollection) {
        return mobCollection.type != 8 && (mobCollection.me & getCollectionFlag(player, mobCollection.hj, mobCollection.bP)[mobCollection.gZ]) != 0x0L;
    }

    public static boolean gainCollectionReward(final MapleCharacter player, final MobCollectionGroup mobCollectionGroup) {
        boolean b = true;
        for (MonsterCollection mc : mobCollectionGroup.mobCollections.values()) {
            final MonsterCollection mobCollection;
            if ((mobCollection = mc).type != 8) {
                if ((mobCollection.me & getCollectionFlag(player, mobCollection.hj, mobCollection.bP)[mobCollection.gZ]) == 0x0L) {
                    b = false;
                    break;
                }
            }
        }
        return b && mobCollectionGroup.mobCollections.size() > 0;
    }

    private static long[] getCollectionFlag(final MapleCharacter player, final int n, int i) {
        final String mobCollection = player.getMobCollection(n, String.valueOf(i));
        final long[] array = new long[6];
        if (mobCollection != null && mobCollection.length() == 48) {
            for (i = 0; i < 6; ++i) {
                array[i] = Long.parseLong(mobCollection.substring(i << 3, i + 1 << 3), 16);
            }
        } else {
            for (i = 0; i < 6; ++i) {
                array[i] = 0L;
            }
        }
        return array;
    }

    public static int getMobCollectionStatus(final MapleCharacter player, final MobCollectionRecord mobCollectionRecord) {
        int n = 0;
        for (MobCollectionReward b1150 : mobCollectionRecord.mobCollectionRewards.values()) {
            for (MobCollectionGroup mobCollectionGroup : b1150.rewardGroup.values()) {
                for (MonsterCollection l1160 : mobCollectionGroup.mobCollections.values()) {
                    if ((l1160).type != 8) {
                        if ((l1160.me & getCollectionFlag(player, l1160.hj, l1160.bP)[l1160.gZ]) != 0x0L) {
                            ++n;
                        }
                    }
                }
            }
        }
        return n;
    }

    public static MonsterCollection getRandomMonsterCollection(final MapleCharacter player) {
        MonsterCollection monsterCollection = null;
        final List<MonsterCollection> list = new ArrayList<>();
        for (List<MonsterCollection> vals : MobCollectionData.values()) {
            for (MonsterCollection mc : vals) {
                if (!checkMobCollection(player, mc) && mc.type < 8) {
                    list.add(mc);
                }
            }
        }
        if (!list.isEmpty()) {
            monsterCollection = list.get(Randomizer.nextInt(list.size()));
        }
        return monsterCollection;
    }
}
