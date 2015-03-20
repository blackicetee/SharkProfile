package net.sharkfw.apps.sharknet;

import net.sharkfw.knowledgeBase.PeerSemanticTag;

/**
 *
 * @author thsc
 */
public interface SharkNetListener {
    /**
     * Profile of a peer has been added.
     * @param peer 
     */
    public void notifyProfileAdded(PeerSemanticTag peer);
    
    /**
     * Informs about invitation to a Chat
     * @param invitedTo 
     */
    public void notifyInvitation(SharkNetChat invitedTo);
}
