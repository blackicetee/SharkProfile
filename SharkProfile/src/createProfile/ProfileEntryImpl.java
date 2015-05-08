package createProfile;

import net.sharkfw.knowledgeBase.SpatialSemanticTag;
import net.sharkfw.knowledgeBase.TimeSemanticTag;

/**
 * Created by Mr.T on 06.05.2015.
 */
public class ProfileEntryImpl implements ProfileEntry {
    @Override
    public void setUniqueName(String uniqueName) {

    }

    @Override
    public String getUniqueName() {
        return null;
    }

    @Override
    public void setDescription(byte[] description) {

    }

    @Override
    public byte[] getDescription() {
        return new byte[0];
    }

    @Override
    public void setLocation(SpatialSemanticTag sst) {

    }

    @Override
    public SpatialSemanticTag getLocation() {
        return null;
    }

    @Override
    public void setTimeFrom(TimeSemanticTag timeFrom) {

    }

    @Override
    public TimeSemanticTag getTimeFrom() {
        return null;
    }

    @Override
    public void setTimeTo(TimeSemanticTag timeTo) {

    }

    @Override
    public TimeSemanticTag getTimeTo() {
        return null;
    }

    @Override
    public void setContentType(String type) {

    }

    @Override
    public String getContentType() {
        return null;
    }
}
