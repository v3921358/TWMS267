// 
// Quest ID:17658
// [凱梅爾茲共和國] 克萊爾的故事


npc.ui(1).uiMax().next().sayX("我需要謝謝你肯聽我說嗎?你們還記得我是救過你們的恩人嗎?");
npc.ui(1).uiMax().meFlip().next().sayX("關於那點是要謝謝你。但是知道你是班·特來敏的女兒之後不得不與你劃清界線也是事實。");
if (npc.askYesNoE("也對。那也不是不能理解的。那我就說明一下吧。現在要聽嗎?")) {
    npc.startQuest();
    npc.ui(1).uiMax().next().sayX("剛才也說了我是班·特來敏的獨生女。母親呢在生我的時候就去世了。所以父親非常寶貝地養育了我。");
    npc.ui(1).uiMax().meFlip().next().sayX("你生得金貴,為什麼這麼遮頭蓋臉地過日子?");
    npc.ui(1).uiMax().next().sayX("嗯.. 該從哪說起呢..");
} else {
    npc.ui(1).sayX("你們不是想聽我的故事嗎?");
}