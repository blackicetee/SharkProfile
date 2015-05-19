package subSpaceTests;

import net.sharkfw.apps.sharknet.SharkNetChat;
import net.sharkfw.apps.sharknet.SharkNetChatEntry;
import net.sharkfw.apps.sharknet.SharkNetChatListener;
import net.sharkfw.apps.sharknet.SharkNetListener;
import net.sharkfw.knowledgeBase.PeerSemanticTag;

/**
 *
 * @author thsc
 */
public class SNListener_Dummy implements SharkNetListener, SharkNetChatListener {
     public SharkNetChat lastInvitedChat;


    @Override
    public void notifyInvitation(SharkNetChat chat) {
        lastInvitedChat = chat;
    }

    //////////////////////////////////////////////////////////////////////
    //                        chat listener                             //
    //////////////////////////////////////////////////////////////////////
    
    public SharkNetChatEntry lastChatEntry = null;

    @Override
    public void newEntryReached(SharkNetChatEntry snce) {
        lastChatEntry = snce;
    }

    @Override
    public void notifyProfileAdded(PeerSemanticTag peer) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
