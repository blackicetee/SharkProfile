package net.sharkfw.subspace;

import net.sharkfw.knowledgeBase.PeerSemanticTag;

/**
 *
 * @author thsc
 */
public interface SubSpaceListener {
    /**
     * Called if a member was added
     *
     * @param addedPeer the value of addedPeer 
     */
    
    void memberAdded(PeerSemanticTag addedPeer);

    void memberRemoved(PeerSemanticTag addedPeer);
    
    /**
     * Called if subscriber list has changed.
     * 
     * @param peer 
     */
    void subscriberChanged();

}
