// ID:[240060202]
// MapName:混沌闇黑龍王的洞窟
// 混沌闇黑龍王的洞窟

let event = player.getEvent("boss_hontale_chaos");
if (event != null) {
        event.setVariable("boss3", false);
        event.startTimer("Hontale", 2000);
}