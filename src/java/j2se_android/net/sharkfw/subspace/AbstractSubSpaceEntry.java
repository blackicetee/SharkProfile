package net.sharkfw.subspace;

import net.sharkfw.knowledgeBase.ContextPoint;
import net.sharkfw.knowledgeBase.PeerSemanticTag;

/**
 *
 * @author thsc
 */
public abstract class AbstractSubSpaceEntry implements SubSpaceEntry {
    protected final ContextPoint cp;
    
    public AbstractSubSpaceEntry(ContextPoint cp) {
        this.cp = cp;
    }
    
    @Override
    public ContextPoint getSubSpaceCP() {
        return this.cp;
    }
    
    @Override
    public PeerSemanticTag getAuthor() {
        if(cp != null) {
            return this.cp.getContextCoordinates().getOriginator();
        }
        
        return null;
    }
}
