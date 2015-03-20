package net.sharkfw.apps.sharknet;

import java.util.Iterator;
import net.sharkfw.knowledgeBase.PeerSemanticTag;
import net.sharkfw.knowledgeBase.SemanticTag;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.knowledgeBase.SharkVocabulary;
import net.sharkfw.peer.SharkEngine;

/**
 * SharkNet is an application based on the Shark P2P Framework. SharkNet is 
 * a kind of social network and dropbox. It can also be seen and used as
 * file sharing system, discussion forum and similar tasks.
 * 
 * The core SharkNet concepts are pretty straightforward. 
 * 
 * <ul>
 * <li>SharkNet is an application. It has to be installed on a machine.
 * There is no server. That's an advantage: There is no central entity
 * neither in SharkNet itself nor in your system that holds information about
 * SharkNet users. Thus, SharkNet actually exists but nobody can and will have
 * a map of that system. SharkNet is so real and distributed as Internet itself. 
 * <li>
 * Each installation must be <i>owned</i> be someone. Owner can declare their
 * name and addresses. Note: Any data remains on the system. There is no server
 * which has implications: If you system loses your data - they are gone. Full
 * stop. Make yourself familiar with backups or be nicely to an administrator
 * nearby.
 * <li>
 * SharkNet can store data, namely information. These data are stored locally
 * and won't be sent to anybody unless on an explicit user demand. Sharing
 * information is a deliberate action.
 * </ul>
 * 
 * @author thsc
 */
public interface SharkNet {
    
    public SharkEngine getSharkEngine();
    public SharkVocabulary getVocabulary();
    
     //////////////////////////////////////////////////////////////////////////
     //                           listener                                   //
     //////////////////////////////////////////////////////////////////////////

    /**
     * Add object to be called back when new data had been added to knowledge base.
     * @param listener 
     */
    public void addListener(SharkNetListener listener);
    public void removeListener(SharkNetListener listener);
    
     //////////////////////////////////////////////////////////////////////////
     //                           profile                                    //
     //////////////////////////////////////////////////////////////////////////

    /**
     * Returns a peer semantic tag.
     * @param si one subject identifier describing the peer
     * @return the peer
     * @throws net.sharkfw.knowledgeBase.SharkKBException
     */
     public PeerSemanticTag getPeer(String si) throws SharkKBException;
     
    /**
     * Returns a peer semantic tag.
     * @param si subject identifiers describing the peer
     * @return the peer 
     * @throws SharkKBException if no peer can be found with one si
     */
     public PeerSemanticTag getPeer(String[] si) throws SharkKBException;
     
     public SharkNetPeerProfile getPeerProfile(String si) throws SharkKBException;
     
     public SharkNetPeerProfile getPeerProfile(String[] si) throws SharkKBException;
     
     /**
      * Takes a profile that was received by a defined sender.
      * @param peer
      * @param sender
      * @return
      * @throws SharkKBException 
      */
     public SharkNetPeerProfile getPeerProfile(PeerSemanticTag peer, PeerSemanticTag sender) throws SharkKBException;

     public SharkNetPeerProfile getPeerProfile(PeerSemanticTag peer) throws SharkKBException;
     
     public SharkNetPeerProfile createPeerProfile(PeerSemanticTag peer) throws SharkKBException;
     
     /**
      * Sends address of peerToSend to remotePeer. This method should only be used
      * of peerToSend has no profile. If there is a profile - take this and publish 
      * to remote peer.
      * @param peerToSend
      * @param remotePeer
      * @throws SharkNetException communication fails or one peer isn't valid.
      */
     public void sendPeerAddress(PeerSemanticTag peerToSend, PeerSemanticTag remotePeer) throws SharkNetException;

     public void removePeerProfile(PeerSemanticTag peer) throws SharkKBException;
     
     //////////////////////////////////////////////////////////////////////////
     //                           chats                                      //
     //////////////////////////////////////////////////////////////////////////
     
     /**
      * Create a new chat
      * @param name
      * @param topic
      * @param participants
      * @return
      * @throws SharkKBException 
     * @throws net.sharkfw.apps.sharknet.SharkNetException 
      */
     public SharkNetChat createChat(String name, SemanticTag topic, 
             Iterator<PeerSemanticTag> participants) 
                throws SharkKBException, SharkNetException;
     
     /**
      * The chat is closes. This method only works if chat was created by the peer.
      * It notifies each participant that the chat is closed.
      * 
      * @param chat
      * @throws SharkKBException 
      * @throws SharkNetException This method can only be called if this peer is owner of the chat
      */
     public void closeChat(SharkNetChat chat) throws SharkNetException, SharkKBException;

     /**
      * The owner leaves this chat. The chat remains active but other peers get
      * informed that this peer has unsubscribed.
      * @param chat
      * @throws SharkKBException 
      * @throws SharkNetException This method can only be called if this peer is not owner of the chat. Owner must no leave a chat.
      */
     public void leaveChat(SharkNetChat chat) throws SharkNetException, SharkKBException;

     /**
      * That chat is permanenty removed from this system.
      * 
      * @param chat
      * @throws SharkNetException
      * @throws SharkKBException 
      */
     public void removeChat(SharkNetChat chat) throws SharkNetException, SharkKBException;

     /**
      * Define whether this peers is in general willing to receive an invitation
      * to a chat or a makan. If yes, peer will get an invitation notification.
      * If not, each message from peer is removed without further notice.
      * 
      * @param peer
      * @param accept
      * @throws SharkKBException 
      */
     public void acceptInvitation(PeerSemanticTag peer, boolean accept) throws SharkKBException;
     
     public boolean isAcceptedPeer(PeerSemanticTag peer) throws SharkKBException;
     
     public Iterator<SharkNetChat> getChats() throws SharkKBException;
     
     /**
      * get Chat by chat-SI (which is actually a sub space si).
      * @param si
      * @return
      * @throws SharkKBException 
      */
     public SharkNetChat getChat(String si) throws SharkKBException;
}
