
// ID:[271040100]
// MapName:西格諾斯殿堂
// 西格諾斯殿堂

let event = player.getEvent("boss_shinas_normal");
if (event != null && event.getVariable("boss") == null) {
        event.setVariable("boss", false);
        map.spawnMob(8850012, -160, 115);
        map.blowWeather(5120043, "已經很久沒有看到這個地方有人到來了，但同時也沒有平安無事回去的人。", 5);
}