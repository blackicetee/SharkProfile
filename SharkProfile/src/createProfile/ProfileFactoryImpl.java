package createProfile;

import net.sharkfw.knowledgeBase.PeerSemanticTag;
import net.sharkfw.knowledgeBase.SharkKB;
import net.sharkfw.knowledgeBase.SharkKBException;

/**
 * Created by Mr.T on 06.05.2015.
 */
public class ProfileFactoryImpl implements ProfileFactory {
    private static ProfileFactory pf = null;

    private ProfileFactoryImpl() {

    }

    private ProfileFactoryImpl(SharkKB kb) {

    }
    public static ProfileFactory createProfileFactory(SharkKB kb) {
        if(ProfileFactoryImpl.pf == null)
            pf = new ProfileFactoryImpl(kb);
        return pf;
    }

    @Override
    public Profile getProfile(PeerSemanticTag creator, PeerSemanticTag target) throws SharkKBException {
        return null;
    }

    @Override
    public Profile createProfile(PeerSemanticTag creator, PeerSemanticTag target) throws SharkKBException {
        return null;
    }
}
