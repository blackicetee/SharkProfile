package Profile;

import net.sharkfw.knowledgeBase.*;
import net.sharkfw.knowledgeBase.inmemory.InMemoInformation;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by Mr.T on 16.04.2015.
 */
public class ProfileImpl implements Profile, Serializable {
    private ContextPoint cp;
    private SharkKB kb;
    private PeerSemanticTag profileOwner = null;
    private PeerSemanticTag profileCreator = null;
    ProfileImpl(SharkKB kb, PeerSemanticTag creator, PeerSemanticTag target) throws SharkKBException {
        this.kb = kb;
        this.profileOwner = target;
        this.profileCreator = creator;
        SemanticTag pr = this.kb.createSemanticTag("Profile", "http://www.sharksystem.net/Profile.html");
        //ist jetzt der Originator also der erste PST Tag der creator oder ist er das Target?
        ContextCoordinates cc = kb.createContextCoordinates(pr, creator, target, null, null, null, SharkCS.DIRECTION_INOUT);
        cp = this.kb.createContextPoint(cc);
    }

    ProfileImpl(SharkKB kb, ContextPoint cp) {
        this.kb = kb;
        this.cp = cp;
        profileOwner = cp.getContextCoordinates().getPeer();
        profileCreator = cp.getContextCoordinates().getOriginator();
    }

    private void addAndSerializeObjInContextPoint(String objName, Object obj) throws SharkKBException {
        Information i = new InMemoInformation();
        try {
            i.setContent(Serializer.serialize(obj));
        } catch (IOException e) {
            throw new SharkKBException(e.getMessage());
        }
        i.setName(objName);
        cp.addInformation(i);
    }

    private Object getAndDeserializeObjFromContextPoint(String objName) throws SharkKBException {
        Information i = cp.getInformation(objName).next();
        try {
            return Serializer.deserialize(i.getContentAsByte());
        } catch (IOException e) {
            throw new SharkKBException(e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new SharkKBException(e.getMessage());
        }
    }

    @Override
    public PeerSemanticTag getProfileOwner() {
        return profileOwner;
    }

    @Override
    public PeerSemanticTag getProfileCreator() {
        return profileCreator;
    }

    @Override
    public void setName(ProfileName profileName) throws SharkKBException {
        addAndSerializeObjInContextPoint("ProfileName", profileName);
    }

    @Override
    public ProfileName getName() throws SharkKBException {
        return (ProfileName) getAndDeserializeObjFromContextPoint("ProfileName");
    }

    //Interface von Information anschauen
    //Bei jeder Methode überlegen wie ich sie Implementieren würde bis zu nächster Woche
    @Override
    public void setPicture(byte[] content, String contentType, String identifier) throws SharkKBException {
        Information i = cp.addInformation(content);
        i.setContentType(contentType);
        i.setName(identifier);
    }

    @Override
    public Information getPicture(String identifier) throws SharkKBException {
        return cp.getInformation(identifier).next();
    }

    @Override
    public void clearPicture(String identifier) throws SharkKBException {
        cp.removeInformation(getPicture(identifier));
    }

    @Override
    public void setBirthday(Date datum) throws SharkKBException {
        addAndSerializeObjInContextPoint("ProfileBirthday", datum);
    }

    @Override
    public Date getBirthday() throws SharkKBException {
        return (Date) getAndDeserializeObjFromContextPoint("ProfileBirthday");
    }

    @Override
    public String[] getProfileAddresses() {
        return profileOwner.getAddresses();
    }

    @Override
    public void setTelephoneNumber(String number, String identifier) throws SharkKBException {
        addAndSerializeObjInContextPoint(identifier, number);
    }

    @Override
    public String getTelephoneNumber(String identifier) throws SharkKBException {
            return (String) getAndDeserializeObjFromContextPoint(identifier);
    }

    @Override
    public void setQualifications(ProfileQualification profileQualification, String identifier) throws SharkKBException {
        addAndSerializeObjInContextPoint(identifier, profileQualification);
    }

    @Override
    public ProfileQualification getQualification(String identifier) throws SharkKBException {
        return (ProfileQualification) getAndDeserializeObjFromContextPoint(identifier);
    }

    @Override
    public void setKnownLanguages(ProfileKnownLanguages knownLanguages) throws SharkKBException {
        addAndSerializeObjInContextPoint("KnownLanguages", knownLanguages);
    }

    @Override
    public ProfileKnownLanguages getKnownLanguages() throws SharkKBException {
        return (ProfileKnownLanguages) getAndDeserializeObjFromContextPoint("KnownLanguages");
    }

    @Override
    public void setProblem(ProfileProblem profileProblem, String identifier) throws SharkKBException {
        addAndSerializeObjInContextPoint(identifier, profileProblem);
    }

    @Override
    public ProfileProblem getProblem(String identifier) throws SharkKBException {
        return (ProfileProblem) getAndDeserializeObjFromContextPoint(identifier);
    }

    @Override
    public void setCurrentPosition(ProfileCurrentPosition currentPosition) throws SharkKBException {
        addAndSerializeObjInContextPoint("CurrentPosition", currentPosition);
    }

    @Override
    public ProfileCurrentPosition getCurrentPosition() throws SharkKBException {
        return (ProfileCurrentPosition) getAndDeserializeObjFromContextPoint("CurrentPosition");
    }

    @Override
    public void setSupportPossibilities(ProfileSupportPossibilities profileSupportPossibilities, String identifier) throws SharkKBException {
        addAndSerializeObjInContextPoint(identifier, profileSupportPossibilities);
    }

    @Override
    public ProfileSupportPossibilities getSupportPossibilities(String identifier) throws SharkKBException {
        return (ProfileSupportPossibilities) getAndDeserializeObjFromContextPoint(identifier);
    }

}
