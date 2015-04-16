package net.sharkfw.apps.sharknet;

import net.sharkfw.knowledgeBase.Information;
import net.sharkfw.knowledgeBase.SemanticTag;
import net.sharkfw.knowledgeBase.SharkKBException;

/**
 *
 * @author thsc
 */
public interface SharkNetChatEntry {
    
    /**
     * @return True if this entry contains information
     * @throws net.sharkfw.knowledgeBase.SharkKBException
     */
    public boolean hasInformation() throws SharkKBException;

    /**
     * @return True if this entry contains a text message
     * @throws net.sharkfw.knowledgeBase.SharkKBException
     */
    public boolean hasTextMessage() throws SharkKBException;
    
    /**
     * @return True if this entry contains a semantic tag
     * @throws net.sharkfw.knowledgeBase.SharkKBException
     */
    public boolean hasSemanticTag() throws SharkKBException;
    
    public SemanticTag getSemanticTag() throws SharkKBException;
    public Information getInformation() throws SharkKBException;
    public String getTextMessage() throws SharkKBException;
    
    /**
     * Entry creation time.
     * @return 
     */
    public long getTime();
}
