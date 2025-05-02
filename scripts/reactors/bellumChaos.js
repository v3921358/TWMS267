
// ID:[350160240]
// MapName:深潭洞穴 混沌
//深潭洞穴 混沌

let event = player.getEvent("boss_bellum_chaos");
if (event != null && event.getVariable("boss") == null) {
        event.setVariable("boss", true);
        let boss = map.makeMob(8930000);
        map.spawnMob(boss, -190, 443);
        map.blowWeather(5120103, "你這傢伙無視我的警告又來了，我不會再憐憫你了。", 3);
}