// ID:[262030100]
// MapName:走廊1
// 走廊1

let key;
let mobId;
switch (map.getId()) {
        case 262030100:
                key = "S1_Summon";
                mobId = 8870003;
                break;
        case 262031101:
                key = "S1_Summon";
                mobId = 8870103;
                break;
        case 262030200:
                key = "S2_Summon";
                mobId = 8870004;
                break;
        case 262031201:
                key = "S2_Summon";
                mobId = 8870104;
                break;
}
if (party == null || player.getCharacterId() != party.getLeader().getCharacterId()) {
        player.showSpouseMessage(6, `請讓你的組隊長通過吧`)
} else {
        let event = player.getEvent();
        if (event != null) {
                let canSummon = event.getVariable(key) == null;
                if (map.getEventMobCount() > 0) {
                        if (canSummon) {
                                player.showSpouseMessage(6, `為了前往希拉的塔的最頂樓，要將骷髏騎士全部除掉。`)

                        } else {
                                player.showSpouseMessage(6, `因血牙的阻礙無法移動至下一個場所。`)
                        }
                } else {
                        if (canSummon) {
                                event.setVariable(key, true)
                                player.showSpouseMessage(6, `血牙察覺到我們入侵了！擊退血牙。`)
                                for (let i = 0; i < 3; i++) {
                                        let mob = map.makeMob(mobId);
                                        mob.setAppearType(0x2B);
                                        map.spawnMob(mob, 777, 196)
                                }
                        } else {
                                party.changeMap(map.getId() + 100)
                        }
                }
        }
}