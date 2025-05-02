/**
 * Pet Evolution2 (NPC 9102001 Quest 8185) End
 * New Leaf City Town Street: Nea Leaf City - Town Center (Map 600000000)
 *
 * Handles evolution of Baby Dragon.
 *
 *
 */

//TODO: GMS-like lines
if (player.hasMesos(10000)) {
    player.evolveBossPet();
    player.loseItem(5380000, 1);
    player.loseMesos(10000);
    npc.completeQuest();
} else {
    npc.ui(1).sayX("Insufficient mesos. You need #b10000#k.");
}