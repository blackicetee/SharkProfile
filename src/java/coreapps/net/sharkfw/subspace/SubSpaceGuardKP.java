package net.sharkfw.subspace;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import net.sharkfw.apps.sharknet.SharkNetException;
import net.sharkfw.knowledgeBase.Knowledge;
import net.sharkfw.knowledgeBase.PeerSTSet;
import net.sharkfw.knowledgeBase.PeerSemanticTag;
import net.sharkfw.knowledgeBase.STSet;
import net.sharkfw.knowledgeBase.SemanticTag;
import net.sharkfw.knowledgeBase.SharkCS;
import net.sharkfw.knowledgeBase.SharkCSAlgebra;
import net.sharkfw.knowledgeBase.SharkKB;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.kp.KPListener;
import net.sharkfw.peer.KEPConnection;
import net.sharkfw.peer.SharkEngine;
import net.sharkfw.peer.StandardKP;
import net.sharkfw.system.L;
import net.sharkfw.system.SharkException;

/**
 * This shark net default kp handles just incomming invitations to new
 * sub spaces (chat / makan). 
 * 
 * This KP doesn't handle insert requests. It handles interests.
 * Interests must indicate an interest in communicating within a sub space.
 * (Checked with subSpace:// prefix). It is than checked
 * 
 * <ul>
 * <li>Is peer willing to enter a cub space by originator of the invitation.
 * <li>Such a sub space doesn't already exist in this peer.
 * </ul>
 * 
 * If so, a new sub space is created locally and a notification is send
 * to shark net engine which shall inform the user in some way. (This is out
 * of scope of this knowledge port, though).
 * 
 * @author thsc
 */
public class SubSpaceGuardKP extends StandardKP {
    private final SubSpaceManager ssm;
    private final SharkKB baseKB;
    
    public SubSpaceGuardKP(SharkEngine se, SubSpaceManager sne, SharkKB kb) {
        super(se, null, kb);
        this.baseKB = kb;
        this.ssm = sne;
    }

    @Override
    protected void doInsert(Knowledge knowledge, KEPConnection response) {
        L.d("Sub space guard insert reached", this);
        try {
            // offer message to each known sub space
            // TODO - can be optimized: maybe be checking wether knowledge was inserted or not

            L.d("iterating existing sub spaces", this);
            Iterator<SubSpace> subSpaces = this.ssm.getSubSpaces();
            
            while (subSpaces.hasNext()) {
                SubSpace subSpace = subSpaces.next();
                L.d("calling doInsert on subspace: " + subSpace.getName(), this);
                subSpace.doInsert(knowledge, response);
            }
            
        } catch (SharkKBException ex) {
            L.d("probleme while handling insert message in guard: " + ex.getMessage(), this);
        }
    }

    @Override
    protected void doExpose(SharkCS interest, KEPConnection response) {
        String ownerName = "unknown";
        if(this.kb != null) {
            PeerSemanticTag owner = this.kb.getOwner();
            ownerName = owner.getName();
        }

        L.d("\n******************************************\n\tGuardKP of " + ownerName + " doExpose\n******************************************\n", this);
        L.d("Received interest\n" + L.contextSpace2String(interest), this);
        
        // direction ok?
        boolean readonlyInvitation = true; // make compiler happy
        
        switch(interest.getDirection()) {
            case SharkCS.DIRECTION_NOTHING : return; // cannot handle unsubscribe notification or whatever it is
            case SharkCS.DIRECTION_IN : return; // we don't support write only subspace (yet)
            case SharkCS.DIRECTION_OUT : readonlyInvitation = true; break; 
            case SharkCS.DIRECTION_INOUT : readonlyInvitation = false; break; 
        }
        
        SemanticTag topic = null;
        
        try {
            // sub Space request?
             topic = interest.getTopics().tags().nextElement();
             if(topic == null) { return;}
        } catch (Exception ex) {
            /* if something goes wrong here - this interest
             * isn't worht beeing handled
             */
            return;
        }
       
        // topic isn't null here
        
        // is it a subspace ?
        if(!AbstractSubSpace.isSubSpaceTag(topic)) {
            L.d("It's not a subSpace - don't handle that request", this);
            return;
        }
        
        /* 
         * Do we like the originator?
         * More formal: Is this peer willing to accept invitations by sending peer?
         */
        
        PeerSemanticTag remoteOriginator = interest.getOriginator();
        
        if(!this.se.isAccepted(remoteOriginator)) {
            // we don't like it
            if(remoteOriginator != null) {
                L.d("Sender is not allowed to send invitations: owner / sender " + this.kb.getOwner().getName() + " / " + remoteOriginator.getName(), this);
            }
            return;
        }
        
        // remote peer is ok.
        
        /* 
         * Maybe - sub space already active
         * Actually, this shouldn't be the case if implementation works fine.
         * But better cross check in this stage of development - this code
         * should / can be removed in stable version.
         */
        
        try {
            L.d("iterating existing sub spaces", this);
            // already activ?
            Iterator<SubSpace> subSpaceIter = this.ssm.getSubSpaces();
            while(subSpaceIter.hasNext()) {
                // debugging - remove those lines later.
                ownerName = "owner not set";
                PeerSemanticTag o = this.kb.getOwner();
                if(o != null) {
                    ownerName = o.getName();
                }
                
                SubSpace subSpace = subSpaceIter.next();
                
                SemanticTag subSpaceTopic = subSpace.getSubSpaceTag();
                
                L.d("check subSpaceTopic; owner / topicSI: " + ownerName + " / " + subSpaceTopic.getSI()[0], this);
                // for debugging only
                Iterator<PeerSemanticTag> subMemberIter = subSpace.member();
                while(subMemberIter.hasNext()) {
                    PeerSemanticTag subMember = subMemberIter.next();
                    
                    boolean subscribed = subSpace.isSubscribed(subMember);
                    
                    String name = subMember.getName();
                    
                    L.d("sub space member: name / is subscribed: " + name + "/" + String.valueOf(subscribed), this);
                    
                }
                // end debugging only code
                
                if(SharkCSAlgebra.identical(subSpaceTopic, topic)) {
                    // already created - should be handled by sub space kp
                    L.d("SN guard KP got request for existing sub space - delegate to sub space", this);
                    subSpace.doExpose(interest, response);
                    return;
                }
            }
            
            // no such chat here
            
        } catch (SharkKBException ex) {
            // that's odd - we cannot proceed if SN is broken
            L.d("SN guard KP.expose(): Stop executing because of failure: " + ex.getMessage(), this);
            return;
        }
        
        // there is no such sub space yet - create one
        
        ///////////////////////////////////////////////////////////////////
        //                       create new subSpace                     //
        ///////////////////////////////////////////////////////////////////
        
        L.d("creating a new subSpace", this);
        
        
        ////////////           Topic           ////////////////////////////        
        
        // split interest into its parts first
        String subSpaceName = topic.getName();
        String subSpaceSI = topic.getSI()[0];
        
        // are there a piggy-packed topics ?
        SemanticTag subSpaceTopic = null;
        String childString = null;
        
        try {
            String topicSetString = topic.getProperty(SubSpace.SUBSPACE_PIGGY_BACK_TOPIC_SET);
            if(topicSetString != null) {
                STSet topicSet = AbstractSubSpace.deserializeSubSpaceTopicSetString(topicSetString);
                if(!topicSet.isEmpty()) {
                    subSpaceTopic = topicSet.tags().nextElement();
                }
            }
            childString = topic.getProperty(SubSpace.SUBSPACE_CHILDREN);
        }
        catch(SharkKBException e) {
            // ignore
        }
        
        
        ////////////           Member           ////////////////////////////        
        
        // owner is the remoteOriginator, see above
        
        // member: full member: peer dim, all member remotePeerDim
        ArrayList<PeerSemanticTag> roMember = new ArrayList<PeerSemanticTag>();
        ArrayList<PeerSemanticTag> member = new ArrayList<PeerSemanticTag>();
        
        PeerSTSet remotePeers = interest.getRemotePeers();
        PeerSTSet peers = interest.getPeers();
        
        // NOTE: peer dimensions are already twisted here.
        
        // fill member first but don't out owner in
        if(remotePeers != null) {
            Enumeration<PeerSemanticTag> remotePeerTags = remotePeers.peerTags();
            if(remotePeerTags != null) {
                while(remotePeerTags.hasMoreElements()) {
                    PeerSemanticTag peer = remotePeerTags.nextElement();
                    
                    // is not owner?
                    if(!SharkCSAlgebra.identical(peer, remoteOriginator)) {
                        member.add(peer);
                    }
                }
            }
        }
        
        // fill ro member now - take any pst in remotePeer but not in peer dim
        if(peers != null) {
            Enumeration<PeerSemanticTag> peerTags = peers.peerTags();
            if(peerTags != null) {
                while(peerTags.hasMoreElements()) {
                    PeerSemanticTag peer = peerTags.nextElement();
                    
                    // is not owner?
                    if(!SharkCSAlgebra.identical(peer, remoteOriginator)) {
                        // already in member list?
                        boolean found = false;
                        
                        Iterator<PeerSemanticTag> memberIter = member.iterator();
                        while(!found && memberIter.hasNext()) {
                            PeerSemanticTag remotePeer = memberIter.next();
                            if(SharkCSAlgebra.identical(peer, remotePeer)) {
                                found = true;
                                break;
                            }
                        }
                        
                        if(!found) {
                            roMember.add(peer);
                        }
                    }
                }
            }
        }
        
        ////////////           Open/Closed           /////////////////////////        
        
        try {
            // open or closed?
            String openValueString = topic.getProperty(SubSpace.SUBSPACE_OPEN);

            // it is false in default
            boolean subSpaceOpen = Boolean.valueOf(openValueString);

            String subSpaceType = topic.getProperty(SubSpace.SUBSPACE_SUBTYPE);

            String parentSpaceSI = topic.getProperty(SubSpace.PARENT_SUBSPACE_SI_VALUE);

            // better creating a thread here?
            L.d("ask SN engine to notify about invitation", this);
            this.ssm.invitedToSubSpace(subSpaceName, subSpaceSI, subSpaceType, 
                    subSpaceTopic, parentSpaceSI, childString, remoteOriginator, member, roMember,
                    subSpaceOpen, readonlyInvitation);
        }
        catch(SharkNetException e){
            L.e("cannot create new sub space (chat/makan) after invitation: " + e.getMessage(), this);
        }
        catch(SharkException see){
            L.e("cannot create new sub space (chat/makan) after invitation: " + see.getMessage(), this);
        }
    }

    /**
     * @param localInterest
     * @param k
     * @param response 
     */
    void doStandardInsert(SharkCS localInterest, SharkKB kb, KPListener listener, Knowledge k, KEPConnection response) {
        /* TODO
         * standard assimilation process should become a static method
         * instead of faking that stuff with this ugly hack.
         */
        this.setKB(kb);
        this.setInterest(localInterest);
        this.addListener(listener);
        super.doInsert(k, response);
        this.removeListener(listener);
        this.setKB(this.baseKB);
        this.setInterest(null);
    }
}
