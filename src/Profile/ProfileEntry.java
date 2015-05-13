package Profile;

import net.sharkfw.knowledgeBase.*;

/**
 * Created by Mr.T on 22.04.2015.
 */
public interface ProfileEntry {
    int getUniqueID();

    void setDescription(byte[] description, String type);
    byte[] getDescription();

    void setLocation(SpatialSemanticTag sst);
    SpatialSemanticTag getLocation();

    void setTimeFrom(TimeSemanticTag timeFrom);
    TimeSemanticTag getTimeFrom();

    void setTimeTo(TimeSemanticTag timeTo);
    TimeSemanticTag getTimeTo();

    String getContentType();
}
