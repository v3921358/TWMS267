/* MapleShark msb Creat Script tools */

npc.next().sayX("我就知道你會來幫忙。");
npc.next().sayX("最近周圍的怪物變得太強大。以前的話，孩子們在外面玩耍都沒問題的說。");
npc.next().sayX("沒有找到確切的原因。但有點擔心會不會是邪惡氣息已經滲入到這個地方了。");
npc.next().sayX("比起其他的，那麼多的怪物光靠居民們的力量是遠遠不足的。");
if (npc.askYesNo("話說…可不可以幫幫這邊的居民們，讓這裡可以重新找回活力呢？只要幫助我們的話，我就會給你能夠在這裡充分使用的獎勵。")) {
    npc.completeQuest();
} else { npc.sayX("如果想重新接取任務,請記得找我"); }
