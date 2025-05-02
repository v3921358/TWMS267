// 
// Quest ID:17674
// [凱梅爾茲共和國] 我們是友邦


npc.ui(1).uiMax().next().sayX("嗯,都整理好了。不是別的事,我有件事想委託你可以嗎?");
npc.ui(1).uiMax().meFlip().next().sayX("委託我嗎?是和萊文一起做的事情嗎?");
npc.ui(1).uiMax().next().sayX("不是,與萊文無關只是拜託你的事情。因為貌似只有你能完成。你要聽嗎?");
npc.ui(1).uiMax().meFlip().next().sayX("是,雖然不知道是什麼事情,但是隻要是我能力所及的事情我就聽您的。");
if (npc.askYesNoE("果然夠豪放,呵呵。你能把我給你的書信轉交給你們的西格諾斯女皇嗎?")) {
    npc.startQuest();
    npc.ui(1).uiMax().meFlip().next().sayX("啊?給西格諾斯女皇大人嗎?什麼....啊..難道是!!");
    npc.ui(1).uiMax().next().sayX("是的。是我作為凱梅爾茲的首領代表凱梅爾茲給西格諾斯女皇傳遞的和平提議書信。怎麼樣?可以嗎?");
    npc.ui(1).uiMax().meFlip().next().sayX("啊..那當然啦。當然啦!不管幾次都可以!");
    npc.ui(1).uiMax().next().sayX("那麼就拜託你了。去冒險島世界的船已經準備好了。去碼頭,萊文在那裡等你呢。");
    npc.ui(1).uiMax().meFlip().next().sayX("謝謝。首領大人。我這就趕快去向女皇大人轉達這好訊息,以後再來拜見您。");
    npc.ui(1).uiMax().next().sayX("一路順風。");
} else {
    npc.ui(1).sayX("你覺得有負擔嗎?呃嗯..可惜我還以為是你一直在等的訊息呢。");
}