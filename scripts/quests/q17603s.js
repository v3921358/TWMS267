//
// Quest ID:17603
// [凱梅爾茲共和國] 偉大的(?) 探險家帕爾巴特


npc.ui(1).uiMax().meFlip().next().sayX("請問你是探險家帕爾巴特嗎？");
npc.id(9390200).ui(1).uiMax().npcFlip().next().sayX("你看見了我還不知道嗎？冒險島世界最偉大的探險家帕爾巴特在你面前你都認不出來，最近的年輕人真是……哎……！");
npc.ui(1).uiMax().meFlip().next().sayX("(……似乎是個性格很讓人棘手的人啊。)");
npc.id(9390200).ui(1).uiMax().npcFlip().next().sayX("你是要去凱梅爾茲吧？");
npc.ui(1).uiMax().meFlip().next().sayX("是的……沒錯……");
npc.id(9390200).ui(1).uiMax().npcFlip().next().sayX("你打算怎麼去呢？");
npc.ui(1).uiMax().meFlip().next().sayX("啊……？那個……探險家您不是準備了船隻……");
npc.id(9390200).ui(1).uiMax().npcFlip().next().sayX("當然，我，偉大的帕爾巴特准備了船隻。所以說，你打算怎麼辦呢？");
npc.ui(1).uiMax().meFlip().next().sayX("難道不是您……帶我去嗎……？");
npc.id(9390200).ui(1).uiMax().npcFlip().next().sayX("你這笨蛋，到底有沒有耳朵啊！！！我的意思是，我把你帶到凱梅爾茲，你會給我什麼回報！\r\n因為那個老學究的委託，我準備了去凱梅爾茲的船，#e但是，你不應該詳細地說一下你要做什麼來作為代價嗎？！#n");
npc.ui(1).uiMax().meFlip().next().sayX("(……老學究說的是南哈特嗎……如果我回到聖地，我一定要讓女皇教訓一下南哈特！！)");
npc.ui(1).uiMax().meFlip().next().sayX("這個嘛……抱歉啊，我現在沒準備什麼東西，但是我以後一定會好好地報答你的。賭上女皇的名譽。");
if (npc.askYesNoE("嘖嘖……所以說最近的年輕人都一樣……你也和那個只是嘴上厲害的老學究沒什麼不一樣！算了，你先坐上去吧。我會遵守約定，把你送到凱梅爾茲，你以後可別忘了回報哦！你得知道，我帕爾巴特親自來給你駕船，一定會把你平安送到，這可是你的榮幸！\r\n#b(接受時立刻前往凱梅爾茲。)", 9390200)) {
    // Response is Yes
    npc.completeQuest();
    npc.rememberMap("UNITY_PORTAL");
    player.changeMap(865090003);
} else {
    npc.id(9390200).ui(1).npcFlip().sayX("喂！你到了現在才這樣可不行啊！我好不容易才準備好了船呢！");
}
