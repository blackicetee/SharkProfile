package Profile;

import net.sharkfw.knowledgeBase.SpatialSemanticTag;

/**
 * Created by Mr.T on 11.05.2015.
 */
public class ProfileCurrentPositionImpl extends ProfileEntryImpl implements ProfileCurrentPosition {
    ProfileCurrentPositionImpl(SpatialSemanticTag sst) {
        super();
        this.setLocation(sst);
    }
}
