/**
 *
 *
 */
npc.next().sayX("哦，起來啦，小不點？大清早的，怎麼這麼大的黑眼圈啊？晚上沒睡好嗎？什麼？做了奇怪的夢？什麼夢啊？嗯？夢見遇到了龍？");
npc.next().sayX("哈哈哈哈~龍？不得了。居然夢到了龍！但是夢裡有狗嗎？哈哈哈哈~");
npc.completeQuest();
player.gainExp(20);
let string = ["UI/tutorial/evan/2/0"];
npc.sayImage(string);
