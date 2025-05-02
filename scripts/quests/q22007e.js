/**
 *
 *
 */
npc.next().sayX("哦，雞蛋 拿來了嗎？快把蛋給我吧。我來幫你把它孵化。");
let ret = npc.askYesNo("來，拿著。不知道這到底可以用來幹什麼……");
if (ret == 1) {
    npc.completeQuest();
    player.gainExp(360);
    player.loseItem(4032451, 1);
    player.loseItem(4033081, 1);
    var string = ["UI/tutorial/evan/9/0"];
    npc.sayImage(string);
} else {
    npc.ui(1).sayX("嗯？奇怪。孵化器沒有設定好。重新嘗試一下吧。");
}

