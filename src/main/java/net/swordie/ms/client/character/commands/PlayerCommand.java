package net.swordie.ms.client.character.commands;

/**
 * Created on 12/22/2017.
 */
public abstract class PlayerCommand implements ICommand {

    public PlayerCommand() {
    }

    private static char prefix = '@';

    public static char getPrefix() {
        return prefix;
    }
}
