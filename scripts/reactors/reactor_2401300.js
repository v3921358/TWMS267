// ID:[240060300]
// MapName:闇黑龍王洞穴(簡單)
// 闇黑龍王洞穴(簡單)

let event = player.getEvent("boss_hontale_easy");
if (event != null) {
        event.setVariable("boss3", false);
        event.startTimer("Hontale", 2000);
}