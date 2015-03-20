package net.sharkfw.apps.sharknet;

import java.io.InputStream;
import net.sharkfw.knowledgeBase.*;

/**
 * @author thsc
 */
public interface SharkNetChat {
    /**
     * Create an entry in this chat with a message.
     * @param message
     * @return
     * @throws SharkNetException
     * @throws SharkKBException
     */
    public SharkNetChatEntry createEntry(String message) throws SharkNetException, SharkKBException;
    
    /**
     * Create an entry in this chat that only contains some content.
     * @param content
     * @return
     * @throws SharkNetException
     * @throws SharkKBException
     */
    public SharkNetChatEntry createEntry(InputStream content) throws SharkNetException, SharkKBException;
    
    /**
     * Create an entry in this chat. entry has a message and arbitrary content
     * @param message
     * @param content
     * @return
     * @throws SharkNetException
     * @throws SharkKBException
     */
    public SharkNetChatEntry createEntry(String message, InputStream content) throws SharkNetException, SharkKBException;
    
    /**
     * Publish that (newly created) entry to all listeners.
     * @param entry 
     */
    public void publishEntry(SharkNetChatEntry entry);

    /**
     * Add listener that is informed if a new entry is added to this chat.
     * @param chatListener 
     */
    public void addListener(SharkNetChatListener chatListener);
    
    public void removeListener(SharkNetChatListener chatListener);
}
