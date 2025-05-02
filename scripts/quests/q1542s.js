/**
 *
 *
 */
npc.ui(1).uiMax().next().sayX("雖然我曾經預想過，但一想到雙弩精靈都不記得我了，我的心就……");
npc.ui(1).uiMax().next().sayX("就算繼續變得強大下去，又有什麼意義呢？");
npc.ui(1).uiMax().next().sayX("……迄今為止之所以我可以一直強大，都要多虧了阿琅借給我的守護精靈。");
npc.ui(1).uiMax().next().sayX("原本都塵埃落定之後，我是打算將精靈還回去的。");
npc.ui(1).uiMax().next().sayX("當再次與阿琅相遇之時，想要證明我們從前就認識，拿出阿琅的精靈是最為確鑿的證據。");
npc.ui(1).uiMax().next().sayX("但是不能在繼續拖延下去了，阿琅會一直在沒有守護精靈的陪伴下孤獨生活下去的。");
npc.ui(1).uiMax().next().sayX("日後只希望看到精靈時，阿琅能夠記起我，但現在也只能放下這虛無的希望了。");
let ret = npc.askYesNoS("雖然已經遲了許久，單不要在做拖延，得趕緊去見阿琅了。\r\n#b(接受後會立即前往狐狸村。)");
if (ret == 1) {
    npc.ui(1).uiMax().next().sayX("去狐狸村吧，阿琅應該就在狐狸樹附近。");
    npc.startQuest();
    npc.rememberMap("RETURN_MAP");
    player.changeMap(940204115, 0);
}
