package net.sharkfw.subspace;

import net.sharkfw.knowledgeBase.ContextPoint;
import net.sharkfw.knowledgeBase.PeerSemanticTag;

/**
 *
 * @author thsc
 */
public interface SubSpaceEntry {
    public PeerSemanticTag getAuthor();
    
    public ContextPoint getSubSpaceCP();
    
    /**
     * An entry is created and will be submitted afterwards.
     * This methods allows to check whether this entry was already submitted
     */
    public boolean submitted();

    public void setSubmitted();
}
