/* Npc: 9010106 || Quest: q503281s.js , Date:2024-11-19 21:27:50  */
player.updateQuestRecordEx(503281,"startDate=24/11/19");
npc.sayNext("您好，勇士！您有好好經營\r\n#b楓之谷聯盟#k嗎？看著\r\n勇士您和您的#b楓之谷聯盟#k一同成長的樣子，是我最大的喜悅。");
if (npc.askYesNo("擊敗護衛#r巨龍#k的#b迷你龍#k和#r黃金翼龍#k各#b100隻#k、#b20隻#k，\r\n就會給予#b#i4310229:##t4310229#200個#k當作獎勵。\r\n你要執行任務嗎？\r\n\r\n#r※每個世界一星期只能執行1次，\r\n#e#r星期日午夜#n過後任務就會初始化。#k")) {
    player.updateQuestRecordEx(503281,"dow=2;startDate=24/11/19");
    npc.say("果然是懂得享受挑戰的人物！\r\n#r迷你龍#k和#r黃金翼龍#k可在#r龍的戰場#k透過#b聯盟戰地副本#k狩獵。\r\n想完成每週任務，就必須親自來村莊找我，\r\n那麼祝你好運！");
    npc.completeQuest();
}
