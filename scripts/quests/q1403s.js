/**
 *
 *
 */

npc.ui(1).uiMax().next().noEsc().sayX("你好，#h0#……我經常聽麥加提起你的名字。聽說你對弓箭手很感興趣。我是弓箭手轉職官赫麗娜。見到你很高興……");
npc.ui(1).uiMax().next().noEsc().sayX("你對弓箭手的瞭解有多少呢？弓箭手是使用弓或弩，在遠距離攻擊敵人的職業……雖然移動速度相對較慢，但銳利的箭矢從來不會射偏，每一發都非常具有威脅。");

let sel = npc.ui(1).noEsc().askAcceptX("如果你真的想成為弓箭手，我就用轉職官的特權，邀請你到#b射手村的弓箭手培訓中心#k來。#r如果你想選擇其他職業，可以拒絕。我會幫助你走上其他道路的#k……你想成為弓箭手嗎？");
if (sel == 1) {
    npc.startQuest();
    player.changeMap(100000201, 0);
} else {
    let njob = npc.askMenuX("你想選擇其他職業啊……雖然不無遺憾，但這是你自己的選擇……那在弓箭手之外，你想選擇哪條道路呢？\r\n#b#L1#戰士#l \r\n#b#L2#魔法師#l \r\n#b#L4#飛俠#l \r\n#b#L5#海盜#l");
    player.startQuest(1406, 0, njob);
    switch (njob) {
        case 1:
            npc.ui(1).uiMax().next().noEsc().sayX("你想選擇戰士嗎？雖然很遺憾，但是沒辦法。武術教練會聯絡你的。你可以留意#b左側的任務提示#k。");
            break;
        case 2:
            npc.ui(1).uiMax().next().noEsc().sayX("魔法師……你想和擁有驚人魔法力量的人成為同伴嗎？漢斯馬上會聯絡你的。你只要看一下#b左側的任務提示#k就行。");
            break;
        case 4:
            npc.ui(1).uiMax().next().noEsc().sayX("你想走飛俠之路嗎？雖然很遺憾，但我尊重你的選擇。#b達克魯#k會聯絡你的。你可以透過#b左側的任務提示#k檢視。");
            break;
        case 5:
            npc.ui(1).uiMax().next().noEsc().sayX("你想走海盜之路嗎？雖然很遺憾，但我尊重你的選擇。#b凱琳#k會聯絡你的。你可以透過#b左側的任務提示#k檢視。");
            break;
    }
}