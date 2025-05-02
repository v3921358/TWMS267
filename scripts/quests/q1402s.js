/**
 *
 *
 */

npc.ui(1).uiMax().next().noEsc().sayX("你好，#h0#哦，你就是麥加說的那個人啊。你好？聽說你對魔法師之路感興趣。那麼，我魔法師轉職官漢斯可以幫助你。");
npc.ui(1).uiMax().next().noEsc().sayX("相信你應該已經對魔法師有所瞭解了。那是以較高的智力為基礎，使用魔法的職業。雖然遠距離和近距離戰鬥都很出色，但體力稍微有點弱……但是魔法師創造出了很多魔法來克服這一缺點，不要太擔心。");

let sel = npc.ui(1).noEsc().askAcceptX("你看上去好像充分具備成為魔法師的素質……你想成為魔法師嗎？接受的話，我就使用轉職官的特權，邀請你到#b魔法密林的魔法圖書館#k去。和我見個面，然後幫你轉職。#r但是就算拒絕，也不是沒有別的路可走。拒絕的話，我可以為你介紹魔法師以外的職業。#k");
if (sel == 1) {
    npc.startQuest();
    player.changeMap(101000003, 0);
} else {
    let njob = npc.askMenuX("你不喜歡魔法師之路嗎？很遺憾。但是我尊重你的選擇。那你想走哪條道路呢？\r\n#b#L1#戰士#l \r\n#b#L3#弓箭手#l \r\n#b#L4#飛俠#l \r\n#b#L5#海盜#l");
    player.startQuest(1406, 0, njob);
    switch (njob) {
        case 1:
            npc.ui(1).uiMax().next().noEsc().sayX("你想選擇戰士嗎？雖然很遺憾，但是沒辦法。武術教練會聯絡你的。你可以留意#b左側的任務提示#k。");
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