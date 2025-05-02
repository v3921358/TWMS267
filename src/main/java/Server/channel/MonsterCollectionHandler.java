package Server.channel;

import Client.MapleCharacter;
import Client.MapleClient;
import Net.server.MapleInventoryManipulator;
import Net.server.collection.MobCollectionGroup;
import Net.server.collection.MobCollectionRecord;
import Net.server.collection.MobCollectionReward;
import Net.server.factory.MobCollectionFactory;
import Packet.MaplePacketCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.DateUtil;
import tools.Pair;
import tools.Randomizer;
import tools.StringTool;
import tools.data.MaplePacketReader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

public class MonsterCollectionHandler {

    private static final Logger log = LoggerFactory.getLogger(MonsterCollectionHandler.class);

    public static void CompleteRewardRequest(MaplePacketReader slea, MapleClient client, MapleCharacter player) {
        if (player == null || player.getMap() == null || player.hasBlockedInventory()) return;
        final Map<Integer, MobCollectionRecord> a1358 = MobCollectionFactory.getMobCollectionData();
        switch (slea.readInt()) {
            case 0: {
                final int ao = slea.readInt();
                final int ao2 = slea.readInt();
                final int ao3 = slea.readInt();
                final int ao4 = slea.readInt();
                final MobCollectionRecord p3 = a1358.get(ao);
                final StringBuilder sb = new StringBuilder("G:");
                if (p3 != null) {
                    final MobCollectionReward b1150;
                    final MobCollectionGroup mobCollectionGroup;
                    if ((b1150 = p3.mobCollectionRewards.get(ao2)) != null && (mobCollectionGroup = b1150.rewardGroup.get(ao3)) != null) {
                        sb.append(ao).append(":").append(ao2).append(":").append(ao3).append(":").append(ao4);
                        if ("1".equals(player.getMobCollection(mobCollectionGroup.recordID, sb.toString()))) {
                            client.announce(MaplePacketCreator.showMobCollectionComplete(4, null, 0, 0));
                            break;
                        }
                        if (MobCollectionFactory.gainCollectionReward(player, mobCollectionGroup)) {
                            if (MapleInventoryManipulator.checkSpace(client, mobCollectionGroup.rewardID, 1, "")) {
                                player.gainItem(mobCollectionGroup.rewardID, (short) 1, "怪物收藏獎勵獲取");
                                player.updateMobCollection(mobCollectionGroup.recordID, sb.toString(), "1");
                                client.announce(MaplePacketCreator.showMobCollectionComplete(0, Collections.singletonList(new Pair<>(mobCollectionGroup.rewardID, 1)), 0, 0));
                                break;
                            }
                            client.announce(MaplePacketCreator.showMobCollectionComplete(3, null, mobCollectionGroup.rewardID, 1));
                            break;
                        } else {
                            client.announce(MaplePacketCreator.showMobCollectionComplete(5, null, 0, 0));
                        }
                    }
                    return;
                }
                break;
            }
            case 1: {
                final int ao5 = slea.readInt();
                final int ao6 = slea.readInt();
                slea.readInt();
                slea.readInt();
                final MobCollectionRecord p4 = a1358.get(ao5);
                final StringBuilder sb2 = new StringBuilder("S:");
                if (p4 != null) {
                    final MobCollectionReward b1151;
                    if ((b1151 = p4.mobCollectionRewards.get(ao6)) != null) {
                        sb2.append(ao5).append(":").append(ao6).append(":0:0");
                        if ("1".equals(player.getMobCollection(b1151.recordID, sb2.toString()))) {
                            client.announce(MaplePacketCreator.showMobCollectionComplete(4, null, 0, 0));
                            break;
                        }
                        boolean b1152 = true;
                        for (MobCollectionGroup mobCollectionGroup : b1151.rewardGroup.values()) {
                            if (!MobCollectionFactory.gainCollectionReward(player, mobCollectionGroup)) {
                                b1152 = false;
                                break;
                            }
                        }
                        if (b1152) {
                            if (MapleInventoryManipulator.checkSpace(client, b1151.rewardID, b1151.rewardCount, "")) {
                                player.gainItem(b1151.rewardID, (short) b1151.rewardCount, "怪物收藏獎勵獲取");
                                player.updateMobCollection(b1151.recordID, sb2.toString(), "1");
                                client.announce(MaplePacketCreator.showMobCollectionComplete(0, Collections.singletonList(new Pair<>(b1151.rewardID, b1151.rewardCount)), 0, 0));
                                break;
                            }
                            client.announce(MaplePacketCreator.showMobCollectionComplete(3, null, b1151.rewardID / 1000000, 1));
                            break;
                        } else {
                            client.announce(MaplePacketCreator.showMobCollectionComplete(5, null, 0, 0));
                        }
                    }
                    return;
                }
                break;
            }
            case 2: {
            }
            case 3: {
                final int ao7 = slea.readInt();
                slea.readInt();
                slea.readInt();
                slea.readInt();
                final MobCollectionRecord p5;
                if ((p5 = a1358.get(ao7)) != null) {
                    for (Pair i1341 : p5.rewards) {
                        final int intValue = (int) (i1341).left;
                        final int intValue2 = (int) i1341.right;
                        if (!"1".equals(player.getMobCollection(p5.recordID, "c" + intValue)) && MobCollectionFactory.getMobCollectionStatus(player, p5) >= intValue) {
                            if (MapleInventoryManipulator.checkSpace(client, intValue2, 1, "")) {
                                player.gainItem(intValue2, (short) 1, "怪物收藏獎勵獲取");
                                player.updateMobCollection(p5.recordID, "c" + intValue, "1");
                                client.announce(MaplePacketCreator.showMobCollectionComplete(0, Collections.singletonList(new Pair<>(intValue2, 1)), 0, 0));
                                break;
                            }
                            client.announce(MaplePacketCreator.showMobCollectionComplete(3, null, intValue2 / 1000000, 1));
                            break;
                        }
                    }
                    client.sendEnableActions();
                    return;
                }
                break;
            }
            case 4: {
                final int ao8 = slea.readInt();
                final int ao9 = slea.readInt();
                final int ao10 = slea.readInt();
                slea.readInt();
                a1358.get(ao8);
                final String string = ao8 + ":" + ao9 + ":" + ao10 + ":0";
                final MobCollectionRecord p6;
                final MobCollectionReward b1153;
                final MobCollectionGroup f1155;
                if ((p6 = a1358.get(ao8)) == null || (b1153 = p6.mobCollectionRewards.get(ao9)) == null || (f1155 = b1153.rewardGroup.get(ao10)) == null) {
                    break;
                }
                int n2 = -1;
                for (int j = 0; j < 5; ++j) {
                    final int an = MobCollectionFactory.getIdByGrade(j);
                    final String mobKey = player.getMobCollection(an, "mobKey");
                    String state = player.getMobCollection(an, "state");
                    if (state != null && Integer.valueOf(state) == 1 && string.equals(mobKey)) {
                        n2 = j;
                        break;
                    }
                }
                if (n2 >= 0) {
                    final int an2 = MobCollectionFactory.getIdByGrade(n2);
                    if (Long.valueOf(DateUtil.getFormatDate(new Date(), "yyyyMMddHHmm")) >= Long.valueOf(player.getMobCollection(an2, "end"))) {
                        final ArrayList<Pair<Integer, Integer>> list2 = new ArrayList<>();
                        MobCollectionFactory.getExplorationRewardIcons().get(f1155.exploraionReward).forEach((n, list) -> {
                            if (n == 0 || Randomizer.nextInt(100) < 30) {
                                list2.add(list.get(Randomizer.nextInt(list.size())));
                            }
                        });
                        boolean b1154 = true;
                        int intValue3 = 0;
                        int intValue4 = 0;
                        for (final Pair<Integer, Integer> i1342 : list2) {
                            if (!MapleInventoryManipulator.checkSpace(client, i1342.left, i1342.right, "")) {
                                intValue3 = i1342.left;
                                intValue4 = i1342.right;
                                b1154 = false;
                                break;
                            }
                        }
                        if (b1154) {
                            player.updateMobCollection(an2, "state", "0");
                            list2.forEach(i1340 -> player.gainItem(i1340.left, i1340.right.shortValue(), ""));
                            client.announce(MaplePacketCreator.showMobCollectionComplete(8, list2, 0, 0));
                            break;
                        }
                        client.announce(MaplePacketCreator.showMobCollectionComplete(3, null, intValue3 / 1000000, intValue4));
                    }
                    return;
                }
                client.announce(MaplePacketCreator.showMobCollectionComplete(13, null, 0, 0));
                break;
            }
        }
    }

    public static void ExploreRequest(MaplePacketReader slea, MapleClient c, MapleCharacter player) {
        if (player == null || player.getMap() == null || player.hasBlockedInventory()) return;
        final int ao = slea.readInt();
        final int ao2 = slea.readInt();
        final int ao3 = slea.readInt();
        final Map<Integer, MobCollectionRecord> a1358 = MobCollectionFactory.getMobCollectionData();
        try {
            final MobCollectionGroup mobCollectionGroup;
            if ((mobCollectionGroup = a1358.get(ao).mobCollectionRewards.get(ao2).rewardGroup.get(ao3)) != null) {
                if (MobCollectionFactory.gainCollectionReward(player, mobCollectionGroup)) {
                    final String questInfo = player.getWorldShareInfo(18821, "count");
                    final String string = ao + ":" + ao2 + ":" + ao3 + ":0";
                    final int i = StringTool.parseInt(questInfo);
                    int n = 0;
                    int n2 = -1;
                    for (int j = 0; j < 5; ++j) {
                        if (i >= MobCollectionFactory.getCountByGrade(j)) {
                            final int an = MobCollectionFactory.getIdByGrade(j);
                            final int k = StringTool.parseInt(player.getMobCollection(an, "state"));
                            final String mobCollection = player.getMobCollection(an, "mobKey");
                            if (k > 0) {
                                ++n;
                                if (string.equals(mobCollection)) {
                                    c.announce(MaplePacketCreator.showMobCollectionComplete(1, null, 0, 0));
                                    return;
                                }
                            } else if (n2 < 0) {
                                n2 = j;
                            }
                        }
                    }
                    if (n > 4) {
                        c.announce(MaplePacketCreator.showMobCollectionComplete(11, null, 0, 0));
                    } else {
                        if (n2 >= 0) {
                            final int an2 = MobCollectionFactory.getIdByGrade(n2);
                            final String a1359 = DateUtil.getFormatDate(new Date(System.currentTimeMillis() + mobCollectionGroup.exploraionCycle * 60L * 1000L), "yyyyMMddHHmm");
                            player.updateMobCollection(an2, "mobKey", string);
                            player.updateMobCollection(an2, "end", a1359);
                            player.updateMobCollection(an2, "state", "1");
                            c.announce(MaplePacketCreator.showMobCollectionComplete(7, null, 0, 0));
                            return;
                        }
                        c.announce(MaplePacketCreator.showMobCollectionComplete(10, null, 0, 0));
                    }
                }
            } else {
                c.announce(MaplePacketCreator.showMobCollectionComplete(1, null, 0, 0));
            }
        } catch (Exception ex) {
            c.announce(MaplePacketCreator.showMobCollectionComplete(1, null, 0, 0));
            log.error("ExploreRequest", ex);
        }
    }

}
