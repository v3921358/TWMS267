var menu = ""
menu += "#L0#SP初始化\r\n"
menu += "#L1#結束對話。\r\n"
if (player.isIntern()) {
    menu += "#L2#我要進行寵物染色\r\n"
}
var res = npc.askMenu("您好嗎？在楓之谷旅行愉快嗎？\r\n\r\n\r\n#b" + menu +"#k")
switch (res) {
    case 0:
        player.spReset()
        npc.say("初始化完成")
        break
    case 1:
        break
    case 2:
        if (!player.isIntern()) {
            break
        }
        var pets = player.getSummonedPets()
        if (pets.size() <= 0) {
            npc.say("我可以為你的寵物進行染色喲，但是我需要一些回報喲。染色是隨機的哦！一次2000樂豆點。 \r\n#r但是你沒有召喚出寵物來呀。#k.")
        } else {
            var str = ""
            for (var i = 0; i < pets.size(); i++) {
                if (pets.get(i) != null) {
                    str += "#L" + i + "#[" + pets.get(i).getName() + "]#k\r\n"
                }
            }
            var select = npc.askMenu("我可以為你的寵物進行染色喲，但是我需要一些回報喲。染色是隨機的哦！一次2000樂豆點。要的現在請選擇你要進行染色的寵物。\r\n" + str)
            if (npc.getNX(1) < 2000) {
                npc.say("請確認你有足夠多的樂豆點.")
            } else {
                npc.changePetColor(select)
                npc.gainNX(1, -2000)
                npc.say("染色好啦！看看是不是你喜歡的顏色，不喜歡可以繼續進行染色喲，歡迎下次再來")
            }
        }
        break
}
