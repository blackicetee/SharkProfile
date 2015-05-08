package createProfile;

import net.sharkfw.knowledgeBase.*;

/**
 * Created by Mr.T on 22.04.2015.
 */
public interface ProfileEntry {
    void setUniqueName(String uniqueName);
    String getUniqueName();

    void setDescription(byte[] description);
    byte[] getDescription();

    void setLocation(SpatialSemanticTag sst);
    SpatialSemanticTag getLocation();

    void setTimeFrom(TimeSemanticTag timeFrom);
    TimeSemanticTag getTimeFrom();

    void setTimeTo(TimeSemanticTag timeTo);
    TimeSemanticTag getTimeTo();

    void setContentType(String type);
    String getContentType();
}
