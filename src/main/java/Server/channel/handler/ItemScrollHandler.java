/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Server.channel.handler;

import Client.MapleCharacter;
import Client.MapleClient;
import Client.inventory.*;
import Client.skills.SkillFactory;
import Config.configs.ServerConfig;
import Config.constants.ItemConstants;
import Config.constants.ServerConstants;
import Config.constants.SkillConstants;
import Config.constants.enums.ScrollIconType;
import Config.constants.enums.SpellTraceScrollType;
import Config.constants.enums.UserChatMessageType;
import Net.server.MapleInventoryManipulator;
import Net.server.MapleItemInformationProvider;
import Net.server.commands.PlayerRank;
import Opcode.Headler.OutHeader;
import Packet.InventoryPacket;
import Packet.MaplePacketCreator;
import Packet.PacketHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.Randomizer;
import tools.data.MaplePacketLittleEndianWriter;
import tools.data.MaplePacketReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author PlayDK
 */
public class ItemScrollHandler {
    private static final Logger log = LoggerFactory.getLogger(ItemScrollHandler.class);


    public static void handlePacket(MaplePacketReader slea, MapleClient c, MapleCharacter chr, boolean cash) {
        if (chr == null || chr.getMap() == null) {
            return;
        }
        chr.updateTick(slea.readInt());
        short slot = slea.readShort();
        Item item = null;
        if (cash) {
            item = chr.getInventory(MapleInventoryType.CASH).getItem(slot);
        }
        if (item == null || !ItemConstants.類型.特殊卷軸(item.getItemId())) {
            item = chr.getInventory(MapleInventoryType.USE).getItem(slot);
            if (item == null ) {
                return;
            }
            cash = false;
        }
        MapleInventoryType ivType = null;
        if (slea.available() >= 5) {
            ivType = MapleInventoryType.getByType((byte) slea.readShort());
        }
        short dst = slea.readShort();
        short ws = 0; //是否使用祝福卷軸
        if (slea.available() >= 2) {
            ws = slea.readShort();
        }
        UseUpgradeScroll(slot, dst, ivType, ws, c, chr, 0, slea.available() >= 1 && slea.readBool(), cash);
    }

    public static boolean UseUpgradeScroll(short slot, short dst, MapleInventoryType ivType, short ws, MapleClient c, MapleCharacter chr, int vegas, boolean legendarySpirit, boolean cash) {
        if (ivType == null) {
            ivType = MapleInventoryType.EQUIP;
        } else if (ivType != MapleInventoryType.EQUIP && ivType != MapleInventoryType.DECORATION) {
            return false;
        }
        boolean whiteScroll = false; //是否使用祝福卷軸
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        chr.setScrolledPosition((short) 0);
        if ((ws & 2) == 2) {
            whiteScroll = true;
        }
        Equip toScroll;
        if (dst < 0) {
            toScroll = (Equip) chr.getInventory(MapleInventoryType.EQUIPPED).getItem(dst);
        } else {
            toScroll = (Equip) chr.getInventory(ivType).getItem(dst);
        }
        if (toScroll == null || c.getPlayer().hasBlockedInventory()) {
            return false;
        }
        if (EnhanceResultType.EQUIP_MARK.check(toScroll.getEnchantBuff())) {
            return false;
        }

        final byte oldLevel = toScroll.getCurrentUpgradeCount();
        final byte oldStarForce = toScroll.getStarForceLevel();
        final byte oldState = toScroll.getState(false);
        final byte oldAddState = toScroll.getState(true);
        final int oldFlag = toScroll.getAttribute();
        final short oldEnhanctBuff = toScroll.getEnchantBuff();
        final byte oldSlots = toScroll.getRestUpgradeCount();
        final byte oldSealedLevel = toScroll.getSealedLevel();

        Item scroll;
        if (cash) {
            scroll = chr.getInventory(MapleInventoryType.CASH).getItem(slot);
        } else {
            scroll = chr.getInventory(MapleInventoryType.USE).getItem(slot);
        }
        if (scroll == null) {
            if (chr.isAdmin()) {
                if (ServerConfig.SCROLL_NOTICE) {
                    chr.dropMessage(-9, "卷軸錯誤: 卷軸道具為空");
                }
            }
            c.announce(InventoryPacket.getInventoryFull());
            return false;
        }
        if (ItemConstants.isForGM(scroll.getItemId()) && !chr.isIntern()) {
            chr.dropMessage(1, "這個捲軸是運營員專用捲軸。");
            return false;
        }

        // 判斷捲軸等級限制
        int limitedLv = ii.getItemProperty(scroll.getItemId(), "info/limitedLv", 0);
        if (limitedLv > 0 && toScroll.getReqLevel() < limitedLv) {
            if (chr.isAdmin()) {
                if (ServerConfig.SCROLL_NOTICE) {
                    chr.dropMessage(-9, "卷軸錯誤:裝等超過捲軸可砸的上限 - 裝等" + toScroll.getReqLevel() + "卷等" + limitedLv);
                }
            }
            return false;
        }

        if (toScroll.getReqLevel() > ii.getReqEquipLevelMax(scroll.getItemId())) {
            if (chr.isAdmin()) {
                if (ServerConfig.SCROLL_NOTICE) {
                    chr.dropMessage(-9, "卷軸錯誤: 裝備等級超過限定等級");
                }
            }
            c.announce(InventoryPacket.getInventoryFull());
            return false;
        }

//        if(ItemConstants.類型.暗黑輪迴星火(scroll.getItemId())){
//            c.announce(resetStartExFire(toScroll, scroll));
//        }

        if (!ItemConstants.類型.特殊卷軸(scroll.getItemId())
                && !ItemConstants.類型.白衣卷軸(scroll.getItemId())
                && !ItemConstants.類型.裝備強化卷軸(scroll.getItemId())
                && !ItemConstants.類型.潛能卷軸(scroll.getItemId())
                && !ItemConstants.類型.附加潛能卷軸(scroll.getItemId())
                && !ItemConstants.類型.回真卷軸(scroll.getItemId())
                && !ItemConstants.類型.輪迴星火(scroll.getItemId())) {
            int scrollSlots = ItemConstants.類型.阿斯旺卷軸(scroll.getItemId()) ? ii.getSlots(scroll.getItemId()) : 1;
            if (toScroll.getRestUpgradeCount() < scrollSlots) {
                chr.dropMessage(1, "當前裝備可升級次數為: " + toScroll.getRestUpgradeCount() + " 成功或失敗需要減少: " + scrollSlots + " 的升級次數，請檢查該裝備是否符合升級條件.");
                c.announce(InventoryPacket.getInventoryFull());
                return false;
            }
        } else if (ItemConstants.類型.裝備強化卷軸(scroll.getItemId())) {
            if (toScroll.getRestUpgradeCount() >= 1 || toScroll.getStarForceLevel() >= ItemConstants.卷軸.getMaxEnhance(toScroll.getItemId()) || vegas > 0 || ii.isCash(toScroll.getItemId())) {
                if (chr.isAdmin()) {
                    if (ServerConfig.SCROLL_NOTICE) {
                        chr.dropMessage(-9, "卷軸錯誤: 強化卷軸檢測 裝備是否有升級次數: " + (toScroll.getRestUpgradeCount() >= 1) + " 裝備星級是否大於可升級的星數: " + ItemConstants.卷軸.getMaxEnhance(toScroll.getItemId()) + " - " + (vegas > 0) + " 裝備是是否為點裝: " + (ii.isCash(toScroll.getItemId())));
                    }
                }
                c.announce(InventoryPacket.getInventoryFull());
                return false;
            }
        } else if (ItemConstants.類型.潛能卷軸(scroll.getItemId())) {
            final boolean isEpic = scroll.getItemId() / 100 == 20497 && scroll.getItemId() < 2049750;//稀有卷
            final boolean isUnique = scroll.getItemId() / 100 == 20497 && scroll.getItemId() >= 2049750 && scroll.getItemId() < 2049780;//罕見卷
            final boolean isLegend = scroll.getItemId() / 100 == 20497 && scroll.getItemId() >= 2049780;//傳說卷
            boolean isSpecialEquip = ItemConstants.類型.副手(toScroll.getItemId()) || ItemConstants.類型.能源(toScroll.getItemId()) || ItemConstants.類型.特殊潛能道具(toScroll.getItemId());
            if ((!isEpic && !isUnique && !isLegend && toScroll.getState(false) > 0) || (isEpic && toScroll.getState(false) >= 18) || (isUnique && toScroll.getState(false) >= 19) || (isLegend && toScroll.getState(false) >= 20) || (ii.getItemProperty(toScroll.getItemId(), "info/tuc", 0) == 0 && !isSpecialEquip) || vegas > 0 || ii.isCash(toScroll.getItemId()) || ItemConstants.類型.無法潛能道具(toScroll.getItemId())) {
                if (chr.isAdmin()) {
                    if (ServerConfig.SCROLL_NOTICE) {
                        chr.dropMessage(-9, "卷軸錯誤: isPotentialScroll " + (toScroll.getState(false) > 0) + " " + (toScroll.getCurrentUpgradeCount() == 0 && toScroll.getRestUpgradeCount() == 0 && !isSpecialEquip) + " " + (vegas > 0) + " " + (ii.isCash(toScroll.getItemId())));
                    }
                }
                c.announce(InventoryPacket.getInventoryFull());
                return false;
            }
        } else if (ItemConstants.類型.附加潛能卷軸(scroll.getItemId())) {
            final boolean isEpic = scroll.getItemId() / 100 == 20497 && scroll.getItemId() < 2049750;//稀有卷
            final boolean isUnique = false;//罕見卷
            final boolean isLegend = false;//傳說卷
            boolean isSpecialEquip = ItemConstants.類型.副手(toScroll.getItemId()) || ItemConstants.類型.能源(toScroll.getItemId()) || ItemConstants.類型.特殊潛能道具(toScroll.getItemId());
            if ((!isEpic && isUnique && !isLegend && toScroll.getState(true) > 0) || (isEpic && toScroll.getState(true) >= 18) || (isUnique && toScroll.getState(true) >= 19) || (isLegend && toScroll.getState(true) >= 20) || (ii.getItemProperty(toScroll.getItemId(), "info/tuc", 0) == 0 && !isSpecialEquip) || vegas > 0 || ii.isCash(toScroll.getItemId())) {
                if (chr.isAdmin()) {
                    if (ServerConfig.SCROLL_NOTICE) {
                        chr.dropMessage(-9, "卷軸錯誤: isPotentialAddScroll " + (toScroll.getState(true) > 0) + " " + (toScroll.getCurrentUpgradeCount() == 0 && toScroll.getRestUpgradeCount() == 0 && !isSpecialEquip) + " " + (vegas > 0) + " " + (ii.isCash(toScroll.getItemId())));
                    }
                }
                c.announce(InventoryPacket.getInventoryFull());
                return false;
            }
        } else if (ItemConstants.類型.特殊卷軸(scroll.getItemId())) {
            if (scroll.getItemId() == 2040727) {// 鞋子防滑卷軸10%
                if (ItemAttribute.NonSlip.check(toScroll.getAttribute()) || !ItemConstants.類型.鞋子(toScroll.getItemId())) {
                    if (chr.isAdmin()) {
                        if (ServerConfig.SCROLL_NOTICE) {
                            chr.dropMessage(-9, "卷軸錯誤:已有效果" + ItemAttribute.NonSlip.check(toScroll.getAttribute()) + " 砸卷裝備為鞋子:" + ItemConstants.類型.鞋子(toScroll.getItemId()));
                        }
                    }
                    c.announce(InventoryPacket.getInventoryFull());
                    return false;
                }
            } else if (scroll.getItemId() == 2041058) {// 披風防寒卷軸10%
                if (ItemAttribute.ColdProof.check(toScroll.getAttribute()) || !ItemConstants.類型.披風(toScroll.getItemId())) {
                    if (chr.isAdmin()) {
                        if (ServerConfig.SCROLL_NOTICE) {
                            chr.dropMessage(-9, "卷軸錯誤:已有效果" + ItemAttribute.ColdProof.check(toScroll.getAttribute()) + " 砸卷裝備為披風:" + ItemConstants.類型.披風(toScroll.getItemId()));
                        }
                    }
                    c.announce(InventoryPacket.getInventoryFull());
                    return false;
                }
            } else if (ItemConstants.類型.幸運日卷軸(scroll.getItemId()) && ItemAttribute.LuckyChance.check(toScroll.getAttribute())) {// 幸運日
                if (chr.isAdmin()) {
                    if (ServerConfig.SCROLL_NOTICE) {
                        chr.dropMessage(-9, "卷軸錯誤:已有效果" + ItemAttribute.LuckyChance.check(toScroll.getAttribute()));
                    }
                }
                c.announce(InventoryPacket.getInventoryFull());
                return false;
            } else if ((ItemConstants.類型.安全卷軸(scroll.getItemId()) || ItemConstants.類型.恢復卡(scroll.getItemId())) && (ItemAttribute.ProtectRUC.check(toScroll.getAttribute()) || ItemAttribute.RegressScroll.check(toScroll.getAttribute()))) {// 安全捲軸 or 恢復卡
                if (chr.isAdmin()) {
                    if (ServerConfig.SCROLL_NOTICE) {
                        chr.dropMessage(-9, "卷軸錯誤:已有效果" + (ItemAttribute.ProtectRUC.check(toScroll.getAttribute()) || ItemAttribute.RegressScroll.check(toScroll.getAttribute())));
                    }
                }
                c.announce(InventoryPacket.getInventoryFull());
                return false;
            } else if (ItemConstants.類型.卷軸保護卡(scroll.getItemId()) && ItemAttribute.ProtectScroll.check(toScroll.getAttribute())) {//捲軸保護卡
                if (chr.isAdmin()) {
                    if (ServerConfig.SCROLL_NOTICE) {
                        chr.dropMessage(-9, "卷軸錯誤:已有效果" + ItemAttribute.ProtectScroll.check(toScroll.getAttribute()));
                    }
                }
                c.announce(InventoryPacket.getInventoryFull());
                return false;
            } else if (ItemConstants.類型.保護卷軸(scroll.getItemId())) {// 保護捲軸
                int maxEqp = ii.getItemProperty(scroll.getItemId(), "info/maxSuperiorEqp", -1);
                boolean forSuperiorEqp = maxEqp != -1;
                maxEqp = !forSuperiorEqp ? 12 : maxEqp;
                if (ItemAttribute.NonCurse.check(toScroll.getAttribute()) || maxEqp <= toScroll.getStarForceLevel() || (forSuperiorEqp && !ii.isSuperiorEquip(toScroll.getItemId()))) {
                    if (chr.isAdmin()) {
                        if (ServerConfig.SCROLL_NOTICE) {
                            chr.dropMessage(-9, "卷軸錯誤:已有保護效果" + ItemAttribute.NonCurse.check(toScroll.getAttribute()) + "武器強化次數超過或等於捲軸限制 " + (maxEqp <= toScroll.getStarForceLevel()) + " 只能砸尊貴裝而裝備不是尊貴裝 " + (forSuperiorEqp && !ii.isSuperiorEquip(toScroll.getItemId())));
                        }
                    }
                    c.announce(InventoryPacket.getInventoryFull());
                    return false;
                }
            }

            switch (scroll.getItemId()) {
                case 2530003:// 寵物專用幸運日卷軸
                case 2610200:// 寵物裝備透明藥水
                case 5068000:// 寵物專用幸運日卷軸
                case 2532001:// 寵物專用終極賽特拉捲軸
                case 5068100:// 寵物安全盾牌卷軸
                case 5068200:// 寵物卷軸保護卡
                    if (ItemConstants.類型.寵物裝備(toScroll.getItemId())) {
                        break;
                    } else {
                        if (chr.isAdmin()) {
                            if (ServerConfig.SCROLL_NOTICE) {
                                chr.dropMessage(-9, "卷軸錯誤:這個道具無法砸寵物專屬道具");
                            }
                        }
                        c.announce(InventoryPacket.getInventoryFull());
                        return false;
                    }
                default:
                    if (ItemConstants.類型.寵物裝備(toScroll.getItemId())) {
                        if (chr.isAdmin()) {
                            if (ServerConfig.SCROLL_NOTICE) {
                                chr.dropMessage(-9, "卷軸錯誤:這個捲軸不能在寵物裝備上砸");
                            }
                        }
                        c.announce(InventoryPacket.getInventoryFull());
                        return false;
                    }
            }
        } else if (ItemConstants.類型.白衣卷軸(scroll.getItemId())) {// 白衣捲軸
            if (ii.getRecover(scroll.getItemId()) <= 0 || toScroll.getCurrentUpgradeCount() + toScroll.getRestUpgradeCount() + ii.getRecover(scroll.getItemId()) > ii.getItemProperty(toScroll.getItemId(), "info/tuc", 0) + toScroll.getTotalHammer()) {
                if (chr.isAdmin()) {
                    if (ServerConfig.SCROLL_NOTICE) {
                        chr.dropMessage(-9, "卷軸錯誤:白衣卷軸 - 砸卷道具不存在卷軸升級次數 " + (ii.getRecover(scroll.getItemId()) <= 0) + " 回復次數超過" + (toScroll.getCurrentUpgradeCount() + toScroll.getRestUpgradeCount() + ii.getRecover(scroll.getItemId()) > ii.getItemProperty(toScroll.getItemId(), "info/tuc", 0) + toScroll.getTotalHammer()));
                    }
                }
                c.announce(InventoryPacket.getInventoryFull());
                return false;
            }
        }
        if (!ItemConstants.卷軸.canScroll(toScroll.getItemId()) && !ItemConstants.類型.混沌卷軸(toScroll.getItemId())) {
            if (chr.isAdmin()) {
                if (ServerConfig.SCROLL_NOTICE) {
                    chr.dropMessage(-9, "卷軸錯誤: 卷軸是否能對裝備進行砸卷 " + !ItemConstants.卷軸.canScroll(toScroll.getItemId()) + " 是否混沌卷軸 " + !ItemConstants.類型.混沌卷軸(toScroll.getItemId()));
                }
            }
            c.announce(InventoryPacket.getInventoryFull());
            return false;
        }
        if ((ItemConstants.類型.白衣卷軸(scroll.getItemId()) || ItemConstants.類型.提升卷(scroll.getItemId()) || ItemConstants.類型.普通升級卷軸(scroll.getItemId()) || ItemConstants.類型.混沌卷軸(scroll.getItemId())) && (vegas > 0 || ii.isCash(toScroll.getItemId()))) {
            if (chr.isAdmin()) {
                if (ServerConfig.SCROLL_NOTICE) {
                    chr.dropMessage(-9, "卷軸錯誤: 卷軸是否白衣卷軸 " + ItemConstants.類型.白衣卷軸(scroll.getItemId()) + " isTablet " + ItemConstants.類型.提升卷(scroll.getItemId()));
                }
            }
            c.announce(InventoryPacket.getInventoryFull());
            return false;
        }
        if (ItemConstants.類型.提升卷(scroll.getItemId()) && !ItemConstants.類型.武器攻擊力卷軸(scroll.getItemId()) && toScroll.getDurability() < 0) {
            if (chr.isAdmin()) {
                if (ServerConfig.SCROLL_NOTICE) {
                    chr.dropMessage(-9, "卷軸錯誤: isTablet " + ItemConstants.類型.提升卷(scroll.getItemId()) + " getDurability " + (toScroll.getDurability() < 0));
                }
            }
            c.announce(InventoryPacket.getInventoryFull());
            return false;
        } else if ((!ItemConstants.類型.提升卷(scroll.getItemId()) && !ItemConstants.類型.潛能卷軸(scroll.getItemId()) && !ItemConstants.類型.裝備強化卷軸(scroll.getItemId()) && !ItemConstants.類型.白衣卷軸(scroll.getItemId()) && !ItemConstants.類型.特殊卷軸(scroll.getItemId()) && !ItemConstants.類型.附加潛能卷軸(scroll.getItemId()) && !ItemConstants.類型.混沌卷軸(scroll.getItemId())) && toScroll.getDurability() >= 0 && !ItemConstants.類型.海外服特殊卷軸(scroll.getItemId())) {
            if (chr.isAdmin()) {
                if (ServerConfig.SCROLL_NOTICE) {
                    chr.dropMessage(-9, "卷軸錯誤: !isTablet ----- 1");
                }
            }
            c.announce(InventoryPacket.getInventoryFull());
            return false;
        }

        if (scroll.getItemId() == 2049405 && !ItemConstants.類型.真楓葉之心(toScroll.getItemId())) { //2049405 - 真·覺醒冒險之心專用潛能力卷軸 - 不會扣除使用卷軸的次數，會賦予真·覺醒冒險之心項鏈專用潛在能力。\n#c只有真·冒險之心項鏈可以使用。#\n成功率 100%
            chr.dropMessage(1, "這個卷軸只能對真. 楓葉之心使用。");
            c.announce(InventoryPacket.getInventoryFull());
            return false;
        }

        if (scroll.getItemId() == 2049423 && !(toScroll.getItemId() / 100 == 10121 && toScroll.getItemId() % 100 >= 64 && toScroll.getItemId() % 100 <= 74 && toScroll.getItemId() % 100 != 65 && toScroll.getItemId() % 100 != 66)) {
            c.announce(InventoryPacket.getInventoryFull());
            chr.dropMessage(1, "這個捲軸只能鬼娃恰吉的傷口使用。");
            return false;
        }

        Item wscroll = null;
        // 騙子卷軸什麼的 有些道具只能砸特定的卷軸
        Map<Integer, Integer> scrollReqs = ii.getScrollReqs(scroll.getItemId());
        if (scrollReqs != null && scrollReqs.size() > 0 && !scrollReqs.containsValue(toScroll.getItemId())) {
            if (chr.isAdmin()) {
                if (ServerConfig.SCROLL_NOTICE) {
                    chr.dropMessage(-9, "卷軸錯誤: 特定卷軸只能對指定的卷軸進行砸卷.");
                }
            }
            c.announce(InventoryPacket.getInventoryFull());
            return false;
        }

        if (whiteScroll) {
            wscroll = chr.getInventory(MapleInventoryType.USE).findById(2340000); //祝福卷軸
            if (wscroll == null) {
                if (chr.isAdmin()) {
                    if (ServerConfig.SCROLL_NOTICE) {
                        chr.dropMessage(-9, "卷軸錯誤: 使用祝福卷軸 但祝福卷軸信息為空.");
                    }
                }
                c.announce(InventoryPacket.getInventoryFull());
                whiteScroll = false;
            }
        }
        if (scroll.getItemId() == 2041200) {// 龍族水晶
            switch (toScroll.getItemId()) {
                case 1122000: // 闇黑龍王項鍊
                case 1122076: { // 混沌闇黑龍王項鍊
                    break;
                }
                default: {
                    if (chr.isAdmin()) {
                        if (ServerConfig.SCROLL_NOTICE) {
                            chr.dropMessage(-9, "卷軸錯誤:道具不是闇黑龍王項鍊或混沌闇黑龍王項鍊");
                        }
                    }
                    c.announce(InventoryPacket.getInventoryFull());
                    return false;
                }
            }
        } else if (!ItemConstants.類型.心臟(toScroll.getItemId()) && (ItemConstants.類型.提升卷(scroll.getItemId()) || ItemConstants.類型.普通升級卷軸(scroll.getItemId()))) {
            switch (scroll.getItemId() % 1000 / 100) {
                case 0: //1h
                    if (ItemConstants.類型.雙手武器(toScroll.getItemId()) || !(ItemConstants.類型.武器(toScroll.getItemId()) || MapleWeapon.雙刀.check(toScroll.getItemId()))) {
                        if (chr.isAdmin()) {
                            chr.dropMessage(-9, "卷軸錯誤: 最後檢測 --- 0");
                        }
                        c.announce(InventoryPacket.getInventoryFull());
                        return false;
                    }
                    break;
                case 1: //2h
                    if (!ItemConstants.類型.雙手武器(toScroll.getItemId()) || !ItemConstants.類型.武器(toScroll.getItemId())) {
                        if (chr.isAdmin()) {
                            chr.dropMessage(-9, "卷軸錯誤: 最後檢測 --- 1");
                        }
                        c.announce(InventoryPacket.getInventoryFull());
                        return false;
                    }
                    break;
                case 2: //armor
                    if (ItemConstants.類型.飾品(toScroll.getItemId()) || ItemConstants.類型.武器(toScroll.getItemId()) || MapleWeapon.雙刀.check(toScroll.getItemId())) {
                        if (chr.isAdmin()) {
                            chr.dropMessage(-9, "卷軸錯誤: 最後檢測 --- 2");
                        }
                        c.announce(InventoryPacket.getInventoryFull());
                        return false;
                    }
                    break;
                case 3: //accessory
                    if (!ItemConstants.類型.飾品(toScroll.getItemId()) || ItemConstants.類型.武器(toScroll.getItemId()) || MapleWeapon.雙刀.check(toScroll.getItemId())) {
                        if (chr.isAdmin()) {
                            chr.dropMessage(-9, "卷軸錯誤: 最後檢測 --- 3");
                        }
                        c.announce(InventoryPacket.getInventoryFull());
                        return false;
                    }
                    break;
            }
        } else if (!ItemConstants.類型.心臟(toScroll.getItemId()) && (ItemConstants.類型.海外服特殊卷軸(scroll.getItemId()))) {
            switch (scroll.getItemId() % 10000 / 1000) {
                case 0:
                    // 寵物裝備透明藥水
                    if (scroll.getItemId() == 2610200) {
                        if (!ItemConstants.類型.寵物裝備(toScroll.getItemId())) {
                            chr.dropMessage(1, "該道具無法使用卷軸！");
                            c.announce(InventoryPacket.getInventoryFull());
                            return false;
                        }
                    } else {
                        chr.dropMessage(-9, "此卷軸未修復");
                        c.announce(InventoryPacket.getInventoryFull());
                        return false;
                    }
                    break;
                case 2: // 雙手2h
                    if (!ItemConstants.類型.雙手武器(toScroll.getItemId()) || !ItemConstants.類型.武器(toScroll.getItemId())) {
                        if (chr.isAdmin()) {
                            chr.dropMessage(-9, "卷軸錯誤: 最後檢測 --- 4");
                        }
                        c.announce(InventoryPacket.getInventoryFull());
                        return false;
                    }
                    break;
                case 3: // 單手1h
                    if (ItemConstants.類型.雙手武器(toScroll.getItemId()) || !(ItemConstants.類型.武器(toScroll.getItemId()) || MapleWeapon.雙刀.check(toScroll.getItemId()))) {
                        if (chr.isAdmin()) {
                            chr.dropMessage(-9, "卷軸錯誤: 最後檢測 --- 5");
                        }
                        c.announce(InventoryPacket.getInventoryFull());
                        return false;
                    }
                    break;
                case 5: // 飾品accessory
                    if (!ItemConstants.類型.飾品(toScroll.getItemId()) || ItemConstants.類型.武器(toScroll.getItemId()) || MapleWeapon.雙刀.check(toScroll.getItemId())) {
                        if (chr.isAdmin()) {
                            if (ServerConfig.SCROLL_NOTICE) {
                                chr.dropMessage(-9, "卷軸錯誤: 最後檢測 --- 6");
                            }
                        }
                        c.announce(InventoryPacket.getInventoryFull());
                        return false;
                    }
                    break;
                case 6: // 防具armor
                    if (ItemConstants.類型.飾品(toScroll.getItemId()) || ItemConstants.類型.武器(toScroll.getItemId()) || MapleWeapon.雙刀.check(toScroll.getItemId())) {
                        if (chr.isAdmin()) {
                            if (ServerConfig.SCROLL_NOTICE) {
                                chr.dropMessage(-9, "卷軸錯誤: 最後檢測 --- 7");
                            }
                        }
                        c.announce(InventoryPacket.getInventoryFull());
                        return false;
                    }
                    break;
            }
        } else if (ItemConstants.類型.飾品卷軸(scroll.getItemId()) && (!ItemConstants.類型.飾品(toScroll.getItemId()) && !ItemConstants.類型.心臟(toScroll.getItemId()))) {
            if (chr.isAdmin()) {
                if (ServerConfig.SCROLL_NOTICE) {
                    chr.dropMessage(-9, "卷軸錯誤: 最後檢測 --- 8");
                }
            }
            c.announce(InventoryPacket.getInventoryFull());
            return false;
        } else if (!ItemConstants.類型.混沌卷軸(scroll.getItemId())
                && !ItemConstants.類型.白衣卷軸(scroll.getItemId())
                && !ItemConstants.類型.裝備強化卷軸(scroll.getItemId())
                && !ItemConstants.類型.潛能卷軸(scroll.getItemId())
                && !ItemConstants.類型.附加潛能卷軸(scroll.getItemId())
                && !ItemConstants.類型.特殊卷軸(scroll.getItemId())
                && !ItemConstants.類型.回真卷軸(scroll.getItemId())
                && !ItemConstants.類型.輪迴星火(scroll.getItemId())) {
            if (!ii.canScroll(scroll.getItemId(), toScroll.getItemId())) {
                if (chr.isAdmin()) {
                    if (ServerConfig.SCROLL_NOTICE) {
                        chr.dropMessage(-9, "卷軸錯誤: 砸卷的卷軸無法對裝備進行砸卷");
                    }
                }
                c.announce(InventoryPacket.getInventoryFull());
                return false;
            }
        }
        if (scroll.getQuantity() <= 0) {
            chr.dropSpouseMessage(UserChatMessageType.系統, "卷軸錯誤，背包捲軸[" + ii.getName(scroll.getItemId()) + "]數量為 0 .");
            c.announce(InventoryPacket.getInventoryFull());
            return false;
        }
        if (legendarySpirit && vegas == 0) {
            if (chr.getSkillLevel(SkillFactory.getSkill(SkillConstants.getSkillByJob(1003, chr.getJob()))) <= 0 && ServerConstants.MapleMajor < 110) {
                if (chr.isAdmin()) {
                    if (ServerConfig.SCROLL_NOTICE) {
                        chr.dropMessage(-9, "卷軸錯誤: 檢測是否技能砸卷 角色沒有擁有技能");
                    }
                }
                c.announce(InventoryPacket.getInventoryFull());
                return false;
            }
        }

        Equip returnEqp = null;
        if (ItemAttribute.RegressScroll.check(toScroll.getAttribute())
                && !ItemConstants.類型.白衣卷軸(scroll.getItemId())
                && !ItemConstants.類型.裝備強化卷軸(scroll.getItemId())
                && !ItemConstants.類型.潛能卷軸(scroll.getItemId())
                && !ItemConstants.類型.附加潛能卷軸(scroll.getItemId())
                && !ItemConstants.類型.特殊卷軸(scroll.getItemId())
                && !ItemConstants.類型.回真卷軸(scroll.getItemId())
                && !ItemConstants.類型.輪迴星火(scroll.getItemId())) {
            toScroll.removeAttribute(ItemAttribute.RegressScroll.getValue());
            returnEqp = (Equip) toScroll.copy();
            returnEqp.removeAttribute(ItemAttribute.NonCurse.getValue());
            returnEqp.removeAttribute(ItemAttribute.LuckyChance.getValue());
            returnEqp.removeAttribute(ItemAttribute.ProtectRUC.getValue());
            returnEqp.removeAttribute(ItemAttribute.ProtectScroll.getValue());
        }
        Equip scrolled = (Equip) ii.scrollEquipWithId(toScroll, scroll, whiteScroll, chr, vegas);
        Equip.ScrollResult scrollSuccess;
        if (scrolled == null) { //如果返回的砸卷後的道具為空
            if (ItemAttribute.NonCurse.check(oldFlag) || EnhanceResultType.NO_DESTROY.check(oldEnhanctBuff)) { //檢測未砸卷前是否有防爆效果
                scrolled = toScroll;
                scrollSuccess = Equip.ScrollResult.失敗;
                scrolled.removeAttribute(ItemAttribute.NonCurse.getValue());
                if (EnhanceResultType.NO_DESTROY.check(oldEnhanctBuff)) {
                    chr.dropSpouseMessage(UserChatMessageType.系統, "因裝備效果所以道具並未破壞。");
                } else {
                    chr.dropSpouseMessage(UserChatMessageType.系統, "因卷軸效果所以道具並未破壞。");
                }
            } else {
                scrollSuccess = Equip.ScrollResult.消失;
            }
        } else {
            if ((scroll.getItemId() / 100 == 20497 && scrolled.getState(false) == 1) || scrolled.getCurrentUpgradeCount() > oldLevel || scrolled.getStarForceLevel() > oldStarForce || scrolled.getState(false) > oldState || scrolled.getAttribute() != oldFlag || scrolled.getState(true) > oldAddState || scrolled.getSealedLevel() > oldSealedLevel) {
                scrollSuccess = Equip.ScrollResult.成功;
            } else if (ItemConstants.類型.白衣卷軸(scroll.getItemId()) && scrolled.getRestUpgradeCount() > oldSlots) {
                scrollSuccess = Equip.ScrollResult.成功;
            } else if (ItemConstants.類型.回真卷軸(scroll.getItemId()) && scrolled != toScroll) {
                scrollSuccess = Equip.ScrollResult.成功;
            } else if (ItemConstants.類型.輪迴星火(scroll.getItemId())) {
                scrollSuccess = Equip.ScrollResult.成功;
            } else {
                scrollSuccess = Equip.ScrollResult.失敗;
            }
            //如果砸卷後道具不為空 就清除防爆卷軸狀態 且道具不為白衣和特殊卷軸
            if (ItemAttribute.NonCurse.check(oldFlag) && !ItemConstants.類型.白衣卷軸(scroll.getItemId()) && !ItemConstants.類型.特殊卷軸(scroll.getItemId())) {
                scrolled.removeAttribute(ItemAttribute.NonCurse.getValue());
            }
            if (chr.getGmLevel() == PlayerRank.實習管理員.getSpLevel()) {
                scrolled.addAttribute(ItemAttribute.Crafted.getValue());
                scrolled.setOwner(chr.getName());
            }

        }
        //裝備帶有保護卷軸不消失的效果
        if (ItemAttribute.ProtectScroll.check(oldFlag)) {
            if (scrolled != null) {
                scrolled.removeAttribute(ItemAttribute.ProtectScroll.getValue());
            }
            if (scrollSuccess == Equip.ScrollResult.成功) {
                chr.getInventory(ItemConstants.getInventoryType(scroll.getItemId())).removeItem(scroll.getPosition(), (short) 1, false); //刪除卷軸信息
            } else {
                chr.dropSpouseMessage(UserChatMessageType.系統, "由於卷軸的效果，卷軸" + ii.getName(scroll.getItemId()) + "沒有消失。");
            }
        } else {
            chr.getInventory(ItemConstants.getInventoryType(scroll.getItemId())).removeItem(scroll.getPosition(), (short) 1, false); //刪除卷軸信息
        }
        if (whiteScroll) { //祝福卷軸
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, wscroll.getPosition(), (short) 1, false, false);
        } else if (scrollSuccess == Equip.ScrollResult.失敗 && scrolled.getRestUpgradeCount() < oldSlots && chr.getInventory(MapleInventoryType.CASH).findById(5640000) != null) {
            chr.setScrolledPosition(scrolled.getPosition());
            if (vegas == 0) {
                c.announce(MaplePacketCreator.pamSongUI());
            }
        }
        List<ModifyInventory> mods = new ArrayList<>();
        mods.add(new ModifyInventory(scroll.getQuantity() > 0 ? 1 : 3, scroll)); //更新卷軸信息 [1 = 更新卷軸數量 3 = 刪除卷軸]
        if (scrollSuccess == Equip.ScrollResult.消失) {
            mods.add(new ModifyInventory(3, toScroll)); //刪除裝備
            if (dst < 0) {
                chr.getInventory(MapleInventoryType.EQUIPPED).removeItem(toScroll.getPosition());
            } else {
                chr.getInventory(ivType).removeItem(toScroll.getPosition());
            }
        } else {
            if (ItemConstants.類型.武器(scrolled.getItemId())
                    && scrolled.getStarForceLevel() == oldStarForce
                    && !ItemConstants.類型.白衣卷軸(scroll.getItemId())
                    && !ItemConstants.類型.潛能卷軸(scroll.getItemId())
                    && !ItemConstants.類型.附加潛能卷軸(scroll.getItemId())
                    && !ItemConstants.類型.特殊卷軸(scroll.getItemId())
                    && !ItemConstants.類型.輪迴星火(scroll.getItemId())) {
                scrolled.getStarForce().resetEquipStats(scrolled);
            }
            if (vegas == 0) {
                mods.add(new ModifyInventory(3, scrolled)); //刪除裝備
                mods.add(new ModifyInventory(0, scrolled)); //獲得裝備
            }
        }

        c.announce(InventoryPacket.modifyInventory(true, mods, chr));
        chr.getMap().broadcastMessage(chr, InventoryPacket.getScrollEffect(chr.getId(), scrollSuccess, legendarySpirit, whiteScroll, scroll.getItemId(), toScroll.getItemId()), vegas == 0);
        if (returnEqp != null) {
            chr.getTempValues().put("ReturnEquip", returnEqp);
            c.announce(InventoryPacket.encodeReturnEffectConfirm(returnEqp, scroll.getItemId()));
            c.announce(InventoryPacket.encodeReturnEffectModified(returnEqp, scroll.getItemId()));
        }
        if (dst < 0 && (scrollSuccess == Equip.ScrollResult.成功 || scrollSuccess == Equip.ScrollResult.消失) && vegas == 0) {
            chr.equipChanged();
        }
        chr.finishActivity(120102);
        if (scrolled != null) {
            chr.forceReAddItem_NoUpdate(scrolled, scrolled.getPosition() >= 0 ? ivType : MapleInventoryType.EQUIPPED);
            if (scrolled.getStarForceLevel() > oldStarForce && scrolled.getStarForceLevel() > 12) {
                chr.getClient().getChannelServer().startMapEffect(chr.getName() + "成功將" + ii.getName(scrolled.getItemId()) + "強化至 " + scrolled.getStarForceLevel() + "星！", 5120037);
            }
            if(ItemConstants.類型.輪迴星火(scroll.getItemId())){
                if(ItemConstants.類型.暗黑輪迴星火(scroll.getItemId())){
                    c.announce(resetStartExFire(toScroll, scroll));
                    c.announce(resetStartExFireResult(chr,scrolled, (byte) 1));
                    c.announce(resetStartExFireResult(chr,scrolled, (byte) 0));
                }else{
                    //                c.announce(resetStartFire(scroll.getItemId(), scrolled.getPosition(), scroll.getQuantity()));
                    c.announce(resetStartFire(scroll.getItemId(), scrolled.getPosition(), scroll.getPosition()));
                }
            }
        }
        return scrollSuccess == Equip.ScrollResult.成功;
    }

    public static void UseEquipEnchanting(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        int mode = slea.readByte();
        switch (mode) {
            case 0: { // 咒文的痕跡 強化
                chr.updateTick(slea.readInt());
                short position = slea.readShort();
                short sposition = slea.readShort();
                short success = slea.readShort();
                Equip toScroll = (Equip) chr.getInventory(position < 0 ? MapleInventoryType.EQUIPPED : MapleInventoryType.EQUIP).getItem(position);
                if (toScroll == null) {
                    chr.send(InventoryPacket.enchantingOperation(104, 0, null, null, null, "", false, false, 0, chr.getMvpLevel()));
                    c.sendEnableActions();
                    return;
                }
                ArrayList<EnchantScrollEntry> scrolls = EnchantHandler.getScrollList(toScroll);
                if (scrolls.isEmpty() || scrolls.size() < sposition - 1 || (scrolls.get(sposition).getIcon() < 4 && toScroll.getRestUpgradeCount() < 1) || (scrolls.get(sposition).getIcon() == 4 && toScroll.getRestUpgradeCount() == ii.getSlots(toScroll.getItemId()) + toScroll.getTotalHammer()) || (scrolls.get(sposition).getIcon() == 5 && toScroll.getCurrentUpgradeCount() + toScroll.getRestUpgradeCount() == ii.getSlots(toScroll.getItemId()) + toScroll.getTotalHammer()) || !c.getPlayer().haveItem(4001832, scrolls.get(sposition).getCost())) {
                    c.sendEnableActions();
                    if (c.getPlayer().isAdmin()) {
                        c.getPlayer().dropMessage(-9, "[咒文強化] 檢測：找不到卷軸：" + scrolls.isEmpty() + " 收到的卷軸不在範圍：" + (scrolls.size() < sposition - 1) + " 捲軸為普通捲軸且沒有剩餘升級次數：" + (scrolls.get(sposition).getIcon() < 4 && toScroll.getRestUpgradeCount() < 1) + " 捲軸為回真捲軸沒有上過卷：" + (scrolls.get(sposition).getIcon() == 4 && toScroll.getRestUpgradeCount() == ii.getSlots(toScroll.getItemId()) + toScroll.getTotalHammer()) + " 捲軸為純白捲軸且沒有可拉次數：" + (scrolls.get(sposition).getIcon() == 5 && toScroll.getCurrentUpgradeCount() + toScroll.getRestUpgradeCount() == ii.getSlots(toScroll.getItemId()) + toScroll.getTotalHammer()) + " 咒文不足：" + !c.getPlayer().haveItem(4001832, scrolls.get(sposition).getCost()));
                    }
                    chr.send(InventoryPacket.enchantingOperation(104, 0, null, null, null, "", false, false, 0, chr.getMvpLevel()));
                    return;
                }
                EnchantScrollEntry scroll = scrolls.get(sposition);
                if (Randomizer.isSuccess(scroll.getSuccessRate())) {
                    success = 1;
                } else {
                    success = 0;
                }
                if (c.getPlayer().isAdmin()) {
                    c.getPlayer().dropMessage(-9, "[咒文強化] 強化道具：" + toScroll + " 選中卷軸:" + scroll.getName() + " 消耗咒文：" + scroll.getCost() + " 成功率：" + scroll.getSuccessRate() + "%  強化結果：" + (success == 1));
                }
                int mask = scroll.getMask();
                Equip oldEq = (Equip) toScroll.copy();
                if (success == 1) {
                    if (scroll.getIcon() < ScrollIconType.卷軸_15Percentage.ordinal()) {
                        for (Map.Entry<EnchantScrollFlag, Integer> stat : scroll.getEnchantScrollStat().entrySet()) {
                            switch (stat.getKey()) {
                                case 物攻:
                                    toScroll.setPad((short) (toScroll.getPad() + stat.getValue()));
                                    break;
                                case 魔攻:
                                    toScroll.setMad((short) (toScroll.getMad() + stat.getValue()));
                                    break;
                                case 力量:
                                    toScroll.setStr((short) (toScroll.getStr() + stat.getValue()));
                                    break;
                                case 敏捷:
                                    toScroll.setDex((short) (toScroll.getDex() + stat.getValue()));
                                    break;
                                case 智力:
                                    toScroll.setInt((short) (toScroll.getInt() + stat.getValue()));
                                    break;
                                case 幸運:
                                    toScroll.setLuk((short) (toScroll.getLuk() + stat.getValue()));
                                    break;
                                case 物防:
                                    toScroll.setLuk((short) (toScroll.getLuk() + stat.getValue()));
                                    break;
                                case 魔防:
                                    toScroll.setLuk((short) (toScroll.getLuk() + stat.getValue()));
                                    break;
                            }
                        }
                        toScroll.setCurrentUpgradeCount((byte) (toScroll.getCurrentUpgradeCount() + 1));
                    } else if (scroll.getType() == SpellTraceScrollType.回真卷軸.ordinal()) {
                        toScroll = ii.resetEquipStats(toScroll, (byte) (scroll.getIcon() == ScrollIconType.亞克回真卷軸.ordinal() ? 1 : -1));
                    } else if (scroll.getIcon() == ScrollIconType.純白的卷軸.ordinal()) {
                        toScroll.setRestUpgradeCount((byte) (toScroll.getRestUpgradeCount() + 1));
                    }
                }

                MapleInventoryManipulator.removeById(c, ItemConstants.getInventoryType(4001832), 4001832, (short) scroll.getCost(), true, false);
                if (scroll.getIcon() < 4) {
                    if (ItemAttribute.ProtectRUC.check(toScroll.getAttribute())) {
                        toScroll.removeAttribute(ItemAttribute.ProtectRUC.getValue());
                        if (success != 1) {
                            chr.dropSpouseMessage(UserChatMessageType.黑_黃, "由於卷軸的效果，升級次數沒有減少。");
                        } else {
                            toScroll.setRestUpgradeCount((byte) (toScroll.getRestUpgradeCount() - 1));
                        }
                    } else {
                        toScroll.setRestUpgradeCount((byte) (toScroll.getRestUpgradeCount() - 1));
                    }
                }
                if (ItemAttribute.RegressScroll.check(toScroll.getAttribute())) {
                    toScroll.removeAttribute(ItemAttribute.RegressScroll.getValue());
                    chr.getTempValues().remove("ReturnEquip");
                    c.announce(MaplePacketCreator.spouseMessage(UserChatMessageType.系統, "已消失可以升級的次數保護效果"));
                }
                chr.forceReAddItem(toScroll);
                chr.send(InventoryPacket.enchantingOperation(100, success, oldEq, toScroll, null, scroll.getName(), false, false, 0, chr.getMvpLevel()));
                chr.send(InventoryPacket.enchantingOperation(50, 0, null, toScroll, EnchantHandler.getScrollList(toScroll), "", false, false, 0, chr.getMvpLevel()));
                break;
            }

            case 1: {
                chr.updateTick(slea.readInt());
                short position = slea.readShort();
                Equip toScroll = (Equip) chr.getInventory(position < 0 ? MapleInventoryType.EQUIPPED : MapleInventoryType.EQUIP).getItem(position);
                byte flag = slea.readByte();
                boolean maplePoint = flag != 0;
                boolean safe = flag == 2;
                boolean mgsuccess = slea.readByte() == 1;
                if (mgsuccess) {
                    slea.skip(4);
                }
                slea.readInt(); // [01 00 00 00]
                slea.readInt(); // [FF FF FF FF]
                boolean mesoSafe = slea.readByte() == 1;
                slea.readByte(); // [01]
                if (enchantEnhance(chr, position, maplePoint, mgsuccess, safe, mesoSafe)) {
                    chr.send(InventoryPacket.enchantingOperation(33, 0, null, null, null, "", false, false, 0, 0));
                    break;
                }
                chr.send(InventoryPacket.enchantingOperation(104, 0, null, null, null, "", false, false, 0, chr.getMvpLevel()));
                break;
            }
            case 2: { // 星力強化 繼承
                chr.updateTick(slea.readInt());
                short src = slea.readShort();
                short dec = slea.readShort();
                inheritEquip(chr, src, dec);
                chr.sendEnableActions();
                break;
            }
            case 50: { // 咒文的痕跡 放置道具
                int slot = slea.readInt();
                Equip equip = (Equip) chr.getInventory(slot < 0 ? MapleInventoryType.EQUIPPED : MapleInventoryType.EQUIP).getItem((short) slot);
                chr.send(InventoryPacket.enchantingOperation(mode, 0, null, equip, EnchantHandler.getScrollList(equip), "", false, false, 0, chr.getMvpLevel()));
                break;
            }
            case 51: {
                chr.getClient().ctx(OutHeader.LP_EquipmentEnchantDisplay.getValue(), (short) 51);
                break;
            }
            case 0x34: { // 星力強化 放置道具
                int position = slea.readInt();
                boolean safe = slea.readByte() == 1;
                Equip toEnhance = position < 0 ? (Equip) chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short) position) : (Equip) chr.getInventory(MapleInventoryType.EQUIP).getItem((short) position);
                if (toEnhance == null || EnhanceResultType.EQUIP_MARK.check(toEnhance.getEnchantBuff())) {
                    break;
                }
                if (toEnhance.getStarForceLevel() >= ItemConstants.卷軸.getMaxEnhance(toEnhance.getItemId())) {
                    break;
                }
                chr.send(InventoryPacket.enchantingOperation(mode, 0, toEnhance, null, null, "", safe, false, 0, chr.getMvpLevel()));
                break;
            }
            case 0x35: { // 星力強化成功率加成遊戲
                c.announce(InventoryPacket.enchantingOperation(mode, 0, null, null, null, "", false, false, 0, chr.getMvpLevel()));
                break;
            }
        }
    }

    public static byte[] resetStartFire(int fromId, int ItemSlot, int count){
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_RESET_FIRE_STAR.getValue());
        mplew.writeInt(fromId);
        mplew.writeInt(ItemSlot);
        mplew.write(0);
        mplew.writeInt(count);
        return mplew.getPacket();
    }

    public static byte[] resetStartExFire(Equip equip, Item item){
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.CharacterPotentialResult.getValue());
        mplew.writeHexString("DB C0 00 00 06 06 00 04");
        mplew.writeLong(equip.getFlameFlag());
        mplew.writeInt(equip.getPosition());
        mplew.writeInt(item.getItemId());
        mplew.writeShort(item.getPosition());
        mplew.writeShort(item.getPosition());
        mplew.writeShort(0);
        mplew.writeInt(equip.getPosition());
        mplew.writeInt(ItemConstants.getInventoryType(item.getItemId()).getType());
        return mplew.getPacket();
    }

    public static byte[] resetStartExFireResult(MapleCharacter chr, Equip equip,byte position){
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_DressUpInfoModified.getValue());
        mplew.writeHexString("00 00 00 00");
        mplew.write(1);
        mplew.write(position);
        if(position == 1){
            long nirvana_flame_state_old = Long.parseLong(chr.getKeyValue("nirvana_flame_state_old")) ;
            Map<Integer, Integer> before = NirvanaFlame.getExStat(equip,nirvana_flame_state_old);
            mplew.writeInt(before.size());
            for(Map.Entry<Integer, Integer> entry : before.entrySet()){
                mplew.writeInt(entry.getKey());
                mplew.writeInt(entry.getValue());
            }
        }else{
            long nirvana_flame_state_new = Long.parseLong(chr.getKeyValue("nirvana_flame_state_new")) ;
            Map<Integer, Integer> after = NirvanaFlame.getExStat(equip,nirvana_flame_state_new);
            mplew.writeInt(after.size());
            for(Map.Entry<Integer, Integer> entry : after.entrySet()){
                mplew.writeInt(entry.getKey());
                mplew.writeInt(entry.getValue());
            }
            PacketHelper.GW_ItemSlotBase_Encode(mplew,equip);
        }
        return mplew.getPacket();
    }

    public static void inheritEquip(MapleCharacter chr, short src, short dec) {
        ArrayList<ModifyInventory> mods = new ArrayList<>();
        Equip srcEquip = (Equip) chr.getInventory(src < 0 ? MapleInventoryType.EQUIPPED : MapleInventoryType.EQUIP).getItem(src);
        Equip decEquip = (Equip) chr.getInventory(MapleInventoryType.EQUIP).getItem(dec);
        if (srcEquip == null || decEquip == null || srcEquip.getItemId() != decEquip.getItemId() || srcEquip.isMvpEquip()) {
            return;
        }
        srcEquip.inherit(decEquip, srcEquip);
        if (EnhanceResultType.EQUIP_MARK.check(decEquip.getEnchantBuff())) {
            srcEquip.setEnchantBuff((short) (decEquip.getEnchantBuff() - EnhanceResultType.EQUIP_MARK.getValue()));
            mods.add(new ModifyInventory(3, srcEquip));
            mods.add(new ModifyInventory(0, srcEquip));
            mods.add(new ModifyInventory(3, decEquip));
            chr.getInventory(MapleInventoryType.EQUIP).removeItem(decEquip.getPosition());
            chr.send(InventoryPacket.enchantingOperation(103, 0, decEquip, srcEquip, null, "", false, false, 0, chr.getMvpLevel()));
            chr.send(InventoryPacket.modifyInventory(true, mods, chr));
            chr.equipChanged();
        } else {
            chr.send(InventoryPacket.enchantingOperation(104, 0, null, null, null, "", false, false, 0, chr.getMvpLevel()));
        }
    }

    //星力強化
    public static final int[] sfSuccessProp = {95, 90, 85, 85, 80, 75, 70, 65, 60, 55, 45, 35, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 3, 2, 1};
    public static final int[] sfDestroyProp = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 10, 20, 20, 20, 30, 30, 30, 30, 70, 70, 100, 100, 100};
    public static final int[] sfSuccessPropSup = {50, 50, 45, 40, 40, 40, 40, 40, 40, 37, 35, 35, 3, 2, 1};
    public static final int[] sfDestroyPropSup = {0, 0, 0, 0, 0, 6, 10, 14, 20, 30, 40, 50, 100, 100, 100};

    public enum StarForceResult {

        降級(0),
        成功(1),
        損壞(2),
        失敗(3),
        默認(-1);

        private final byte value;

        StarForceResult(int value) {
            this.value = (byte) value;
        }

        public byte getValue() {
            return value;
        }
    }

    public static boolean enchantEnhance(MapleCharacter chr, short slot, boolean maplePoint, boolean mgsuccess, boolean safe, boolean mesoSafe) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        chr.setScrolledPosition((short) 0);
        Equip item = slot < 0 ? (Equip) chr.getInventory(MapleInventoryType.EQUIPPED).getItem(slot) : (Equip) chr.getInventory(MapleInventoryType.EQUIP).getItem(slot);
        if (item == null || chr.hasBlockedInventory()) {
            return false;
        }
        short freeSlot = chr.getInventory(MapleInventoryType.EQUIP).getNextFreeSlot();
        if (slot < 0 && freeSlot == -1) {
            chr.dropMessage(1, "裝備欄位請空出一格。");
            return false;
        }
        if (item.getStarForceLevel() >= ItemConstants.卷軸.getMaxEnhance(item.getItemId())) {
            return false;
        }
        byte enhance = item.getStarForceLevel();
        if (enhance < 12 || enhance > 16) {
            mesoSafe = false;
        }
        if (!maplePoint) {
            safe = mesoSafe;
        }
        boolean isSuperior = ii.isSuperiorEquip(item.getItemId());
        long meso;
        if (maplePoint) {
            meso = safe ? ServerConfig.SF_MP_SAFE_AMOUNT : ServerConfig.SF_MP_AMOUNT;
        } else {
            meso = EnchantHandler.getStarForceMeso(ii.getReqLevel(item.getItemId()), enhance, isSuperior);
            if (meso < 0) {
                return false;
            } else if (mesoSafe) {
                meso *= 2;
            }
        }
        if (item.getRestUpgradeCount() > 0 || enhance >= ItemConstants.卷軸.getMaxEnhance(item.getItemId()) || ii.isCash(item.getItemId()) || maplePoint ? chr.getCSPoints(2) < meso : chr.getMeso() < meso) {
            if (chr.isAdmin()) {
                chr.dropMessage(6, "[星力強化] 星力強化檢測 裝備是否有剩餘升級次數：" + (item.getRestUpgradeCount() > 0) + " 裝備星級是否大於可升級的星數：" + (enhance >= ItemConstants.卷軸.getMaxEnhance(item.getItemId())) + " 裝備是否是為點裝：" + ii.isCash(item.getItemId()) + (maplePoint ? " 楓點" : " 楓幣") + "不足：" + (maplePoint ? chr.getCSPoints(2) < meso : chr.getMeso() < meso));
            }
            return false;
        }
        Item toEnhance = item.copy();

        int successprop = (isSuperior ? sfSuccessPropSup[enhance] : sfSuccessProp[enhance]) * 100;
        int destroyprop = (isSuperior ? sfDestroyPropSup[enhance] : sfDestroyProp[enhance]) * 100;
        boolean fall = false;
        int fallprop = Math.max(0, 10000 - successprop - destroyprop);
        if (mgsuccess) {
            successprop *= 1.05;
        }
        if ((isSuperior && enhance > 0) || (enhance >= 10 && enhance % 5 != 0)) {
            fall = true;
        }
        StarForceResult result;
        if (Randomizer.nextInt(10000) < successprop || item.getFailCount() >= 2) { // 成功
            result = StarForceResult.成功;
        } else if (Randomizer.nextInt(10000) < destroyprop && !safe && ServerConfig.SF_ENABLE_CURSE) { // 損壞概率
            result = StarForceResult.損壞;
        } else if (fall) {
            result = StarForceResult.降級;
        } else {
            result = StarForceResult.失敗;
        }
        if (chr.isAdmin()) {
            chr.dropSpouseMessage(UserChatMessageType.系統, "星力強化 - 強化道具：" + item + " 當前星級：" + enhance + "星 消耗幣種：" + (maplePoint ? "楓點" : "楓幣") + " 消耗量：" + meso + " 成功機率：" + (successprop / 100.) + "% 損壞機率：" + (destroyprop / 100.) + "% 失敗(" + (fall ? "下滑" : "維持") + ")機率：" + (fallprop / 100.) + "% 強化結果：" + result.name());
        }
        if (chr.isAdmin() && chr.isInvincible()) {
            result = StarForceResult.成功;
            //chr.dropMessage(-6, "伺服器管理員無敵狀態升星成功率提升到100%");
        }
        // 道具處理
        switch (result) {
            case 降級:
                item.setStarForceLevel((byte) Math.max(0, item.getStarForceLevel() - 1));
                item.setFailCount(item.getFailCount() + 1);
                break;
            case 成功:
                item.setStarForceLevel((byte) (item.getStarForceLevel() + 1));
                break;
            case 損壞:
                if (item.getStarForceLevel() > 12) {
                    item.setStarForceLevel((byte) 12);
                }
                item.setEnchantBuff((short) (item.getEnchantBuff() | EnhanceResultType.EQUIP_MARK.getValue()));
                item.setFailCount(0);
                break;
            case 失敗:
                item.setFailCount(0);
                break;
        }
        if (maplePoint) {
            chr.modifyCSPoints(2, (int) -meso);
        } else {
            if (!isSuperior && enhance <= 17) {
                if (chr.isSilverMvp()) {
                    meso *= chr.getMvpLevel() <= PlayerRank.MVP銀牌.getLevel() ? 0.97 : chr.getMvpLevel() <= PlayerRank.MVP金牌.getLevel() ? 0.95 : 0.9;
                }
            }
            chr.gainMeso(-meso, false, false);
        }

        ArrayList<ModifyInventory> arrayList = new ArrayList<>();
        arrayList.add(new ModifyInventory(3, item));
        if (result == StarForceResult.損壞 && slot < 0) {
            MapleInventoryManipulator.unequip(chr.getClient(), slot, freeSlot);
        } else {
            arrayList.add(new ModifyInventory(0, item));
        }
        if (slot < 0) {
            chr.equipChanged();
        }
        chr.send(InventoryPacket.enchantingOperation(101, result.getValue(), toEnhance, item, null, "", false, false, 0, chr.getMvpLevel()));
        chr.forceUpdateItem(item, true);
        chr.send(InventoryPacket.modifyInventory(true, arrayList, chr));
        return true;
    }

    public static void enchantScrollEquip(EnchantScrollEntry scrollEntry, Equip equip, int mode, MapleClient c, boolean update) {
        int mask = scrollEntry.getMask();
        if (scrollEntry.getIcon() < ScrollIconType.回真卷軸.ordinal()) {
            if (mode == 1) {
                if (mask > 0) {
                    for (Map.Entry<EnchantScrollFlag, Integer> stat : scrollEntry.getEnchantScrollStat().entrySet()) {
                        switch (stat.getKey()) {
                            case 物攻:
                                equip.setPad((short) (equip.getPad() + stat.getValue()));
                                break;
                            case 魔攻:
                                equip.setMad((short) (equip.getMad() + stat.getValue()));
                                break;
                            case 力量:
                                equip.setStr((short) (equip.getStr() + stat.getValue()));
                                break;
                            case 敏捷:
                                equip.setDex((short) (equip.getDex() + stat.getValue()));
                                break;
                            case 智力:
                                equip.setInt((short) (equip.getInt() + stat.getValue()));
                                break;
                            case 幸運:
                                equip.setLuk((short) (equip.getLuk() + stat.getValue()));
                                break;
                            case 物防:
                                equip.setLuk((short) (equip.getLuk() + stat.getValue()));
                                break;
                            case 魔防:
                                equip.setLuk((short) (equip.getLuk() + stat.getValue()));
                                break;
                        }
                    }
                }
                equip.setCurrentUpgradeCount((byte) (equip.getCurrentUpgradeCount() + 1));
            }
            equip.setRestUpgradeCount((byte) (equip.getRestUpgradeCount() - 1));
        }
        if (!update) {
            MapleInventoryManipulator.removeById(c, ItemConstants.getInventoryType(4001832), 4001832, scrollEntry.getCost(), true, false);
            c.getPlayer().forceReAddItem(equip, equip.getPosition() >= 0 ? MapleInventoryType.EQUIP : MapleInventoryType.EQUIPPED);
        }
    }

    public static void ChangeWeaponPotential_WP(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        if (chr.getWeaponPoint() >= 600 && chr.getMeso() >= 100000L) {
            final Equip lazuliequip = (Equip) chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short) (-10));
            lazuliequip.magnify();
            final Equip lapisequip = ((Equip) chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short) (-11))).copyPotential(lazuliequip);
            chr.forceUpdateItem(lazuliequip, true);
            chr.forceUpdateItem(lapisequip, true);
            chr.equipChanged();
            chr.gainWeaponPoint(-600);
            chr.gainMeso(-100000L, false);
            chr.dropSpouseMessage(UserChatMessageType.頻道喇叭, "潛能被變更了。");
        } else {
            chr.dropMessage(-1, "楓幣或WP不足，無法更改潛能。");
            c.sendEnableActions();
        }
    }

    public static boolean equipScrollCheck(Equip equip, Item scroll) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        boolean checked = true;
        final int t1 = scroll.getItemId() / 10000;
        final int t2 = scroll.getItemId() / 100 % 100;
        final int t3 = equip.getItemId() / 10000 % 100;
        Label_1184:
        {
            switch (t1) {
                case 204: {
                    switch (t2) {
                        case 60:
                        case 69:
                        case 70: {
                            if ((!ItemConstants.類型.武器(equip.getItemId()) || ItemConstants.類型.雙手武器(equip.getItemId())) && !ItemConstants.類型.心臟(equip.getItemId())) {
                                checked = false;
                                break Label_1184;
                            }
                            break Label_1184;
                        }
                        case 61:
                        case 71:
                        case 78: {
                            if ((!ItemConstants.類型.武器(equip.getItemId()) || !ItemConstants.類型.雙手武器(equip.getItemId())) && !ItemConstants.類型.心臟(equip.getItemId())) {
                                checked = false;
                                break Label_1184;
                            }
                            break Label_1184;
                        }
                        case 62:
                        case 65:
                        case 66:
                        case 72:
                        case 79: {
                            if (!ItemConstants.eH(equip.getItemId()) && !ItemConstants.類型.心臟(equip.getItemId())) {
                                checked = false;
                                break Label_1184;
                            }
                            break Label_1184;
                        }
                        case 63:
                        case 67:
                        case 68:
                        case 73:
                        case 74: {
                            if (!ItemConstants.eI(equip.getItemId()) && !ItemConstants.類型.心臟(equip.getItemId())) {
                                checked = false;
                                break Label_1184;
                            }
                            break Label_1184;
                        }
                        case 92: {
                            if (!ItemConstants.eJ(equip.getItemId()) && !ItemConstants.類型.心臟(equip.getItemId())) {
                                checked = false;
                                break Label_1184;
                            }
                            break Label_1184;
                        }
                        case 80:
                        case 88: {
                            if (!ItemConstants.ez(equip.getItemId()) && !ItemConstants.類型.心臟(equip.getItemId())) {
                                checked = false;
                                break Label_1184;
                            }
                            break Label_1184;
                        }
                        case 81: {
                            if ((!ItemConstants.類型.裝備(equip.getItemId()) && !ItemConstants.類型.心臟(equip.getItemId())) || ItemConstants.ez(equip.getItemId())) {
                                checked = false;
                                break Label_1184;
                            }
                            break Label_1184;
                        }
                        case 89: {
                            if (!ItemConstants.isZeroWeapon(equip.getItemId())) {
                                checked = false;
                                break Label_1184;
                            }
                            break Label_1184;
                        }
                        case 90: {
                            if (ii.getRecover(scroll.getItemId()) <= 0 || ii.getSlots(equip.getItemId()) <= 0 || equip.getCurrentUpgradeCount() + equip.getRestUpgradeCount() >= ii.getSlots(equip.getItemId()) + equip.getTotalHammer()) {
                                checked = false;
                                break Label_1184;
                            }
                            break Label_1184;
                        }
                        case 91: {
                            if (!ItemConstants.類型.裝備(equip.getItemId()) && !ItemConstants.類型.心臟(equip.getItemId())) {
                                checked = false;
                                break Label_1184;
                            }
                            break Label_1184;
                        }
                        case 93: {
                            if (ItemConstants.ez(equip.getItemId()) || equip.getRestUpgradeCount() > 0) {
                                checked = false;
                                break Label_1184;
                            }
                            break Label_1184;
                        }
                        default: {
                            if (t2 > 20 && t2 < 59 && t3 != t2) {
                                checked = false;
                                break Label_1184;
                            }
                            break Label_1184;
                        }
                    }
                }
                case 261: {
                    switch (t2) {
                        case 0: {
                            final long exp = equip.getEquipExp();
                            final int equipItemID = equip.getItemId();
                            final byte level = equip.getCurrentUpgradeCount();
                            final int fc = ItemConstants.getMaxLevel(equipItemID);
                            final int au = ItemConstants.getExpForLevel(level, equipItemID);
                            if (scroll.getItemId() == 2610000 || level > fc || exp <= au || (scroll.getItemId() / 1000 == 2610 && !ItemConstants.is漩渦裝備(equip.getItemId()))) {
                                checked = false;
                                break;
                            }
                            break;
                        }
                        case 1: {
                            if (ItemConstants.ez(equip.getItemId()) || equip.getRestUpgradeCount() > 0) {
                                checked = false;
                            }
                            log.warn("[Upgrade Check] Uncheck scrollType:" + t2);
                            break;
                        }
                        case 2: {
                            if (!ItemConstants.ez(equip.getItemId()) && !ItemConstants.類型.心臟(equip.getItemId())) {
                                checked = false;
                                break;
                            }
                            break;
                        }
                        case 10: {
                            if (!ItemConstants.ee(equip.getItemId()) && !ItemConstants.類型.心臟(equip.getItemId())) {
                                checked = false;
                                break;
                            }
                            break;
                        }
                        case 20:
                        case 61: {
                            if ((!ItemConstants.類型.武器(equip.getItemId()) || !ItemConstants.類型.雙手武器(equip.getItemId())) && !ItemConstants.類型.心臟(equip.getItemId())) {
                                checked = false;
                                break;
                            }
                            break;
                        }
                        case 30: {
                            if ((!ItemConstants.類型.武器(equip.getItemId()) || ItemConstants.類型.雙手武器(equip.getItemId())) && !ItemConstants.類型.心臟(equip.getItemId())) {
                                checked = false;
                                break;
                            }
                            break;
                        }
                        case 40: {
                            if (!ItemConstants.類型.武器(equip.getItemId())) {
                                checked = false;
                                break;
                            }
                            break;
                        }
                        case 50: {
                            if (!ItemConstants.eI(equip.getItemId()) && !ItemConstants.類型.心臟(equip.getItemId())) {
                                checked = false;
                                break;
                            }
                            break;
                        }
                        case 60:
                        case 62: {
                            if (((scroll.getItemId() != 2616000 && scroll.getItemId() != 2616001) || ItemConstants.類型.雙手武器(equip.getItemId())) && !ItemConstants.eH(equip.getItemId()) && !ItemConstants.類型.心臟(equip.getItemId())) {
                                checked = false;
                                break;
                            }
                            break;
                        }
                    }
                    break;
                }
                case 264: {
                    switch (t2) {
                        case 0: {
                            if ((!ItemConstants.類型.武器(equip.getItemId()) || ItemConstants.類型.雙手武器(equip.getItemId())) && !ItemConstants.類型.心臟(equip.getItemId())) {
                                checked = false;
                                break;
                            }
                            break;
                        }
                        case 30: {
                            if (!ItemConstants.eI(equip.getItemId()) && !ItemConstants.類型.心臟(equip.getItemId())) {
                                checked = false;
                                break;
                            }
                            break;
                        }
                        case 31: {
                            if (!ItemConstants.ef(equip.getItemId()) && !ItemConstants.類型.心臟(equip.getItemId())) {
                                checked = false;
                                break;
                            }
                            break;
                        }
                    }
                    break;
                }
                default: {
                    log.warn("[Upgrade Check] Unknow scrollType:" + t2);
                    break;
                }
            }
        }
        return checked;
    }

    public static void ReturnEffectConfirm(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        if (c == null || chr == null) {
            return;
        }
        chr.updateTick(slea.readInt());
        boolean result = slea.readBool();
        if (result) {
            Object obj = chr.getTempValues().getOrDefault("ReturnEquip", null);
            if (obj == null) {
                return;
            }
            Equip oldEqp = (Equip) obj;
            Map<Short, Item> iv = chr.getInventory(oldEqp.getPosition() < 0 ? MapleInventoryType.EQUIPPED : ItemConstants.getInventoryType(oldEqp.getItemId())).getInventory();
            iv.put(oldEqp.getPosition(), oldEqp);
            chr.forceReAddItem(oldEqp);
        }
        c.announce(MaplePacketCreator.spouseMessage(UserChatMessageType.系統, "恢復卷軸的效果消失。"));
        c.announce(InventoryPacket.encodeReturnEffectModified(null, 0));
        chr.getTempValues().remove("ReturnEquip");
    }

    public static void userSelectExItemUpgradeItemUseRequest(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        // 00 00 00 00 00 00 00 00 - 請求發送before和after的屬性
        // 01 00 00 00 00 00 00 00 - 選擇了before的屬性
        // 02 00 00 00 00 00 00 00 - 選擇了after的屬性
        // 03 00 00 00 04 00 00 00 - 選擇了再一次
        int type = slea.readInt();
        switch (type){
            case 1: {
                chr.setKeyValue("nirvana_flame_state_old",null);
                chr.setKeyValue("nirvana_flame_state_equip_position",null);
                chr.setKeyValue("nirvana_flame_state_new",null);
                chr.setKeyValue("nirvana_flame_state_itemId",null);
                chr.equipChanged();
                c.announce(againStartExFire());
                break;
            }
            case 2: {
                String nirvana_flame_state_new = chr.getKeyValue("nirvana_flame_state_new");
                String nirvana_flame_state_equip_position = chr.getKeyValue("nirvana_flame_state_equip_position");
                if(!nirvana_flame_state_new.isEmpty()&&!nirvana_flame_state_equip_position.isEmpty()){
                    long flag = Long.parseLong(nirvana_flame_state_new);
                    int position = Integer.parseInt(nirvana_flame_state_equip_position);
                    Equip equip = (Equip) chr.getInventory(MapleInventoryType.EQUIP).getItem((short)position);
                    if(equip!= null){
                        equip.setFlameFlag(flag);
                        chr.forceUpdateItem(equip);
                    }
                    chr.equipChanged();
                }
                chr.setKeyValue("nirvana_flame_state_old",null);
                chr.setKeyValue("nirvana_flame_state_equip_position",null);
                chr.setKeyValue("nirvana_flame_state_new",null);
                chr.setKeyValue("nirvana_flame_state_itemId",null);
                c.announce(againStartExFire());
                break;
            }
            case 3: {
                String nirvana_flame_state_equip_position = chr.getKeyValue("nirvana_flame_state_equip_position");
                String nirvana_flame_state_itemId = chr.getKeyValue("nirvana_flame_state_itemId");
                chr.setKeyValue("nirvana_flame_state_old",null);
                chr.setKeyValue("nirvana_flame_state_equip_position",null);
                chr.setKeyValue("nirvana_flame_state_new",null);
                chr.setKeyValue("nirvana_flame_state_itemId",null);
                c.announce(againStartExFire());
                int slot = slea.readInt();
                int position = Integer.parseInt(nirvana_flame_state_equip_position);
                Equip equip = (Equip) chr.getInventory(MapleInventoryType.EQUIP).getItem((short)position);
                MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();

                UseUpgradeScroll((short) slot, equip.getPosition(), MapleInventoryType.EQUIP, (short) 0, c, chr, 0, false, ii.isCash(Integer.parseInt(nirvana_flame_state_itemId)));
                break;
            }
            default: {
                break;
            }
        }
    }

    public static byte[] againStartExFire(){
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.CharacterPotentialResult.getValue());
        mplew.writeHexString("DB C0 00 00 06 06 00 04 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00");
        return mplew.getPacket();
    }

}
