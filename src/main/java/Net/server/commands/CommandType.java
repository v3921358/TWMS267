/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Net.server.commands;

/**
 * @author PlayDK
 */
public enum CommandType {

    NORMAL(0),
    TRADE(1);
    private final int level;

    CommandType(int level) {
        this.level = level;
    }

    public int getType() {
        return level;
    }
}
