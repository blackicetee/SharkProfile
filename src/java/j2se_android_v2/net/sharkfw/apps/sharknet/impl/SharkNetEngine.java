package net.sharkfw.apps.sharknet.impl;

import java.util.ArrayList;
import java.util.Iterator;
import net.sharkfw.apps.sharknet.SharkNet;
import net.sharkfw.apps.sharknet.SharkNetChat;
import net.sharkfw.apps.sharknet.SharkNetException;
import net.sharkfw.apps.sharknet.SharkNetListener;
import net.sharkfw.apps.sharknet.SharkNetPeerProfile;
import net.sharkfw.knowledgeBase.PeerSemanticTag;
import net.sharkfw.knowledgeBase.SemanticTag;
import net.sharkfw.knowledgeBase.SharkKB;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.knowledgeBase.SharkVocabulary;
import net.sharkfw.peer.SharkEngine;
import net.sharkfw.subspace.SharkSubSpaceException;
import net.sharkfw.subspace.SimpleSubSpaceManager;
import net.sharkfw.subspace.SubSpace;
import net.sharkfw.subspace.SubSpaceGuardKP;

/**
 * That class is going to become the (better) version 2 
 * implementation of SharkNet interface. There is work
 * todo.
 * 
 * @author thsc
 */
public class SharkNetEngine implements SharkNet {
    private static SharkNet snEngine = null;
    
    private SharkEngine se;
    private SharkKB baseKB;
    
    private SharkNetEngine() {} // not to be used
    
    private SharkNetEngine(SharkEngine se, SharkKB baseKB) {
        this.se = se;
        this.baseKB = baseKB;
        
        this.subSpaceManager = new SimpleSubSpaceManager(se, baseKB);
        
        this.guardKP = new SubSpaceGuardKP(se, this.subSpaceManager, baseKB);
    }

    public static SharkNet createSharkNet(SharkEngine se, SharkKB baseKB) {
        if(snEngine == null) {
            SharkNetEngine.snEngine = new SharkNetEngine(se, baseKB);
        }
        
        return SharkNetEngine.snEngine;
    }

    @Override
    public SharkEngine getSharkEngine() {
        return this.se;
    }

    @Override
    public SharkVocabulary getVocabulary() {
        return this.baseKB;
    }
    

    @Override
    public void setOwner(PeerSemanticTag owner) {
        this.baseKB.setOwner(owner);
    }

    @Override
    public PeerSemanticTag getOwner() {
        return this.baseKB.getOwner();
    }
    
    //////////////////////////////////////////////////////////////////////////
    //                        listener stuff                                //
    //////////////////////////////////////////////////////////////////////////

    private ArrayList<SharkNetListener> listener = new ArrayList<>();

    @Override
    public void addListener(SharkNetListener listener) {
        // don't add twice
        if (this.listener.indexOf(listener) == -1) {
            this.listener.add(listener);
        }
    }

    @Override
    public void removeListener(SharkNetListener listener) {
        this.listener.remove(listener);
    }

    //////////////////////////////////////////////////////////////////////////
    //                        profile stuff                                 //
    //////////////////////////////////////////////////////////////////////////

    @Override
    public PeerSemanticTag getPeer(String si) throws SharkKBException {
        return this.baseKB.getPeerSTSet().getSemanticTag(si);
    }

    @Override
    public PeerSemanticTag getPeer(String[] si) throws SharkKBException {
        return this.baseKB.getPeerSTSet().getSemanticTag(si);
    }

    @Override
    public SharkNetPeerProfile getPeerProfile(String si) throws SharkKBException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SharkNetPeerProfile getPeerProfile(String[] si) throws SharkKBException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SharkNetPeerProfile getPeerProfile(PeerSemanticTag peer, PeerSemanticTag sender) throws SharkKBException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SharkNetPeerProfile getPeerProfile(PeerSemanticTag peer) throws SharkKBException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SharkNetPeerProfile createPeerProfile(PeerSemanticTag peer) throws SharkKBException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void sendPeerAddress(PeerSemanticTag peerToSend, PeerSemanticTag remotePeer) throws SharkNetException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removePeerProfile(PeerSemanticTag peer) throws SharkKBException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    //////////////////////////////////////////////////////////////////////////
    //                           chat stuff                                 //
    //////////////////////////////////////////////////////////////////////////
    
     SimpleSubSpaceManager subSpaceManager;
     SubSpaceGuardKP guardKP;
    
    @Override
    public SharkNetChat createChat(String name, SemanticTag topic, Iterator<PeerSemanticTag> participants) throws SharkKBException, SharkNetException {
        SharkNetChat newChat;
        try {
            newChat = new SharkNetChat_Impl(this.guardKP, this, this.baseKB, name, topic, participants);
        } catch (SharkSubSpaceException ex) {
            throw new SharkNetException(ex);
        }
        
//        this.subSpaceManager.addSubSpace(newChat);
        
        return newChat;
    }

    @Override
    public void closeChat(SharkNetChat chat) throws SharkNetException, SharkKBException {
        chat.close();
    }

    @Override
    public void leaveChat(SharkNetChat chat) throws SharkNetException, SharkKBException {
        // TODO
    }

    @Override
    public void removeChat(SharkNetChat chat) throws SharkNetException, SharkKBException {
        chat.close();
        // TODO: chat isn't a subspace yet
        this.subSpaceManager.removeSubSpace((SubSpace) chat);
    }

    @Override
    public Iterator<SharkNetChat> getChats() throws SharkKBException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SharkNetChat getChat(String si) throws SharkKBException {
        SubSpace subSpace = this.subSpaceManager.getSubSpace(si);
        try {
            return new SharkNetChat_Impl(subSpace);
        } catch (SharkSubSpaceException ex) {
            throw new SharkKBException(ex.getLocalizedMessage());
        }
    }
    
    //////////////////////////////////////////////////////////////////////////
    //                   black / white list management                      //
    //////////////////////////////////////////////////////////////////////////
    
    @Override
    public void acceptInvitation(PeerSemanticTag peer, boolean accept) throws SharkKBException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isAcceptedPeer(PeerSemanticTag peer) throws SharkKBException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
