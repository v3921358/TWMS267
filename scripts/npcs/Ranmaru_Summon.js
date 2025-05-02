
// [森蘭丸]  |  [9130090]
// MapName:秘密祭壇

if (party != null){
        npc.askAccept("別再多說了，你能阻止我嗎？#k");{
                let event = player.getEvent("boss_ranmaru_normal");
                if (event != null && event.getVariable("boss") == null) {
                        event.setVariable("boss", false);
                        let monster = map.makeMob(9421581);
                        monster.changeBaseHp(999999999999);
                        map.spawnMob(monster, -424, -15);
                }
                map.destroyTempNpc(9130090);
        }
}
