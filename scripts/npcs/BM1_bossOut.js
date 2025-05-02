if (npc.askYesNo("#fs14##r打不過戴斯克嗎,還是想離開了??")) {
    npc.next().sayX("好的，我馬上把你送出去。")
    player.changeMap(450009301, 0)
} else npc.askAccept("#b如果是想離開,請再次跟我對話!")
