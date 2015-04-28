package net.sharkfw.apps.sharknet.j2se_android;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import net.sharkfw.apps.sharknet.SharkNetChat;
import net.sharkfw.apps.sharknet.SharkNetException;
import net.sharkfw.knowledgeBase.PeerSemanticTag;
import net.sharkfw.knowledgeBase.SemanticTag;
import net.sharkfw.knowledgeBase.SharkKB;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.subspace.SharkSubSpaceException;
import net.sharkfw.subspace.SubSpace;
import net.sharkfw.subspace.SubSpaceGuardKP;
import net.sharkfw.system.SharkException;
import net.sharkfw.system.SharkSecurityException;
import net.sharkfw.system.Util;

/**
 *
 * @author thsc
 */
public class SharkNetChatManager {
	public static final String SNE_FORUM_FOLDERNAME_PROPERTY = "sn_forumFolder";
	public static final String SNE_BOARD_FOLDERNAME_PROPERTY = "sn_boardFolder";
	public static final String SNE_THREAD_FOLDERNAME_PROPERTY = "sn_threadFolder";

	private final SharkNetEngine sharkEngine;
	private final String foldername;
	private final SharkKB baseKB;

	List<SharkNetChat> chatList = new ArrayList<SharkNetChat>();
    private final SubSpaceGuardKP guardKP;

	SharkNetChatManager(SubSpaceGuardKP guard, SharkNetEngine sharkEngine, SharkKB baseKB, String foldername) {
            this.guardKP = guard;
            this.sharkEngine = sharkEngine;
            this.foldername = foldername;
            this.baseKB = baseKB;
	}

	String getFoldername() {
		return this.foldername;
	}

	PeerSemanticTag getOwner() {
		return this.baseKB.getOwner();
	}

	SharkKB getKB() {
		return this.baseKB;
	}

        public Iterator<SharkNetChat> getChats() {
            return this.chatList.iterator();
        }
        
	public SharkNetChat createChat(String name, SemanticTag topic, Iterator<PeerSemanticTag> member) throws SharkKBException, SharkSubSpaceException, SharkSecurityException {
		return this.createChat(name, null, topic, this.getOwner(), this.getOwner(), member, null, false, false);
	}

	public SharkNetChat createChat(String name, String chatSI, SemanticTag topic, // topic of this chat
			PeerSemanticTag chatOwner, PeerSemanticTag peer, Iterator<PeerSemanticTag> member, Iterator<PeerSemanticTag> roMember, boolean open, boolean readonly) throws SharkKBException, SharkSubSpaceException, SharkSecurityException {

		List<SemanticTag> topics = new ArrayList<SemanticTag>();

		topics.add(topic);

		SharkNetChat_Impl chat = new SharkNetChat_Impl(this.guardKP, 
                        this.sharkEngine, name, chatSI, topics.iterator(), 
                        chatOwner, peer, member, roMember, open, readonly, 
                        this.getFoldername() + SharkNetEngine.CHATS_SUBDIRECTORY, 
                        this.getKB());

		this.chatList.add(chat);

		this.persist();

		return chat;
	}

	public void closeChat(SharkNetChat chat) throws SharkSubSpaceException, SharkKBException {
		this.chatList.remove(chat);

		chat.close();

		this.persist();

	}

	public void leaveChat(SharkNetChat chat) throws SharkSubSpaceException, SharkKBException {
		this.closeChat(chat);
	}

	public void persist() {
		// persist chats
//		Iterator i = this.chatList.iterator();
//		String foldernames = SharkNetEngine.foldernameIter2PropertyStringValue(i);
//
//		this.getKB().setProperty(SharkNetEngine.SNE_CHAT_FOLDERNAME_PROPERTY, foldernames, false);
//
//		i = this.forumList.iterator();
//		foldernames = SharkNetEngine.foldernameIter2PropertyStringValue(i);
//		this.getKB().setProperty(SNE_FORUM_FOLDERNAME_PROPERTY, foldernames, false);
//
//		i = this.boardMapping.values().iterator();
//		foldernames = SharkNetEngine.foldernameIter2PropertyStringValue(i);
//		this.getKB().setProperty(SNE_BOARD_FOLDERNAME_PROPERTY, foldernames, false);
//
//		i = this.threadMapping.values().iterator();
//		foldernames = SharkNetEngine.foldernameIter2PropertyStringValue(i);
//		this.getKB().setProperty(SNE_THREAD_FOLDERNAME_PROPERTY, foldernames, false);
	}

	public final void refreshStatus() throws SharkKBException, SharkNetException, SharkSubSpaceException {

		// chats
		String folderNamesString = this.getKB().getProperty(SharkNetEngine.SNE_CHAT_FOLDERNAME_PROPERTY);
		Vector<String> folderNames = Util.string2Vector(folderNamesString, SharkNetEngine.DELIMITER);
		if (folderNames != null) {
			// bring all cps (back) into memory
			Enumeration<String> chatFoldernameEnum = folderNames.elements();
			while (chatFoldernameEnum.hasMoreElements()) {
				String chatFoldername = chatFoldernameEnum.nextElement();

				// restore
				SharkNetChat_Impl chat;
                            try { 
                                chat = new SharkNetChat_Impl(this.guardKP, 
                                        this.sharkEngine, chatFoldername, 
                                        this.getKB(), this.getOwner());
                            } catch (SharkSubSpaceException ex) {
                                throw new SharkNetException(ex.getMessage());
                            }
				this.chatList.add(chat);
			}
		}

		// threads
	}
}
