
if (player.isQuestStarted(2073))
	if (portal.makeEvent("pigFarm", player) != null)
		portal.playPortalSE();
	else
		portal.sayErrorInChat("It seems like someone already has visited Yoota's Farm.");
else
	portal.sayErrorInChat("There's a door that'll lead me somewhere, but I can't seem to get in there.");