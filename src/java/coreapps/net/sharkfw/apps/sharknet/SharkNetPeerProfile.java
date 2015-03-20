package net.sharkfw.apps.sharknet;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import net.sharkfw.knowledgeBase.Information;
import net.sharkfw.knowledgeBase.PeerSemanticTag;
import net.sharkfw.knowledgeBase.SemanticTag;
import net.sharkfw.knowledgeBase.SharkCS;
import net.sharkfw.knowledgeBase.SharkKBException;

/**
 *
 * @author thsc
 */
public interface SharkNetPeerProfile {
    
    public static final String PEER_PROFILE_TOPIC_NAME = "SharkNet Peer Profile";
    public static final String PEER_PROFILE_TOPIC_SI = "http://www.sharksystem.net/apps/sharknet/vocabulary/peerProfile.html";
    public static final String PEER_PROFILE_RECIPIENT_PROPERTY = "snPeerProfile_recipientTime";

    public static final String PEER_PROFILE_INFO_TYPE = "peerProfileType";
    public static final String INFO_CV = "cv";
    public static final String INFO_NOTES = "notes";
    public static final String INFO_PICTURE = "picture";
    
    public static final String PEER_IS_GROUP = "groupPeer";
    public static final String PEER_IS_MEMBER_PREDICATE = "isGroupMember";

    public static final String STATUS_MESSAGE_CHAT_SI = "statusMessageSI";

    public PeerSemanticTag getOriginator() throws SharkKBException;

    public PeerSemanticTag getSender() throws SharkKBException;

    public PeerSemanticTag getPeer() throws SharkKBException;

    public long getRecipientTime();
     /**
      * @return
      * @throws SharkKBException 
      */
     public Information getPicture() throws SharkKBException;

     /**
      * Get a full file name. This name might change.
      * @param peer
      * @return
      * @throws SharkKBException 
      */
     public String getPictureFilename() throws SharkKBException;

     /**
      * Set a picture for this profile
      * @param filename Absolute Pathname of file which contains the picture
      * @param contentType Mime-Type of this picture. Please follow IANA definition
      * which can be found e.g. here: http://www.iana.org/assignments/media-types/index.html
      * If not set - application/x-unknown is used.
      * 
      * @throws SharkKBException 
      */
     
     public void setPicture(String filename, String contentType) throws SharkKBException;
     
     /**
      * @return
      * @throws SharkKBException 
      */
     public void setPicture(byte[] content, String contentType) throws SharkKBException;

     public void setPicture(InputStream picture, long len, String contentType) throws SharkKBException;     
     
     /**
      * removes a profile picture 
      */
     public void clearPicture();
     
     /**
      * Gets a note for that peer which is an arbitrary text.
      * @return
      * @throws SharkNetException if no notes are set.
      */
     public Iterator<String> getNotes() throws SharkNetException;
     
     public String getNote(int index) throws SharkNetException;

     /**
      * Set an arbitrary text which is part of peers profile.
      * @param notes 
      * @throws SharkKBException if cv can not be written to kb
      */
     public void addNotes(String notes) throws SharkKBException;
     
     public void removeAllNotes() throws SharkKBException;
     
     public void removeNote(int index) throws SharkKBException;
     
     /**
      * Anything is locally stored in Shark. Opening a entity offers those data
      * to other peers. who are interested in it and ask for it. Thus, if a profile
      * is open - it is offered to other peers.
      * 
      * @param open true - profile is open, false - profile is closed and won't be
      * transfered to other peers.
     * @throws net.sharkfw.apps.sharknet.SharkNetException
      */
     public void openProfile(boolean open) throws SharkNetException;
     
     /**
      * @return true if profile is open to other peers - false otherwise
      */
     public boolean isOpenProfile();
     
     public void openCV(boolean open) throws SharkNetException;
     
     public void openNotes(boolean open) throws SharkNetException;

     /**
      * Shark peer only exchange data if both parties are interested in an exchange.
      * This methode defines that profiles are accepted and inserted if they are offered
      * by the declared peer. 
     * @param accept
     * @throws net.sharkfw.apps.sharknet.SharkNetException
      */
     public void setAcceptProfiles(boolean accept) throws SharkNetException;
     
     public void askForProfiles() throws SharkNetException, IOException;

     /**
      * Peers profile is sent to recipient. This method actually sends those data.
      * They are not only offered. The remote peer must be interested in receiving such
      * data. In this case, the profile is included in recipients knowledge base. 
      * @param recipient 
     * @throws net.sharkfw.apps.sharknet.SharkNetException 
      */
     public void publishProfile(PeerSemanticTag recipient) throws SharkNetException;
          
     /**
      * Peers' profile is sent to a number of recipients
     * @throws net.sharkfw.apps.sharknet.SharkNetException
     * @throws java.io.IOException
      */
     public void publishProfile() throws SharkNetException, IOException;

     /**
      * Methode can only be called on owners profile. It creates a new interest
      * where originator and peer is set with owner. A knowledge port is created
      * with that interest immediately. (The interest isn't published, though.)
      * 
     * @param topics
     * @param remotePeers
     * @param direction
      * @return
      * @throws SharkKBException something wrong with knowledge base
      * @throws SharkNetException if profile is not owners profile 
      */
     public SharkNetInterest createInterest(Iterator<SemanticTag> topics, 
             Iterator<PeerSemanticTag> remotePeers, int direction) 
             throws SharkNetException, SharkKBException;
     
     /**
      * Save an interest with peer profile.
      * @param interest
      * @return
      * @throws SharkNetException is thrown if profile owner is not interest 
      * originator and it is not in peer dimension. It is also thrown if 
      * profile owner is also engine owner. createInterest has to be used instead
      * @throws SharkKBException problems with knowledge base
      */
     public SharkNetInterest saveInterest(SharkCS interest)
             throws SharkNetException, SharkKBException;
     
     public SharkNetInterest getInterest(int id) throws SharkKBException;

     public void removeInterest(int id) throws SharkKBException;

     public void removeInterest(SharkNetInterest interest) throws SharkKBException;
     
     public Iterator<SharkNetInterest> getInterests() throws SharkKBException;
}
