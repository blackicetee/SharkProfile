package net.sharkfw.subspace;

import java.util.ArrayList;
import java.util.Iterator;
import net.sharkfw.apps.sharknet.SharkNetException;
import net.sharkfw.knowledgeBase.PeerSemanticTag;
import net.sharkfw.knowledgeBase.SemanticTag;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.system.SharkSecurityException;

/**
 *
 * @author thsc
 */
public interface SubSpaceManager {
    public Iterator<SubSpace> getSubSpaces() throws SharkKBException;

    public void invitedToSubSpace(String subSpaceName, 
            String subSpaceSI, String subSpaceType, SemanticTag subSpaceTopic,
            String parentSpaceSI, String childString,
            PeerSemanticTag remoteOriginator, ArrayList<PeerSemanticTag> member, 
            ArrayList<PeerSemanticTag> roMember, boolean subSpaceOpen, 
            boolean readonlyInvitation) 
                throws SharkSubSpaceException, SharkNetException, SharkKBException, SharkSecurityException;
    
    /**
     * Return sub space with a given subject identifier.
     * @param si
     * @return
     * @throws SharkKBException if no sub space with that si exists
     */
    public SubSpace getSubSpace(String si) throws SharkKBException;
}
