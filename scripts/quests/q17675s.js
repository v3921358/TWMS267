// 
// Quest ID:17675
// [凱梅爾茲共和國] 他的委託


npc.id(9390240).ui(1).uiMax().npcFlip().next().sayX("....那麼我先告辭了。");
player.setNpcViewOrHide(9390237, false, false);
npc.ui(1).uiMax().meFlip().next().sayX("萊文。");
npc.ui(1).uiMax().next().sayX("嗯?啊,你來啦。我聽首領說了。準備好的話..");
npc.ui(1).uiMax().meFlip().next().sayX("怎麼回事。兩人是什麼關係啊。");
npc.ui(1).uiMax().next().sayX("嗯?啊..你是說克萊爾小姐吧?");
npc.ui(1).uiMax().meFlip().next().sayX("怎麼回事...怎麼回事怎麼回事..稱呼從#r特來敏小姐#k變成了#b克萊爾小姐#k了呢。這是怎麼回事。兩人究竟發生了什麼事。");
npc.ui(1).uiMax().next().sayX("啊...哈哈..不就是更親近了一些而已嘛。");
npc.ui(1).uiMax().meFlip().next().sayX("就算是那樣眼神也非比尋常啊。好好把握吧。");
npc.ui(1).uiMax().next().sayX("少來了。我現在還有好多事要做好多東西要學呢。我知道我自己在很多方面都非常不足以至於還不能夠對另一個人的人生負責。");
npc.ui(1).uiMax().meFlip().next().sayX("(結婚...我還沒說到結婚呢..)");
if (npc.askYesNoE("啊,對了,離出航貌似還有一些時間。不忙的話能拜託你件事嗎?")) {
    npc.startQuest();
    npc.ui(1).uiMax().meFlip().next().sayX("這種情況下有事拜託我。說吧什麼事。");
    npc.ui(1).uiMax().next().sayX("呃,呃嗯。其實剛剛我想把這封信給克萊爾小姐的,但是丟了魂了結果沒把信給她。你替我轉交給她吧。可能又馬上要去海本王國了,你得趕緊了。");
    npc.ui(1).uiMax().meFlip().next().sayX("你親手交給她去豈不更好?現在被拖去海本王國的話,就不知道什麼時候能再見了。");
    npc.ui(1).uiMax().next().sayX("我當然也想親手交給她啦。剛剛分手我就已經開始想她了。");
    npc.ui(1).uiMax().meFlip().next().sayX("(呃哦...)");
    npc.ui(1).uiMax().next().sayX("但是現在還不是時候。我要變成更帥的男人再出現在她面前。還有,兩人各在遠方思唸對方也很浪漫不是嗎?哈哈");
    npc.ui(1).uiMax().meFlip().next().sayX("啊啊,好吧,我知道了。我會支援你的,你這傢伙。");
} else {
    npc.ui(1).sayX("現在離出航還早著呢。如果改變心意再來找我。");
}