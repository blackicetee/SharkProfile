package net.sharkfw.subspace;

import java.util.ArrayList;
import java.util.Iterator;
import net.sharkfw.apps.sharknet.SharkNetException;
import net.sharkfw.knowledgeBase.PeerSemanticTag;
import net.sharkfw.knowledgeBase.SemanticTag;
import net.sharkfw.knowledgeBase.SharkCSAlgebra;
import net.sharkfw.knowledgeBase.SharkKB;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.peer.SharkEngine;
import net.sharkfw.system.SharkSecurityException;

/**
 * TODO
 * @author thsc
 */
public class SimpleSubSpaceManager implements SubSpaceManager {
    private final SharkEngine se;
    private final SharkKB baseKB;
    private final SubSpaceGuardKP guardKP;
    private final ArrayList<SubSpace> subspaces;
    
    public SimpleSubSpaceManager(SharkEngine se, SharkKB baseKB) {
        this.se = se;
        this.baseKB = baseKB;
        this.guardKP = new SubSpaceGuardKP(se, this, baseKB);
        
        this.subspaces = new ArrayList();
        
        this.restore();
    }
    
    public void addSubSpace(SubSpace subSpace) {
        this.subspaces.add(subSpace);
        this.persist();
    }
    
    public void removeSubSpace(SubSpace subSpace) {
        this.subspaces.remove(subSpace);
        this.persist();
    }
    
    void persist() {
        // TODO: store subspace description in kb
    }
    
    void restore() {
        // TODO: restrieve and reinstanciate subspaces from kb
    }
    
    @Override
    public Iterator<SubSpace> getSubSpaces() throws SharkKBException {
        return this.subspaces.iterator();
    }
    
    @Override
    public SubSpace getSubSpace(String si) throws SharkKBException {
        Iterator<SubSpace> subSpacesIter = this.getSubSpaces();

        if(subSpacesIter == null) {
            throw new SharkKBException("no such sub space found - sub space list is empty");
        }

        while(subSpacesIter.hasNext()) {
            SubSpace subSpace = subSpacesIter.next();
            if(SharkCSAlgebra.identical(subSpace.getSubSpaceSI(), si)) {
                return subSpace;
            }
        }

        throw new SharkKBException("no sub space found with si: " + si);
    }

    @Override
    public void invitedToSubSpace(String subSpaceName, String subSpaceSI, 
        String subSpaceType, SemanticTag subSpaceTopic, 
        String parentSpaceSI, String childDescription,
        PeerSemanticTag remoteOriginator, 
        ArrayList<PeerSemanticTag> member, 
        ArrayList<PeerSemanticTag> roMember, boolean subSpaceOpen, 
        boolean readonlyInvitation) throws SharkNetException, 
            SharkKBException, SharkSecurityException {
        
        
        // TODO
    }
    
}
