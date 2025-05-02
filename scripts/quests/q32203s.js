/**
 * Roger's Apple (NPC 2000 Quest 1021) Start
 * Maple Road: Lower level of the Training Camp (Map 2)
 *
 * Gives advice to new players on how to use items.
 *
 *
 */

startScript();

function startScript() {
    npc.next().sayX("你好，我的名字叫麥加。我從來沒有見過你哦，看來你是新來的#b冒險家#k吧？");
    npc.ui(1).uiMax().npcFlip().next().sayX("#b冒險家？#k");
    npc.next().sayX("是的，為了在冒險島世界展開冒險而從其他世界來的人。我們稱這種人為“冒險家”。所有冒險家都是從#b冒險島#k開始冒險的。");
    npc.ui(1).uiMax().npcFlip().next().sayX("#b冒險島？#k");
    npc.next().sayX("嗯，冒險島！這裡本來只是個無名小島，但不知從何時起有很多冒險家都紛至沓來。為了他們的到來，這裡開始陸陸續續有設施搭建起來，現在這裡已經變成了一個不錯的村莊。並且由我來為像你一樣的新手冒險家提供幫助。");
    npc.next().sayX("你是叫……#h0#吧？既然你是第一次來到冒險島世界，那就多聽一下我做的說明吧？透過我的小測試的話，我就會給你對冒險非常有用的禮物哦！");
    npc.next().sayX("如果你不想聽我的說明，我馬上送你去村莊。不過那樣一來，你就無法獲得禮物。");
    let sel = npc.askMenuX("明白了的話，現在開始選擇吧。.你要怎麼做呢？\r\n#b#L0# 聽取麥加的說明，並獲得新裝備作為禮物。 #l\r\n#L1# 不聽說明，立即移動至村莊。#l#k");
    if (sel == 0) {
        npc.next().sayX("不錯的選擇！如果你按照我的說明去做，以後在冒險島世界生活不會有任何問題的。");
        npc.completeQuest();
        player.gainExp(20);
    } else if (sel == 1) {
        npc.next().sayX("好吧，那麼我現在立刻送你去彩虹村。");
        npc.next().sayX("我已經把禮物放到你揹包裡，是恢複用藥水。你待會兒開啟消耗欄確認一下吧。");
        npc.next().sayX("你到了彩虹村的話，別忘了去見見#b路卡斯#k村長!他會給你一些建議，讓你能剛好地去適應新世界。");
        player.changeMap(4000020, 0);
        player.gainExp(273);
        npc.completeQuest();
        player.startQuest(32210, 0);
    }
}
