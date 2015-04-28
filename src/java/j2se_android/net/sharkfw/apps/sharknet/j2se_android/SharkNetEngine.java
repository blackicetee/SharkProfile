package net.sharkfw.apps.sharknet.j2se_android;

import static net.sharkfw.subspace.SubSpace.PARENT_SUBSPACE_SI_VALUE;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sharkfw.apps.sharknet.SharkNet;
import net.sharkfw.apps.sharknet.SharkNetChat;
import net.sharkfw.apps.sharknet.SharkNetException;
import net.sharkfw.apps.sharknet.SharkNetListener;
import net.sharkfw.apps.sharknet.SharkNetPeerProfile;
import net.sharkfw.kep.SharkProtocolNotSupportedException;
import net.sharkfw.knowledgeBase.ContextCoordinates;
import net.sharkfw.knowledgeBase.ContextPoint;
import net.sharkfw.knowledgeBase.FragmentationParameter;
import net.sharkfw.knowledgeBase.Information;
import net.sharkfw.knowledgeBase.Knowledge;
import net.sharkfw.knowledgeBase.PeerSNSemanticTag;
import net.sharkfw.knowledgeBase.PeerSTSet;
import net.sharkfw.knowledgeBase.PeerSemanticNet;
import net.sharkfw.knowledgeBase.PeerSemanticTag;
import net.sharkfw.knowledgeBase.SNSemanticTag;
import net.sharkfw.knowledgeBase.STSet;
import net.sharkfw.knowledgeBase.SemanticNet;
import net.sharkfw.knowledgeBase.SemanticTag;
import net.sharkfw.knowledgeBase.SharkCS;
import net.sharkfw.knowledgeBase.SharkCSAlgebra;
import net.sharkfw.knowledgeBase.SharkKB;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.knowledgeBase.SharkVocabulary;
import net.sharkfw.knowledgeBase.TXSemanticTag;
import net.sharkfw.knowledgeBase.Taxonomy;
import net.sharkfw.knowledgeBase.filesystem.FSSharkKB;
import net.sharkfw.knowledgeBase.inmemory.InMemoContextPoint;
import net.sharkfw.knowledgeBase.inmemory.InMemoKnowledge;
import net.sharkfw.knowledgeBase.inmemory.InMemoSharkKB;
import net.sharkfw.peer.J2SEAndroidSharkEngine;
import net.sharkfw.peer.KnowledgePort;
import net.sharkfw.peer.SharkEngine;
import net.sharkfw.peer.StandardKP;
import net.sharkfw.protocols.Protocols;
import net.sharkfw.subspace.SharkSubSpaceException;
import net.sharkfw.subspace.SubSpace;
import net.sharkfw.subspace.SubSpaceGuardKP;
import net.sharkfw.subspace.SubSpaceManager;
import net.sharkfw.system.Base64;
import net.sharkfw.system.Iterator2Enumeration;
import net.sharkfw.system.IteratorChain;
import net.sharkfw.system.L;
import net.sharkfw.system.SharkException;
import net.sharkfw.system.SharkSecurityException;
import net.sharkfw.system.Util;

/**
 * Implements SharkNet interface and holds SharkEngine, knowledge base and sub
 * spaces.
 *
 * @author thsc
 * @author df
 */
public class SharkNetEngine implements SharkNet, SubSpaceManager {

    protected static SharkNetEngine engine = null;
    private static int DEFAULT_PORT_NUMBER = 7070;
    private FSSharkKB baseKB = null; // holds documents and general settings
    protected SharkKB profileKB = null; // holds profiles
    public SharkEngine sharkEngine = null;
    private SharkNetChatManager chatManager;
    private final SubSpaceGuardKP subSpaceGuardKP;
    private String rootFolder;
    protected static String staticRootFolder;
    private StandardKP profileKP = null;
    private SemanticTag profileTopic;
    public static final String TODO_PROPERTY_DUEDATE = "sn_makan_todo_dueDateAsLong";
    public static final String TODO_PROPERTY_STATUS = "sn_makan_todo_status";

    /**
     * Create a new SharkNet instance. Note: This method creates a new instance
     * with each call. Subsequent getSharkNet() calls return the same instance,
     * though. Use this method once in your application during initializing.
     *
     * @param folder
     * @return
     * @throws SharkKBException
     * @throws SharkNetException
     */
    public static SharkNet createSharkNet(String folder) throws SharkKBException, SharkNetException, SharkSecurityException {
        SharkNetEngine.engine = new SharkNetEngine(folder);
        // remember folder
        SharkNetEngine.setSharkNetFolderName(folder);
        return SharkNetEngine.engine;
    }

    protected final void setSharkEngine(SharkEngine se) throws SharkKBException {
        this.sharkEngine = se;
        ((J2SEAndroidSharkEngine) this.sharkEngine).setPropertyHolder(this.baseKB);
        ((J2SEAndroidSharkEngine) this.sharkEngine).refreshStatus();
    }

    public static void setSharkNetFolderName(String foldername) throws SharkNetException {
        if (SharkNetEngine.staticRootFolder != null) {
            // throw new SharkNetException("shark net folder already set");
            return;
        }

        SharkNetEngine.staticRootFolder = foldername;
    }

    public static boolean isSharkFolderSet() {
        return SharkNetEngine.staticRootFolder != null;
    }

    public static String getSharkNetFolderName() throws SharkNetException {
        if (SharkNetEngine.staticRootFolder == null) {
            throw new SharkNetException("shark net folder not set");
        }

        return SharkNetEngine.staticRootFolder;
    }

    public static SharkNet getSharkNet() throws SharkKBException, SharkNetException, SharkSecurityException {
        if (SharkNetEngine.staticRootFolder == null) {
            throw new SharkNetException("folder not yet set");
        }

        // singleton
        if (SharkNetEngine.engine == null) {
            SharkNetEngine.engine = new SharkNetEngine(SharkNetEngine.staticRootFolder);
        }

        return SharkNetEngine.engine;
    }

    public static SharkNet getSharkNet(String foldername) throws SharkKBException, SharkNetException {
        if (foldername == null || !foldername.equalsIgnoreCase(SharkNetEngine.staticRootFolder)) {
            throw new SharkNetException("folder not given or different folder already set");
        }

        if (SharkNetEngine.staticRootFolder == null) {
            SharkNetEngine.setSharkNetFolderName(foldername);
        }

        // singleton
        if (SharkNetEngine.engine == null) {
            SharkNetEngine.engine = new SharkNetEngine(SharkNetEngine.staticRootFolder);
        }

        return SharkNetEngine.engine;
    }

    @Override
    public J2SEAndroidSharkEngine getSharkEngine() {
        return ((J2SEAndroidSharkEngine) this.sharkEngine);
    }

    /**
     * for debugging only
     *
     * @deprecated use createSharkNet to create a fresh instance that restores
     * its data from filesystem
     */
    public static void debugDiscardSharkNetEngine() {
        SharkNetEngine.engine = null;
    }

    public SharkNetEngine(String rootFolder) throws SharkKBException, SharkNetException {
        // ATTENTION: sequence of the following initialization part is crucial

        // create kb that holds arbitrary documents and create all seven context dimensions
        this.baseKB = new FSSharkKB(rootFolder);

        // create a shark engine with that baseKB as property holder - must be after baseKB init
        this.setSharkEngine(new J2SEAndroidSharkEngine());

        /*
         * create a kb that only holds profiles. This KB shares
         * any dimension with the documents KB except topics.
         */
        // create InMemo tag set for just a single topic
        SemanticNet topics = InMemoSharkKB.createInMemoSemanticNet();

        this.profileTopic = topics.createSemanticTag(SharkNetPeerProfile.PEER_PROFILE_TOPIC_NAME, SharkNetPeerProfile.PEER_PROFILE_TOPIC_SI);

        // create symbiant KB with its own topics and knowledge
        this.profileKB = new FSSharkKB(topics, this.baseKB.getPeersAsTaxonomy(), this.baseKB.getSpatialSTSet(), this.baseKB.getTimeSTSet(), rootFolder + "/profiles");

//        // create profile exchange KP if there is already an owner
//        try {
//            PeerSemanticTag owner = this.getOwner();
//            if (owner != null) {
//                // init profile kp - sharkEngine  must already have been initialized at this point
//                this.getProfileKP();
//            }
//        } catch (SharkNetException e) {
//            // no profileKP
//        }

        this.rootFolder = rootFolder;

        this.subSpaceGuardKP = new SubSpaceGuardKP(this.getSharkEngine(), this, this.baseKB);
        this.chatManager = new SharkNetChatManager(this.subSpaceGuardKP, this, this.baseKB, rootFolder);
//        this.deliusManager = new SharkNetDeliusManager(this, this.baseKB, rootFolder);
//        this.deliusManager.startReceiver();

        // activate interest catcher
//        new SharkNetInterestCatcherKP(this);

        this.refreshStatus();

        //        this.sharkEngine.useWhiteList(true);
    }

    private ArrayList<SharkNetListener> listener = new ArrayList<SharkNetListener>();

    //////////////////////////////////////////////////////////////////
    //                     shark net listener                       //
    //////////////////////////////////////////////////////////////////
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

    @Override
    public final PeerSemanticTag getOwner() {
        PeerSemanticTag owner = this.baseKB.getOwner();

        return owner;
    }

    @Override
    public void setOwner(PeerSemanticTag owner) {
        if (this.baseKB.getOwner() == null || !SharkCSAlgebra.identical(this.baseKB.getOwner(), owner)) {
            this.baseKB.setOwner(owner);
        } else {
            return;
        }
    }

    private PeerSemanticTag returnOrException(PeerSemanticTag pst) throws SharkKBException {
        if (pst == null) {
            throw new SharkKBException("peer semantic tag not found");
        }

        return pst;
    }

    @Override
    public PeerSemanticTag getPeer(String si) throws SharkKBException {
        return this.returnOrException(this.baseKB.getPeerSemanticTag(si));
    }

    @Override
    public PeerSemanticTag getPeer(String[] si) throws SharkKBException {
        return this.returnOrException(this.baseKB.getPeerSemanticTag(si));
    }

    /////////////////////////////////////////////////////////////////////////
    //                            peer profile                             //
    /////////////////////////////////////////////////////////////////////////
    @Override
    public SharkNetPeerProfile getPeerProfile(String si) throws SharkKBException {
        PeerSemanticTag peer = this.returnOrException(this.profileKB.getPeerSemanticTag(si));
        return SharkNetPeerProfile_Impl.getProfile(this, this.profileKB, peer);
    }

    @Override
    public SharkNetPeerProfile getPeerProfile(String[] si) throws SharkKBException {
        PeerSemanticTag peer = this.returnOrException(this.profileKB.getPeerSemanticTag(si));
        return SharkNetPeerProfile_Impl.getProfile(this, this.profileKB, peer);
    }

    @Override
    public SharkNetPeerProfile getPeerProfile(PeerSemanticTag peer, PeerSemanticTag sender) throws SharkKBException {
        return SharkNetPeerProfile_Impl.getProfile(this, this.profileKB, peer, sender);
    }

    @Override
    public SharkNetPeerProfile getPeerProfile(PeerSemanticTag peer) throws SharkKBException {
        try {
            SharkNetPeerProfile profile
                    = SharkNetPeerProfile_Impl.getProfile(this, this.profileKB, peer);

            return profile; // return already existing profile
        } catch (SharkKBException se) {
            // no profile found - create one
            return this.createPeerProfile(peer);
        }
    }

    @Override
    public SharkNetPeerProfile createPeerProfile(PeerSemanticTag peer) throws SharkKBException {
        return SharkNetPeerProfile_Impl.createProfile(this, this.profileKB, peer);
    }

    @Override
    public void removePeerProfile(PeerSemanticTag peer) throws SharkKBException {
        // TODO
//        try {
//            SharkNetPeerProfile peerProfile = this.getPeerProfile(peer);
//            SharkNetChat statusChat = peerProfile.getPeerStatusMessages();
//            this.removeChat(statusChat);
//        } catch (SharkException e) {
//            // cannot remove status chat - so long
//        }
//        SharkNetPeerProfile_Impl.removeProfile(this, this.profileKB, peer);
    }

    private KnowledgePort getKP(SharkCS interest) throws SharkNetException {
        Enumeration<KnowledgePort> kpEnum = this.sharkEngine.getKPs();
        if (kpEnum != null) {
            while (kpEnum.hasMoreElements()) {
                KnowledgePort kp = kpEnum.nextElement();

                try {
                    if (SharkCSAlgebra.identical(interest, kp.getInterest())) {
                        return kp;
                    }
                } catch (SharkKBException e) {
                    // shouldn't happen
                    throw new SharkNetException(e.getMessage());
                }
            }
        }

        throw new SharkNetException("no KP with this interest exists");
    }

    //////////////////////////////////////////////////////////////////////
    //                           address                                //
    //////////////////////////////////////////////////////////////////////
    @Override
    public void sendPeerAddress(PeerSemanticTag peerToSend, PeerSemanticTag remotePeer) throws SharkNetException {
//
//        Knowledge k = new InMemoKnowledge();
//        try {
//            // create an empty profile cp and send it to remote peer via profileKP
//
//            ContextCoordinates cc = SharkNetPeerProfile_Impl.getProfileCoordinates(this.getProfileKB(), peerToSend, peerToSend);
//
//            // create empty cp
//            ContextPoint cp = new InMemoContextPoint(cc);
//
//            k.addContextPoint(cp);
//
//            // send empty cp
////            this.getProfileKP().sendKnowledge(k, remotePeer);
//
//        } catch (SharkException ex) {
//            throw new SharkNetException(ex.getMessage());
//        }
        
        throw new SharkNetException("not implemented");
    }

    //////////////////////////////////////////////////////////////////////
    //                           peer groups                            //
    //////////////////////////////////////////////////////////////////////

    public static final String SHARKNET_PEER_ID_URI_DOMAIN = "sharkPeerID://";

    private ArrayList<String> getAddresses(PeerSemanticTag pst, ArrayList<String> addressList) {
        if (addressList == null) {
            addressList = new ArrayList<String>();
        }

        addressList.addAll(Arrays.asList(pst.getAddresses()));

        return addressList;
    }

    private void sendInformation(KnowledgePort kp, ContextPoint cp, Information info, PeerSemanticTag recipient) throws SharkNetException, SharkKBException {

        // create empty knowledge
        Knowledge k = InMemoSharkKB.createInMemoKnowledge();

        // create empty cp
        ContextPoint tmpCP = InMemoSharkKB.createInMemoContextPoint(cp.getContextCoordinates());

        // add a single information object
        tmpCP.addInformation(info);

        // add to knowledge
        k.addContextPoint(cp);

        //        ArrayList<String> addresses = this.getAddresses(recipient, null);
        try {
            kp.sendKnowledge(k, recipient);
        } catch (SharkException ex) {
            throw new SharkNetException(ex.getMessage());
        } catch (IOException e) {
            ((J2SEAndroidSharkEngine) this.sharkEngine).rememberUnsentKnowledge(k, recipient);
        }
    }

    private KnowledgePort getFittingKPOrThrowException(ContextCoordinates cc) throws SharkKBException, SharkNetException {
        KnowledgePort kp = null;
        Enumeration<KnowledgePort> kpEnum = this.sharkEngine.getKPs();
        if (kpEnum != null) {
            while (kpEnum.hasMoreElements()) {
                kp = kpEnum.nextElement();

                if (kp.getInterest() != null && SharkCSAlgebra.isIn(kp.getInterest(), cc)) {
                    // we take this kp
                    break;
                }
            }
        }

        if (kp == null) {
            throw new SharkNetException("tried to send information but their is no active knowledge port / interest which allows sending - abort");
        }

        return kp;
    }

    ////////////////////////////////////////////////////////////////////////
    //                               chat                                 //
    ////////////////////////////////////////////////////////////////////////
    public final static String CHATS_SUBDIRECTORY = "/chats";
    public final static String MAKAN_SUBDIRECTORY = "/makan";

    @Override
    public SharkNetChat createChat(String name, SemanticTag topic, Iterator<PeerSemanticTag> participants) throws SharkKBException, SharkNetException {
        try {
            SharkNetChat chat = this.chatManager.createChat(name, topic, participants);
//            chat.inviteAll();
            return chat;
        } catch (SharkException ex) {
            throw new SharkNetException(ex.getMessage());
        }
    }
    public static final String HIDDEN_SUBSPACE_TOPIC_SI = "http://www.sharksystem.net/apps/sharknet/vocabulary/hiddenSubSpace.html";
    private SemanticTag hiddenSubSpaceTopic = InMemoSharkKB.createInMemoSemanticTag("Hidden SubSpace", HIDDEN_SUBSPACE_TOPIC_SI);

    /**
     * Create a chat that won't be listed when calling getChats(). It will be
     * found with getChat(String si) though.
     *
     * @param member
     * @param roMember
     * @return
     * @throws SharkKBException
     * @throws SharkNetException
     */
//    public SharkNetChat createHiddenChat(Iterator<PeerSemanticTag> member, Iterator<PeerSemanticTag> roMember) throws SharkKBException, SharkSubSpaceException {
//        SharkNetChat hiddenChat = this.createChildSubSpace(null, null, null, null, member, roMember);
//
//        this.setHidden(hiddenChat, true);
//
//        return hiddenChat;
//    }
    private static final String IS_HIDDEN = "HIDDEN";

    private void setHidden(SubSpace subSpace, boolean isHidden) throws SharkKBException {
        subSpace.setProperty(IS_HIDDEN, Boolean.toString(isHidden));
    }

    private boolean isHidden(SubSpace subSpace) throws SharkKBException {
        String property = subSpace.getProperty(IS_HIDDEN);
        return (Boolean.parseBoolean(property));
    }

    /**
     * create a sub space that is child of another one - this method will be
     * moved to subSpace when subSpace becomes part of the SharkFW
     *
     * @param superSubSpaceSI
     * @param childSpaceSI will be created if null
     * @param member
     * @param roMember
     * @return
     * @throws SharkKBException
     * @throws SharkNetException
     */
//    public SharkNetChat createChildSubSpace(SubSpace parent, String subSpaceName,
//            String superSubSpaceSI, String childSpaceSI,
//            Iterator<PeerSemanticTag> member, Iterator<PeerSemanticTag> roMember)
//            throws SharkKBException, SharkSubSpaceException {
//
//        SemanticTag subsubSpaceTopic;
//        if (superSubSpaceSI == null) {
//            subsubSpaceTopic = this.hiddenSubSpaceTopic;
//        } else {
//            subsubSpaceTopic = InMemoSharkKB.createInMemoCopy(this.hiddenSubSpaceTopic);
//            // add supersubspace si
//            subsubSpaceTopic.setProperty(PARENT_SUBSPACE_SI_VALUE, superSubSpaceSI, true);
//        }
//
//        PeerSemanticTag owner;
//        try {
//            owner = this.getOwner();
//        } catch (SharkNetException ex) {
//            throw new SharkSubSpaceException(ex.getMessage());
//        }
//        String ownerName = owner.getName();
//        if (ownerName == null) {
//            ownerName = "";
//        }
//
//        String timeStamp = "_" + System.currentTimeMillis();
//        try {
//            if (subSpaceName == null) {
//                subSpaceName = "Hidden";
//            }
//
//            String chatName = subSpaceName + SubSpace.CHILD_SUBSPACE_NAME_DELIMITER + ownerName + timeStamp;
//
//            SharkNetChat newChat = this.chatManager.createChat(chatName, childSpaceSI, subsubSpaceTopic, this.getOwner(), /* owner of the chat - sn owner of course */
//                    this.getOwner(), /* peer who creates that chat - owner again of course */
//                    member, /* no member with write privileges */
//                    roMember, /* recipient of my status messages */
//                    false, /* no, it is not an open chat - owner invites member */
//                    false); /* no, this peer is not a readonly peer */
//
//            // add child
//            if (parent != null) {
//                parent.addChildSubSpace(newChat);
//            }
//
//            return newChat;
//        } catch (SharkException ex) {
//            throw new SharkSubSpaceException(ex.getMessage());
//        }
//    }

    @Override
    public void closeChat(SharkNetChat chat) throws SharkNetException, SharkKBException {
        try {
            this.chatManager.closeChat(chat);
        } catch (SharkSubSpaceException ex) {
            throw new SharkNetException(ex.getMessage());
        }
    }

    @Override
    public void removeChat(SharkNetChat chat) throws SharkNetException, SharkKBException {
        try {
            this.chatManager.closeChat(chat);
        } catch (SharkException e) {
            // ??
        } finally {
//            chat.remove();
        }
    }

    @Override
    public void leaveChat(SharkNetChat chat) throws SharkNetException, SharkKBException {
        try {
            this.chatManager.leaveChat(chat);
        } catch (SharkSubSpaceException ex) {
            throw new SharkNetException(ex.getMessage());
        }
    }

    @Override
    public Iterator<SharkNetChat> getChats() throws SharkKBException {
        return this.getChats(true);
    }

    public Iterator<SharkNetChat> getChats(boolean hideHiddenAndChildSubSpaces)
            throws SharkKBException {

        if (!hideHiddenAndChildSubSpaces) {
//            return this.chatManager.getChats();
        } else {
            // hide status chats
            ArrayList<SharkNetChat> chatList = new ArrayList<SharkNetChat>();

            Iterator<SharkNetChat> chatsIter = this.chatManager.getChats();
            while (chatsIter.hasNext()) {
                SharkNetChat chat = chatsIter.next();

//                if (!chat.isChildSubSpace() && !this.isHidden(chat)) {
//                    chatList.add(chat);
//                }
            }

            return chatList.iterator();
        }
        
        throw new SharkKBException("no chats");
    }

    @Override
    public SharkNetChat getChat(String si) throws SharkKBException {
        if (si != null) {
            Iterator<SharkNetChat> chats = this.getChats(false); // all chats
            if (chats != null) {
                while (chats.hasNext()) {
                    SharkNetChat chat = chats.next();
                    String subSpaceSI = chat.getSubSpaceSI();

                    if (subSpaceSI.equalsIgnoreCase(si)) {
                        return chat;
                    }
                }
            }
        }

        throw new SharkKBException("no chat with this si found: " + si);
    }
    
    //////////////////////////////////////////////////////////////////////
    //                         sub space handling                       //
    //////////////////////////////////////////////////////////////////////
    /**
     * TODO: that must be the other way around: Chat, Makan etc must be
     * extracted from a sub space list. Sub space list should be the primary
     * structure that contains sub types.
     *
     * @return
     * @throws SharkKBException
     */
    @Override
    public Iterator<SubSpace> getSubSpaces() throws SharkKBException {
        ArrayList<Iterator> subSpaces = new ArrayList<Iterator>();

        subSpaces.add(this.getChats(false));

        return new IteratorChain(subSpaces.iterator());
    }

    @Override
    public SubSpace getSubSpace(String si) throws SharkKBException {
        Iterator<SubSpace> subSpacesIter = this.getSubSpaces();

        if (subSpacesIter == null) {
            throw new SharkKBException("no such sub space found - sub space list is empty");
        }

        while (subSpacesIter.hasNext()) {
            SubSpace subSpace = subSpacesIter.next();
            if (SharkCSAlgebra.identical(subSpace.getSubSpaceSI(), si)) {
                return subSpace;
            }
        }

        throw new SharkKBException("no sub space found with si: " + si);
    }

    @Override
    public void acceptInvitation(PeerSemanticTag peer, boolean accept) throws SharkKBException {
        ((J2SEAndroidSharkEngine) this.sharkEngine).acceptPeer(peer, accept);
    }

    @Override
    public boolean isAcceptedPeer(PeerSemanticTag peer) throws SharkKBException {
//        try {
            if (SharkCSAlgebra.identical(this.getOwner(), peer)) {
                return false;
            }
//        } catch (SharkNetException e) {
//            throw new SharkKBException(e.getMessage());
//        }
//
//        if (this.isGroup(peer)) {
//            return false;
//        }

        return this.sharkEngine.isAccepted(peer);
    }
    public static final String SNE_CHAT_FOLDERNAME_PROPERTY = "sn_chatFolder";
    public static final String SNE_MAKAN_FOLDERNAME_PROPERTY = "sn_makanFolder";

    public static final String DELIMITER = "|";

//    public static String foldernameIter2PropertyStringValue(Iterator<FSSubSpace> iter) {
//        ArrayList<String> folderNames = new ArrayList<String>();
//
//        while (iter.hasNext()) {
//            folderNames.add(iter.next().getFoldername());
//        }
//
//        // write cp folder names
//        String foldernames = Util.iteration2String(folderNames.iterator(), DELIMITER);
//
//        return foldernames;
//    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void persist() throws SharkKBException {
        // persist chats
        this.chatManager.persist();

        // persist makan
//        Iterator i = this.makanList.iterator();
//        String foldernames = SharkNetEngine.foldernameIter2PropertyStringValue(i);

//        this.baseKB.setProperty(SNE_MAKAN_FOLDERNAME_PROPERTY, foldernames, false);

        // persist engine
        ((J2SEAndroidSharkEngine) this.sharkEngine).setPropertyHolder(this.baseKB);
        ((J2SEAndroidSharkEngine) this.sharkEngine).persist();
    }

    public final void refreshStatus() throws SharkKBException, SharkNetException {
        try {
            // chats
            this.chatManager.refreshStatus();
        } catch (SharkSubSpaceException ex) {
            throw new SharkKBException(ex.getMessage());
        }

        // makan
        String foldernames = this.baseKB.getProperty(SNE_MAKAN_FOLDERNAME_PROPERTY);

        Vector<String> folderNamesVector = Util.string2Vector(foldernames, DELIMITER);
        if (folderNamesVector == null) {
            return; // nothing to refresh here
        }

        // bring all cps (back) into memory
        Enumeration<String> makanFoldernameEnum = folderNamesVector.elements();
        while (makanFoldernameEnum.hasMoreElements()) {
            String makanFoldername = makanFoldernameEnum.nextElement();

            // create Makan
//            SharkNetMakan_Impl makan;
//            try {
//                makan = new SharkNetMakan_Impl(this.subSpaceGuardKP, this, makanFoldername, this.baseKB, this.getOwner());
//            } catch (SharkSubSpaceException ex) {
//                throw new SharkNetException(ex.getMessage());
//            }
//
//            this.makanList.add(makan);
        }

        // restore engine 
        ((J2SEAndroidSharkEngine) this.sharkEngine).setPropertyHolder(this.baseKB);
        ((J2SEAndroidSharkEngine) this.sharkEngine).refreshStatus();
    }

    @Override
    public void invitedToSubSpace(String subSpaceName, String subSpaceSI,
            String subSpaceType, SemanticTag subSpaceTopic, String parentSpaceSI,
            String childString, PeerSemanticTag owner,
            ArrayList<PeerSemanticTag> member, ArrayList<PeerSemanticTag> roMember,
            boolean subSpaceOpen, boolean readonly)
            throws SharkSubSpaceException, SharkNetException, SharkKBException, SharkSecurityException {

        // what kind of sub space is it?
        if (subSpaceType.equalsIgnoreCase(SharkNetChat.CHAT_SUBSPACE_SUBTYPE)) {
            // create chat
            SharkNetChat chat = this.chatManager.createChat(subSpaceName, subSpaceSI, subSpaceTopic, owner, this.getOwner(), member.iterator(), roMember.iterator(), subSpaceOpen, readonly);

            // inherit real type - ugly implementation - will be changed when finalizing sub space
//            chat.setSubSpaceType(subSpaceType);

            // chat which is subspace of a huger structure?
//            if (parentSpaceSI != null) {
//                // is a child space - parent already in place?
//                try {
//                    SubSpace parent = this.getSubSpace(parentSpaceSI);
//
//                    // parent space already exist - connect both.
//                    parent.addChildSubSpace(chat);
//
//                    /* NOTE: subspaces are not automatically subscribed.
//                     * parent-spaces might so so, children don't
//                     */
//                    // chat.subscribe();
//                } catch (SharkException sse) {
//                    // ignore
//                }
//            } else {
//
//                // TODO: review that code!
//                if (subSpaceTopic != null) {
//                    // status chat?
//                    if (SharkCSAlgebra.identical(this.hiddenSubSpaceTopic, subSpaceTopic)) {
//                        // is it a subsubspace
//                        String superSubSpaceSI = null;
//                        if (subSpaceTopic != null) {
//                            superSubSpaceSI = subSpaceTopic.getProperty(PARENT_SUBSPACE_SI_VALUE);
//                        }
//                        if (superSubSpaceSI != null) {
//                            // there is a super subspace
//                            SubSpace superSubSpace = this.getChat(superSubSpaceSI);
//
//                            // in any case - notify super subspace of new child space
//                            superSubSpace.addChildSubSpace(chat);
//                            if (superSubSpace.isSubscribed()) {
//                                chat.subscribe();
//                            }
//                        } else { // not a child subspace
//                            // it is a status chat - remember chatSI in profile
//                            SharkNetPeerProfile peerProfile = this.createPeerProfile(owner);
//                            if (peerProfile instanceof SharkNetPeerProfile_Impl) {
//                                SharkNetPeerProfile_Impl snProfile = (SharkNetPeerProfile_Impl) peerProfile;
//                                snProfile.setStatusMessageChatSI(subSpaceSI);
//                            }
//                            // subscribe
//                            chat.subscribe();
//                        }
//                    } else {
//                        // no status chat - notify
//                        this.notifyInvitedSubSpace(chat);
//                    }
//                } else {
//                    // no subSpaceTopic - notify
//                    this.notifyInvitedSubSpace(chat);
//                }
//            }
//        } else if (subSpaceType.equalsIgnoreCase(SharkNetMakan.MAKAN_SUBSPACE_SUBTYPE)) {
//            // create makan
//            ArrayList<SemanticTag> topics = new ArrayList<SemanticTag>();
//            topics.add(subSpaceTopic);
//
//            SharkNetMakan makan = this.createMakan(subSpaceName, subSpaceSI, childString, owner, this.getOwner(), topics.iterator(), member.iterator(), roMember.iterator(), subSpaceOpen, readonly);
//
//            this.notifyInvitedMakan(makan);
//        } else if (subSpaceType.equals(SharkNetForum.TYPE)) {
//            // create forum
//            ArrayList<SemanticTag> topics = new ArrayList<SemanticTag>();
//            topics.add(subSpaceTopic);
//
//            SharkNetForum forum = this.chatManager.createForum(subSpaceName, subSpaceSI, subSpaceTopic, owner, this.getOwner(), member.iterator(), roMember.iterator(), subSpaceOpen, readonly);
//
//            if (forum instanceof SubSpace) {
//                this.notifyInvitedSubSpace((SubSpace) forum);
//            } else {
//                this.notifyInvitedForum(forum);
//            }
//        } else if (subSpaceType.equals(SharkNetBoard.TYPE)) {
//            // create board
//            ArrayList<SemanticTag> topics = new ArrayList<SemanticTag>();
//            topics.add(subSpaceTopic);
//
//            SubSpace subSpace = getSubSpace(parentSpaceSI);
//            if ((subSpace instanceof SharkNetBoard || subSpace instanceof SharkNetForum) && subSpace instanceof Restorator) {
//                SharkNetBoard board = this.chatManager.createBoard(subSpaceName, subSpaceSI, subSpaceTopic, owner, this.getOwner(), member.iterator(), roMember.iterator(), subSpaceOpen, readonly);
//                ((Restorator) subSpace).restore(board);
//                ((SubSpace) board).subscribe();
//            }
//
//            //No notification here because notification for forum already happened
//        } else if (subSpaceType.equals(SharkNetThread.TYPE)) {
//            // create thread
//            ArrayList<SemanticTag> topics = new ArrayList<SemanticTag>();
//            topics.add(subSpaceTopic);
//
//            SubSpace subSpace = getSubSpace(parentSpaceSI);
//            if (subSpace instanceof SharkNetBoard && subSpace instanceof Restorator) {
//                SharkNetThread thread = this.chatManager.createThread(subSpaceName, subSpaceSI, subSpaceTopic, owner, this.getOwner(), member.iterator(), roMember.iterator(), subSpaceOpen, readonly);
//                ((Restorator) subSpace).restore(thread);
//                ((SubSpace) thread).subscribe();
//            }
//
//            //No notification here because notification for forum already happened
//        } else {
//            throw new SharkSubSpaceException("unknown sub space type: " + subSpaceType);
//        }

        // ?????????????????????????
        // this was a subSpace invitation - we can assume that any member is already subscribed
        //        Iterator<PeerSemanticTag> iter = member.iterator();
        //        while(iter.hasNext()) {
        //            subSpace.setSubscribed(iter.next(), true);
        //        }
        //        
        //        iter = roMember.iterator();
        //        while(iter.hasNext()) {
        //            subSpace.setSubscribed(iter.next(), true);
        //        }
        //        
        //        subSpace.setAllSubscribed();
    }}

    private void notifyInvitedSubSpace(SubSpace subSpace) {
        String subSpaceType = subSpace.getSubSpaceType();

        L.d("received invitation with subSpaceType " + subSpaceType, this);

        boolean isOK = true;
        try {
            if (subSpace.getSubSpaceType().equalsIgnoreCase(SharkNetChat.CHAT_SUBSPACE_SUBTYPE)) {
                SharkNetChat chat = (SharkNetChat) subSpace;
                this.notifyInvitedChat(chat);
                return;
            } 
//            else if (subSpace.getSubSpaceType().equalsIgnoreCase(SharkNetMakan.MAKAN_SUBSPACE_SUBTYPE)) {
//                SharkNetMakan makan = (SharkNetMakan) subSpace;
//                this.notifyInvitedMakan(makan);
//                return;
//            } else if (subSpace.getSubSpaceType().equalsIgnoreCase(SharkNetForum.TYPE)) {
//                SharkNetForum forum = (SharkNetForum) subSpace;
//                this.notifyInvitedForum(forum);
//                return;
//            }
        } catch (RuntimeException re) {
            // go ahead and print message
        }

        L.w("received unknown sub space: classname / subSpaceType: " + subSpace.getClass().getName() + " / " + subSpace.getSubSpaceType(), this);
    }

    private void notifyInvitedChat(SharkNetChat chat) {
        Iterator<SharkNetListener> snListenerIter = this.listener.iterator();
        while (snListenerIter.hasNext()) {
            snListenerIter.next().notifyInvitation(chat);
        }
    }

//    private void notifyInvitedMakan(SharkNetMakan makan) {
//        Iterator<SharkNetListener> snListenerIter = this.listener.iterator();
//        while (snListenerIter.hasNext()) {
//            snListenerIter.next().notifyInvitation(makan);
//        }
//    }
//
//    private void notifyInvitedForum(SharkNetForum forum) {
//        Iterator<SharkNetListener> snListenerIter = this.listener.iterator();
//        while (snListenerIter.hasNext()) {
//            snListenerIter.next().notifyInvitation(forum);
//        }
//    }
//
//    @Override
//    public SharkKB getBaseKB() {
//        return this.baseKB;
//    }
//
//    @Override
//    public SharkKB getProfileKB() {
//        return this.profileKB;
//    }

    /**
     * Produces an interator with peer which are allowed to communicate with
     * this peers. Note: "allowed" depends in a policy which can be set with
     * SharkEngine. The engine keep two lists: a black list and a white list.
     * Peers can be put onto either list by using SNSharkEngine.acceptPeer()
     * with appropriate parameters.
     *
     * There a two policies. A weaker policy allows any peer that is not on the
     * black list to communicate with this peer. The more harsh policy allows
     * only peers to communicate with this peer which are explitely put to the
     * white list.
     *
     * A policy can be set with SNSharkEngine.useWhiteList(boolean).
     *
     * Output of this method depends of chosen policy and status of black and
     * white list.
     *
     * There are two:
     *
     * @return
     * @throws SharkKBException
     */
//    @Override
//    public Iterator<PeerSemanticTag> getAllowedPeers() throws SharkKBException {
//        ArrayList<PeerSemanticTag> allowedPeers = new ArrayList<PeerSemanticTag>();
//
//        Enumeration<PeerSemanticTag> peerEnum = this.getPeers().peerTags();
//        if (peerEnum != null) {
//            while (peerEnum.hasMoreElements()) {
//                PeerSemanticTag peer = peerEnum.nextElement();
//
//                if (this.isAcceptedPeer(peer)) {
//                    allowedPeers.add(peer);
//                }
//            }
//        }
//
//        return allowedPeers.iterator();
//    }
//
//    @SuppressWarnings("unchecked")
//    @Override
//    public Enumeration<PeerSemanticTag> getAllowedPeersAsEnum() throws SharkKBException {
//        return new Iterator2Enumeration(this.getAllowedPeers());
//    }
//
//    @Override
//    public ContextPoint getCPByInfoID(ContextCoordinates cc, String infoID) throws SharkKBException {
//
//        if (cc == null || infoID == null) {
//            throw new SharkKBException("parameter must no be null");
//        }
//
//        ContextPoint cp = this.baseKB.getContextPoint(cc);
//        if (cp == null) {
//            throw new SharkKBException("cannot find context point at coordinates");
//        }
//
//        // find info
//        Information info = null;
//        Enumeration<Information> infoEnum = cp.enumInformation();
//        if (infoEnum == null) {
//            throw new SharkKBException("context point has no information at all");
//        }
//
//        while (infoEnum.hasMoreElements()) {
//            Information i = infoEnum.nextElement();
//
//            String idString = i.getUniqueID();
//            if (idString != null && idString.equalsIgnoreCase(infoID)) {
//                info = i;
//                break;
//            }
//        }
//
//        if (info == null) {
//            throw new SharkKBException("cannot find info with given id at existing context point");
//        }
//
//        Knowledge k = InMemoSharkKB.createInMemoKnowledge();
//
//        // do we have to copy cp?
//        if (cp.getNumberInformation() == 1) {
//            return cp;
//        } else {
//            // copy
//            ContextPoint copyCP = InMemoSharkKB.createInMemoContextPoint(cc);
//            Util.copyPropertiesFromPropertyHolderToPropertyHolder(cp, copyCP);
//
//            return copyCP;
//        }
//    }

    /**
     * Gibt die aktuelle Instanz des Chatmanagers zurück, den diese Net-Instanz
     * nutzt.
     *
     * @return Chatmanager
     */
    public SharkNetChatManager getChatManager() {
        return chatManager;
    }

    @Override
    public SharkVocabulary getVocabulary() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
