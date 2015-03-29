package net.sharkfw.apps.sharknet.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import net.sharkfw.apps.sharknet.SharkNetChat;
import net.sharkfw.apps.sharknet.SharkNetChatEntry;
import net.sharkfw.apps.sharknet.SharkNetChatListener;
import net.sharkfw.apps.sharknet.SharkNetException;
import net.sharkfw.knowledgeBase.*;
import net.sharkfw.peer.J2SEAndroidSharkEngine;
import net.sharkfw.peer.KEPConnection;
import net.sharkfw.subspace.SharkSubSpaceException;
import net.sharkfw.subspace.SubSpace;
import net.sharkfw.subspace.SubSpaceGuardKP;
import net.sharkfw.subspace.SubSpaceListener;
import net.sharkfw.system.SharkSecurityException;

/**
 * Each chat has its own kb but they share peers and locations with peers general kb. There is a system wide topic and time dimension that is only used for chats (and maybe later makan)
 *
 * @author thsc
 */
public class SharkNetChat_Impl implements SharkNetChat {

    private SharkNetEngine sharkNet;
    private InMemoSubSpace subSpace;

    /**
     * refresh chat from file system.
     *
     * @param guard
     * @param sharkNet
     * @param foldername
     * @param baseKB
     * @param peer
     * @throws SharkKBException
     * @throws net.sharkfw.subspace.SharkSubSpaceException
     */
    protected SharkNetChat_Impl(SubSpaceGuardKP guard, SharkNetEngine sharkNet, String foldername,
            SharkKB baseKB, PeerSemanticTag peer) throws SharkKBException, SharkSubSpaceException {

  //      super(guard);
//        super(guard, sharkNet, foldername, baseKB, peer);
        this.sharkNet = sharkNet;
    }

    /**
     * Creating a new stand alone chat
     *
     * @param guard
     * @param sharkNet
     * @param name
     * @param chatSI
     * @param topics
     * @param owner
     * @param peer
     * @param member
     * @param roMember
     * @param open
     * @param readonly
     * @param foldername
     * @param baseKB
     * @throws net.sharkfw.subspace.SharkSubSpaceException
     * @throws SharkKBException
     * @throws net.sharkfw.system.SharkSecurityException
     */
    protected SharkNetChat_Impl(SubSpaceGuardKP guard,
            SharkNetEngine sharkNet, String name,
            String chatSI,
            Iterator<SemanticTag> topics,
            PeerSemanticTag owner,
            PeerSemanticTag peer,
            Iterator<PeerSemanticTag> member,
            Iterator<PeerSemanticTag> roMember,
            boolean open,
            boolean readonly,
            String foldername,
            SharkKB baseKB) throws SharkSubSpaceException, SharkKBException, SharkSecurityException {

//        super(guard);
//        super(guard, sharkNet, name, chatSI, topics, owner, peer, member, roMember, open, readonly, foldername, baseKB);
        this.sharkNet = sharkNet;

//        this.inviteAll();
    }
    
    
            
    public SharkNetChat_Impl(SubSpace subSpace) throws SharkSubSpaceException {
        
        this.subSpace = (InMemoSubSpace) subSpace; // TODO
        
    }
            
    public SharkNetChat_Impl(SubSpaceGuardKP guard, SharkNetEngine sharkNet, SharkKB baseKB, String name, SemanticTag topic, 
        Iterator<PeerSemanticTag> participants) throws SharkSubSpaceException {
        
        // create subSpace
        this.subSpace = new InMemoSubSpace(guard);
        
        // TODO setup subSpace

        this.sharkNet = sharkNet;
        
//        this.inviteAll();
    }            

    public SharkNetChatEntry addTextMessage(String message) throws SharkNetException, SharkKBException {
        ContextPoint informationCarrier = this.subSpace.createSubSpaceCP();
        SharkNetChatEntry newEntry = new SharkNetChatEntry_Impl(informationCarrier, message);
        try {
            // inform other peers
            this.subSpace.publishToSubscribers(informationCarrier);
        } catch (SharkSecurityException ex) {
            throw new SharkNetException(ex.getMessage());
        }
        return newEntry;
    }

    public SharkNetChatEntry addInformation(ContextPoint cp, Information info) throws SharkNetException, SharkKBException {
        ContextPoint informationCarrier = this.subSpace.createSubSpaceCP();
        SharkNetChatEntry newEntry = new SharkNetChatEntry_Impl(this.subSpace.createSubSpaceCP(), cp, info);
        try {
            // inform other peers
            this.subSpace.publishToSubscribers(informationCarrier);
        } catch (SharkSecurityException ex) {
            throw new SharkNetException(ex.getMessage());
        }
        return newEntry;
    }

    public SharkNetChatEntry addSemanticTag(SemanticTag tag) throws SharkNetException, SharkKBException {
        ContextPoint informationCarrier = this.subSpace.createSubSpaceCP();
        SharkNetChatEntry newEntry = new SharkNetChatEntry_Impl(this.subSpace.createSubSpaceCP(), tag);
        try {
            // inform other peers
            this.subSpace.publishToSubscribers(informationCarrier);
        } catch (SharkSecurityException ex) {
            throw new SharkNetException(ex.getMessage());
        }
        return newEntry;
    }

    ///////////////////////////////////////////////////////////////////////
    //             notifying about entries from remote peers             //
    ///////////////////////////////////////////////////////////////////////
    private ArrayList<SharkNetChatListener> chatListener = new ArrayList();

    @Override
    public void addListener(SharkNetChatListener listener) {
        this.chatListener.add(listener);
    }

    @Override
    public void removeListener(SharkNetChatListener listener) {
        this.chatListener.remove(listener);
    }

    /**
     * A new context point has been added to this sub space.
     *
     * @param cp
     */
    public void cpReached(ContextPoint cp) {
        // wrap cp into ChatEntry and notify listener

        if (!this.chatListener.isEmpty()) {
            SharkNetChatEntry_Impl chatEntry
                    = new SharkNetChatEntry_Impl(cp, this.subSpace.getBaseKB());

            Iterator<SharkNetChatListener> chatListenerIter
                    = this.chatListener.iterator();

            while (chatListenerIter.hasNext()) {
                SharkNetChatListener listener = chatListenerIter.next();
                listener.newEntryReached(chatEntry);
            }
        }
    }

    public SharkNetEngine getSharkNet() {
        return sharkNet;
    }

//    SubSpaceEnumerator createEnumerator(Enumeration msgCPEnum, SharkKB baseKB) {
//        return new TodoEnumerator(msgCPEnum, baseKB);
//    }
    

    @Override
    public SharkNetChatEntry createEntry(String message) throws SharkNetException, SharkKBException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SharkNetChatEntry createEntry(InputStream content) throws SharkNetException, SharkKBException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SharkNetChatEntry createEntry(String message, InputStream content) throws SharkNetException, SharkKBException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void publishEntry(SharkNetChatEntry entry) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Enumeration<SharkNetChatEntry> entries() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SemanticTag getSubSpaceTag() throws SharkKBException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SemanticTag getTopic() throws SharkKBException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getSubSpaceType() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setSubSpaceType(String type) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public PeerSemanticTag getOwner() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getSubSpaceSI() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public PeerSemanticTag addFullMember(PeerSemanticTag peer) throws SharkSubSpaceException, SharkKBException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public PeerSemanticTag addReadonlyMember(PeerSemanticTag peer) throws SharkSubSpaceException, SharkKBException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setFullMember(PeerSemanticTag peer) throws SharkSubSpaceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setReadonlyMember(PeerSemanticTag peer) throws SharkSubSpaceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setLastVisitingTime(long time) throws SharkSubSpaceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long getLastVisitingTime() throws SharkSubSpaceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ArrayList<Information> getNewInformationSinceLastVisit() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isOpenSpace() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeMember(PeerSemanticTag peer) throws SharkSubSpaceException, SharkKBException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void inviteMember(PeerSemanticTag peer, boolean fullmember) throws SharkKBException, SharkSubSpaceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean inviteAll() throws SharkSecurityException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean allInvited() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void tellPeer(PeerSemanticTag peer) throws SharkSubSpaceException, SharkSubSpaceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void close() throws SharkSubSpaceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isClosed() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void subscribe() throws SharkSubSpaceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void subscribe(long from, long duration) throws SharkSubSpaceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void subscribe(long from) throws SharkSubSpaceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isSubscribed() throws SharkKBException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isSubscribed(PeerSemanticTag peer) throws SharkKBException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setSubscribed(PeerSemanticTag peer, boolean subscribed) throws SharkKBException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void unsubscribe() throws SharkSubSpaceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Iterator<PeerSemanticTag> member() throws SharkKBException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Enumeration<PeerSemanticTag> subscribedMember() throws SharkKBException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addListener(SubSpaceListener listener) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeListener(SubSpaceListener listener) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void syncAllowedPeers(Iterator<PeerSemanticTag> allowedPeersIter) throws SharkSubSpaceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public J2SEAndroidSharkEngine getSharkEngine() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void doInsert(Knowledge knowledge, KEPConnection response) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void doExpose(SharkCS interest, KEPConnection response) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void invitePeer(PeerSemanticTag peer) throws SharkKBException, SharkSecurityException, IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setProperty(String name, String value) throws SharkKBException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getProperty(String name) throws SharkKBException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setProperty(String name, String value, boolean transfer) throws SharkKBException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeProperty(String name) throws SharkKBException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Enumeration<String> propertyNames() throws SharkKBException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Enumeration<String> propertyNames(boolean all) throws SharkKBException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
