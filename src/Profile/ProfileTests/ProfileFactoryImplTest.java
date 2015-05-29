package Profile.ProfileTests;

import Profile.ProfileFactory;
import Profile.ProfileFactoryImpl;
import net.sharkfw.knowledgeBase.PeerSemanticTag;
import net.sharkfw.knowledgeBase.SharkKB;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.knowledgeBase.inmemory.InMemoSharkKB;
import org.junit.Test;

/**
 * Created by Mr.T on 29.05.2015.
 */
public class ProfileFactoryImplTest {
    private SharkKB kb = new InMemoSharkKB();
    private ProfileFactory profileFactory;
    private String[] aliceAddresses = {"mail://alice@wonderland.net", "mail://alice@wizards.net", "http://www.sharksystem.net/alice.html"};

    public ProfileFactoryImplTest() throws SharkKBException {
        profileFactory = new ProfileFactoryImpl(kb);
    }

    private PeerSemanticTag createAlice() throws SharkKBException {
        return this.kb.createPeerSemanticTag("Alice",
                "http://www.sharksystem.net/alice.html",
                aliceAddresses);

    }
    @Test
    public void testGetProfile() throws Exception {

    }

    @Test
    public void testCreateProfile() throws Exception {

    }
}