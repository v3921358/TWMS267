npc.sayNext("你好？我是楓之谷世界最好的賞金獵人#r#e保羅#n#k！", 9001059)
var selection = npc.askMenu("我剛好想去打獵。你愿意和我一起去消滅#b火焰狼#k嗎？#b\r\n\r\n#L1#一起去。#l\r\n#L2#不想去。#l", 9001059)
switch (selection) {
    case 1:
        npc.rememberMap("RETURN_MAP");
        if (npc.makeEvent("fire_wolf", player) == null) {
            npc.sayNext("#e#r發生錯誤，請聯系管理員！");
        }
        break
    case 2:
        break
}
