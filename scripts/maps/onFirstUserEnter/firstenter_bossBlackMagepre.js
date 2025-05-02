let event = player.getEvent("boss_blackmage_hard")
if (event != null) {
    switch (map.getId()) {
        case 450013100: {

            let wBoss = map.makeMob(8880500);
            wBoss.changeBaseHp(63_000_000_000);
            map.spawnMob(wBoss, -350, 85);

            map.spawnMob(8880512, 5, 85);

            let spiritMob = map.makeMob(8880505);
            spiritMob.changeBaseHp(63_000_000_000);
            map.spawnMob(spiritMob, -45, -157);

            let bBoss = map.makeMob(8880501);
            bBoss.changeBaseHp(63_000_000_000);
            map.spawnMob(bBoss, 350, 85);


            map.startFieldEvent();
            player.openUI(1204);
            //npc.userWzChat(4000, "在這個地區產生的攻擊似乎都受到創造或破壞的詛咒，假若同時具有兩種詛咒將可能受到#b極大傷害#k請小心點");
            break;
        }
        case 450013300: {
            let boss = map.makeMob(8880502);
            boss.changeBaseHp(115_500_000_000);
            map.spawnMob(boss, -52, 88);
            let floor = map.makeMob(8880516);
            map.spawnMob(floor, -52, 88);

            map.spawnMob(8880512, 0, 88);
            map.startFieldEvent();
            player.openUI(1204);
            break;
        }
        case 450013500: {
            let boss = map.makeMob(8880503);
            boss.changeBaseHp(117_500_000_000);
            map.spawnMob(boss, -52, 88);
            map.spawnMob(8880512, 0, 88);
            map.startFieldEvent();
            player.openUI(1204);
            break;
        }
        case 450013700: {
            let spiritMob = map.makeMob(8880504);
            spiritMob.changeBaseHp(140_000_000_000);
            map.spawnMob(spiritMob, 0, 218);
            map.spawnMob(8880512, 0, 218);
            map.startFieldEvent();
            player.openUI(1204);
            break;
        }
        case 450013750: {
            map.spawnMob(8880518, 0, 218);
            map.startFieldEvent();
            break;
        }
    }
}
