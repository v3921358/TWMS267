// 
// Quest ID:17676
// [凱梅爾茲共和國] 她的心意


npc.ui(1).uiMax().meFlip().next().sayX("克萊爾何時離開呢。");
npc.ui(1).uiMax().next().sayX("這會兒馬上父親準備好就要離開了。");
npc.ui(1).uiMax().meFlip().next().sayX("看來班·特來敏大人要一起去了?");
npc.ui(1).uiMax().next().sayX("是的。他好像擔心說不定又中途在哪消失了似的。");
npc.ui(1).uiMax().meFlip().next().sayX("原來如此。那也別放棄自己的夢想。成為大魔法師的夢想。");
npc.ui(1).uiMax().next().sayX("謝謝你。有空的時候我自己會繼續籌集資本...不是,是學習。");
npc.ui(1).uiMax().meFlip().next().sayX("(這姑娘,如此的話又該離家出走了。)");
npc.ui(1).uiMax().meFlip().next().sayX("你沒什麼話要我轉告萊文嗎?現在離開的話想見面可就難了。");
npc.ui(1).uiMax().next().sayX("嗯?應該是那樣吧。我也沒什麼要說的呢。");
npc.ui(1).uiMax().meFlip().next().sayX(".....???你會想他都沒關係嗎?");
npc.ui(1).uiMax().next().sayX("啊?為什麼?啊,當然也不會完全不想他啦。");
npc.ui(1).uiMax().meFlip().next().sayX("原,原來如此.(萊,萊文你這傢伙..呵呵)");
if (npc.askYesNoE("啊,那麼能請你把這個轉交給他嗎?")) {
    npc.startQuest();
    npc.ui(1).uiMax().meFlip().next().sayX("知道了。我會轉交給他的。");
    npc.ui(1).uiMax().next().sayX("我嘗試著做的。那就拜託你了。");
} else {
    npc.ui(1).sayX("如果你很忙的話也沒辦法..");

}