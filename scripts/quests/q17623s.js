//
// Quest ID:17623
// [凱梅爾茲共和國] 我只是個異邦人


if (npc.askYesNoS("都怪萊文沒事亂出頭,現在情況變得很奇怪。好累啊,還是先休息吧。", true)) {
    // Response is Yes
    npc.startQuest();
    player.changeMap(865000004, 2);
} else {
    // Response is No
    npc.ui(1).meFlip().sayX("還有事情要做。");
}
