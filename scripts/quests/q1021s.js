/**
 * Roger's Apple (NPC 2000 Quest 1021) Start
 * Maple Road: Lower level of the Training Camp (Map 2)
 *
 * Gives advice to new players on how to use items.
 *
 *
 */

startScript();

function startScript() {
    npc.next().sayX("Hey, Man~ What's up? Haha! I am Roger who can teach you adorable new Maplers lots of information.");
    npc.next().sayX("You are asking who made me do this? Ahahahaha!\r\nMyself! I wanted to do this and just be kind to you new travellers.");
    let hurt = npc.askAcceptX("So..... Let me just do this for fun! Abaracadabra~!");
    if (hurt == 1) {
        if (!player.hasItem(2010007, 1)) {
            if (!player.gainItem(2010007, 1)) {
                npc.ui(1).sayX("Please clear an empty slot in your use inventory and then click me again.");
                return;
            }
        }
        npc.startQuest();
        player.setHp(25);
        npc.next().sayX("Surprised? If HP becomes 0, then you are in trouble. Now, I will give you #rRoger's Apple#k. Please take it. You will feel stronger. Open the Item window and double click to consume. Hey, it's very simple to open the Item window. Just press #bI#k on your keyboard.");
        npc.ui(1).sayX("Please take all Roger's Apples that I gave you. You will be able to see the HP bar increasing. Please talk to me again when you recover your HP 100%.");
    } else {
        npc.next().sayX("I can't believe you have just turned down an attractive guy like me!");
    }
}
