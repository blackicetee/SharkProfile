package Profile;

import net.sharkfw.knowledgeBase.SpatialSemanticTag;
import net.sharkfw.knowledgeBase.TimeSemanticTag;

import java.io.Serializable;

/**
 * Created by Mr.T on 06.05.2015.
 */
public class ProfileEntryImpl implements ProfileEntry, Serializable {
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
    public void setDescription(byte[] description, String type) {
        this.description = description;
        this.contentType = type;
    }

    @Override
    public byte[] getDescription() {
        return description;
    }

    @Override
    public void setLocation(SpatialSemanticTag sst) {
        location = sst;
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
    public String getDescriptionContentType() {
        return contentType;
    }
}
