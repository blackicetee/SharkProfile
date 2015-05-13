package Profile;

import net.sharkfw.knowledgeBase.SpatialSemanticTag;
import net.sharkfw.knowledgeBase.TimeSemanticTag;

/**
 * Created by Mr.T on 06.05.2015.
 */
public class ProfileEntryImpl implements ProfileEntry {
    private static int uniqueID = 0;
    private byte[] description = null;
    private SpatialSemanticTag location = null;
    private TimeSemanticTag timeFrom = null;
    private TimeSemanticTag timeTo = null;
    private String contentType = null;

    ProfileEntryImpl() {
        this.uniqueID += 1;
    }

    @Override
    public int getUniqueID() {
        return uniqueID;
    }

    @Override
    public void setDescription(byte[] description, ) {

        this.description = description;
    }

    @Override
    public byte[] getDescription() {
        return description;
    }

    @Override
    public void setLocation(SpatialSemanticTag sst) {
        this.location = sst;
    }

    @Override
    public SpatialSemanticTag getLocation() {
        return location;
    }

    @Override
    public void setTimeFrom(TimeSemanticTag timeFrom) {
        this.timeFrom = timeFrom;
    }

    @Override
    public TimeSemanticTag getTimeFrom() {
        return timeFrom;
    }

    @Override
    public void setTimeTo(TimeSemanticTag timeTo) {
        this.timeTo = timeTo;
    }

    @Override
    public TimeSemanticTag getTimeTo() {
        return timeTo;
    }

    @Override
    public void setContentType(String type) {
        contentType = type;

    }

    @Override
    public String getContentType() {
        return contentType;
    }
}
