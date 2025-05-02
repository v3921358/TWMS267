// ID:[240060200]
// MapName:闇黑龍王洞穴
// 闇黑龍王洞穴

let event = player.getEvent("boss_hontale_normal");
if (event != null) {
        event.setVariable("boss3", false);
        event.startTimer("Hontale", 2000);
}