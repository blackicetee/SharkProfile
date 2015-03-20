package net.sharkfw.apps.sharknet;

import net.sharkfw.knowledgeBase.SharkCS;
import net.sharkfw.knowledgeBase.SharkKBException;

/**
 *
 * @author thsc
 */
public interface SharkNetInterest extends SharkCS {
    /**
     * 
     * @return a temporary id. This ID might change from time to time.
     * It can be used for brief interactions with user interface
     */
    public int getTempID();
    
    /**
     * Return time when this interest has arrived this peer.
     * Creation time is returned when using this methode on
     * interests which are created by peer itself.
     * @return 
     */
    public long arrivalTime();

    public String serialize() throws SharkKBException;
    
    public SharkCS getSharkCS();
    
}
