let NormalEvents = "fire_wolf_normal";
let ChaosEvents =  "fire_wolf_chaos";
let ExtremeEvents = "fire_wolf_extreme";

npc.sayNext("我是楓之谷世界最厲害的賞金獵人#r#e波羅#n#k。正和\r\n弟弟#b#e普里托#n#k一起擊退魔物們。", 9001059)
var selection = npc.askMenu("我們兄弟終於找到追擊好長一段時間的最強怪物#r#e烈燄戰狼#n#k的巢穴。那傢伙只要遇到楓之谷的旅行者就會掠奪，實在非常毒辣…怎樣，要不要跟我一起消滅那傢伙？\r\n#b#L0#同行。#l\r\n#b#L1#不同行。")
switch (selection) {
    case 0:
        npc.rememberMap("RETURN_MAP");
        if (player.getLevel() < 200) {
            if (npc.makeEvent(NormalEvents, player) == null) {
                npc.sayNext("#e#r發生錯誤，請聯系管理員！");
            }
        }
        if (player.getLevel() < 260 && player.getLevel() >= 200) {
            if (npc.makeEvent(ChaosEvents, player) == null) {
                npc.sayNext("#e#r發生錯誤，請聯系管理員！");
            }
        }
        if (player.getLevel() >= 260) {
            if (npc.makeEvent(ExtremeEvents, player) == null) {
                npc.sayNext("#e#r發生錯誤，請聯系管理員！");
            }
        }
        break
    case 1:
        break
}
