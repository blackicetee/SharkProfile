package Profile;

import net.sharkfw.knowledgeBase.*;

/**
 * Created by Mr.T on 22.04.2015.
 */
public interface ProfileEntry {
    int getUniqueID();

    void setDescription(byte[] description, String type);
    byte[] getDescription();

    void setLocation(SpatialSemanticTag sst) throws SharkKBException;
    SpatialSemanticTag getLocation() throws SharkKBException;

    void setTimeFrom(TimeSemanticTag timeFrom) throws SharkKBException;
    TimeSemanticTag getTimeFrom() throws SharkKBException;

    void setTimeTo(TimeSemanticTag timeTo) throws SharkKBException;
    TimeSemanticTag getTimeTo() throws SharkKBException;

    String getDescriptionContentType();
}
