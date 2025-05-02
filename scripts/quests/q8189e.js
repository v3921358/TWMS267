/**
 * Pet Re-Evolution (NPC 9102001 Quest 8189) End
 * New Leaf City Town Street: Nea Leaf City - Town Center (Map 600000000)
 *
 * Handles re-evolution of robo and dragons.
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