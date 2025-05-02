/**
 *
 *
 */

startScript();

function startScript() {
    npc.ui(1).uiMax().next().noEsc().sayX("#h0#, 我有東西要交給你。這是一本#b冒險之書#k, 它可以記錄你日後在冒險島世界旅行過程中所經歷的事情。在這裡可以記錄將要發生的只屬於你的故事。");
    let ret = npc.askYesNoNoEsc("怎麼樣，要領取#b冒險之書#k嗎？");
    if (ret == 1) {
        let trueJobGrade = parseInt(player.getJob() / 100);
        npc.startQuest(trueJobGrade);
        let job = "";
        switch (trueJobGrade) {
            case 1:
                job = "戰士";
                break;
            case 2:
                job = "魔法師";
                break;
            case 3:
                job = "弓箭手";
                break;
            case 4:
                job = "飛俠";
                break;
            case 5:
                job = "海盜";
                break;
        }
        npc.ui(1).uiMax().next().noEsc().sayX("給你......這本冒險之書正適合你這名" + job + "......");
        npc.ui(1).uiMax().next().noEsc().sayX("詳細內容你可以慢慢檢視。......");
        npc.ui(1).uiMax().next().noEsc().sayX("雖然這條路並不平坦, 但我希望即將啟程的你可以盡情享受這次冒險之旅。");
    }
}
