// ID:[272020210]
// MapName:阿卡伊農的祭壇

let event = player.getEvent("boss_akayrum_easy");
if (event != null && event.getVariable("npc") == null) {
        event.setVariable("npc", true);
        map.spawnTempNpc(2144021, 320, -190);
        map.blowWeather(5120056, "無法區分勇氣和魯莽的人們呀。 如果不要命的話，就儘管過來吧！ 呵呵。", 5);
}