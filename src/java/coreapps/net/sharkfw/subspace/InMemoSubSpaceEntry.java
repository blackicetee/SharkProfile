package net.sharkfw.subspace;

import net.sharkfw.knowledgeBase.ContextPoint;
import net.sharkfw.knowledgeBase.PeerSemanticTag;

/**
 *
 * @author thsc
 */
public class InMemoSubSpaceEntry extends AbstractSubSpaceEntry implements SubSpaceEntry {
    protected final ContextPoint cp;
    
    public InMemoSubSpaceEntry(ContextPoint cp) {
        super(cp);
        this.cp = cp;
    }
    
    @Override
    public ContextPoint getSubSpaceCP() {
        return this.cp;
    }
    
    public void setSubmitted() {
        this.setSubmitted(true);
    }
    
    private boolean submitted = false;
    public void setSubmitted(boolean submitted) {
        this.submitted = submitted;
    }

    @Override
    public boolean submitted() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
