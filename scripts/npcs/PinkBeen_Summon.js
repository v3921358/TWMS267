// [奇勒斯坦]  |  [2141000]
// MapName:神祇的黃昏

if (party == null || player.getCharacterId() != party.getLeader().getCharacterId()) {
        npc.noEsc().next().sayX("請讓隊長與我交談。");
} else if (npc.noEsc().askAcceptX("只要有女神之鏡的話…就能再次傳喚黑魔法師了…！\r\n好，好奇怪…為什麼沒有召喚來黑魔法師？這氣息是什麼？這是跟黑色魔法師完全不同的…喀啊啊啊！\r\n\r\n#b(將手放在奇勒斯坦的肩膀上。)#k")) {
        let eventName;
        let mobId;
        let soulMobId;
        let mobs;
        switch (map.getId()) {
                case 270050100:
                        mobId = 8820008;
                        soulMobId = 8820014;
                        mobs = [8820002, 8820003, 8820004, 8820005, 8820006];
                        eventName = "boss_pinkbeen_normal";
                        break;
                case 270051100:
                        mobId = 8820108;
                        soulMobId = 8820114;
                        mobs = [8820102, 8820103, 8820104, 8820105, 8820106];
                        eventName = "boss_pinkbeen_chaos";
                        break;
        }
        let event = player.getEvent(eventName);
        if (event != null && event.getVariable("boss") == null) {
                event.setVariable("boss", false);
                map.clearMobs();
                map.spawnMob(mobId, 5, -42);
                let soulMob = map.makeMob(soulMobId);
                map.spawnMob(soulMob, 5, -42);
                for (let i = 0; i < mobs.length; i++) {
                        let mId = mobs[i];
                        let cMob = map.makeMob(mId);
                        map.spawnMob(cMob, 5, -42);
                }
        }
        map.destroyTempNpc(2141000);
}
