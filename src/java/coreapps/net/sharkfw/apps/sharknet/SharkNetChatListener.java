package net.sharkfw.apps.sharknet;

/**
 *
 * @author thsc
 */
public interface SharkNetChatListener {

    /**
     * Method is called when a new entry was entered into this chat. 
     * @param newEntry
     */
    public void newEntryReached(SharkNetChatEntry newEntry);
}
