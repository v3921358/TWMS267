//
// Quest ID:17613
// [凱梅爾茲共和國] 統帥的兒子


if (map.getId() == 865090001) {
    npc.ui(1).uiMax().next().noEsc().sayX("額…這些強盜一樣的貓...");
    npc.ui(1).uiMax().next().noEsc().sayX("喂。你還好吧?");
    player.setStandAloneMode(false);
    player.changeMap(865010200, 1);
} else {
    npc.id(9390241).ui(1).uiMax().npcFlip().next().sayX("你是誰?");
    npc.ui(1).uiMax().meFlip().next().sayX("#b(第一次見面語氣就這麼隨意...)#k\r\n我叫做#h #。你是萊文·達尼爾拉吧?你沒受傷吧?");
    npc.id(9390241).ui(1).uiMax().npcFlip().next().sayX("就那些強盜貓而已,我自己就能對付他們。 總之,謝謝你。你的身手還不錯嘛!但你是怎麼知道我的?我們見過嗎?");
    npc.ui(1).uiMax().meFlip().next().sayX("啊，那是不可能的。我是從很遠的地方來的……而且還是在不久前剛來的……");
    npc.id(9390241).ui(1).uiMax().npcFlip().next().sayX("但是，你知道我的名字啊？");
    npc.ui(1).uiMax().meFlip().next().sayX("是的，我其實是來見你的。我們是第一次見面，你的名字我是從村長那裡聽說的。而且，在那個強盜貓和你戰斗的時候，我聽見了你的名字，所以我知道我要找的人就是你。");
    npc.id(9390241).ui(1).uiMax().npcFlip().next().sayX("是嗎？你要找我，那你就是貿易商咯？\r\n既然你是遠道而來，那看來你對這附近並不熟悉啊。作為剛才你幫助我的回報，我告訴你個簡單的情報吧。這裡是個偏僻的小漁村，所以不適合作為遠距離交易的據點。如果你想進行大筆的交易，你還是去#b桑凱梅爾茲#k吧。");
    npc.ui(1).uiMax().meFlip().next().sayX("不不，我不是貿易商。當然，我確實是打算去桑凱梅爾茲。");
    npc.id(9390241).ui(1).uiMax().npcFlip().next().sayX("你遠道而來，卻又不是貿易商，而且還來見我……那你為什麼要到這麼偏僻的小村子來找我呢？");
    npc.ui(1).uiMax().meFlip().next().sayX("我不是特地到這個偏僻的村子來找你，#e而是來到了這個偏僻的村子，你正好在這裡#n。我，嗯……我是轉達和凱梅爾茲友好合作的資訊而來，你應該能理解吧？我聽說正好你和達尼爾拉商團在這裡停留，所以才回來見你。");
    npc.id(9390241).ui(1).uiMax().npcFlip().next().sayX("友好的資訊？你這傢伙是從#e#b海本#k#n來的嗎？你們又有什麼事？");
    npc.ui(1).uiMax().meFlip().next().sayX("(……啊！我是不是太快揭開身份了？而且，#e#b海本#k#n？雖然不知道他在說哪裡，不過從這麼敵對的態度來看，凱梅爾茲和海本的關係應該不好吧……我一定不能讓他們產生誤會。)");
    npc.ui(1).uiMax().meFlip().next().sayX("海本？我是第一次聽說這個名字啊？我是作為冒險島世界西格諾斯女皇的和平使者來的。我不知道你所說的海本在哪裡，但是那裡和我們沒關係。");
    npc.id(9390241).ui(1).uiMax().npcFlip().next().sayX("嗯……是嗎？你不是來自海本，卻跑到凱梅爾茲來做和平使者……算了，只要不是海本的人就行。\r\n但是,你!你怎麼對我說話這麼隨便?");
    npc.ui(1).uiMax().meFlip().next().sayX("是你先開始用這樣的語氣說話的吧?");
    npc.id(9390241).ui(1).uiMax().npcFlip().next().sayX("啊... 是嗎?你啊。雖然有點傲慢,但我喜歡。你叫什麼？");
    npc.ui(1).uiMax().meFlip().next().sayX("你叫我#h0#就行。");
    npc.id(9390241).ui(1).uiMax().npcFlip().next().sayX("#h0#？原來如此。我們做朋友吧?可不是什麼人都能和我萊文·達尼爾拉做朋友的!但是,我們做了朋友,就一輩子不能變。信任是商人的原則!");
    npc.ui(1).uiMax().meFlip().next().sayX("#b(哈!雖然他有點奇怪,但不是壞人。我喜歡)#k\r\n好的。我還蠻喜歡你的。我們做朋友吧。");
    npc.completeQuest();
    player.gainExp(630724);
}