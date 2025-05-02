// 
// Quest ID:17629
// [凱梅爾茲共和國] 擊退阿庫旁多（4）


if (npc.askAcceptX("這裡也到處都是#b#e#o9390810##k#n！那好！我們來做個了結吧！你來消滅#b#e30只左右#k#n這些傢伙！")) {
    // Response is Yes
    npc.startQuest();
    npc.ui(1).sayX("加油！沒剩多少了。");
} else {
    // Response is No
    npc.ui(1).sayX("你為什麼總是猶豫不決。是不是害怕了?");
}