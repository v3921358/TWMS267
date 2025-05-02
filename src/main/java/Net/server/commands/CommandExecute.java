package Net.server.commands;

import Client.MapleClient;

/**
 * Interface for the executable part of a {@link CommandObject}.
 *
 * @author Emilyx3
 */
public interface CommandExecute {

    /**
     * The method executed when this command is used.
     *
     * @param c        the Client.client executing this command
     * @param splitted the command and any arguments attached
     * @return 1 if you want to log the command, 0 if not.
     */
    int execute(MapleClient c, String[] splitted) throws Exception;
    //1 = Success
    //0 = Something Went Wrong

    String getShortCommand();

    default CommandType getType() {
        return CommandType.NORMAL;
    }

    enum ReturnValue {

        DONT_LOG,
        LOG
    }

    interface TradeExecute extends CommandExecute {

        @Override
        default CommandType getType() {
            return CommandType.TRADE;
        }
    }
}
