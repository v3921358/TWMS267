package Config.constants.enums;

public enum ScriptType {
    Npc("npcs"),
    QuestStart("quests"),
    QuestEnd("quests"),
    Item("items"),
    Map("maps"),
    onUserEnter("maps/onUserEnter"),
    onFirstUserEnter("maps/onFirstUserEnter"),
    Portal("portals"),
    Event("events"),
    Reactor("reactors"),
    Command("commands"),
    None(""),
    expands("expands"),
    BossUI("boss");

    private final String dir;

    ScriptType(String dir) {
        this.dir = dir;
    }

    public String getDir() {
        return dir;
    }

}
