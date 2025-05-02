/**
 *
 *
 */

let ret = npc.askYesNo("你不覺得奇怪嗎？最近的雞怎麼和以前不一樣了？以前它們會下很多 雞蛋 ，但現在越來越少了。是不是因為狐狸增多了呢？那樣的話，必須趕緊想辦法才行。你說對不對？");
if (ret == 1) {
    npc.startQuest();
    npc.next().sayX("好吧，讓我們去消滅狐狸吧。你先去 #b後院#k 消滅#r10只 陰險的狐狸#k 。我會負責剩下的事情的。好了，你快到 後院 去吧~");
    let string = ["UI/tutorial/evan/10/0"];
    npc.sayImage(string);
} else {
    npc.next().sayX("嗯？什麼？害怕 陰險的狐狸 ？沒想到我弟弟這麼膽小。");
}
