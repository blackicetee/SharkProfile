package net.sharkfw.subspace.filesystem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;
import net.sharkfw.knowledgeBase.ContextPoint;
import net.sharkfw.knowledgeBase.Information;
import net.sharkfw.knowledgeBase.PeerSTSet;
import net.sharkfw.knowledgeBase.PeerSemanticTag;
import net.sharkfw.knowledgeBase.PeerTaxonomy;
import net.sharkfw.knowledgeBase.SNSemanticTag;
import net.sharkfw.knowledgeBase.SemanticTag;
import net.sharkfw.knowledgeBase.SharkKB;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.knowledgeBase.filesystem.FSGenericTagStorage;
import net.sharkfw.knowledgeBase.filesystem.FSInformation;
import net.sharkfw.knowledgeBase.filesystem.FSSharkKB;
import net.sharkfw.knowledgeBase.inmemory.InMemoGenericTagStorage;
import net.sharkfw.knowledgeBase.inmemory.InMemoSTSet;
import net.sharkfw.knowledgeBase.inmemory.InMemoSharkKB;
import net.sharkfw.peer.J2SEAndroidSharkEngine;
import net.sharkfw.apps.sharknet.impl.InMemoSubSpace;
import net.sharkfw.subspace.SharkSubSpaceException;
import net.sharkfw.subspace.SubSpace;
import net.sharkfw.subspace.SubSpaceGuardKP;
import net.sharkfw.system.L;
import net.sharkfw.system.SharkSecurityException;

/**
 * See {@link SubSpace SubSpace interface} for a description
 * @author thsc
 */
public class FSSubSpace extends InMemoSubSpace {
    /**
     * Constructor refreshes a sn sub space from filesystem.
     * 
     * @param foldername Folder in which sub space is stored
     * @param baseKB 
     * @param peer
     * @throws SharkKBException 
     */
    public FSSubSpace(SubSpaceGuardKP guard, J2SEAndroidSharkEngine se, 
            String foldername, SharkKB baseKB, 
            PeerSemanticTag peer) throws SharkKBException, SharkSubSpaceException {
        
        super(guard);
        
        this.se = se;
        
        // remember base KB
        this.baseKB = baseKB;
        
        // remember foldername
        this.foldername = foldername;
        
        FSSharkKB fskb = this.createFSSharkKB(foldername, baseKB);
        fskb.refreshStatus();
        this.subKB = fskb;
        
        this.peer = peer;
        
        this.subKB.getProperty(SubSpace.SUBSPACE_SUBTYPE);

        String invited = this.getProperty(SUBSPACE_ALL_INVITED);
        if(invited != null) {
            this.inviteAllSucessfully = Boolean.parseBoolean(invited);
        }

//        this.setupEngine();
    }
    
    /**
     * create a new fresh sub space
     * 
     * @param name
     * @param topic
     * @param owner
     * @param member
     * @param readOnlyMember
     * @param foldername proposed folder name
     * @param baseKB
     * @param defaultEntryName
     * @throws SharkKBException 
     */
    public FSSubSpace(SubSpaceGuardKP guard, J2SEAndroidSharkEngine se, 
            String name, 
            String subSpaceSI,
            String childString, 
            Iterator<SemanticTag> topics, 
            PeerSemanticTag owner,
            PeerSemanticTag peer,
            Iterator<PeerSemanticTag> member, 
            Iterator<PeerSemanticTag> readOnlyMember, 
            String foldername, SharkKB baseKB,
            boolean open, 
            boolean readonly,
            String defaultEntryName,
            String subSpaceType) throws SharkSubSpaceException, SharkKBException, SharkSecurityException 
    {
        super(guard);
        
        // define name
        if(name == null) {
            name = "subSpace";
        }

        // create chat own knowledge base.
        FSSharkKB newSubKB = this.createFSSharkKB(foldername + "/" + name, baseKB);
        String newFoldername = newSubKB.getFoldername();
        
        this.setupNewSubSpace(se, name, subSpaceSI, newSubKB, owner, 
                peer, newFoldername, baseKB, open, subSpaceType);
        
        // save topics - if any
        if(topics != null) {
            while(topics.hasNext()) {
                SemanticTag t = topics.next();
                SNSemanticTag toMerge = null;
                if(t instanceof SNSemanticTag) {
                    toMerge = (SNSemanticTag) t;
                } else {
                    toMerge = InMemoSharkKB.createInMemoCopyToSNSemanticTag(t);
                }
                this.subKB.getTopicsAsSemanticNet().merge(toMerge);
            }
        }
        
        PeerTaxonomy peers = this.subKB.getPeersAsTaxonomy();
        
        // fill with participants
        if(member != null) {
            while(member.hasNext()) {
                PeerSemanticTag newPeer = (PeerSemanticTag) peers.merge(member.next());
                this.setReadOnlyMember(newPeer, false);
            }
        }
        
        // fill with read only participants
        if(readOnlyMember != null) {
            while(readOnlyMember.hasNext()) {
                SemanticTag newROPeer = peers.merge(readOnlyMember.next());
                this.setReadOnlyMember(newROPeer, true);
            }
        }
        
        // add owner 
        peers.merge(owner);
        
        // add peer
        this.peer = (PeerSemanticTag) peers.merge(peer);
        
        this.setSubscribed(this.getOwner(), true);
        
        this.setReadOnlyMember(this.getLocalPeer(), readonly);
        
//        if(childString != null) {
//            Vector<String> childDescription = this.getChildDescription(childString);
//            
//            // create childSub based on those descriptions
//            Enumeration<String> childDescrEnum = childDescription.elements();
//            while(childDescrEnum.hasMoreElements()) {
//                String childName = childDescrEnum.nextElement();
//                
//                childName = this.getRealChildName(childName);
//                
//                try {
//                    String childSI = childDescrEnum.nextElement();
//                    
//                    this.setProperty(childName, childSI);
//                }
//                catch(RuntimeException e) {
//                    // oops, wrong format - means we have a bug in string creation code
//                    L.w("malformed childSpaceDescription string: si missing:" + childString, this);
//                    break;
//                }
//            }

//        }
    }
    
    private FSSharkKB createFSSharkKB(String foldername, SharkKB baseKB) 
            throws SharkKBException {
        
        FSSharkKB kb = new FSSharkKB(foldername);
        PeerSTSet peerSTSet = kb.getPeerSTSet();
        kb.setPeers(baseKB.getPeersAsTaxonomy());
        
        /* we replace the whole peer dimension with
         * base KBs dimension. 
         * 
         * Drop filesystem space in foldername/peers now:
         */
        if(peerSTSet instanceof InMemoSTSet) {
            InMemoSTSet stSet = (InMemoSTSet) peerSTSet;
            InMemoGenericTagStorage tagStorage = stSet.getTagStorage();
            
            if(tagStorage instanceof FSGenericTagStorage) {
                FSGenericTagStorage fsts = (FSGenericTagStorage) tagStorage;
                
                FSSharkKB.removeFSStorage(fsts.getFolderName());
            }
        }
        
        return kb;
    }
    
    public String getFoldername() {
        return this.foldername;
    }

    public String getInformationFilename(ContextPoint cp, Information info) throws SharkKBException {
        ContextPoint localCP = this.subKB.getContextPoint(cp.getContextCoordinates());
        
        if(localCP == null) {
            throw new SharkKBException("context point is not in this chat or makan");
        }
        
        Enumeration<Information> infoEnum = localCP.enumInformation();
        if(infoEnum == null) {
            throw new SharkKBException("cannot find information: context point has no information at all");
        }
        
        boolean found = false;
        Information localInformation = null;
        while(infoEnum.hasMoreElements()) {
            localInformation = infoEnum.nextElement();
            
            if(localInformation == info || localInformation.hashCode() == info.hashCode()) {
                found = true;
                break;
            }
        }
        
        if(!found) {
            throw new SharkKBException("information is not part of this chat or makan");
        }
        
        try {
            FSInformation fsInfo = (FSInformation) localInformation;
            return fsInfo.getContentFilename();
        }
        catch(ClassCastException cce) {
            // create tempFile and copy that info
            try {
                File tmpFile = this.createTempFile(localInformation);
                return tmpFile.getAbsolutePath();
            }
            catch(IOException ioe) {
                throw new SharkKBException("cannot create temporary files and/or stream info content in it");
            }
        }
        catch(IOException cce) {
            L.w("cannot read info file name - fatal and strange", this);
            throw new SharkKBException("cannot read file system info file name - fatal and strange");
        }
    }
    
    private String tmpInfoFolderName = null;
    private File tmpInfoFolder = null;
    
    private File createTempFile(Information i) throws IOException {
        if(this.tmpInfoFolderName == null) {
            this.tmpInfoFolderName = this.getFoldername() + "/tmpInfoCopies";
            this.tmpInfoFolder = new File(this.tmpInfoFolderName);
            this.tmpInfoFolder.mkdirs();
        }
        
        File tmpFile = File.createTempFile("InfoTemp_", "sharkfw", this.tmpInfoFolder);
        
        i.streamContent(new FileOutputStream(tmpFile));
        
        return tmpFile;
    }
    
    @Override
    public void remove() {
        super.remove();
        
        if(this.subKB instanceof FSSharkKB) {
            FSSharkKB fsKB = (FSSharkKB)this.subKB;
            FSSharkKB.removeFSStorage(fsKB.getFoldername());
        }
    }
}
