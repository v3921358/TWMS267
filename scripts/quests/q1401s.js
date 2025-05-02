/**
 *
 *
 */

npc.ui(1).uiMax().next().noEsc().sayX("你就是麥加推薦的那個人啊。聽說你想轉職成戰士……對嗎？我就是戰士轉職官武術教練。我可以為想要以戰士身份冒險的人提供幫助。");
npc.ui(1).uiMax().next().noEsc().sayX("你對戰士瞭解多少呢？戰士是以強大的力量和體力為基礎，揮舞近戰武器打倒敵人的職業。在最接近敵人的地方戰斗的無畏的人。是不是很有魅力？");

let sel = npc.ui(1).noEsc().askAcceptX("你好像充分擁有了資格。像你這樣的人想成為戰士，我隨時表示歡迎。你想成為戰士嗎？接受的話，我就使用轉職官的特權，邀請你到#b勇士部落的戰士聖殿#k去。#r但是就算拒絕，也不是沒有別的路可走。拒絕的話，我可以引導你走其他職業的道路#k。");
if (sel == 1) {
    npc.startQuest();
    player.changeMap(102000003, 0);
} else {
    let njob = npc.askMenuX("你不想走戰士之路嗎？不願意的話，我就不能勉強。那你就去選擇其他道路吧。除了戰士之外，還有四條道路可供選擇。\r\n#b#L2#魔法師#l \r\n#b#L3#弓箭手#l \r\n#b#L4#飛俠#l \r\n#b#L5#海盜#l");
    player.startQuest(1406, 0, njob);
    switch (njob) {
        case 2:
            npc.ui(1).uiMax().next().noEsc().sayX("你想走魔法師之路嗎？雖然很遺憾，但我尊重你的選擇。#b漢斯#k會聯絡你的。你可以透過#b左側的任務提示#k檢視。");
            break;
        case 3:
            npc.ui(1).uiMax().next().noEsc().sayX("你想走弓箭手之路嗎？雖然很遺憾，但我尊重你的選擇。#b赫麗娜#k會聯絡你的。你可以透過#b左側的任務提示#k檢視。");
            break;
        case 4:
            npc.ui(1).uiMax().next().noEsc().sayX("你想走飛俠之路嗎？雖然很遺憾，但我尊重你的選擇。#b達克魯#k會聯絡你的。你可以透過#b左側的任務提示#k檢視。");
            break;
        case 5:
            npc.ui(1).uiMax().next().noEsc().sayX("你想走海盜之路嗎？雖然很遺憾，但我尊重你的選擇。#b凱琳#k會聯絡你的。你可以透過#b左側的任務提示#k檢視。");
            break;
    }
}