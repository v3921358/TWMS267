/**
 *
 *
 */

npc.ui(1).uiMax().next().noEsc().sayX("麥加說的那個人就是你？#h0#……嗯，據她說，你是個很有天賦的小孩……喂，你想成為飛俠嗎？你知道飛俠嗎？");
npc.ui(1).uiMax().next().noEsc().sayX("有些人覺得飛俠是些偷偷摸摸的小偷，但其實不是這樣的。冒險島世界的飛俠是在黑暗中用鋒利的短刀和飛鏢戰斗的人。可能有一點不是那麼堂堂正正，但是這就是飛俠的本質，沒有必要否定。");
npc.ui(1).uiMax().next().noEsc().sayX("飛俠是可以用快速強力技能攻擊敵人的職業。雖然你體力較弱，但因為行動快速，所以可以輕鬆地躲避怪物。因為運氣很強，所以擅長爆擊。");

let sel = npc.ui(1).noEsc().askAcceptX("怎麼樣？你想一起踏上飛俠之路嗎？如果你決定選擇飛俠之路，我就使用轉職官的特權，邀請你到#b廢都的廢都酒吧#k去……那是個隱祕的地方，你應該感到榮幸。#r但是如果更喜歡其他職業的話，你可以拒絕。我會為你推薦飛俠之外的其他道路#k");
if (sel == 1) {
    npc.startQuest();
    player.changeMap(103000003, 0);
} else {
    let njob = npc.askMenuX("你不喜歡飛俠之路嗎？不喜歡的話，我也不想勉強。那你想選擇什麼職業呢？\r\n#b#L1#戰士#l \r\n#b#L2#魔法師#l \r\n#b#L3#弓箭手#l \r\n#b#L5#海盜#l");
    player.startQuest(1406, 0, njob);
    switch (njob) {
        case 1:
            npc.ui(1).uiMax().next().noEsc().sayX("你想選擇戰士嗎？雖然很遺憾，但是沒辦法。武術教練會聯絡你的。你可以留意#b左側的任務提示#k。");
            break;
        case 2:
            npc.ui(1).uiMax().next().noEsc().sayX("你想走魔法師之路嗎？雖然很遺憾，但我尊重你的選擇。#b漢斯#k會聯絡你的。你可以透過#b左側的任務提示#k檢視。");
            break;
        case 3:
            npc.ui(1).uiMax().next().noEsc().sayX("你想走弓箭手之路嗎？雖然很遺憾，但我尊重你的選擇。#b赫麗娜#k會聯絡你的。你可以透過#b左側的任務提示#k檢視。");
            break;
        case 5:
            npc.ui(1).uiMax().next().noEsc().sayX("你想走海盜之路嗎？雖然很遺憾，但我尊重你的選擇。#b凱琳#k會聯絡你的。你可以透過#b左側的任務提示#k檢視。");
            break;
    }
}