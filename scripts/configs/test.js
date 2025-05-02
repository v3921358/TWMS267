// var str = `12312321"\r\n`
// str += "#L0#0#l\r\n"
// str += "#L1#1#l\r\n"
// sh.debug("123")
// var cc = npc.me().askMenu(str)
var cc = npc.askYesNo("123456")
sh.debug(String(cc))
npc.me().say(String(cc))
// npc.say("123")