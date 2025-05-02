

if (Math.floor(player.getJob() / 100) == 101 && player.getLevel() < 130 || player.getLevel() < 60) {// isZero
    npc.ui(1).sayX("你好，#b勇士#k~見到你很高興~，你現在的能力還不夠使用<冒險島聯盟>！");
} else {
    if (!player.isQuestCompleted(16013)) {
        player.completeQuest(16013, 0);
        player.actionMapleUnion();
        npc.ui(1).sayX("你好，#b勇士#k~見到你很高興~你現在可以開始使用<冒險島聯盟>啦！");
    } else {
        let sel = npc.askMenuX("今天可是個適合去屠龍的好日子！\r\n關於#b冒險島聯盟#k的工作，有什麽我可以幫忙的嗎？\r\n\r\n\r\n#L0##b<查看我的冒險島聯盟信息。>#l\r\n#L1##b<提升冒險島聯盟級別。>#l\r\n#L2##b<了解有關冒險島聯盟的說明。>#k#l\r\n#L3##b<每周獲得聯盟幣排名>#k#l");
        switch (sel) {
            case 0:
                player.openUI(1148);
                break;
            case 1:
                let nowRank = player.getNowMapleUnionRank();
                let nextRank = player.getNextMapleUnionRank();
                if (nowRank != null && nextRank != null) {
                    let str = getUpdateDesc()
                    str += "\r\n\r\n現在就要對冒險島聯盟進行#e升級#n嗎？";
                    let ret = npc.askYesNo(str);
                    if (ret == 1) {
                        var nRank = player.getMapleUnionNextLevel();
                        if (player.getMapleUnionLevel() >= nRank) {
                            if (player.doMapleUnionLevelUp()) {//還需要判斷時有足夠聯盟幣 
                                npc.ui(1).sayX("恭喜你！，現在的冒險島聯盟等級提高了#k");
                            } else {
                                npc.ui(1).sayX("當前冒險島聯盟已經達到最高等級！");
                            }
                        } else {
                            if (nRank != 0xFFFFFFFF) {
                                npc.ui(1).sayX("升級失敗，請確認是否達到了#n#b<聯盟等級>#e" + nRank + "#n#k");
                            } else {
                                npc.ui(1).sayX("當前冒險島聯盟已經達到最高等級！");
                            }

                        }
                    }
                } else {
                    npc.sayX("你好，#b勇士#k~你的<冒險島聯盟>已經不能在升級了！" + nowRank + " " + nextRank);
                }
                break;
        }
    }
}

function convertToRoman(num) {
    let roman = {
        XL: 40,
        X: 10,
        IX: 9,
        V: 5,
        IV: 4,
        I: 1
    };
    let str = '';
    for (let i of Object.keys(roman)) {
        let q = Math.floor(num / roman[i]);
        num -= q * roman[i];
        str += i.repeat(q);
    }
    return str;
}

function getUpdateDesc() {
    let now = player.getNowMapleUnionRank();
    let next = player.getNextMapleUnionRank();
    return "你想要進行#e冒險島聯盟升級#n嗎？\r\n\r\n#e當前級別：#n#b#e<" +
        now.getName() + " " + convertToRoman(now.getSubRank()) +
        ">#n#k\r\n#e下一個級別：#n#b#e<" +
        next.getName() + " " + convertToRoman(next.getSubRank()) +
        ">#n#k\r\n#e升級時可以投入的攻擊隊隊員增加：#n #b#e<" +
        next.getAttackerCount() +
        "→" +
        next.getAttackerCount() +
        "人>#n#k\r\n\r\n為了升級，需要滿足以下條件。\r\n\r\n#e<聯盟等級> #r#e" +
        next.getLevel() +
        "以上#n#k #n\r\n";
}