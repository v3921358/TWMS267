let event = player.getEvent()
if (event != null) {
    switch (map.getId()) {
        case 450013910:
            player.showWeatherEffectNotice("為了要與黑魔法師對決，必須除掉他的護衛，創造與破壞的騎士們。", 265, 8000);
            break
            // player.addPopupSay(0, 4000, "在這個地區產生的攻擊似乎都受到創造或破壞的詛咒，\r\n假若同時具有兩種詛咒將可能受到#b極大傷害#k請小心點。", "")
        case 450013930:
            player.showWeatherEffectNotice("黑魔法師總算就在眼前，盡全力擊退他吧！", 265, 8000);
            // player.addPopupSay(0, 4000, "在這個地區產生的攻擊似乎都受到創造或破壞的詛咒，\r\n假若同時具有兩種詛咒將可能受到#b極大傷害#k請小心點。", "")
            break
        case 450013950:
            player.showWeatherEffectNotice("看他的樣子，感覺彷彿是擁有了神的權能，即使對手是神，也必須為了大家將他擋下。", 265, 8000);
            // player.addPopupSay(3003902, 4000, "#face1#走吧！我負責報仇，你則守護世界。", "")
            break
        case 450013970:
            player.showWeatherEffectNotice("什麼都沒有的空間...只剩下我一個人嗎？", 265, 8000);
            // player.addPopupSay(0, 4000, "什麼都沒有的空間...只剩下我一個人嗎？", "")
            break;
        default:
            break;
    }
}