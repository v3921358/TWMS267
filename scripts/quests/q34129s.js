/*  
 *  
 *  功能：[消亡旅途]每日任務
 *  
 *  修改 Yanran
 */
let quests = {};
let delQuest = {};
let questinfocount = 0;
let sele;
let questcount = 5;
questcount = player.isQuestCompleted(34164) ? questcount - 1 : questcount; //用於判斷調查團任務，決定最終任務個數
questcount = player.isQuestCompleted(34165) ? questcount - 1 : questcount;
questcount = player.isQuestCompleted(34166) ? questcount - 1 : questcount;
questcount = player.isQuestCompleted(34167) ? questcount - 1 : questcount;

let text = "你來得正好，#ho#。今天我有 " + questcount + " 件事要拜託#ho#你。那立刻開始執行吧？如果不喜歡的話，可以按切換按鈕，切換到其他任務。\r\n\r\n";
initQuest();
for (let i = 0; i < questcount; i++) {
    text += "#b#e#y" + quests[i] + "##k#n\r\n";
}
let selection = npc.askYesNo(text);
if (selection == 1) {
    //替換
    let sel = npc.askYesNo("你對列表中的任務不滿意嗎？那你可以找找其他任務。\r\n\r\n#b(刪除部分或全部任務，重置列表。)#k");
    if (sel == 1) {
        selectDel();
        switch (sele) {
            case 5:
                delDone();
                npc.ui(1).sayX("結束任務後，只要來找我完成就行了。你必須在今天午夜之前來找我。那祝你好運。");
                break;
            default:
                let check = delQuest[sele];
                if (check == null) {
                    delQuest[sele] = sele;
                }
                let size = 0;
                for (let idx in delQuest) {
                    size++;
                }
                if (size >= questcount) {
                    //開始執行任務
                    delDone();
                } else {
                    //selectDel();
                    delDone();
                }
                break;
        }
    }
} else {
    //執行
    execute();
    npc.next().sayX("結束任務後，只要來找我完成就行了。你必須在今天午夜之前來找我。那祝你好運。");
}

function selectDel() {
    var str = "請選擇想要刪除的任務。\r\n\r\n";
    for (var i = 0; i < questcount; i++) {
        if (delQuest[i] != null) {
            str += "#k#e#L" + i + "# #y" + quests[i] + "##l#k#n\r\n";
        } else {
            str += "#b#e#L" + i + "# #y" + quests[i] + "##l#k#n\r\n";
        }
    }
    str += "\r\n#L5# #r#e沒有想要刪除的任務了。#k#n#l";
    sele = npc.askMenuX(str);
}

function delDone() {
    let size = 0;
    for (let idx in delQuest) {
        player.forfeitQuest(quests[idx]);
        quests[idx] = null;
        size++;
    }
    let str = "今天拜託你的事情是這" + questcount + "件。\r\n\r\n";
    if (size > 0) {
        str = "新生成了 " + size + "個新任務，以代替被刪除的" + size + "個任務。今天拜託你的事情是這" + questcount + "件。\r\n\r\n";
    }
    randQuest();
    for (let i = 0; i < questcount; i++) {
        if (delQuest[i] != null) {
            str += "#b#e#y" + quests[i] + "##k #r[NEW]#k#n\r\n";
        } else {
            str += "#b#e#y" + quests[i] + "##k#n\r\n";
        }
    }
    npc.next().sayX(str);
    execute();
}


function execute() {
    for (let i = 0; i < questcount; i++) {
        let questID = Number(player.getQuestRecordEx(34129, String(i)));
        player.startQuest(questID, 0);
    }
    npc.startQuest();
    questinfocount = player.isQuestCompleted(34164) ? questinfocount + 1 : questinfocount;
    questinfocount = player.isQuestCompleted(34165) ? questinfocount + 1 : questinfocount;
    questinfocount = player.isQuestCompleted(34166) ? questinfocount + 1 : questinfocount;
    questinfocount = player.isQuestCompleted(34167) ? questinfocount + 1 : questinfocount;
    player.updateQuestRecordEx(34127, "count", String(questinfocount));
}


function initQuest() {
    //Done
    if (player.getPQLog("LoraDay") < 10) {
        player.addPQLog("LoraDay");
        if (player.getQuestRecordEx(34129) != null) {
            for (let i = 0; i < questcount; i++) {
                let questID = Number(player.getQuestRecordEx(34129, String(i)));
                player.forfeitQuest(questID);
            }
        }
        randQuest();
    } else {
        //載入原來記錄
        for (let i = 0; i < questcount; i++) {
            quests[i] = Number(player.getQuestRecordEx(34129, String(i)));
        }
    }
}


function randQuest() {
    //34130 - 34150  隨機選取5個 Done
    for (let i = 0; i < questcount; i++) {
        let questID = quests[i];
        if (questID == null) { //沒有寫入任務
            let bFlag = false; //判斷是否已經重複
            while (!bFlag) {
                bFlag = true;
                let number = 34130 + Math.floor(Math.random() * 21);
                for (let idx = 0; idx < 5; idx++) {
                    let cc = quests[idx];
                    if (cc == number) {
                        bFlag = false;
                    }
                }
                if (bFlag) {
                    quests[i] = number;
                }
            }
        }
    }
    for (let idx in quests) {
        let questID = quests[idx];
        player.updateQuestRecordEx(34129, String(idx), String(questID));
    }
}