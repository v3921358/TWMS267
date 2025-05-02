package Net.server.commands;

import Client.MapleClient;

/**
 * Represents a command given by a user
 *
 * @author Emilyx3
 */
public class CommandObject {

    /**
     * what {@link MapleClient#gmLevel} is required to use this command
     */
    private final int levelReq;
    /**
     * what gets done when this command is used
     */
    private final CommandExecute exe;

    public CommandObject(CommandExecute c, int level) {
        exe = c;
        levelReq = level;
    }

    /**
     * Call this to apply this command to the specified {@link MapleClient} with
     * the specified arguments.
     *
     * @param c        the MapleClient to apply this to
     * @param splitted the arguments
     * @return See {@link CommandExecute#execute}
     */
    public int execute(MapleClient c, String[] splitted) throws Exception {
        return exe.execute(c, splitted);
    }

    public CommandType getType() {
        return exe.getType();
    }

    public String getShortCommand() {
        return exe.getShortCommand();
    }

    /**
     * Returns the GMLevel needed to use this command.
     *
     * @return the required GM Level
     */
    public int getReqLevel() {
        return levelReq;
    }

    public int getSpReqLevel() {
        return isGm() ? levelReq - PlayerRank.實習管理員.getLevel() + 1 : levelReq;
    }

    public boolean isGm() {
        return levelReq >= PlayerRank.實習管理員.getLevel();
    }

    protected String getName() {
        return exe.getClass().getSimpleName();
    }
}
