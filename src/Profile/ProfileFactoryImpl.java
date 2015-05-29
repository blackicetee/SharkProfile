package Profile;

import net.sharkfw.knowledgeBase.*;
import net.sharkfw.knowledgeBase.inmemory.InMemoSharkKB;

/**
 * Created by Mr.T on 06.05.2015.
 */
public class ProfileFactoryImpl implements ProfileFactory {
    private SharkKB kb = null;

    public ProfileFactoryImpl(SharkKB kb) throws SharkKBException {
        this.kb = kb;
    }
    //To Do
    @Override
    public Profile getProfile(PeerSemanticTag creator, PeerSemanticTag target) throws SharkKBException {
        SemanticTag pr = this.kb.createSemanticTag("Profile", "http://www.sharksystem.net/Profile.html");
        ContextCoordinates cc = InMemoSharkKB.createInMemoContextCoordinates(pr, creator, target, null, null, null, SharkCS.DIRECTION_INOUT);
        Knowledge k = SharkCSAlgebra.extract(kb, cc);
        Profile p = new ProfileImpl(kb, k.contextPoints().nextElement());
        return p;
    }

    @Override
    public Profile createProfile(PeerSemanticTag creator, PeerSemanticTag target) throws SharkKBException {
        ProfileImpl profile = new ProfileImpl(kb, creator, target);
        return profile;
    }
}
