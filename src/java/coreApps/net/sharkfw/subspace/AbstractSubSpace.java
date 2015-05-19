package net.sharkfw.subspace;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sharkfw.system.TimeLong;
import net.sharkfw.kep.format.XMLSerializer;
import net.sharkfw.knowledgeBase.ContextCoordinates;
import net.sharkfw.knowledgeBase.ContextPoint;
import net.sharkfw.knowledgeBase.FragmentationParameter;
import net.sharkfw.knowledgeBase.Information;
import net.sharkfw.knowledgeBase.Interest;
import net.sharkfw.knowledgeBase.Knowledge;
import net.sharkfw.knowledgeBase.PeerSTSet;
import net.sharkfw.knowledgeBase.PeerSemanticTag;
import net.sharkfw.knowledgeBase.STSet;
import net.sharkfw.knowledgeBase.SemanticTag;
import net.sharkfw.knowledgeBase.SharkCS;
import net.sharkfw.knowledgeBase.SharkCSAlgebra;
import net.sharkfw.knowledgeBase.SharkKB;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.knowledgeBase.TimeSTSet;
import net.sharkfw.knowledgeBase.TimeSemanticTag;
import net.sharkfw.knowledgeBase.filesystem.FSSharkKB;
import net.sharkfw.knowledgeBase.inmemory.InMemoKnowledge;
import net.sharkfw.knowledgeBase.inmemory.InMemoSharkKB;
import net.sharkfw.kp.KPListener;
import net.sharkfw.peer.J2SEAndroidSharkEngine;
import net.sharkfw.peer.KEPConnection;
import net.sharkfw.peer.KnowledgePort;
// import static net.sharkfw.subspace.SubSpace.PARENT_SUBSPACE_SI_VALUE;
import net.sharkfw.system.Iterator2Enumeration;
import net.sharkfw.system.L;
import net.sharkfw.system.SharkException;
import net.sharkfw.system.SharkSecurityException;
import net.sharkfw.system.Util;

/**
 * @author thsc
 */
abstract public class AbstractSubSpace implements SubSpace, KPListener {
    // sub space related peer property names
    protected static final String SUBSCRIBED_PEER_PREFIX = "subSpace_subscribed";
    protected static final String SUBSPACE_VISITING_TIME_PREFIX = "subSpace_lastVisit";
    protected static final String READONLY_PEER_PREFIX = "subSpace_readonly";
    private final SubSpaceGuardKP guardKP;
    
    public AbstractSubSpace(SubSpaceGuardKP guardKP) throws SharkSubSpaceException {
        if(guardKP == null) {
            throw new SharkSubSpaceException("cannot create sub space without guard KP: guardKP was null");
        }
        this.guardKP = guardKP;
    }
    
    public SubSpaceGuardKP getSubSpaceGuardKP() {
        return this.guardKP;
    }
    
    protected String getPeerSubscribedPropertyName() {
        return SUBSCRIBED_PEER_PREFIX + "_" + this.getSubSpaceSI();
    }

    protected String getPeerLastVisitPropertyName() {
        return SUBSPACE_VISITING_TIME_PREFIX + "_" + this.getSubSpaceSI();
    }

    protected String getPeerReadOnlyPropertyName() {
        return READONLY_PEER_PREFIX + "_" + this.getSubSpaceSI();
    }

    private HashSet<SubSpaceListener> listener = new HashSet<SubSpaceListener>();
    
    @Override
    public void addListener(SubSpaceListener l) {
        this.listener.add(l);
    }
    
    @Override
    public void removeListener(SubSpaceListener l) {
        this.listener.remove(l);
    }

    public final void memberAdded(PeerSemanticTag addedPeer) {
        Iterator<SubSpaceListener> listenerIter = this.listener.iterator();
        while(listenerIter.hasNext()) {
            listenerIter.next().memberAdded(addedPeer);
        }
    }

    public final void memberRemoved(PeerSemanticTag removedPeer) {
        Iterator<SubSpaceListener> listenerIter = this.listener.iterator();
        while(listenerIter.hasNext()) {
            listenerIter.next().memberRemoved(removedPeer);
        }
    }
    
    /**
     * Called if subscriber list has changed.
     * 
     * @param peer 
     */
    public final void subscriberChanged() {
        Iterator<SubSpaceListener> listenerIter = this.listener.iterator();
        while(listenerIter.hasNext()) {
            listenerIter.next().subscriberChanged();
        }
    }
    
    protected void removeSubSpaceEntry(SharkKB kb, AbstractSubSpaceEntry entry) throws SharkKBException {
        kb.removeContextPoint(entry.getSubSpaceCP().getContextCoordinates());
    }
    
    boolean isActiv = true; // sub space is active - means it willing to communicate
    protected void setPassiv() {
        this.isActiv = false;
    }
    
    /////////////////////////////////////////////////////////////////////
    //                      came from SubSpaceKP                       //
    /////////////////////////////////////////////////////////////////////
    
    private SharkKB kb; // TODO init with constructor
    /**
     * Iterate context points and change topics if they fit to this sub space. 
     * Delegate assimilating to standard KP. 
     * 
     * Do nothing otherwise.
     * 
     * @param knowledge
     * @param response 
     */
    @Override
    public void doInsert(Knowledge knowledge, KEPConnection response) {
        L.d("Sub space was called to insert something: " + 
                L.knowledge2String(knowledge.contextPoints()), this);
        
        if(!this.isActiv) {
            L.d("stop processing sub space request because sub space is not activ anymore", this);
            return;
        }
        
        // is this message for this sub space - check it with first cp
        Enumeration<ContextPoint> contextPoints = knowledge.contextPoints();
        ContextPoint cp = null;
        if(contextPoints != null) {
            cp = contextPoints.nextElement();
            
            // each cp in knowledge must have sub space topic in topic dim
            String[] sis = cp.getContextCoordinates().getTopic().getSI();
            
            // does it match with sub space
            if(!SharkCSAlgebra.identical(sis, this.getSubSpaceSIs())) {
                // no - leave it
                return;
            }
        }
        
        // ok - at least first cp is for this sub space
        
        // create a in memo knowledge to fill it with adjusted cps
        SharkKB cpFactory = new InMemoSharkKB();
        
        // use cpFactory as background - it is an empty kb and that what we need here
        Knowledge k = new InMemoKnowledge(cpFactory);
        
        do {
            try {
                SemanticTag topic = null;
                
                // change topic with piggy-pack topic if any
                SemanticTag piggyBackTopic = cp.getContextCoordinates().getTopic();
                String topicSetString = piggyBackTopic.getProperty(SubSpace.SUBSPACE_PIGGY_BACK_TOPIC_SET);
                if(topicSetString != null) {
                    XMLSerializer xs = new XMLSerializer();
                    STSet topicSet = InMemoSharkKB.createInMemoSTSet();
                    xs.deserializeSTSet(topicSet, topicSetString);
                    
                    if(!topicSet.isEmpty()) {
                        topic = topicSet.tags().nextElement();
                    }
                }
                
                ContextCoordinates cpCC = cp.getContextCoordinates();

                ContextCoordinates copyCC = 
                        cpFactory.createContextCoordinates(topic, 
                        cpCC.getOriginator(), cpCC.getPeer(), 
                        cpCC.getRemotePeer(), cpCC.getTime(), 
                        cpCC.getLocation(), cpCC.getDirection());
                
                ContextPoint copyCP = cpFactory.createContextPoint(copyCC);
                
                // copy Information
                Enumeration<Information> enumInformation = cp.enumInformation();
                if(enumInformation != null) {
                    while(enumInformation.hasMoreElements()) {
                        Information i = enumInformation.nextElement();
                        copyCP.addInformation(i); // just add reference - there is nothing copied
                    }
                }
                
                k.addContextPoint(copyCP);
                
            } catch (SharkKBException ex) {
                L.w("failure while processing sub space knowledge port:" + ex.getMessage(), this);
            }
            
            cp = null;

            // so far - are there more fitting cp?
            while(contextPoints.hasMoreElements()) {
                cp = contextPoints.nextElement();

                // does it fit?
                String[] sis = cp.getContextCoordinates().getTopic().getSI();

                // does it match with sub space
                if(SharkCSAlgebra.identical(sis, this.getSubSpaceSIs())) {
                    // no - leave it
                    break;
                } else {
                    cp = null;
                }
            }
        } while(cp != null);

        // ok, we have at least one cp in knowledge copy. 
        
        // delegate rest to standard implementation which also checks all other dimensions beside topics
        
        try {
            this.refreshInsertInterest();
        }
        catch(SharkKBException e) {
            // TODO
        }
        
//        L.d("interest before calling super.doInsert(): \n" + L.contextSpace2String(this.getInterest()), this);
//        L.d("knowledge before calling super.doInsert(): \n" + L.knowledge2String(k.contextPoints()), this);
        
        L.d("sub space has decided to assimilate knowledge - delegate to standard assimilation", this);
        this.guardKP.doStandardInsert(this.localInterest, this.getSubKB(), this, k, response);
    }
    
    /**
     * Check if interest topic describes this sub space. Piggy-back 
     * topics are ignored in this implementation.
     * 
     * Each sub space has an owner. Reaction of this KP depends whether this
     * peer is owner or not.
     * 
     * Note: Message sender must set itself as originator of this interest.
     * 
     * <b>If sender is owner:</b>
     * 
     * <ul>
     * <li> If direction is <i>in</i>: Sender becomes subscriber if it is defined to
     * be full or r/o member in this sub space or if the space is <i>open</i>

     * <li> If direction is <i>in / out</i> or just <i>out</i>: Sender becomes 
     * subscriber if it is defined to be fullmember in this sub space or if the space is <i>open</i>

     * <li> If direction is <i>no</i>: Sender is removed from subscriber list.
     * It is not removed from sender list.
     * </ul>
     * 
     * I any case: Owner informs all other subscribers about changes in subscriber
     * list.

     * <b>If sender is not owner:</b>
     * If message from owner: Drop local member lists and recreate from
     * peer and remote peer list
     * 
     * If message is not from owner: Ignore.
     * 
     * (Apparently, this makes just real sense with signed messages.)
     * 
     * @param interest
     * @param response 
     */
    @Override
    public void doExpose(SharkCS interest, KEPConnection response) {
        if(!this.isActiv) {
            L.d("stop processing sub space request because sub space is not activ anymore", this);
            return;
        }
        
        try {
            STSet subTopics = this.getSubSpaceTopics();
            
            STSet remoteTopics = interest.getTopics();
            
            STSet fragment = InMemoSharkKB.createInMemoSTSet();
            
            // is there a match ?
            SharkCSAlgebra.contextualize(fragment, remoteTopics, subTopics.tags());
            
            if(!fragment.isEmpty()) {
                // it fits
                // get sender
                PeerSemanticTag sender = interest.getOriginator();

                if(this.isOwnSubSpace()) {
                    L.d("got a request regarding my own subSpace - going to handle request", this);
                    
                //////////////////////////////////////////////////////////////
                //                        own sub space                     //
                //////////////////////////////////////////////////////////////
                    
                    // find local sender description
                    PeerSemanticTag localSender = 
                            this.getSubKB().getPeerSemanticTag(sender.getSI());

                    // localSender can be null here

                    // handle open sub space
                    if(this.isOpenSpace()) {
                        // debug messages
                        String senderName = "null Sender - shouldn't be!!!";
                        if(localSender != null) {
                            senderName = localSender.getName();
                        }
                        
                        if(interest.getDirection() != SharkCS.DIRECTION_NOTHING) {
                            L.d("subSpace is open - add and subscribe member: " + senderName, this);
                            // make it a full member - it will also be merged in
                            this.addFullMember(localSender);

                            // make is subscriber
                            this.setSubscribed(localSender, true);
                            
                            // send old content
//                            Enumeration<TimeSemanticTag> timeTags = interest.getTimes().timeTags();
//                            if(timeTags != null) {
//                                TimeSemanticTag tst = timeTags.nextElement();
//                                
//                                long now = System.currentTimeMillis();
//                                long creationTime = this.getCreationTime();
//                                
//                                // check if tst fits into life time of this sub space
//                                if(SharkCSAlgebra.isIn(tst, creationTime, now-creationTime)) {
//                                    this.sendExistingContent(localSender, tst);
//                                }
//                            }
                        } else {
                            L.d("subSpace is open - unsubscribe member: " + senderName, this);
                            // remove as subscriber - leave as peer
                            if(localSender != null) {
                                this.setSubscribed(localSender, false);
                            }
                        }
                    }

                    // handle closed sub space - that is what we use e.g. in Salto and SharkNet 1.0
                    else {
                        if(localSender == null) {
                            L.d("subSpace is closed but sender is null - do nothing", this);
                            // we don't know this peer - bye
                            return;
                        }

                        switch(interest.getDirection()) {
                            case SharkCS.DIRECTION_NOTHING: 
                                // remove as subscriber
                                L.d("unsubscribe member: " + localSender.getName(), this);
                                this.setSubscribed(localSender, false);
                                break;

                            case SharkCS.DIRECTION_OUT: 
                            case SharkCS.DIRECTION_INOUT: 
                                // full member?
                                if(this.isReadOnlyPeer(localSender)) {
                                    // just r/o member - don't go ahead
                                    return;
                                }

                            case SharkCS.DIRECTION_IN: 
                                /* no further checks required:
                                 * peer is known in sub space: 
                                 * It is at least r/o peer: add it
                                 */
                                L.d("subscribe member: " + localSender.getName(), this);
                                this.setSubscribed(localSender, true);
                                break;
                        }
                    }

                    /* inform all subscribed member about change in subscriber list
                     * send message also to sender - other might have subscribed 
                     * while sender considered whether to subscribe or not
                     */
                    L.d("Send update to all subscribed member", this);
//                    this.sendUpdateToAll(localSender, true);
                    this.sendUpdateToAll(null, true);
                    
                } else {
                    L.d("this is not my own subSpace", this);
                
                //////////////////////////////////////////////////////////////
                //                    not own sub space                     //
                //////////////////////////////////////////////////////////////

                    // message from owner?
                    if(SharkCSAlgebra.identical(sender, this.getOwner())) {
                        L.d("setting up again subSpace with owner message", this);
                        // reset and recreate member list
                        this.setupMemberListByOwnerMessage(sender, interest);
                    }
                    
                    // message is not from owner and I'm not owner: do nothing
                    
                } // end handling not owned subspace
                
                // if owner or not - in any case adjust interest
                this.refreshInsertInterest();
                
            } // interest fits to this sub space
            
        } catch (Exception ex) {
            L.w("failure while processing sub space knowledge port:" + ex.getMessage(), this);
        }
    }
    
    private Interest invitationInterest = null;
    
    private void refreshInvitationInterest() throws SharkKBException {
        if(!SharkCSAlgebra.identical(this.getOriginator(), 
                this.getLocalPeer())) {
            
            throw new SharkKBException("invitation can only be created by sub space owner");
        }
        
        Interest i = InMemoSharkKB.createInMemoInterest();
        
        // these topics describe the subscpace including piggy-packed real topics
        i.setTopics(this.getSubSpaceTopics());
        
        // invitation is made by owner
        i.setOriginator(this.getOriginator());
        
        /* peers dimensions include only subscribed member
         * peers are comprised of full member and owner
         * remote peer are all subscribed member including owner
         */
        PeerSTSet peers = InMemoSharkKB.createInMemoPeerSTSet();
        PeerSTSet remotePeers = InMemoSharkKB.createInMemoPeerSTSet();
        
        peers.merge(this.getOriginator());
        remotePeers.merge(this.getOriginator());
        
        Iterator<PeerSemanticTag> subscribedMemberIter = this.getSubscribedMember();
        while (subscribedMemberIter.hasNext()) {
            PeerSemanticTag subscribedMember = subscribedMemberIter.next();
            if (this.isReadOnlyPeer(subscribedMember)) {
                remotePeers.merge(subscribedMember);
            } else {
                peers.merge(subscribedMember);
                remotePeers.merge(subscribedMember);
            }
        }
        
        i.setPeers(peers);
        i.setRemotePeers(remotePeers);
        
        // Time from creation until closing time
        long created = this.getCreationTime();
        long closing = this.getClosingTime();
        long duration = closing == TimeSemanticTag.FOREVER ? 
                TimeSemanticTag.FOREVER : closing - created;
        
        
        TimeSTSet time = InMemoSharkKB.createInMemoTimeSTSet();
        time.createTimeSemanticTag(created, duration);
        
        i.setTimes(time);
        
        i.setDirection(SharkCS.DIRECTION_INOUT); // just for full member
        
        this.invitationInterest = i;
    }

    private void refreshInsertInterest() throws SharkKBException {
        Interest i = InMemoSharkKB.createInMemoInterest();
        
        // accept any topic within this sub topic
        i.setTopics(this.getSubSpaceTopics());
        
        // in principle anybody can do something in that sub space
        i.setOriginator(null);
        
        /* peers dimensions include only subscribed member
         * peers are comprised of full member and owner
         * remote peer are all subscribed member including owner
         */
        PeerSTSet peers = InMemoSharkKB.createInMemoPeerSTSet();
        PeerSTSet remotePeers = InMemoSharkKB.createInMemoPeerSTSet();
        
        // This peer is just a single peer
        peers.merge(this.getLocalPeer());
        
        // all peers in subspace
        remotePeers.merge(this.getOriginator());
        
        Iterator<PeerSemanticTag> subscribedMemberIter = this.getSubscribedMember();
        while (subscribedMemberIter.hasNext()) {
            remotePeers.merge(subscribedMemberIter.next());
        }
        
        i.setPeers(peers);
        i.setRemotePeers(remotePeers);
        
        // Time from creation until closing time
        long created = this.getCreationTime();
        long closing = this.getClosingTime();
        long duration = closing == TimeSemanticTag.FOREVER ? 
                TimeSemanticTag.FOREVER : closing - created;
        
        
        TimeSTSet time = InMemoSharkKB.createInMemoTimeSTSet();
        time.createTimeSemanticTag(created, duration);
        
        i.setTimes(time);
        
        i.setDirection(SharkCS.DIRECTION_IN); // just for full member
        
        this.setInterest(i);
    }
    
    Interest localInterest;
    
    private void setInterest(Interest i) {
        this.localInterest = i;
    }

    public void sendInviteAll() throws SharkKBException, SharkSecurityException, IOException {
        this.sendUpdateToAll(null, false);
    }
    /**
     * Send a member list to (nearly all member)
     * 
     * @param exceptPeer don't send to this peer
     * @param toSubscribed send only to subscribed peers or only to not subscribed peers
     * @throws SharkKBException 
     */
    public void sendUpdateToAll(PeerSemanticTag exceptPeer, boolean toSubscribed) 
            throws SharkKBException, SharkSecurityException, IOException {
        
        this.refreshInvitationInterest();
        
        Iterator<PeerSemanticTag> member = this.getReadonlyMember();
        if(member != null) {
            this.invitationInterest.setDirection(SharkCS.DIRECTION_OUT);
            this.invitePeers(this.invitationInterest, member, 
                    this.getOwner(), exceptPeer, toSubscribed);        
        }
        
        
        member = this.getFullMember();
        if(member != null) {
            this.invitationInterest.setDirection(SharkCS.DIRECTION_INOUT);
            this.invitePeers(this.invitationInterest, member, 
                    this.getOwner(), exceptPeer, toSubscribed);        
        }
    }
    
    private void invitePeers(SharkCS interest, 
            Iterator<PeerSemanticTag> recipients, PeerSemanticTag owner, 
            PeerSemanticTag except, boolean toSubscribed) throws SharkSecurityException, SharkKBException, IOException {
        
        if(recipients == null) {
            return;
        }
        
//        this.invitationInterest.setDirection(SharkCS.DIRECTION_INOUT);
        while(recipients.hasNext()) {
            PeerSemanticTag recipient = recipients.next();
            
            // don't send to owner: it knows the lists
            if(owner != null && SharkCSAlgebra.identical(owner, recipient)) {
                continue;
            } 
            
            // not to omit
            if(except != null && SharkCSAlgebra.identical(except, recipient)) {
                continue;
            } 
            
            if(this.isSubscribed(recipient)) {
                // subscribed
                if(!toSubscribed) {
                    continue;
                }
            } else {
                // not subscribed
                if(toSubscribed) {
                    continue;
                }
            }
            
            L.d("send interest: sender / recipient" + this.getLocalPeer().getName() + "/" + recipient.getName(), this);
            this.guardKP.sendInterest(interest, recipient);
        }
    }
    
    public void invitePeer(PeerSemanticTag peer) throws SharkKBException, SharkSecurityException, IOException {
        if(SharkCSAlgebra.identical(peer, this.getOwner())) {
            throw new SharkKBException("Inviting sub space owner is useless.");
        }
        
        this.refreshInvitationInterest();
        
        if(this.isReadOnlyPeer(peer)) {
            this.invitationInterest.setDirection(SharkCS.DIRECTION_OUT);
        } else {
            this.invitationInterest.setDirection(SharkCS.DIRECTION_INOUT);
        }
        
        this.guardKP.sendInterest(this.invitationInterest, peer);
                
        if(this.hasChildSubSpace()) {
            Iterator<SubSpace> childIter = this.getChildSubSpaces();
            while(childIter.hasNext()) {
                SubSpace child = childIter.next();
                child.invitePeer(peer);
            }
        }
        
    }

    public void sendSubscribe(boolean readonly) throws SharkKBException, SharkSecurityException, IOException {
        this.sendSubscribe(readonly, -1, -1);
    }
    
    /**
     * Sends a subscribe message to sub space owner.
     * @param readonly
     * @param from
     * @param duration
     * @throws SharkKBException
     * @throws SharkSecurityException 
     * @throws java.io.IOException 
     */
    public void sendSubscribe(boolean readonly, long from, long duration) throws SharkKBException, SharkSecurityException, IOException {
        // expose interest in subscribing this sub space to sub space owner
        
        /* interest has this structure:
         * topic: sub space topic
         * peer: this peer
         * originator: this peer
         * time/location irrelevant
         * direction: depending on what peer was invented
         */
        
        SemanticTag topic = this.getSubSpaceTag();
        PeerSemanticTag peer = this.getLocalPeer();

        int direction = readonly ? SharkCS.DIRECTION_IN : SharkCS.DIRECTION_INOUT;
        
        TimeSemanticTag tst = null;
        if(from >= 0) {
            tst = InMemoSharkKB.createInMemoTimeSemanticTag(from, duration);
        }
        
        // create interest
        SharkCS subscriptionInterest = InMemoSharkKB.createInMemoContextCoordinates(
                topic, peer, peer, null, tst, null, direction);
        
        L.d("try to send subscribe message>>>>>>>>>>>>>>>>>", this);
        // send to owner
        this.guardKP.sendInterest(subscriptionInterest, this.getOwner());
        L.d("end try sending subscribe message<<<<<<<<<<<<<<<<", this);
    }

    public void sendUnsubscribe() throws SharkKBException, SharkSecurityException {
        SemanticTag topic = this.getSubSpaceTag();
        PeerSemanticTag peer = this.getLocalPeer();

        int direction = SharkCS.DIRECTION_NOTHING;
        
        // create interest
        SharkCS unsubscriptionInterest = InMemoSharkKB.createInMemoContextCoordinates(
                topic, peer, peer, null, null, null, direction);
        
        // send to owner
        L.d("try sending subscribe message>>>>>>>>>>>>>>>>>>", this);
        try {
            guardKP.sendInterest(unsubscriptionInterest, this.getOwner());
        } catch (IOException ex) {
            throw new SharkKBException(ex.getMessage());
        }
        L.d("end try sending subscribe message<<<<<<<<<<<<<<<", this);
    }
    
    /**
     * Send an interest in which just sub space topic and originator is set. 
     * Thus, there are not a single invited peer which means that sub space is 
     * closed.
     * @throws SharkKBException
     * @throws SharkSecurityException 
     */
    public void sendCloseToSubscribedMember() throws SharkKBException, SharkSecurityException, IOException {
        if(!this.isOwnSubSpace()) {
            throw new SharkSecurityException("SubSpace closing is only permitted to owner");
        }
        
        SemanticTag topic = this.getSubSpaceTag();
        int direction = SharkCS.DIRECTION_NOTHING;
        
        // create interest
        SharkCS closingInterest = InMemoSharkKB.createInMemoContextCoordinates(
                topic, this.getOwner(), null, null, null, null, direction);
        
        Iterator<PeerSemanticTag> subscribedMember = this.getSubscribedMember();
        while(subscribedMember.hasNext()) {
            PeerSemanticTag peer = subscribedMember.next();
            try {
                // not to myself
                if(!SharkCSAlgebra.identical(this.getLocalPeer(), peer)) {
                    this.guardKP.sendInterest(closingInterest, peer);
                }
            }
            catch(SharkException e) {
                L.d("couldn't send closing message to peer: " + e.getMessage() , this);
            }
        }
    }
    
    public void sendInterest(SharkCS interest, PeerSemanticTag recipient) throws SharkSecurityException, SharkKBException {
        try { 
            guardKP.sendInterest(interest, recipient);
        } catch (IOException ex) {
            // remember unsend message
            this.getSharkEngine().rememberUnsentInterest(interest, recipient);
        }
    }
    
    public void sendKnowledge(Knowledge k, PeerSemanticTag recipient) throws SharkSecurityException, SharkKBException {
        try {
            this.guardKP.sendKnowledge(k, recipient);
        } catch (IOException ex) {
            // remember unsend knowledge
            this.getSharkEngine().rememberUnsentKnowledge(k, recipient);
        }
    }    
    
    /////////////////////////////////////////////////////////////////////////
    //                          from FSSubSpace                            //
    /////////////////////////////////////////////////////////////////////////
    
    protected SharkKB subKB;
    protected SharkKB baseKB;
    protected PeerSemanticTag peer;
    protected String foldername;
    
    protected J2SEAndroidSharkEngine se;
    protected static String SUBSPACE_ALL_INVITED = "SubSpace_AllInvited";
    
    protected String getRealChildName(String childName) {
        
        int index = childName.indexOf(SubSpace.CHILD_SUBSPACE_NAME_DELIMITER);
        if(index > 0) {
            return childName.substring(0, index);
        }
        
        return childName;
    }
    
    
    protected boolean inviteAllSucessfully = false; // assume nobody has been invited
    
    public boolean allInvited() {
        return this.inviteAllSucessfully;
    }
    
    private static final String CHILD_STRING_DELIMITER = "|";
    
    /**
     * Send invitation to any potential member.
     * Make just sense, if this sub space is owned
     */
    @Override
    public synchronized boolean inviteAll() throws SharkSecurityException {
        
        if(this.inviteAllSucessfully) return true;
        
        if(!this.isOwnSubSpace()) {
            throw new SharkSecurityException("only subspace owner are allowed to call inviteAll");
        }
        
        try {
            this.sendInviteAll();
            // no exception thrown
            this.inviteAllSucessfully = true;

            // remember that success
            this.setProperty(SUBSPACE_ALL_INVITED, Boolean.toString(true), false);
            
            if(this.hasChildSubSpace()) {
                Iterator<SubSpace> childIter = this.getChildSubSpaces();
                while(childIter.hasNext()) {
                    SubSpace child = childIter.next();
                    child.inviteAll();
                }
            }
            
            return true;
        }
        catch(Exception skbe) {
            // couldn't invite
            L.l("couldn't send invite message: " + skbe.getMessage(), this);
            return false;
        } 
    }
    
    protected void reinviteAll() throws SharkSecurityException {
    	this.inviteAllSucessfully = false;
        this.inviteAll();
    }
    
    /**
     * Checks whether at least one subject identifier describes a subspace.
     * 
     * @param topic semantic tag describing a topic
     * @return True if at least one SI describes a sub space. False otherwise.
     */
    public static boolean isSubSpaceTag(SemanticTag topic) {
        if(topic == null) {
            return false;
        }
        
        String[] sis = topic.getSI();
        
        if(sis == null) {
            return false;
        }
        
        for(String si : sis ) {
            if(si.startsWith(SubSpace.SUBSPACE_URI_DOMAIN)) {
                return true;
            }
        }
        
        return false;
    }
    
    protected void setupNewSubSpace(J2SEAndroidSharkEngine se, String name, 
                        String subSpaceSI,
                        FSSharkKB subKB,
                        PeerSemanticTag owner, 
                        PeerSemanticTag peer, 
                        String foldername,
                        SharkKB baseKB, boolean open, 
                        String subSpaceType) throws SharkSubSpaceException, SharkKBException 
    {
        this.se = se;
        
        this.subKB = subKB;
//        this.subKB.addListener(this);
        
        // remember owner
        this.subKB.setOwner(owner);
        
        this.peer = peer;
        
        this.foldername = foldername;

        // remember base KB
        this.baseKB = baseKB;
        
        // remember opening time
        long now = System.currentTimeMillis();
        this.subKB.setProperty(SubSpace.SUBSPACE_CREATION_TIME, 
                Long.toString(now));
        
        // remember name
        this.subKB.setProperty(SubSpace.SUBSPACE_NAME, name);
        
        // set initial visit
        this.setLastVisitingTime(now);
        
        // remember open
        this.subKB.setProperty(SubSpace.SUBSPACE_OPEN, Boolean.toString(open));
        
        if(subSpaceSI == null) {
            subSpaceSI = this.createSubSpaceSI();
        }
        
        this.setSubSpaceSI(subSpaceSI);
        
        this.subKB.setProperty(SubSpace.SUBSPACE_SUBTYPE, subSpaceType);
        
        this.subKB.persist();
        
        // save context space
        
//        this.persistSubSpaceContext();
        
//        this.setupEngine();
        
//        this.setupKBListener();
    }

    private String createSubSpaceSI() throws SharkKBException {
        // create sub space si
        StringBuilder sb = new StringBuilder();
        sb.append(SubSpace.SUBSPACE_URI_DOMAIN);
        
        PeerSemanticTag originator = this.getOriginator();
        if(originator != null) {
            // append one creator SI
            String[] origSIs = originator.getSI();
            if(origSIs != null) {
                sb.append(origSIs[0]);
            } else {
                String name = originator.getName();
                if(name != null) {
                    sb.append(name);
                } else {
                    sb.append(this.getName());
                }
            }
        } else {
            sb.append(this.getName());
        }
        
        sb.append(":");
        
        sb.append(Long.toString(System.currentTimeMillis()));
        
        return sb.toString();
    }
    
    private void setSubSpaceSI(String si) throws SharkKBException {
        this.subKB.setProperty(SubSpace.SUBSPACE_SI, si);
    }
    
    @Override
    public String getSubSpaceSI() {
        try {
            return this.subKB.getProperty(SubSpace.SUBSPACE_SI);
        } catch (SharkKBException ex) {
            L.e("cannot find subspace SI", this);
            return null;
        }
    }
    
    private STSet subSpaceTopics = null;
    
    public long getCreationTime() throws SharkKBException {
        String value = this.subKB.getProperty(SubSpace.SUBSPACE_CREATION_TIME);
        if(value == null) {
            throw new SharkKBException("creation time not set");
        }

        long time = TimeLong.parse(value);
        
        return time;
    }

    /**
     * time when this space was closed or -1 if still open
     * @return 
     */
    public long getClosingTime() throws SharkKBException {
        String value = this.subKB.getProperty(SubSpace.SUBSPACE_CLOSING_TIME);
        if(value == null) {
            return TimeSemanticTag.FOREVER;
        }

        long time = TimeLong.parse(value);
        
        return time;
    }
    
    public boolean subSpaceClosed() throws SharkKBException {
        return this.getClosingTime() != TimeSemanticTag.FOREVER;
    }
    
    @Override
    public String getName () {
        try {
            return this.subKB.getProperty(SubSpace.SUBSPACE_NAME);
        } catch (SharkKBException ex) {
            L.e("cannot find sub name SI", this);
            return null;
        }
    }
    
    public String[] getSubSpaceSIs() {
        String[] sis = new String[] { this.getSubSpaceSI() };
        return sis;
    }
    
    @Override
    public boolean isClosed() {
        String value;
        try {
            value = this.subKB.getProperty(SubSpace.SUBSPACE_CLOSING_TIME);
        } catch (SharkKBException ex) {
            L.e("cannot find out if subspace is closed", this);
            return true;

        }
        return value != null;
    }
    
    protected void setSubSpaceClosed() throws SharkKBException {
        if(!this.subSpaceClosed()) {
             long closingTime = System.currentTimeMillis();
            
            // save
            this.subKB.setProperty(SubSpace.SUBSPACE_CLOSING_TIME, 
                    Long.toString(closingTime));
        }
    }

    @Override
    public final PeerSemanticTag getOwner() {
        return this.subKB.getOwner();
    }
    
    @Override
    public void remove() {}
    
    public SharkKB getSubKB() {
        return this.subKB;
    }
    
    public SharkKB getBaseKB() {
        return this.baseKB;
    }
    
    @Override
    public J2SEAndroidSharkEngine getSharkEngine() {
        return this.se;
    }
    
    @Override
    public SemanticTag getTopic() throws SharkKBException {
        if(!this.subKB.getTopicSTSet().isEmpty()) {
            return this.subKB.getTopicSTSet().tags().nextElement();
        }
        
        return null;
    }
    
    public final PeerSemanticTag getLocalPeer() {
        return this.peer;
    }
    
    public STSet topics() throws SharkKBException {
        return this.subKB.getTopicSTSet();
    }

    public void removeTopic(SemanticTag st) throws SharkKBException {
        this.subKB.getTopicSTSet().removeSemanticTag(st);
    }

    public void addTopic(SemanticTag st) throws SharkKBException {
        this.subKB.getTopicSTSet().merge(st);
    }
    
    @Override
    public final void setLastVisitingTime(long time) throws SharkSubSpaceException {
        this.subKB.setSystemProperty(
                this.getPeerLastVisitPropertyName(),
                Long.toString(time));
    }

    @Override
    public long getLastVisitingTime() throws SharkSubSpaceException {
        String value = this.subKB.getSystemProperty(this.getPeerLastVisitPropertyName());
        if(value == null) {
            throw new SharkSubSpaceException("last visiting time not set");
        }

        long time = TimeLong.parse(value);
        
        return time;
    }
    
    public boolean isReadOnlyPeer(PeerSemanticTag pTag) {
        String valueString = pTag.getSystemProperty(this.getPeerReadOnlyPropertyName());

        if(valueString != null) {
            return Boolean.parseBoolean(valueString);
        }
        
        return false;
    }
    
    @Override
    public boolean isSubscribed() {
        return this.isSubscribed(this.getLocalPeer());
    }
    
    @Override
    public boolean isSubscribed(PeerSemanticTag pTag) {
        String propertyName = this.getPeerSubscribedPropertyName();
        String valueString = pTag.getSystemProperty(propertyName);

        if(valueString != null) {
            return Boolean.parseBoolean(valueString);
        }
        
        return false;
    }
    
    @Override
    public void setFullMember(PeerSemanticTag peer) {
        this.setReadOnlyMember(peer, false);
    }
    
    @Override
    public void setReadonlyMember(PeerSemanticTag peer) {
        this.setReadOnlyMember(peer, true);
    }
    
    // TODO: sub space and child spaces should actually share the list and no duplicates
    protected void setReadOnlyMember(SemanticTag pTag, boolean ro) {
        pTag.setSystemProperty(this.getPeerReadOnlyPropertyName(), Boolean.toString(ro));
        Iterator<SubSpace> childIter = this.getChildSubSpaces();
        while(childIter.hasNext()) {
            SubSpace child = childIter.next();
            if(child instanceof AbstractSubSpace) {
                AbstractSubSpace subSpaceChild = (AbstractSubSpace) child;
                subSpaceChild.setReadOnlyMember(pTag, ro);
            }
        }
    }

    /**
     * Sets peer subscribed. No commmunication is triggered by calling this method.
     * 
     * @param peer
     * @param subscribe
     * @throws SharkKBException 
     */
    @Override
    public final void setSubscribed(PeerSemanticTag peer, boolean subscribe) throws SharkKBException {
        L.d("setSubscribe: localPeer/who/what: " + this.getLocalPeer().getName() + "/" + peer.getName() + "/" + Boolean.toString(subscribe), this);
        PeerSemanticTag pTag = this.subKB.getPeerSemanticTag(peer.getSI());
        
        if(pTag == null) {
            // does not exist - add
            this.subKB.getPeerSTSet().merge(peer);
        }        
        
        boolean isSubscribed = this.isSubscribed(pTag);
        pTag.setSystemProperty(this.getPeerSubscribedPropertyName(), Boolean.toString(subscribe));
    }
    
    @Override
    public void removeMember(PeerSemanticTag peer) throws SharkKBException, SharkSubSpaceException {
        this.removeSubSpaceSettings(peer);
        
        if(this.isOwnSubSpace()) {
            try {
                // notify others about removing that member
                this.sendUpdateToAll(null, true);
            } catch (Exception ex) {
                throw new SharkKBException(ex.getMessage());
            }
        }
    }
    
    private void removeSubSpaceSettings(PeerSemanticTag peer) {
        // remove ro property
        peer.setSystemProperty(this.getPeerLastVisitPropertyName(), null);
        peer.setSystemProperty(this.getPeerReadOnlyPropertyName(), null);
        peer.setSystemProperty(this.getPeerSubscribedPropertyName(), null);
    }

    /**
     * Add full member to this sub space - not communication between sub space
     * peers is triggered. Subspace listener is informed.
     * 
     * @param peer
     * @return
     * @throws SharkSubSpaceException
     * @throws SharkKBException 
     */
    @Override
    public PeerSemanticTag addFullMember(PeerSemanticTag peer) throws SharkSubSpaceException, SharkKBException {
        return this.addMember(peer, false);
    }

    /**
     * Add readonly member to this sub space - not communication between sub space
     * peers is triggered. Subspace listener is informed.
     * 
     * @param peer
     * @return
     * @throws SharkSubSpaceException
     * @throws SharkKBException 
     */
    @Override
    public PeerSemanticTag addReadonlyMember(PeerSemanticTag peer) throws SharkSubSpaceException, SharkKBException {
        return this.addMember(peer, true);
    }

    /**
     * Add a peer as member (either readonly or not) to this sub space. The peer 
     * will be merged into sub space if it isn't part of its peer dimension. 
     * Peer status is changed if peer exists but has readonly status.
     * 
     * Subspace listener gets informed about that fact. No communication takes
     * place between sub space member
     *
     * @param peer
     * @param readonly
     * @return
     * @throws SharkSubSpaceException
     * @throws SharkKBException
     */
    public PeerSemanticTag addMember(PeerSemanticTag peer, boolean readonly) throws SharkSubSpaceException, SharkKBException {
        if(peer == null) {
            throw new SharkSubSpaceException("peer that shall be added must not be null");
        }
        
        PeerSemanticTag newMember = this.subKB.getPeerSemanticTag(peer.getSI());
        
        if(newMember == null) {
            // does not exist - add            
            newMember = (PeerSemanticTag) this.subKB.getPeerSTSet().merge(peer);
            L.d("Added new member " + newMember.getName() + " to list.", peer);
            L.d("Readonly is set to: " + Boolean.toString(readonly), this);
            this.setReadOnlyMember(newMember, readonly);
            return newMember;
            
        } else {
            // does already exist
            //if(!this.isReadOnlyPeer(newMember)) {
                this.setReadOnlyMember(newMember, readonly);
                L.d("Peer " + newMember.getName() + " is known.", this);
                L.d("Readonly is set to: " + Boolean.toString(readonly), this);
            //} else {
                // else - nothing todo
                //L.d("Peer " + newMember.getName() + " is read only.", peer);
            //}
        }
        
        this.memberAdded(newMember);
        return newMember;
    }

    /**
     * Return iteration of readonly member of this sub space
     * @return
     * @throws SharkKBException 
     */
    public Iterator<PeerSemanticTag> getReadonlyMember() throws SharkKBException {
        return this.getMember(true);
    }
    
    /**
     * Return iteration of full member of this sub space
     * @return
     * @throws SharkKBException 
     */
    public Iterator<PeerSemanticTag> getFullMember() throws SharkKBException {
        return this.getMember(false);
    }
    
    /**
     * Return iteration of member - either readonly or full member
     * @param readonly
     * @return
     * @throws SharkKBException 
     */
    private Iterator<PeerSemanticTag> getMember(boolean readonly) throws SharkKBException {
        ArrayList<PeerSemanticTag> member = new ArrayList<PeerSemanticTag>();
        Enumeration<PeerSemanticTag> peerTags = this.subKB.getPeerSTSet().peerTags();
        if(peerTags != null) {
            while(peerTags.hasMoreElements()) {
                PeerSemanticTag pst = peerTags.nextElement();
                
                // get ro property
                String valueString = pst.getSystemProperty(this.getPeerReadOnlyPropertyName());
                
                // there must be a value or this peer is not member
                if(valueString != null) {
                    if(this.isReadOnlyPeer(pst)) {
                        if(readonly) {
                            member.add(pst);
                        }
                    } else {
                        if(!readonly) {
                            member.add(pst);
                        }
                    }
                }
            }
        }
        
        return member.iterator();
    }
    
    /**
     * Returns an iterator of all sub space member full as well as readonly member
     * @return
     * @throws SharkKBException 
     */
    @Override
    public Iterator<PeerSemanticTag> member() throws SharkKBException {
        ArrayList<PeerSemanticTag> member = new ArrayList<PeerSemanticTag>();
        Enumeration<PeerSemanticTag> peerTags = this.subKB.getPeerSTSet().peerTags();
        if(peerTags != null) {
            while(peerTags.hasMoreElements()) {
                PeerSemanticTag pst = peerTags.nextElement();
                
                // get ro property
                String valueString = pst.getSystemProperty(this.getPeerReadOnlyPropertyName());
                
                // there must be a value or this peer is not member
                if(valueString != null) {
                    member.add(pst);
                }
            }
        }
        return member.iterator();
    }

    /**
     * Add member and send invitation.
     * @param peer
     * @param fullMember
     * @throws SharkSubSpaceException
     * @throws SharkKBException 
     */
    @Override
    public void inviteMember(PeerSemanticTag peer, boolean fullMember) throws SharkSubSpaceException, SharkKBException, SharkSubSpaceException {
        PeerSemanticTag member = this.addMember(peer, !fullMember);
        
        try {
            this.invitePeer(member);
        } catch (Exception ex) {
            throw new SharkSubSpaceException(ex.getMessage());
        }
    }
    
    /**
     * Not yet implemented: Shall tell another peer about this sub space.
     * @param peer
     * @throws SharkSubSpaceException
     * @throws SharkKBException 
     */
    @Override
    public void tellPeer(PeerSemanticTag peer) 
            throws SharkSubSpaceException, SharkSubSpaceException {
        // TODO
    }
    
    /////////////////////////////////////////////////////////////////////////
    //                      subscribing / unsubscribing                    //
    /////////////////////////////////////////////////////////////////////////
    
    /**
     * Returns an iteration of all subsribed member of this subspace.
     * 
     * @return
     * @throws SharkKBException 
     */
    @Override
    public Enumeration<PeerSemanticTag> subscribedMember()  throws SharkKBException {
        return new Iterator2Enumeration(this.getSubscribedMember());
    }
    
    @Override
    public void subscribe() throws SharkSubSpaceException {
        this.subscribe(System.currentTimeMillis(), TimeSemanticTag.FOREVER);
    }

    @Override
    public void subscribe(long from) throws SharkSubSpaceException {
        this.subscribe(from, TimeSemanticTag.FOREVER);
    }
    
    /**
     * Is to be called by the local peer if it wat's to join that
     * sub space. This methode sets status to subscribe and sends a 
     * message to subspace owner - if local peer is not owner.
     * 
     * A sub space KP is created and started that handels further 
     * communication.
     * 
     * Nothing happens if this peer is already subscribed or an
     * communication error is detected.
     * 
     * @param duration
     * @throws SharkSubSpaceException 
     */
    @Override
    public void subscribe(long from, long duration) throws SharkSubSpaceException {
        if(this.isSubscribed()) {
            // already subscribed
            return;
        }
        
        try {
            this.setSubscribed(this.getLocalPeer(), true);
            
            // if not owner - notity owner
            if(!this.isOwnSubSpace()) {
                try {
                    this.sendSubscribe(
                            this.isReadOnlyPeer(this.getLocalPeer()),
                            from, duration);
                } catch (Exception ex) {
                    throw new SharkSubSpaceException(ex.getMessage());
                }
            }
            
            // subscribe child sub spaces if any
            Iterator<SubSpace> childIter = this.getChildSubSpaces();
            while(childIter.hasNext()) {
                SubSpace childSubSpace = childIter.next();
                if(!childSubSpace.isSubscribed()) {
                    childSubSpace.subscribe();
                }
            }
            
        } catch (SharkKBException ex) {
            throw new SharkSubSpaceException(ex.getMessage());
        }
        
    }
    
    @Override
    public void unsubscribe() throws SharkSubSpaceException {
        try {
            this.setSubscribed(this.getLocalPeer(), false);
//            if(this.subSpaceEngine != null) {
                if(!this.isOwnSubSpace()) {
                    try {
                        this.sendUnsubscribe();
                    } catch (SharkSecurityException ex) {
                        throw new SharkSubSpaceException(ex.getMessage());
                    }
                }
                this.setPassiv();
//                this.subSpaceEngine.stop();
//                this.subSpaceEngine = null;
//            }
        } catch (SharkKBException ex) {
            throw new SharkSubSpaceException(ex.getMessage());
        }
    }
    
    public Iterator<PeerSemanticTag> getSubscribedMember() throws SharkKBException {
        ArrayList<PeerSemanticTag> member = new ArrayList<PeerSemanticTag>();
        
        Enumeration<PeerSemanticTag> peerTags = this.subKB.getPeerSTSet().peerTags();
        if(peerTags != null) {
            while(peerTags.hasMoreElements()) {
                PeerSemanticTag pst = peerTags.nextElement();
                L.d("Checking: " + pst.getName(), this);
                L.d("isSubscribed: " + Boolean.toString(this.isSubscribed(pst)), this);
                L.d("isReadOnlyPeer: " + Boolean.toString(this.isReadOnlyPeer(pst)), this);
                if(this.isSubscribed(pst) ) {
                    member.add(pst);
                    L.d("Added: " + pst.getName(), this);
                }
            }
        }
        
        return member.iterator();
    }
    
//    public void remove() {}

    @Override
    public void close() throws SharkSubSpaceException {
        if(!this.isOwnSubSpace()) {
            throw new SharkSubSpaceException("closing not permitted - it is not own subspace");
        }
        // first insubscribe each member
        try {
            try {
                // notify all subscribers about closing that sub space
                this.sendCloseToSubscribedMember();
            } catch (Exception ex) {
                throw new SharkSubSpaceException(ex.getMessage());
            }
            
            // remove peers from sub space
            Iterator<PeerSemanticTag> member = this.member();
            while(member.hasNext()) {
                /* call remove space setting - remove peer would try to 
                 * inform other peers about removal - that not necessary here
                 */
                this.removeSubSpaceSettings(member.next());
            }
        }
        catch(SharkKBException e) {
            throw new SharkSubSpaceException(e.getMessage());
        }

        try {
            // closed
            this.setSubSpaceClosed();
        } catch (SharkKBException ex) {
            L.e("cannot set subspace closed", this);
        }
        
        // remove kp
//        this.subSpaceEngine.stop();
        
        this.setPassiv();
    }

//    public void leave() throws SharkSubSpaceException {
//        if(this.isOwnSubSpace()) {
//            this.close();
//            return;
//        }
//        
//        this.unsubscribe();
//    }
    
    /** 
     * TODO: send message to owner inviting that peer
     * used by salto?? it is not used by SharkNet
     * defined in Makan interface
     * @param toInvite
     */
    public void askToInvite(PeerSemanticTag toInvite) {
    }

    /** 
     * TODO: send message to owner inviting that peer
     * used by salto?? it is not used by SharkNet
     * defined in Makan interface
     * @param published
     */
    public void setPublished(boolean published) {
        // TODO: expose this sub space
    }

    @Override
    public boolean isOpenSpace() {
        try {
            return Boolean.parseBoolean(this.subKB.getProperty(SubSpace.SUBSPACE_OPEN));
        } catch (SharkKBException ex) {
            L.e("don't know if subspace is open or not", this);
            return false;
        }
    }

    public PeerSemanticTag getOriginator() throws SharkKBException {
        return this.getOwner();
    }
    
    private String tmpInfoFolderName = null;
    private File tmpInfoFolder = null;
    
    public Enumeration<ContextPoint> subSpaceContextPoints() throws SharkKBException {
        return this.subKB.getAllContextPoints();
    }

    public ContextPoint createSubSpaceCP(SemanticTag topic) throws SharkKBException, SharkSubSpaceException {
        TimeSemanticTag tst = 
                InMemoSharkKB.createInMemoTimeSTSet().createTimeSemanticTag(
                System.currentTimeMillis(), TimeSemanticTag.FOREVER);
        
        ContextCoordinates cc = InMemoSharkKB.createInMemoContextCoordinates(
                topic /* no topic inside subspace */, this.getLocalPeer(), 
                this.getLocalPeer(), null, tst, null, SharkCS.DIRECTION_INOUT);
        
        ContextPoint cp = this.subKB.createContextPoint(cc);
        
        return cp;
    }
    
    @Override
    public ContextPoint createSubSpaceCP() throws SharkKBException {
        try {
            ContextPoint cp = this.createSubSpaceCP(null);
            return cp;
        }
        catch(SharkSubSpaceException sne) {
            throw new SharkKBException(sne.getMessage());
        }
    }
    
    public ArrayList<Information> getNewInformationSinceLastVisit() {
    	
    	long t0 = 0;
    	try {
    		t0 = this.getLastVisitingTime();
    	}
    	catch(SharkSubSpaceException sse) {
    		return null;
    	}
    	ArrayList<Information> result = new ArrayList<Information>();
		try {
			Enumeration<ContextPoint> ecp = this.subKB.getAllContextPoints();
			if (ecp != null) {
				while (ecp.hasMoreElements()) {
					ContextPoint cp = ecp.nextElement();
					Enumeration<Information> ei = cp.enumInformation();
					while (ei.hasMoreElements()) {
						Information info = ei.nextElement();
						long t1 = info.creationTime();
						if (t1 > t0) {
							result.add(info);
						}
					}	
	    		}
	    	}
	    	return result;
		} catch (SharkKBException e) {
			e.printStackTrace();
			L.d("getNewInformationSinceLastVisit():", this);
		}
        return null;
    }

    private SemanticTag subSpaceTopic = null;
    
    @Override
    public SemanticTag getSubSpaceTag() throws SharkKBException {
        if(this.subSpaceTopic == null) {
            STSet topics = this.getSubSpaceTopics();
            
            Enumeration<SemanticTag> tagEnum = topics.tags();
            this.subSpaceTopic = tagEnum.nextElement();
        } 
        
        return this.subSpaceTopic;
    }
    
    private String serializeSubSpaceTopicSet() throws SharkKBException {
        STSet topics = this.subKB.getTopicSTSet();
        
        StringBuilder buf = new StringBuilder();
        
        if(topics != null && !topics.isEmpty()) {
            buf.append("[set]");
            Enumeration<SemanticTag> tags = topics.tags();
            while(tags.hasMoreElements()) {
                SemanticTag topic = tags.nextElement();
                
                buf.append("[n]");
                buf.append(topic.getName());
                buf.append("[/n]");
                
                String[] sis = topic.getSI();
                if(sis != null) {
                    buf.append("[sis]");
                    buf.append(Util.array2string(sis));
                    buf.append("[/sis]");
                }
            }
            buf.append("[/set]");
        }
        
        return buf.toString();
    }
    
    public static STSet deserializeSubSpaceTopicSetString(String s) throws SharkKBException {
        STSet set = InMemoSharkKB.createInMemoSTSet();
        
        if(s != null) {
            int start = s.indexOf("[set]");
            start += "[set]".length();
            
            if(start != -1) {
                int end = s.indexOf("[/set]", start);
                
                String tString = s.substring(start, end);
                
                end = 0;
                while(start != -1) {
                    start = tString.indexOf("[n]", end);
                    if(start == -1) {
                        break;
                    }

                    start += "[n]".length();
                    
                    end = tString.indexOf("[/n]", start);
                    
                    String name = tString.substring(start, end);
                    String sis[] = null;
                    
                    start = tString.indexOf("[sis]", end);
                    start += "[sis]".length();
                    if(start != -1) {
                        end = tString.indexOf("[/sis]", start);
                        
                        String sisString = tString.substring(start, end);
                        
                        sis = Util.string2array(sisString);
                    }
                    
                    // add tag
                    set.createSemanticTag(name, sis);
                }
            }
        }
        
        return set;
    }
    
    private String getChildDescriptionString() {
        // child sub spaces?
        Vector<String> childDescriptions = new Vector<String>();

        Iterator<SubSpace> childSubSpaces = this.getChildSubSpaces();
        while(childSubSpaces != null && childSubSpaces.hasNext()) {
            SubSpace child = childSubSpaces.next();
            String childName = child.getName();
            childDescriptions.add(childName);

            String childSI = child.getSubSpaceSI();
            childDescriptions.add(childSI);
        }

            if(!childDescriptions.isEmpty()) {
                String childString = Util.vector2String(childDescriptions, CHILD_STRING_DELIMITER);
                return childString;
            }
            
            return null;
    }
    
    protected Vector<String> getChildDescription(String descriptionString) {
        if(descriptionString != null) {
            return Util.string2Vector(descriptionString, CHILD_STRING_DELIMITER);
        }
        
        return null;
    }
    
    public STSet getSubSpaceTopics() throws SharkKBException {
        if(this.subSpaceTopics == null) {
            this.subSpaceTopics = InMemoSharkKB.createInMemoSTSet();
            
            this.subSpaceTopic = 
                    this.subSpaceTopics.createSemanticTag(this.getName(), 
                    this.getSubSpaceSI());
            
            this.subSpaceTopic.setProperty(SubSpace.SUBSPACE_SUBTYPE, 
                    this.getSubSpaceType());
            
            String childString = this.getChildDescriptionString();
            if(childString != null) {
                this.subSpaceTopic.setProperty(SubSpace.SUBSPACE_CHILDREN, childString);
            }
            
            STSet topics = this.subKB.getTopicSTSet();
            if(topics != null) {
                String topicsString = this.serializeSubSpaceTopicSet();
                this.subSpaceTopic.setProperty(SubSpace.SUBSPACE_PIGGY_BACK_TOPIC_SET, topicsString);
            }
            
            if(this.hasParentSubSpace()) {
                try {
                    String si = this.getParentSubSpace().getSubSpaceSI();
                    this.subSpaceTopic.setProperty(PARENT_SUBSPACE_SI_VALUE, si);
                } catch (SharkSubSpaceException ex) {
                    // ignore
                }
            }
        }
        
        return this.subSpaceTopics;
    }

    public boolean isOwnSubSpace() {
        return SharkCSAlgebra.identical(this.getLocalPeer(), this.getOwner());
    }

    public void setupMemberListByOwnerMessage(PeerSemanticTag owner, SharkCS interest) throws SharkKBException, SharkSubSpaceException {
        L.d("setting up subspace by owner message. peer/owner: " + this.getLocalPeer().getName() + "/" + owner.getName(), this);
        
        // first: clean up peers
        STSet peerSet = this.getSubKB().getPeerSTSet();
        Enumeration<SemanticTag> tagEnum = peerSet.tags();
        
        if(tagEnum != null) {
            while(tagEnum.hasMoreElements()) {
                // remove all properties
//                this.setSubscribed((PeerSemanticTag)tagEnum.nextElement(), false);
                PeerSemanticTag member = (PeerSemanticTag) tagEnum.nextElement();
                this.removeSubSpaceSettings(member);
            }
        }
        
        // was this sub space closed?
        PeerSTSet fullMemberSet = interest.getPeers();
        PeerSTSet roMemberSet = interest.getRemotePeers();
        
        if( (fullMemberSet == null || fullMemberSet.isEmpty()) 
            && 
            (roMemberSet == null || roMemberSet.isEmpty())
           ) {
            // it is closed
            this.setSubSpaceClosed();
            return;
        }
        
        // recreate peer dimension

        // merge owner first
        PeerSemanticTag localOwner = this.addFullMember(owner);
        this.setSubscribed(localOwner, true);
        
        // r/o member
        Enumeration<PeerSemanticTag> pEnum = roMemberSet.peerTags();
        if(pEnum != null) {
            while(pEnum.hasMoreElements()) {
                PeerSemanticTag roMember = pEnum.nextElement();

                L.d("setting up subspace: add roMember: " + roMember.getName(), this);
                PeerSemanticTag localMember = this.addReadonlyMember(roMember);
                this.setSubscribed(localMember, true);
            }
        }
        
        // add full member
        pEnum = fullMemberSet.peerTags();
        if(pEnum != null) {
            while(pEnum.hasMoreElements()) {
                PeerSemanticTag fullMember = pEnum.nextElement();

                L.d("setting up subspace: add fullmember: " + fullMember.getName(), this);
                
                PeerSemanticTag localMember = this.addFullMember(fullMember);
                this.setSubscribed(localMember, true);
            }
        }
        
        this.subscriberChanged();
    }

    /**
     * This method must be called by implementation deriving from sub space
     * as soon as a new information is added and it shall be propagated to
     * subscribed users.
     * @param cp 
     */
    protected void publishToSubscribers(ContextPoint cp) throws SharkKBException, SharkSecurityException {
        SemanticTag copyTopic;

//        if(this.subSpaceEngine == null) {
//        	this.setupEngine();
//        }
        
        ContextCoordinates cpCC = cp.getContextCoordinates();
        SemanticTag cpTopic = cpCC.getTopic();
        
        if(cpTopic != null) {
            // piggy-back topic
            
            // copy tag first
            copyTopic = InMemoSharkKB.createInMemoCopy(this.getSubSpaceTag());
//            copyTopic.setProperty(SubSpace.SUBSPACE_PIGGY_BACK_TOPIC_NAME, cpTopic.getName());
//            copyTopic.setProperty(SubSpace.SUBSPACE_PIGGY_BACK_TOPIC_SET, Util.array2string(cpTopic.getSI()));
            
        } else { // no piggy-packing - nothing todo
            copyTopic = this.getSubSpaceTag();
        }

        ContextCoordinates copyCC = 
                InMemoSharkKB.createInMemoContextCoordinates(
                copyTopic, cpCC.getOriginator(), cpCC.getPeer(), 
                cpCC.getRemotePeer(), cpCC.getTime(), 
                cpCC.getLocation(), cpCC.getDirection());

        SharkKB cpFactory = new InMemoSharkKB();
        ContextPoint copyCP = cpFactory.createContextPoint(copyCC);

        // add information
        Enumeration<Information> enumInformation = cp.enumInformation();
        if(enumInformation == null) {
            return;
        }

        while(enumInformation.hasMoreElements()) {
            copyCP.addInformation(enumInformation.nextElement());
        }

        Knowledge k = InMemoSharkKB.createInMemoKnowledge(null);
        k.addContextPoint(copyCP);

        // iterate subscriber and send them this knowledge
        Iterator<PeerSemanticTag> subscribedMember = this.getSubscribedMember();
        if(subscribedMember == null) {
            L.d("No subscribers to send to.", this);
            return;
        }
        
        while(subscribedMember.hasNext()) {
            PeerSemanticTag subscriber = subscribedMember.next();
            L.d("testing subscriber: " + subscriber.getName(), this);
            // is this peer ? no message loop
            if(!SharkCSAlgebra.identical(subscriber, this.getLocalPeer())) {
                L.d("Published to subscriber: " + subscriber.getName(), this);
//                if (subSpaceEngine == null) {
//                    L.d("SubSpaceKP was null");
//                    this.subSpaceEngine = new SubSpaceEngine(this.se, this);
//                    this.subSpaceEngine.addListener(this);
//                }
                this.sendKnowledge(k, subscriber);
            } else {
                L.d("I am " + subscriber.getName() + ", avoid sending message to myself.", this);
            }
        }
    }
    
    /**
     * Methode is called if a new context point has received the subspace
     * from remote peer.
     * 
     * @param cp 
     */
    public void cpReached(ContextPoint cp) {}
    
    /**
     * Called if a member was added
     *
     * @param addedPeer the value of addedPeer 
     */
    
    @Override
    public String getSubSpaceType() {
        try {
            return this.subKB.getProperty(SubSpace.SUBSPACE_SUBTYPE);
        } catch (SharkKBException ex) {
            L.e("cannot find subspace type", this);
            return null;
        }
    }
    
    @Override
    public void setSubSpaceType(String type) {
        try {
            this.subKB.setProperty(SubSpace.SUBSPACE_SUBTYPE, type);
        } catch (SharkKBException ex) {
            L.e("cannot set subspace type", this);
        }
    }
    
//    private void setupKBListener() {
//        this.subKB.addListener(this);
//    }
    
    ////////////////////////////////////////////////////////////////////////
    //                            kp listener                             //
    ////////////////////////////////////////////////////////////////////////

    @Override
    public void exposeSent (KnowledgePort kp, SharkCS sentMutualInterest) {
        // don't care
    }

    @Override
    public void insertSent(KnowledgePort kp, Knowledge sentKnowledge) {
        // don't care
    }

    @Override
    public void knowledgeAssimilated(KnowledgePort kp, ContextPoint cp) {
        /* distinguish to event source
         * a) this might happen due to assimilation of remote information
         * b) due to user using this peer.
         * We are interested only in b) because any sn peer is due to send
         * its own new information to any other peer
         */
        
        ContextCoordinates cc = cp.getContextCoordinates();
        L.d("subSpace: contextPointAdded", this);
        
        // this cp made by local peer
        PeerSemanticTag originator = cc.getOriginator();
        
        if(SharkCSAlgebra.identical(originator, this.getLocalPeer())) {
            L.d("locally added cp recognized - don't do anything - wait until local user has finished adding data", this);
//            try {
//                 this.notifyRemoteSubscribedPeers(cp);
//            } catch (SharkKBException ex) {
//                L.w("failure while handling cp added event: " + ex.getMessage(), this);
//            }
        } else {
            // this cp has be added from remote
            L.d("remotely added cp recognized - inform local peer", this);
            this.cpReached(cp);
        }
    }
    
    ////////////////////////////////////////////////////////////////////
    //                   property holder delegate                     //
    ////////////////////////////////////////////////////////////////////
    
    @Override
    public void setProperty(String name, String value) throws SharkKBException {
        this.subKB.setProperty(name, value);
    }

    @Override
    public String getProperty(String name) throws SharkKBException {
        return this.subKB.getProperty(name);
    }
    
    @Override
    public void removeProperty(String name) throws SharkKBException {
        this.subKB.setProperty(name, null);
    }
    
    @Override
    public void setProperty(String name, String value, boolean transfer) throws SharkKBException {
        this.subKB.setProperty(name, value, transfer);
    }

    @Override
    public Enumeration<String> propertyNames() throws SharkKBException {
        return this.subKB.propertyNames();
    }

    @Override
    public Enumeration<String> propertyNames(boolean all) throws SharkKBException {
        return this.subKB.propertyNames(all);
    }
    
    ////////////////////////////////////////////////////////////////////
    //                       child spaces                             //
    ////////////////////////////////////////////////////////////////////
    
    HashSet<SubSpace> childSubSpaces = new HashSet<SubSpace>();
    
    /**
     * add a subSpace to this subspace. Child subspaces are unsubscribed if
     * parent subspace becomes unsubscribed. They share same topic etc.
     * @param subSpace 
     * @deprecated will be removed when sub space becomes part of SharkFW 3.0.
     * Child sub space creation will than be performed by parent sub space itself.
     * 
     */
    @Override
    public void addChildSubSpace(SubSpace subSpace) {
        this.childSubSpaces.add(subSpace);
        subSpace.setParentSubSpace(this);
    }
    
    private SubSpace parentSubSpace;
    
    @Override
    public void setParentSubSpace(SubSpace subSpace) {
        this.parentSubSpace = subSpace;
        try {
            this.setProperty(PARENT_SUBSPACE_SI_VALUE, subSpace.getSubSpaceSI());
        } catch (SharkKBException ex) {
            L.e("cannot save parent subspace sis - fatal", this);
        }
    }
    
    @Override
    public Iterator<SubSpace> getChildSubSpaces() {
        return this.childSubSpaces.iterator();
    }
    
    @Override
    public void removeChildSubSpace(SubSpace subSpace) {
        this.childSubSpaces.remove(subSpace);
    }
    
    @Override
    public void removeSubSpaceEntry(SubSpaceEntry entry) throws SharkKBException {
        if(entry instanceof AbstractSubSpaceEntry) {
            AbstractSubSpaceEntry aEntry = (AbstractSubSpaceEntry) entry;
            
            this.removeSubSpaceEntry(this.subKB, aEntry);
        }
    }    
    
    @Override
    public void syncAllowedPeers(Iterator<PeerSemanticTag> allowedPeersIter) 
            throws SharkSubSpaceException {
        
        L.d("start syncAllowedPeers", this);
        
        if(!this.isOwnSubSpace()) {
            throw new SharkSubSpaceException("Method must only be called by owner of this sub space");
        }
        
        ArrayList<PeerSemanticTag> allowedPeers = null;
        if( (allowedPeersIter != null) && allowedPeersIter.hasNext()) {
            // make a copy of this allowed peer list
            allowedPeers = new ArrayList<PeerSemanticTag>();
            while(allowedPeersIter.hasNext()) {
                allowedPeers.add(allowedPeersIter.next());
            }
        } else {
            return;
        }
        
        ArrayList<PeerSemanticTag> toRemove = new ArrayList<PeerSemanticTag>();
        
        try {
            // iterate member
            Iterator<PeerSemanticTag> memberIter = this.member();
            while(memberIter.hasNext()) {
                PeerSemanticTag member = memberIter.next();
                
                if(SharkCSAlgebra.identical(this.getOwner(), member)) {
                    // never change owner
                    continue;
                }
                
                boolean inList = false;
                // in list?
                Iterator<PeerSemanticTag> allowedIter = allowedPeers.iterator();
                while(!inList && allowedIter.hasNext()) {
                    PeerSemanticTag allowedPeer = allowedIter.next();
                    if(SharkCSAlgebra.identical(member, allowedPeer)) {
                        inList = true;
                        
                        // remove allowed peer - it was found
                        allowedPeers.remove(allowedPeer);
                    }
                }
                
                if(!inList) {
                    toRemove.add(member);
                }
            }
            
            boolean changed = false;

            // remove old member
            Iterator<PeerSemanticTag> toRemoveIter = toRemove.iterator();
            while(toRemoveIter.hasNext()) {
                PeerSemanticTag toRemovePeer = toRemoveIter.next();
                L.d("remove peer because it is no longer allowed: " + toRemovePeer, this);
                this.removeSubSpaceSettings(toRemovePeer);
                
                // something has changed
                changed = true;
            }
            
            // add new member - they are the left peer in list
            Iterator<PeerSemanticTag> allowedIter = allowedPeers.iterator();
            while(allowedIter.hasNext()) {
                PeerSemanticTag allowedPeer = allowedIter.next();
                L.d("add peer because it is allowed now: " + allowedPeer.getName(), this);
                this.addReadonlyMember(allowedPeer);
                
                // something has changed
                changed = true;
            }
            
            if(changed) {
                L.d("something has changed - inviteAll()", this);
                this.sendInviteAll();
            }
        } catch (Exception ex) {
            // couldn't send message - revert changes
            L.d("reset peer lists", this);
            
            // reset newly added peers
            Iterator<PeerSemanticTag> allowedIter = allowedPeers.iterator();
            while(allowedIter.hasNext()) {
                PeerSemanticTag allowedPeer = allowedIter.next();
                
                L.d("re-withdraw peer due to communication failure: " + allowedPeer.getName(), this);
                this.removeSubSpaceSettings(allowedPeer);
            }
            
            // re-add old
            Iterator<PeerSemanticTag> toRemoveIter = toRemove.iterator();
            while(toRemoveIter.hasNext()) {
                try {
                    this.addReadonlyMember(toRemoveIter.next());
                }
                catch(SharkKBException e) {
                    // ignore
                }
            }
        }
    }    
    
    @Override
    public boolean hasChildSubSpace() {
         return (this.childSubSpaces != null && !this.childSubSpaces.isEmpty());
    }
    
    @Override
    public boolean hasParentSubSpace() {
        try {
            if(this.getParentSubSpace() != null) {
                return true;
            }
        }
        catch(SharkException se) {
        }
        
        return false;
    }
    
    @Override
    public boolean isChildSubSpace() {
        return this.hasParentSubSpace();
    }
    
    @Override
    public SubSpace getParentSubSpace() throws SharkSubSpaceException {
        if(this.parentSubSpace != null) {
            return this.parentSubSpace;
        }
        
        try {
            String value = this.getProperty(PARENT_SUBSPACE_SI_VALUE);
        } catch (SharkKBException ex) {
            L.e("cannot get parent subspace sis", this);
            return null;

        }
        
        // TODO
        throw new SharkSubSpaceException("not yet implemented");
    }

    /**
     * Reimplement that method - it is most time consuming
     * @param recipient
     * @param tst
     * @throws SharkKBException 
     */
    private void sendExistingContent(PeerSemanticTag recipient, TimeSemanticTag tst) throws SharkKBException, SharkSecurityException {
        SharkKB tmpKB = new InMemoSharkKB();

        // get whole knowledge
        Knowledge k = SharkCSAlgebra.extract(
                tmpKB,
                subKB,
                InMemoSharkKB.getAnyCoordinates(),
                FragmentationParameter.getZeroFPs(),
                false, 
                recipient);
        
        this.sendKnowledge(k, recipient);
    }
    
    public Enumeration<ContextPoint> getAllCPs() throws SharkKBException {
    	return subKB.getAllContextPoints();
    }
    
}
