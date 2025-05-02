

/* global player, npc, party */

/**
 * NPC 2010007
 * Map 200000301
 *
 * Creates, expands, and destroys guilds.
 *
 *
 */

if (player.getGuildId() == 0) {
    npc.sayNext("你……是因為對公會感興趣，才會來找我的嗎？");

    let selection = npc.askMenu("你想要幹什麼呢？快告訴我吧。\r\n\r\n#b" +
        "#L0#請告訴我公會是什麼#l\r\n" +
        "#L1#怎麼才能建立公會呢？#l\r\n" +
        "#L2#我想建立公會#l\r\n" +
        "#L3#我想瞭解有關公會系統的詳細說明#l");

    switch (selection) {
        case 0:
            npc.sayNext("公會……你可以把它理解成一個小的組織。是擁有相同理想的人為了同一個目的而聚集在一起成立的組織。 但是公會是經過公會總部的正式登記，是經過認可的組織。");
            break;
        case 1:
            npc.sayNext("要想建立公會，至少必須達到100級。");
            npc.sayNext("要想建立公會，你需要總共6人。這6個人應該在同一隊伍，組隊長應該來跟我說話。請注意，組隊長將成為公會族長。");
            npc.sayNext("建立公會還需要1500000金幣的費用！");
            npc.sayNext("建立公會，帶6個人來~你不能沒有6個人就組成一個...哦，當然，6個人不能是其他公會的成員！如果有人已經加入了其他公會，那就不行了！！");
            break;
        case 2:
            selection = npc.askYesNo("哦！你是來建立公會的嗎……要想建立公會，需要500萬金幣。我相信你一定已經準備好了。好的~你想建立公會嗎？");
            if (selection == 1) {
                if (party == null) {
                    npc.sayNext("我不在乎你覺得自己有多強…為了建立一個公會，你需要參加一個6人的組隊。如果你真的很想成立一個公會的話，請建立一個6人的組隊，然後把你所有的隊員帶回來。");
                } else if (player.getId() != party.getLeader().getId()) {
                    npc.sayNext("如果你想建立一個公會，請讓隊長和我交談。");
                } else if (player.getLevel() < 100) {
                    npc.sayNext("嗯...我認為你沒有資格建立公會，請繼續修煉後再來吧！");
                } else {
                    let members = party.getLocalMembers();
                    let eligible = true;
                    for (let i = 0; i < members.length && eligible; i++)
                        if (members[i].getGuildId() != 0)
                            eligible = false;
                    if (!eligible) {
                        npc.sayNext("隊伍中好像有人已經是另一個公會的成員了。要建立一個公會，你所有的隊員都必須脫離他們的當前的公會。請確定後再回來吧。");
                    } else if (!player.getMeso() >= 1500000) {
                        //TODO: GMS-like line
                        npc.say("請再核對一下。你必須支付服務費來建立一個公會。");
                    } else {
                        npc.CreatGuildName();
                        if (player.getGuildId() != 0) {
                            player.loseMesos(1500000);
                        }
                    }
                }
            }
            break;
        case 3:
            npc.sayNext("你想瞭解更多有關公會的內容？如果是那樣的話，公會負責人蕾雅會為你介紹的。");
            break;
    }
} else {
    let selection = npc.askMenu("我能幫你什麼嗎？\r\n#b"
        + "#L0#我想增加公會人數#l\r\n"
        + "#L1#我想解散公會#l");

    if (player.getGuildRank() != 1) {
        npc.say("你不是公會的公會長！！這是隻有公會的公會長才可以決定的工作。");
    } else {
        switch (selection) {
            case 0: {
                let capacity = player.getGuildCapacity();
                if (capacity >= 200) {
                    npc.sayNext("公會人數已經達到最大容量了，不能再增加了！");
                } else {
                    npc.sayNext("你是想增加公會人數嗎？嗯，看來你的公會成長了不少~你也知道，要想增加公會人數，必須在公會本部重新登記。當然，使用金幣作為手續費。此外，公會成員最多可以增加到200個。");
                    let fee = 500000;
                    if (capacity >= 35)
                        fee *= 10;
                    else if (capacity >= 30)
                        fee *= 9;
                    else if (capacity >= 25)
                        fee *= 7;
                    else if (capacity >= 20)
                        fee *= 5;
                    else if (capacity >= 15)
                        fee *= 3;
                    //
                    selection = npc.askYesNo("當前的公會最大人數是#b" + capacity + "人#k，增加#b5人#k所需的手續費是#b" + fee + "金幣#k。怎麼樣？你想增加公會人數嗎？");
                    if (selection == 1) {
                        if (player.hasMeso(fee)) {
                            player.increaseGuildCapacity(5);
                            player.loseMesos(fee);
                        } else {
                            npc.say("您的金幣不足，增加人數需要" + fee + "金幣！");
                        }
                    }
                }
                break;
            }
            case 1:
                if (player.getAllianceId() != 0) {
                    npc.say("在你解散公會之前，你需要先退出公會聯盟！");
                } else if (npc.askYesNo("你真的要解散公會嗎？哎呀……哎呀……解散之後，你的公會就會被永久刪除。很多公會特權也會一起消失。你真的要解散嗎？") == 1) {
                    if (player.getMeso() > 200000) {
                        player.disbandGuild();
                        player.loseMesos(200000);
                    } else {
                        //TODO: GMS-like line
                        npc.say("您的金幣不足，需要200000金幣的手續費！");
                    }
                }
                break;
        }
    }
}
