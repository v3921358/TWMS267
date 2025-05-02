/**
 * Robo Upgrade! (NPC 9102001 Quest 4659) End
 * New Leaf City Town Street: Nea Leaf City - Town Center (Map 600000000)
 *
 * Handles evolution of Baby Robo.
 *
 *
 */

//TODO: GMS-like lines
if (player.hasItem(4000111, 50)) {
    player.evolveBossPet();
    player.loseItem(5380000, 1);
    player.loseItem(4000111, 50);
    npc.completeQuest();
} else {
    npc.ui(1).sayX("Insufficient quantity of #b#t4000111##k. You need #b50#k.");
}