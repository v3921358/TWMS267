import { pic } from "scripts/expands/pic.js";
import { color } from "scripts/expands/color.js";

export default {
    getPayUsed() {
       return player.getHyPay(2);
    },

    // 發送禮物功能
    addReward(...args) {
        return _addReward(...args);
    },
    receiveReward(id) {
        return _receiveReward(id);
    },
    getMyRewardsId() {
        return _getMyRewardsId();
    },
    getRewardInfo(id) {
        return _getRewardInfo(id);
    },
    getMyReceivedRewardsId() {
        return _getMyReceivedRewardsId();
    },

    // 裝備相關功能
    gainItemProp(itemId, attributes, quantity = 1) {
        _gainItemProp(itemId, attributes, quantity);
    },

    // 商店功能
    startShopNpc(shopPrompts, shopContents) {
        _startShopNpc(shopPrompts, shopContents);
    },

    // 其他工具函數
    left(str, len, c = ' ') {
        return _left(str, len, c);
    },
    right(str, len, c = ' ') {
        return _right(str, len, c);
    },
    pad(str, len, c = ' ') {
        return _pad(str, len, c);
    },
    dateDiff(sDate1, sDate2) {
        return _dateDiff(sDate1, sDate2);
    },
    formatNumber(num) {
        return _formatNumber(num);
    },
    show(transaction, showConfirmation = true) {
        return _show(transaction, showConfirmation);
    },
    buy(transaction) {
        return _buy(transaction);
    },
};

function safeInteger(value) {
    return Math.max(0, Math.round(value || 0));
}

// 工具函數實現
function _left(str, len, c = ' ') {
    str = String(str);
    if (str.length >= len) return str;
    return c.repeat(len - str.length) + str;
}

function _right(str, len, c = ' ') {
    str = String(str);
    if (str.length >= len) return str;
    return str + c.repeat(len - str.length);
}

function _pad(str, len, c = ' ') {
    str = String(str);
    if (str.length >= len) return str;
    let totalPadding = len - str.length;
    let leftPadding = Math.ceil(totalPadding / 2);
    let rightPadding = Math.floor(totalPadding / 2);
    return c.repeat(leftPadding) + str + c.repeat(rightPadding);
}

function _dateDiff(sDate1, sDate2) { // sDate1 和 sDate2 是 2016-06-18 格式
    let oDate1 = new Date(sDate1);
    let oDate2 = new Date(sDate2);
    let diffTime = Math.abs(oDate2 - oDate1);
    let diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return diffDays;
}

function _formatNumber(num) {
    return num.toLocaleString();
}

function _multiplyBuyNum(transaction, multiplier) {
    let transactionCopy = JSON.parse(JSON.stringify(transaction));
    transactionCopy.獲得數量 = (transaction.獲得數量 || 1) * multiplier;
    transactionCopy.花費楓幣 = transaction.花費楓幣 ? transaction.花費楓幣 * multiplier : null;
    transactionCopy.花費樂豆 = transaction.花費樂豆 ? transaction.花費樂豆 * multiplier : null;
    transactionCopy.花費楓點 = transaction.花費楓點 ? transaction.花費楓點 * multiplier : null;
    transactionCopy.花費餘額 = transaction.花費餘額 ? transaction.花費餘額 * multiplier : null;

    if (transaction.花費道具陣列 !== null && Array.isArray(transaction.花費道具陣列)) {
        transactionCopy.花費道具陣列 = transaction.花費道具陣列.map(([itemId, qty]) => [itemId, qty * multiplier]);
    }
    return transactionCopy;
}

function _show(transaction, showConfirmation = true) {
    transaction.獲得道具 = transaction.獲得道具 || null;
    transaction.獲得數量 = transaction.獲得數量 || 1;
    transaction.花費楓幣 = transaction.花費楓幣 || null;
    transaction.花費樂豆 = transaction.花費樂豆 || null;
    transaction.花費楓點 = transaction.花費楓點 || null;
    // transaction.花費餘額 = transaction.花費餘額 || null;
    transaction.花費道具陣列 = transaction.花費道具陣列 || null;
    transaction.素質 = transaction.素質 || null;
    transaction.帳限 = transaction.帳限 || null;
    transaction.角限 = transaction.角限 || null;
    transaction.帳限標籤 = transaction.帳限標籤 || '預設標籤';
    transaction.角限標籤 = transaction.角限標籤 || '預設標籤';

    let str = "";
    if (transaction.獲得道具 != null) {
        str += pic.楷體 + "#b獲得道具　　　  : #i" + transaction.獲得道具 + ":##z" + transaction.獲得道具 + "#\r\n";
        if (transaction.素質 != null) {
            for (let prop in transaction.素質) {
                str += "#k     " + prop + " : " + transaction.素質[prop] + "\r\n";
            }
        }
        str += pic.楷體 + "#b獲得道具數量　  : " + transaction.獲得數量 + "\r\n";
    }

    if (transaction.花費楓幣 !== null) {
        if (transaction.花費楓幣 > 0) {
            str += "#r花費楓幣　　　  : " + _formatNumber(transaction.花費楓幣) + "\r\n";
        } else if (transaction.花費楓幣 < 0) {
            str += "#b獲得楓幣　　　  : " + _formatNumber(-transaction.花費楓幣) + "\r\n";
        }
    }

    if (transaction.花費樂豆 !== null) {
        if (transaction.花費樂豆 > 0) {
            str += "#r花費樂豆　　　  : " + _formatNumber(transaction.花費樂豆) + "\r\n";
        } else if (transaction.花費樂豆 < 0) {
            str += "#b獲得樂豆　　　  : " + _formatNumber(-transaction.花費樂豆) + "\r\n";
        }
    }

    if (transaction.花費楓點 !== null) {
        if (transaction.花費楓點 > 0) {
            str += "#r花費楓點        : " + _formatNumber(transaction.花費楓點) + "\r\n";
        } else if (transaction.花費楓點 < 0) {
            str += "#b獲得楓點        : " + _formatNumber(-transaction.花費楓點) + "\r\n";
        }
    }

    /*
    if (transaction.花費餘額 !== null) {
        if (transaction.花費餘額 > 0) {
            str += "#r花費餘額        : " + _formatNumber(transaction.花費餘額) + "\r\n";
        } else if (transaction.花費餘額 < 0) {
            str += "#b獲得餘額         : " + _formatNumber(-transaction.花費餘額) + "\r\n";
        }
    }
    */

    if (transaction.花費道具陣列 !== null && Array.isArray(transaction.花費道具陣列)) {
        transaction.花費道具陣列.forEach(([itemId, qty]) => {
            str += "#fs16##r花費道具  : #i" + itemId + ":##z" + itemId + "# " + qty + "個\r\n";
        });
    }

    if (transaction.帳限 !== null) {
        let 帳限幾天 = transaction.帳限;
        let 帳限標籤 = transaction.帳限標籤;
        let 帳限已買次數 = player.getEventValue(帳限標籤);
        // if (帳限標籤 == '預設標籤') 帳限已買次數 = 99999; // 這行似乎被注釋掉了，若需要可取消注釋
        str += "#fc0xFF00BB00#帳號限定        : " + 帳限幾天 + " 天購買 (" + 帳限已買次數 + "/" + 1 + ") 次" + (player.isGm() ? "(GM不限)" : "") + "\r\n";
        str += "#fc0xFF00BB00#帳號限定共用標籤: " + 帳限標籤 + "\r\n";
    }

    if (transaction.角限 !== null) {
        let 角限幾天 = transaction.角限;
        let 角限標籤 = transaction.角限標籤;
        let 角限已買次數 = player.getPQLog(角限標籤);
        // if (角限標籤 == '預設標籤') 角限已買次數 = 99999; // 這行似乎被注釋掉了，若需要可取消注釋
        str += "#fc0xFF00BB00#角色限定        : " + 角限幾天 + " 天購買 (" + 角限已買次數 + "/" + 1 + ") 次" + (player.isGm() ? "(GM不限)" : "") + "\r\n";
        str += "#fc0xFF00BB00#角色限定共用標籤: " + 角限標籤 + "\r\n";
    }

    if (showConfirmation) {
        str += "                                      #d#e是否確定購買？#k#n";
    }

    return str;
}

function _buy(transaction) {
    transaction.獲得道具 = transaction.獲得道具 || null;
    transaction.獲得數量 = transaction.獲得數量 || 1;
    transaction.花費楓幣 = transaction.花費楓幣 || null;
    transaction.花費樂豆 = transaction.花費樂豆 || null;
    transaction.花費楓點 = transaction.花費楓點 || null;
    // transaction.花費餘額 = transaction.花費餘額 || null;
    transaction.花費道具陣列 = transaction.花費道具陣列 || null;
    transaction.素質 = transaction.素質 || null;
    transaction.帳限 = transaction.帳限 || null;
    transaction.角限 = transaction.角限 || null;
    transaction.帳限標籤 = transaction.帳限標籤 || '預設標籤';
    transaction.角限標籤 = transaction.角限標籤 || '預設標籤';
    transaction.可選倍數 = transaction.可選倍數 || null;

    let str = "";
    if (transaction.花費楓點 !== null && player.getPoint() < transaction.花費楓點) {
        str += "#r楓點不足       : 少 #e" + _formatNumber(transaction.花費楓點 - player.getPoint()) + "#n點#k\r\n";
    }
    if (transaction.花費樂豆 !== null && player.getCash() < transaction.花費樂豆) {
        str += "#r樂豆不足       : 少 #e" + _formatNumber(transaction.花費樂豆 - player.getCash()) + "#n點#k\r\n";
    }
    if (transaction.花費楓幣 !== null && player.getMeso() < transaction.花費楓幣) {
        str += "#r楓幣不足       : 少 #e" + _formatNumber(transaction.花費楓幣 - player.getMeso()) + "#n點#k\r\n";
    }
    /*
    if (cm.getHyPay(1) < transaction.花費餘額 && transaction.花費餘額 !== null){
        str += "#r餘額不足       : 少 #e" + _formatNumber(transaction.花費餘額 - cm.getHyPay(1)) + "#n元#k\r\n";
    }
    */
    if (transaction.花費道具陣列 !== null && Array.isArray(transaction.花費道具陣列)) {
        transaction.花費道具陣列.forEach(([itemId, qty]) => {
            if (!player.hasItem(itemId, qty)) {
                str += "#r道具不足       : #i" + itemId + ":#少 #e" + (qty - player.getAmountOfItem(itemId)) + "#n個\r\n";
            }
        });
    }
    if (transaction.獲得道具 != null && !player.canGainItem(transaction.獲得道具, transaction.獲得數量)) {
        str += "#r空間不足       : 無法獲得 #i" + transaction.獲得道具 + "# #e" + transaction.獲得數量 + "#n個\r\n";
    }
    if (transaction.獲得數量 > 32767) {
        str += "#r數量過大       : 獲得數量不可超過32767\r\n";
    }

    if (transaction.帳限 !== null) {
        let 帳限已買次數 = player.getEventValue(transaction.帳限標籤);
        if (帳限已買次數 >= transaction.帳限 && !player.isGm()) {
            str += "#r帳號限定       : " + transaction.帳限 + " 天購買 (" + 帳限已買次數 + "/" + 1 + ") 次\r\n";
        }
    }

    if (transaction.角限 !== null) {
        let 角限已買次數 = player.getPQLog(transaction.角限標籤);
        if (角限已買次數 >= transaction.角限 && !player.isGm()) {
            str += "#r角色限定       : " + transaction.角限 + " 天購買 (" + 角限已買次數 + "/" + 1 + ") 次\r\n";
        }
    }

    if (str !== "") {
        // 確認失敗，顯示缺少物品
        npc.say(str);
        return false;
    } else {
        // 確認成功，開始購買
        // 扣除楓點
        if (transaction.花費楓點 && transaction.花費楓點 > 0) {
            player.modifyCashShopCurrency(2, -transaction.花費楓點);
            player.dropMessage(-1, "您本次操作花費 " + transaction.花費楓點 + " 楓葉點數.");
        }
        // 扣除樂豆
        if (transaction.花費樂豆 && transaction.花費樂豆 > 0) {
            player.modifyCashShopCurrency(1, -transaction.花費樂豆);
            player.dropMessage(-1, "您本次操作花費 " + transaction.花費樂豆 + " 樂豆點數.");
        }
        // 扣除楓幣
        if (transaction.花費楓幣 && transaction.花費楓幣 > 0) {
            player.loseMesos(transaction.花費楓幣);
            player.dropMessage(-1, "您本次操作花費 " + transaction.花費楓幣 + " 楓幣.");
        }
        /*
        // 扣除餘額
        if (transaction.花費餘額 && transaction.花費餘額 > 0) {
            cm.addHyPay(-transaction.花費餘額);
        }
        */
        // 扣除道具
        if (transaction.花費道具陣列 !== null && Array.isArray(transaction.花費道具陣列)) {
            transaction.花費道具陣列.forEach(([itemId, qty]) => {
                player.loseItem(itemId, qty);
            });
        }
        // 紀錄帳限
        if (transaction.帳限 !== null) {
            player.addEventValue(transaction.帳限標籤, 1, transaction.帳限);
        }
        // 紀錄角限
        if (transaction.角限 !== null) {
            player.addPQLog(transaction.角限標籤, 1, transaction.角限);
        }
        // 給予裝備或道具
        // 給予裝備或道具
        if (transaction.獲得道具 != null) {
            if (transaction.素質 != null) {
                _gainItemProp(transaction.獲得道具, transaction.素質, transaction.獲得數量);
            } else {
                player.gainItem(transaction.獲得道具, transaction.獲得數量);
            }
        }
        return true;
    }
}

// 裝備相關函數
function _gainItemProp(itemId, attributes, quantity) {
    for (let i = 0; i < quantity; i++) {
        let item = sh.itemEquip(itemId).copy(); // 生成一個Eq
        if (attributes["全屬"]) {
            item.setStr(attributes["全屬"]);
            item.setDex(attributes["全屬"]);
            item.setInt(attributes["全屬"]);
            item.setLuk(attributes["全屬"]);
        } else {
            item.setStr(attributes["力"] || item.getStr());
            item.setDex(attributes["敏"] || item.getDex());
            item.setInt(attributes["智"] || item.getInt());
            item.setLuk(attributes["幸"] || item.getLuk());
        }
        ///----------------------------------------------
        item.setPad(attributes["物攻"] || item.getPad());
        item.setMad(attributes["魔攻"] || item.getMad());
        item.setPdd(attributes["防"] || item.getPdd());
        item.setBossDamage(attributes["王傷"] || item.getBossDamage());
        item.setIgnorePDR(attributes["無視"] || item.getIgnorePDR());
        item.setJump(attributes["跳"] || item.getJump());
        item.setSpeed(attributes["移速"] || item.getSpeed());
        item.setTotalDamage(attributes["總傷"] || item.getTotalDamage());
        item.setEnhance(attributes["星力"] || item.getEnhance());
        item.setOwner(attributes["刻名"] || item.getOwner());
        item.setCurrentUpgradeCount(attributes["已充次數"] || item.getCurrentUpgradeCount());
        item.setRestUpgradeCount(attributes["卷軸"] || item.getRestUpgradeCount());
        item.setViciousHammer(attributes["黃槌"] || item.getViciousHammer());
        item.setPlatinumHammer(attributes["白槌"] || item.getPlatinumHammer());
        // 設置其他屬性，如閃避、命中等，如果需要的話，可以根據需求取消注釋並設置
        /*
        item.setAvoid(attributes["閃避"] || item.getAvoid());
        item.setIncACC(attributes["命中"] || item.getAcc());
        item.setItemEXP(attributes["經驗"] || item.getItemEXP());
        item.setFinalStrike(attributes["最終一擊卷"] || item.getFinalStrike());
        item.setYggdrasilWisdom(attributes["樹的祝福"] || item.getYggdrasilWisdom());
        item.setKarmaCount(attributes["KarmaCount"] || item.getKarmaCount());
        item.setFailCount(attributes["FailCount"] || item.getFailCount());
        item.setMvpEquip(attributes["MVP"] || item.isMvpEquip());
        */

        // 設置期限
        if (attributes["期限天數"]) {
            item.setExpiration(new Date().getTime() + attributes["期限天數"] * 24 * 60 * 60 * 1000);
        } else if (attributes["期限分鐘"]) {
            item.setExpiration(new Date().getTime() + attributes["期限分鐘"] * 60 * 1000);
        }

        // 封印狀態
        if (attributes["attribute"]) {
            item.setAttribute(attributes["attribute"]);
        }
        player.gainItem(item);
    }
}

// 發送禮物相關函數
function _addReward(...args) {
    let accountId, itemId, quantity, dateStart, dateEnd, msg;

    if (args.length === 2) {
        // addReward(itemId, msg)
        [itemId, msg] = args;
        accountId = player.getAccountId();
        quantity = 1;
        dateStart = new Date().addHours(8).toISOString().split('T')[0];
        dateEnd = new Date().addDays(1000).addHours(8).toISOString().split('T')[0];
    } else if (args.length === 3) {
        // addReward(accountId, itemId, msg)
        [accountId, itemId, msg] = args;
        quantity = 1;
        dateStart = new Date().addHours(8).toISOString().split('T')[0];
        dateEnd = new Date().addDays(1000).addHours(8).toISOString().split('T')[0];
    } else if (args.length === 4) {
        // addReward(accountId, itemId, quantity, msg)
        [accountId, itemId, quantity, msg] = args;
        dateStart = new Date().addHours(8).toISOString().split('T')[0];
        dateEnd = new Date().addDays(1000).addHours(8).toISOString().split('T')[0];
    } else if (args.length === 5) {
        // addReward(accountId, itemId, quantity, days, msg)
        [accountId, itemId, quantity, days, msg] = args;
        dateStart = new Date().addHours(8).toISOString().split('T')[0];
        dateEnd = new Date().addDays(days).addHours(8).toISOString().split('T')[0];
    } else if (args.length === 6) {
        // addReward(accountId, itemId, quantity, startDate, endDate, msg)
        [accountId, itemId, quantity, dateStart, dateEnd, msg] = args;
    } else {
        return false;
    }

    let sql = "INSERT INTO rewards_custom (account_id, item_id, quantity, start_date, end_date, message) VALUES (?, ?, ?, ?, ?, ?)";
    let result = player.customSqlInsert(sql, accountId, itemId, quantity, dateStart, dateEnd, msg);
    return result >= 1;
}

function _receiveReward(id) {
    let sql = `
        SELECT item_id, quantity
        FROM rewards_custom
        WHERE id='${id}'
          AND receive=0
          AND DATE(NOW()) BETWEEN start_date AND end_date
    `;
    let result = player.customSqlResult(sql);
    if (result.size() === 0 || result.get(0) == null) {
        return false;
    }
    let item_id = result.get(0).get("item_id");
    let quantity = result.get(0).get("quantity");
    if (!player.canGainItem(item_id, quantity)) {
        return false;
    }
    if (player.gainItem(item_id, quantity)) {
        let updateSql = `UPDATE rewards_custom SET receive=1 WHERE id=?`;
        player.customSqlUpdate(updateSql, id);
        return true;
    }
    return false;
}

function _getMyRewardsId() {
    let sql = `
        SELECT id
        FROM rewards_custom
        WHERE account_id='${player.getAccountId()}'
          AND receive=0
          AND DATE(NOW()) BETWEEN start_date AND end_date
    `;
    let result = player.customSqlResult(sql);
    let resultArray = [];
    if (result.size() === 0) {
        return [];
    }
    result.forEach(function (value) {
        resultArray.push(value.get("id"));
    });
    return resultArray;
}

function _getRewardInfo(id) {
    let sql = `
        SELECT item_id, quantity, start_date, end_date, message
        FROM rewards_custom
        WHERE id='${id}'
    `;
    let result = player.customSqlResult(sql);
    if (result.size() === 0) {
        return null;
    }
    return {
        道具ID: result.get(0).get("item_id"),
        數量: result.get(0).get("quantity"),
        開始日期: result.get(0).get("start_date"),
        結束日期: result.get(0).get("end_date"),
        訊息: result.get(0).get("message"),
    };
}

function _getMyReceivedRewardsId() {
    let sql = `
        SELECT id
        FROM rewards_custom
        WHERE account_id='${player.getAccountId()}'
          AND receive=1
    `;
    let result = player.customSqlResult(sql);
    let resultArray = [];
    if (result.size() === 0) {
        return [];
    }
    result.forEach(function (value) {
        resultArray.push(value.get("id"));
    });
    return resultArray;
}

// 商店相關函數
function _startShopNpc(shopPrompts, shopContents) {
    const Npc開場 = shopPrompts.Npc開場;
    const Npc商店內容開場 = shopPrompts.Npc商店內容開場;
    const 交易成功提示 = shopPrompts.交易成功提示 || "兌換成功";

    let str = Npc開場 + "\r\n";
    shopContents.forEach(function (value, index) {
        if (value.是否開放 || player.isGm()) {
            str += `#L${index}#${value.商店名稱}\r\n`;
        }
    });

    let sel_menu = npc.askMenu(str);
    str = "";
    str += pic.黃右箭頭+shopContents[sel_menu].商店名稱 + " 商店\r\n";
    str += Npc商店內容開場 + "\r\n";

    shopContents[sel_menu].商品清單.forEach(function (商品, index) {
        let 花費楓幣 = 商品.花費楓幣 || 0;
        let 花費楓點 = 商品.花費楓點 || 0;
        let 花費樂豆 = 商品.花費樂豆 || 0;
        let 顯示 = 商品.顯示 || "";
        let GM = 商品.GM || false;
        str += `#L${index}##i${商品.獲得道具}:# #b#z${商品.獲得道具}##k#l\r\n`;
        if (花費楓幣)
            str += "#fs11#          └#r花費楓幣: $" + _left(_formatNumber(花費楓幣), 12) + "#k\r\n";
        if (花費楓點)
            str += "#fs11#          └#r花費楓點: $" + _left(_formatNumber(花費楓點), 12) + "#k\r\n";
        if (花費樂豆)
            str += "#fs11#          └#r花費樂豆: $" + _left(_formatNumber(花費樂豆), 12) + "#k\r\n";
        if (顯示 !== "")
            str += "#fs11#          └" + 顯示 + "\r\n";
        if (GM)
            str += "#fs11#          └#r(GM限定)#k\r\n";
    });

    let sel_item = npc.askMenu(str);

    let GM = shopContents[sel_menu].商品清單[sel_item].GM || false;
    if (GM && !player.isGm()) {
        npc.say("尚未開放");
        return;
    }

    str = "";
    str += pic.楷體+"請輸入購買或兌換數量：\r\n\r\n";
    str += _show(shopContents[sel_menu].商品清單[sel_item], false);

    let buyCount = npc.askNumber(str, 1, 1, 30000);
    let checkBuy = npc.askYesNo(_show(_multiplyBuyNum(shopContents[sel_menu].商品清單[sel_item], buyCount)));
    if (checkBuy) {
        if (_buy(_multiplyBuyNum(shopContents[sel_menu].商品清單[sel_item], buyCount))) {
            player.dropAlertNotice(交易成功提示);
        }
    }
}

// 日期擴展方法
Date.prototype.addDays = function (days) {
    this.setDate(this.getDate() + days);
    return this;
};
Date.prototype.addHours = function (hours) {
    this.setHours(this.getHours() + hours);
    return this;
};
