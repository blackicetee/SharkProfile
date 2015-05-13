package Profile;

import net.sharkfw.knowledgeBase.PeerSemanticTag;
import net.sharkfw.knowledgeBase.SharkKB;
import net.sharkfw.knowledgeBase.SharkKBException;

/**
 * Created by Mr.T on 06.05.2015.
 */
public class ProfileFactoryImpl implements ProfileFactory {
    private static ProfileFactoryImpl pf = null;
    private SharkKB kb = null;

    private ProfileFactoryImpl() {

    }

    public ProfileFactoryImpl(SharkKB kb) {
        this.kb = kb;
    }
    /** Single Pattern for the ProfileFactory
    public static ProfileFactory getProfileFactory(SharkKB kb) {
        if(ProfileFactoryImpl.pf == null)
            pf = new ProfileFactoryImpl(kb);
        return pf;
    }
     */

    @Override
    public Profile getProfile(PeerSemanticTag creator, PeerSemanticTag target) throws SharkKBException {
        return null;
    }

    @Override
    public Profile createProfile(PeerSemanticTag creator, PeerSemanticTag target) throws SharkKBException {
        ProfileImpl profile = new ProfileImpl(kb, creator, target);
        return profile;
    }
}
