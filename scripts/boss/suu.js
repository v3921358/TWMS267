import { BossList, BossDifficulty } from "scripts/modals/boss.js";

var boss = BossList.Suu
var practiceMode = false
var eventName = boss.event + BossDifficulty[difficulty]
if (party == null) {
    npc.createParty();
    npc.say("這裡很危險，必須組隊進入。\r\n#b(只有1人以上的隊伍可以申請。)#k");
} else {
    if (player.getCharacterId() != party.getLeader().getCharacterId()) {
        npc.say("請讓你的組隊長和我對話吧!");
    } else if (party.getMembersCount(1, 300) < party.getMembersCount()) {
        npc.say("好像有隊員在其他地方，請把隊員都召集到這裡！");
    } else if (party.getMembersCount(1, 300) < boss.minPlayers || party.getMembersCount(1, 300) > boss.maxPlayers) {
        npc.say("你需要有一個#r" + boss.minPlayers + "~" + boss.maxPlayers + "#k人的隊伍.!");
    } else if (party.getNotAllowedMember(eventName, boss.maxDayLimit) != null) {
        npc.say("你隊員#r#e" + party.getNotAllowedMember(eventName, boss.maxDayLimit).getName() + "#n#k完成次數已經達到上限了。");
    } else if (npc.makeEvent(boss.event + BossDifficulty[difficulty], [party, practiceMode]) == null) {
        npc.say("#e#r發生錯誤，請聯系管理員！");
    }
}


