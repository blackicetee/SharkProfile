package Profile;

import net.sharkfw.kep.format.XMLSerializer;
import net.sharkfw.knowledgeBase.*;
import net.sharkfw.knowledgeBase.inmemory.InMemoSharkKB;

import java.io.Serializable;

/**
 * Created by Mr.T on 06.05.2015.
 */
public class ProfileEntryImpl implements ProfileEntry, Serializable {
    private static int uniqueID = 0;
    private byte[] description = null;
    private String location = null;
    private String timeFrom = null;
    private String timeTo = null;
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
    public void setLocation(SpatialSemanticTag sst) throws SharkKBException {
        SpatialSTSet sstSet = InMemoSharkKB.createInMemoSpatialSTSet();
        sstSet.merge(sst);
        XMLSerializer xs = new XMLSerializer();
        location = xs.serializeSTSet(sstSet);
    }

    @Override
    public SpatialSemanticTag getLocation() throws SharkKBException {
        SpatialSTSet SSTSet = InMemoSharkKB.createInMemoSpatialSTSet();
        XMLSerializer xs = new XMLSerializer();
        xs.deserializeSTSet(SSTSet, location);
        return SSTSet.spatialTags().nextElement();
    }

    @Override
    public void setTimeFrom(TimeSemanticTag timeFrom) throws SharkKBException {
        TimeSTSet TSTSet = InMemoSharkKB.createInMemoTimeSTSet();
        TSTSet.createTimeSemanticTag(timeFrom.getDuration(), timeFrom.getFrom());
        XMLSerializer xs = new XMLSerializer();
        this.timeFrom = xs.serializeSTSet(TSTSet);
    }

    @Override
    public TimeSemanticTag getTimeFrom() throws SharkKBException {
        TimeSTSet TSTSet = InMemoSharkKB.createInMemoTimeSTSet();
        XMLSerializer xs = new XMLSerializer();
        xs.deserializeSTSet(TSTSet, timeFrom);
        return TSTSet.timeTags().nextElement();
    }

    @Override
    public void setTimeTo(TimeSemanticTag timeTo) throws SharkKBException {
        TimeSTSet TSTSet = InMemoSharkKB.createInMemoTimeSTSet();
        TSTSet.createTimeSemanticTag(timeTo.getDuration(), timeTo.getFrom());
        XMLSerializer xs = new XMLSerializer();
        this.timeTo = xs.serializeSTSet(TSTSet);
    }

    @Override
    public TimeSemanticTag getTimeTo() throws SharkKBException {
        TimeSTSet TSTSet = InMemoSharkKB.createInMemoTimeSTSet();
        XMLSerializer xs = new XMLSerializer();
        xs.deserializeSTSet(TSTSet, timeTo);
        return TSTSet.timeTags().nextElement();
    }

    @Override
    public String getDescriptionContentType() {
        return contentType;
    }
}
