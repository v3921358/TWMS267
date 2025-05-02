// Boss: Bloody Queen

let event = player.getEvent("boss_bloody_chaos");
if (event != null && event.getVariable("boss") == null) {
        switch (event.getVariable("show")) {
                case 1:
                        event.setVariable("show", 2);
                        map.blowWeather(5120099, "哎呀，可愛的客人來啦。", 3);
                        break;
                case 2:
                        event.setVariable("show", 3);
                        map.blowWeather(5120100, "無理的家夥，竟然隨意進出大殿！", 3);
                        break;
                case 3:
                        event.setVariable("show", 4);
                        map.blowWeather(5120101, "嘻嘻，你這是送上門來了，這將是你的墳墓。", 3);
                        break;
                case 4:
                        event.setVariable("show", 5);
                        map.blowWeather(5120102, "呵呵，對你的死亡，我表示很悲傷。", 3);
                        break;
                default:
                        event.setVariable("boss", true);
                        let boss = map.makeMob(8920000);
                        map.spawnMob(boss, 36, 134);
                        map.startFieldEvent();
                        break;
        }
}