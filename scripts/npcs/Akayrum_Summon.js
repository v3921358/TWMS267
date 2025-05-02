// [阿卡伊農]  |  [2144010]
// MapName:阿卡伊農的祭壇

if (party == null || player.getCharacterId() != party.getLeader().getCharacterId()) {
        npc.noEsc().next().sayX("請讓隊長與我交談。");
} else {
        if (npc.noEsc().askAcceptX("把我長久的計畫變成泡沫的傢伙們，竟然親自過來找我，真是太開心了。\r\n\r\n#r作為代價，我將給你帶來這世界上最痛苦的死亡。#k")) {
                let event = player.getEvent("boss_akayrum_normal");
                if (event != null && event.getVariable("boss") == null) {
                        event.setVariable("boss", false);
                         map.spawnMob(8860010, 320, -181);
                }
                map.destroyTempNpc(2144010);
        }
}