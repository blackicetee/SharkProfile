package net.sharkfw.apps.sharknet.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import net.sharkfw.apps.sharknet.SharkNetChat;
import net.sharkfw.apps.sharknet.SharkNetChatEntry;
import net.sharkfw.apps.sharknet.SharkNetChatListener;
import net.sharkfw.apps.sharknet.SharkNetException;
import net.sharkfw.knowledgeBase.*;
import net.sharkfw.subspace.SharkSubSpaceException;
import net.sharkfw.subspace.SubSpace;
import net.sharkfw.subspace.SubSpaceGuardKP;
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
    public String getSubSpaceSI() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

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
    

}
