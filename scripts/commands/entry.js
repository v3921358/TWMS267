import {give, giveItemForMap, givePoint, help, notice, rate, spawn, ban, maxSkill} from "scripts/commands/help.js";

var command = npc.getVariable("commands")

switch (command[0]) {
    case "Ap":
        let ApPoint = npc.askNumber("輸入需要獲得的AP數量(#r最大值3000):", 1, 1, 3000);
        player.dropMessage(5,"您獲得了:"+ApPoint+"屬性點數。");
        player.gainAp(ApPoint);
        break;
    case "Exp":
        let exp = npc.askNumber("輸入需要獲得的經驗數量(#r最大值999999999999):", 1, 1, 999999999999);
        player.dropMessage(5,"您獲得了:"+exp+"經驗值。");
        player.gainExp(exp);
        break;
    case "HexaLv":
        let HexaLv = npc.askNumber("輸入需要指定升級艾爾達斯等級(#r最大值100):", 1, 1, 100);
        player.dropMessage(5,"您已經將艾爾達斯升級到了:"+HexaLv+"等級。");
        player.updateQuestRecordEx(1489, "0exp=0;0="+HexaLv);
        break;
    case "HexaItem":
        let HexaPoint = npc.askNumber("輸入需要獲得的艾爾達斯碎片(#r最大值30000):", 1, 1, 30000);
        player.dropMessage(5,"您獲得了:"+HexaPoint+"艾爾達斯碎片。");
        player.gainItem(4009547, HexaPoint);
        break;
    case "VcorePoint":
        let VPoint = npc.askNumber("輸入需要獲得的V核心碎片(#r最大值30000):", 1, 1, 30000);
        player.dropMessage(5,"您獲得了:"+VPoint+"V核心碎片。");
        player.gainVCraftCore(VPoint);
        break;
    case "makeNpc":
        player.makeNpc(Number(command[1]));
        break;
    case "level":
        player.setLevel(Number(command[1]));
        break;
    case "horn":
        player.modifyHonor(Number(command[1]));
        break;
    case "reload":
        var type = Number(command[1]);
        switch (type) {
            case 1:
                player.reload(type);
                player.dropMessage(40, "已經重讀設定..")
                break;
            case 2:
                player.reload(type);
                player.dropMessage(40, "已經重讀掉寶..")
                break;
            case 3:
                player.reloadSkill();
                player.dropMessage(40, "已經重讀技能..")
                break;
        }
        break;
    case "item":
        player.gainItem(Number(command[1]), Number(command[2]));
        break;
    case "maxskill":
        maxSkill();
        break;
    case "packet":
        player.openPacketUIByAdmin();
        break;
    case "warp":
        player.changeMap(Number(command[1]), 0);
        break;
    case "UI":
        player.openUI(Number(command[1]));
        break;
    case "help":
        help();
        break;
    case "meso":
        player.gainMeso(Number(command[1]));
        break;
    case "cash":
        player.modifyCashShopCurrency(1, Number(command[1]));
        break;
    case "point":
        player.modifyCashShopCurrency(2, Number(command[1]));
        break;
    case "mileage":
        player.modifyCashShopCurrency(3, Number(command[1]));
        break;
    case "hide":
        player.gmHide();
        player.setMaxHp(500000);
        player.setMaxMp(500000);
        break;
    case "killall":
        player.gmKillMap();
        break;
    case "mob":
        player.gmGetMob()
        break;
    case "online":
        player.showSpouseMessage(9, "在線玩家人數: " + player.getOnlinePlayersNum())
        break;
    case "pos":
        var position = player.getPosition();
        player.showSpouseMessage(9, "MapNumber: " + map.getId())
        player.showSpouseMessage(9, position.x + "," + position.y);
        break;
    case "cooldown":
        player.gmCooldown()
        break;
    case "auto":
        player.autoAttack(true)
        break;
    case "useskill":
        if (!!command[1] && !!command[2]) {
            player.useSkillEffect(Number(command[1]), Number(command[2]))
        } else {
            player.showSpouseMessage(9, "指令格式錯誤")
        }
        break;
    case "setskill":
        if (!!command[1] && !!command[2]) {
            player.changeSkillLevel(Number(command[1]), Number(command[2]))
        } else {
            player.showSpouseMessage(9, "指令格式錯誤")
        }
        break;
    case "fam":
        player.openUI(0x251);
        break;
    case "ban":
        ban(command)
        break;
    case "unban":
        if (!!command[1]) {
            player.unban(String(command[1]))
        } else {
            player.showSpouseMessage(9, "指令格式錯誤")
        }
        break;
    case "關機":
        player.dropMessage(40, "服務器將在 " + command[1] + " 秒後關閉。");
        npc.setDelay(Number(command[1]));
        sh.shutdown(Number(command[1]));
        break;
    case "高":
        player.runScript("AdvancedSearch");
        break;
    case "主選單 ":
        player.runScript("JT/Menu");
        break;
    case "max":
        player.maxSkills();
        break;
    default:
        break;
}
