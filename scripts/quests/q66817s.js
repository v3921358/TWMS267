npc.next().sayX("你好#b#h0##k。成長了很多啊，我為了變強的你準備了特別的禮物！")
npc.next().sayX("之後的冒險會遇到更多強大的敵人！為此需要更強大的力量！")
npc.next().sayX("#b#i3996222:# #t3996222##k\r\n使用這個方塊可以強化道具，變得更加強大！！！")
var res = npc.askYesNo("#b#h0##k，準備好收下了嗎？")
if (res) {
    if (!player.canGainItem(3996222,1))
        npc.next().sayX("裝飾欄欄位不足，請把欄位空出一格再進行操作。")
    else {
        player.gainItem(3996222, 1)
        npc.completeQuest()
        npc.next().sayX("#b#i3996222:# #t3996222##k\r\n請好好使用。如果不見的話，可以再來找我。")
    }
} else {
    npc.next().sayX("準備好收下的時候，請告訴我")
}
