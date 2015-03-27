package net.sharkfw.subspace;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import net.sharkfw.knowledgeBase.ContextPoint;
import net.sharkfw.knowledgeBase.Information;
import net.sharkfw.knowledgeBase.Knowledge;
import net.sharkfw.knowledgeBase.PeerSemanticTag;
import net.sharkfw.knowledgeBase.PropertyHolder;
import net.sharkfw.knowledgeBase.SemanticTag;
import net.sharkfw.knowledgeBase.SharkCS;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.peer.J2SEAndroidSharkEngine;
import net.sharkfw.peer.KEPConnection;
import net.sharkfw.system.SharkSecurityException;

/**
 * A sub space is defined by a peer. A sub space has features:
 * 
 * <ul>
 * <li> Each sub space has its own unique SI which is a combination
 * of creators identity and creation time.
 * <li> A creator can introduce other peers participating in the sub space.
 * Peer can be full member or read only member. Full member can add and read
 * information. 
 * <li> A sub space stores its data in a shark knowledge base. This KB has no
 * direct knowledge port - communication is mediated by means of a Sub Space knowledge
 * port. 
 * </ul>
 * 
 * Access to sub spaces is mediated by means of sub space knowledge ports. A
 * sub space KP is automatically created by creating a sub space. It's interest has
 * the following structure:
 * 
 * <ul>
 * <li>Topic is the sub space with it's unique SI.
 * Thus, the sub space topic describes a single entity - the sub space itself.
 * It can piggy-back additional topics, though. That's done with a property, that
 * can include topics. It is up to the sub space kp to use or ignore those piggy-back 
 * topics.
 * <li>Peers comprises all full member including sub space owner which are already 
 * subscribed to the sub space. 
 * <li>Remote peers contains any member (full and r/o) including owner which are already 
 * subscribed to the sub space. 
 * </ul>
 * 
 * A sub space is created by an owner. It defines full and r/o member. 
 * An initial interest is sent to all members. Peer list contains only the owner
 * with this initial interest. Full member get an interest with in / out direction.
 * Readonly member get just a OUT interest - they can only receive data.
 * 
 * Sub spaces can be defined to be open or closed. Open sub spaces accept also
 * peers that are not on the initial member list. Closed sub spaces ignore any
 * attempt from not invited peers joining the sub space.
 * 
 * A peer can join the sub space by sending an interest to sub space owner.
 * That interest contains sub space topic and the peer in peer dimension.
 * Owner will out the peer to peers dimension and will resent the sub space interest
 * to all subscribed member. Thus, any member knows all other member.
 * 
 * Peers can add information to sub space. New information are send to all
 * subscribed peers. 
 * 
 * A peer can unsubscribe by sending the initial interest with direction set
 * to NO_DIRECTION to owner. Owner peer will remove the peer from peers dimension 
 * and resent the interest to all other subscribed peers.
 * 
 * The communication is managed by the sub space knowledge port implementation 
 * in this package. Application developer only need to use the sub space interface
 * or derived classes.
 * 
 * @author thsc
 */
public interface SubSpace extends PropertyHolder {
    public static final String SUBSPACE_URI_DOMAIN = "subSpace://";
    
    // sub space property names
    public static final String SUBSPACE_SUBTYPE = "subSpace_type";
    public static final String SUBSPACE_OPEN = "subSpace_canBeJoined";
    public static final String SUBSPACE_NAME = "subSpace_name";
    public static final String SUBSPACE_CLOSING_TIME = "subSpace_closed";
    public static final String SUBSPACE_CREATION_TIME = "subSpace_created";
    public static final String SUBSPACE_SI = "subSpace_si";
    public static final String SUBSPACE_CHILDREN = "subSpace_children";
    
    // sub space topic property names
    public static final String SUBSPACE_PIGGY_BACK_TOPIC_SET = "subSpace_internalTopic_Set";
    
    // default property settings
    public static boolean DEFAULT_OPEN_SETTING = false;
    public static final String CHILD_SUBSPACE_NAME_DELIMITER = "_isChildSubSpace_";
    public static final String PARENT_SUBSPACE_SI_VALUE = "sharkfw_parentSubSpaceSI";
    
    /**
     * Each sub space has it own identiy which is described by a 
     * semantic tag. This methods returns this tag. This tag
     * <b>does not describe the meaning of this sub space</b>
     * @return
     * @throws SharkKBException 
     */
    public SemanticTag getSubSpaceTag() throws SharkKBException; 
    
    /**
     * Sub spaces can be used to store information regarding a topic.
     * This methode returns the semantic tag representing that topic.
     * It describes the content of this sub space. This semantic tag
     * isn't necessarely unique. Usuually, it won't be unique. There will
     * usually be a number of sub spaces discussing same issues/topics.
     * 
     * Get Topic of this subspace if any. Don't mess up this topic with 
     * subSpace topic. Later represents this actual sub space. The topic represents
     * meaning / topics / semantic of sub space content.
     * 
     * A unique semantic tag describing that subspace are available via
     * {@link #getSubSpaceTopic() getSubSpaceTopic()}.
     * 
     * @see #getSubSpaceTag() getSubSpaceTag()
     * @return
     * @throws SharkKBException 
     */
    public SemanticTag getTopic() throws SharkKBException;
    
    /**
     * Sub spaces can have a type which is an ordinatry string. Dealing
     * with types is only required when implementing a new sub space.
     * @return string naming sub space type
     */
    public String getSubSpaceType();
    
    /**
     * Sets type of this subspace. See also comment in {@link #getSubSpaceType() getSubSpaceType()}
     * @param type 
     */
    public void setSubSpaceType(String type);
    
    /**
     * 
     * @return Owner of this sub space. This isn't necessarily the local peer.
     */
    public PeerSemanticTag getOwner();
    
    /**
     * A unique subject identifier is created during subspace creation. This 
     * SI can be retrived with this methode. This SI is encapsulated into
     * a semantic tag, see {@link #getSubSpaceTag() getSubSpaceTag()}
     * @return 
     */
    public String getSubSpaceSI();
    
    /**
     * A subspace can have a name. Don't assume names to be unique. Subject identifiers
     * are unique, see {@link #getSubSpaceSI() getSubSpaceSI()}
     * @return 
     */
    public String getName();
    
    /**
     * Add a peer as full sub space member. Can only be performed by sub space owner
     * in closed sub spaces.
     * @param peer
     * @return
     * @throws SharkSubSpaceException
     * @throws SharkKBException 
     */
    public PeerSemanticTag addFullMember(PeerSemanticTag peer) throws SharkSubSpaceException, SharkKBException;

    /**
     * Add a peer as read only sub space member. Can only be performed by sub space owner
     * in closed sub spaces.
     * @param peer
     * @return
     * @throws SharkSubSpaceException
     * @throws SharkKBException 
     */
    public PeerSemanticTag addReadonlyMember(PeerSemanticTag peer) throws SharkSubSpaceException, SharkKBException;
    
    /**
     * Set this peer a full sub space member - it can read and write.
     * @param peer 
     * @throws net.sharkfw.subspace.SharkSubSpaceException 
     */
    public void setFullMember(PeerSemanticTag peer) throws SharkSubSpaceException;
    
    /**
     * Set this peer a read only member in this sub space.
     * @param peer 
     * @throws net.sharkfw.subspace.SharkSubSpaceException 
     */
    public void setReadonlyMember(PeerSemanticTag peer) throws SharkSubSpaceException;
    
    public void setLastVisitingTime(long time) throws SharkSubSpaceException;
    
    public long getLastVisitingTime() throws SharkSubSpaceException;
    
    public ArrayList<Information> getNewInformationSinceLastVisit();
    
    /**
     * Sub spaces can be open or closed. Open means: Any peer can subscribe without
     * owner permission. Joining a closed sub space requires sub space owner permission, though.
     * 
     * This method return whether this is an open or closed sub space.
     * 
     * @return 
     */
    public boolean isOpenSpace();
    
    /**
     * Remove chat member
     * @param peer
     * @throws SharkSubSpaceException if not allowed
     * @throws SharkKBException 
     */
    public void removeMember(PeerSemanticTag peer) throws SharkSubSpaceException, SharkKBException;

    /**
     * A dedicated peer is invited by sub space owner. Peer data are merged 
     * into the subspace and  becomes a member. The peer gets an invitation. 
     * This method can only be 
     * performed by subspace owner. 
     * 
     * @param peer
     * @param fullmember
     * @throws SharkSubSpaceException
     * @throws SharkKBException 
     */
    public void inviteMember(PeerSemanticTag peer, boolean fullmember) 
            throws SharkKBException, SharkSubSpaceException;
    
    /**
     * invite all sub space member (if not yet done).
     * @throws SharkSecurityException if peer is not owner and not allowed
     * to invite all member
     * @return true if invitations has been sent successfully
     */
    public boolean inviteAll() throws SharkSecurityException;

    /**
     * Sucessfully invited all participants?
     * @return 
     */
    public boolean allInvited();
    
    /**
     * A peer (non-owner) can tell another peer about this subspace. The calling peer
     * issues a message to the remote peer which describes that sub space.
     * The remote peer can than think about asking for invitation.
     * 
     * @param peer
     * @throws SharkSubSpaceException 
     */
    public void tellPeer(PeerSemanticTag peer) 
            throws SharkSubSpaceException, SharkSubSpaceException;

    /**
     * Removes permanently this sub space - physical removal of all data. 
     * Take care. This subspace will be closed or left before if not yet done.
     */
    public void remove();

    /**
     * Closes this subspace. The subspace still exists but no communication
     * is permitted any longer. Use remove to delete subspace content.
     * This method can only be performed by sub space owner. Closing a subspace
     * is notified to each subscribed peer which is not forced to <i>leave</i> the
     * sub space as well. Peer can decide to clone that subspace and go ahead
     * with their communication, though.
     * 
     * @throws SharkSubSpaceException 
     */
    public void close() throws SharkSubSpaceException;
    
    boolean isClosed();

    /**
     * Subscribe to sub space. This peer issues a message to owner to
     * put it in recipient list. Owner will dissiminate this news to all
     * other subscribers. A knowledge port is created for this sub space.
     * 
     * Nothing happens if this peer is already subscribed.
     * @throws net.sharkfw.subspace.SharkSubSpaceException
     */
    public void subscribe() throws SharkSubSpaceException;

    /**
     * Subscribe and ask for information which where entered in subspace after 
     * given timestamp.
     * 
     * @param from Time stamp - milliseconds from 1.1.1970
     * @param duration
     * @throws SharkSubSpaceException 
     */
    public void subscribe(long from, long duration) throws SharkSubSpaceException;
    
    public void subscribe(long from) throws SharkSubSpaceException;
    
    /**
     * Check whether the peer itself has subscribed to a sub space 
     * (e.g. chat/makan) or not.
     * 
     * @return
     * @throws SharkKBException 
     */
    public boolean isSubscribed() throws SharkKBException;
    
    /**
     * Check if the defined peer has subscribed to a sub space 
     * (e.g. chat/makan) or not.
     * 
     * @param peer
     * @return
     * @throws SharkKBException 
     */
    public boolean isSubscribed(PeerSemanticTag peer) throws SharkKBException;
    
    /**
     * Set this peer to be subscribed to this sub space
     * @param peer
     * @param subscribed
     * @throws SharkKBException 
     */
    public void setSubscribed(PeerSemanticTag peer, boolean subscribed) 
            throws SharkKBException;
    
    /**
     * Unsubscribe to sub space. This peer issues a message to all other 
     * subscibers to remove it from subscriber list. It also shuts down it
     * knowledge port - it won't receive any information regarding that sub space.
     * 
     * Nothing happens if this peer is not subscribed.
     * @throws net.sharkfw.subspace.SharkSubSpaceException
     */
    public void unsubscribe() throws SharkSubSpaceException;
    
    /**
     * List chat member who could participate in the sub space (chat/makan). 
     * They are not
     * necessarily subscribed. Note: This method makes sense for owner.
     * Owner keep also information about not subscribed member.
     * 
     * Non-owner can call this method and get the same list as 
     * by calling subscribedMember
     * 
     * @return
     * @throws SharkKBException 
     */
    public Iterator<PeerSemanticTag> member() throws SharkKBException;
    
    /**
     * List of subscribed peers. Those peer are member but has also
     * committed to participate in the sub space. TODO: use iterator.
     * @return
     * @throws SharkKBException 
     */
    public Enumeration<PeerSemanticTag> subscribedMember() throws SharkKBException;   
    
    /**
     * Add a listener to subspace that gets informed about incomming data.
     * @param listener 
     */
    public void addListener(SubSpaceListener listener);
    
    public void removeListener(SubSpaceListener listener);

//    /**
//     * add a subSpace to this subspace. Child subspaces are unsubscribed if
//     * parent subspace becomes unsubscribed. They share same topic etc.
//     * @param subSpace 
//     * 
//     */
//    public void addChildSubSpace(SubSpace subSpace);
//
//    public void removeChildSubSpace(SubSpace subSpace);
//    
//    public Iterator<SubSpace> getChildSubSpaces();
//    
//    public void removeSubSpaceEntry(SubSpaceEntry entry) throws SharkKBException;
    
    /**
     * This methods synchronizes peer list with externally provided list:
     * 
     * <ul>
     * <li>Peers which are in the chat but not in the list are removed.</li>
     * <li>Peers which are in not the chat but in the list become readonly peers.</li>
     * </ul>
     * 
     * Only chat owner are permitted to call that method.
     * 
     * @param allowedPeersIter 
     * @throws SharkSubSpaceException if calling peer doesn't own that chat.
     */
    public void syncAllowedPeers(Iterator<PeerSemanticTag> allowedPeersIter) throws SharkSubSpaceException;
    
    public J2SEAndroidSharkEngine getSharkEngine();
    
//    public boolean hasChildSubSpace();
//    
//    public boolean hasParentSubSpace();
//    
//    public SubSpace getParentSubSpace() throws SharkSubSpaceException;
//    
//    public ContextPoint createSubSpaceCP() throws SharkKBException;
//
//    public void setParentSubSpace(SubSpace parentSubSpace);
//    
//    public boolean isChildSubSpace();
    
    void doInsert(Knowledge knowledge, KEPConnection response);
    
    void doExpose(SharkCS interest, KEPConnection response);
    
//    public Enumeration<ContextPoint> getAllCPs() throws SharkKBException;

    public void invitePeer(PeerSemanticTag peer) throws SharkKBException, SharkSecurityException, IOException;
}