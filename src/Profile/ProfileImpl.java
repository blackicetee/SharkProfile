package Profile;

import net.sharkfw.knowledgeBase.*;
import net.sharkfw.knowledgeBase.inmemory.InMemoInformation;

import java.io.IOException;
import java.util.Date;

/**
 * Created by Mr.T on 16.04.2015.
 */
public class ProfileImpl implements Profile {
    private ContextPoint cp;
    private SharkKB kb;
    private PeerSemanticTag profileOwner = null;
    private PeerSemanticTag profileCreator = null;
    ProfileImpl(SharkKB kb, PeerSemanticTag creator, PeerSemanticTag target) throws SharkKBException {
        this.kb = kb;
        this.profileOwner = target;
        this.profileCreator = creator;
        SemanticTag pr = kb.createSemanticTag("Profile", "http://www.sharksystem.net/Profile.html");
        //ist jetzt der Originator also der erste PST Tag der creator oder ist er das Target?
        ContextCoordinates cc = kb.createContextCoordinates(pr, creator, target, null, null, null, SharkCS.DIRECTION_INOUT);
        cp = kb.createContextPoint(cc);
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
    public void setName(ProfileName profileName) throws SharkKBException, IOException {
        addAndSerializeObjInContextPoint("ProfileName", profileName);
    }

    @Override
    public ProfileName getName() throws SharkKBException, IOException, ClassNotFoundException {
        return (ProfileName) getAndDeserializeObjFromContextPoint("ProfileName");
    }

    //Interface von Information anschauen
    //Bei jeder Methode überlegen wie ich sie Implementieren würde bis zu nächster Woche
    @Override
    public void setPicture(byte[] content, String contentType) throws SharkKBException {
        Information i = cp.addInformation(content);
        i.setContentType(contentType);
        i.setName("ProfilePicture");
    }

    @Override
    public Information getPicture() throws SharkKBException {
        return cp.getInformation("ProfilePicture").next();
    }

    @Override
    public void clearPicture() throws SharkKBException {
        cp.removeInformation(getPicture());
    }

    @Override
    public void setBirthday(Date datum) throws IOException, SharkKBException {
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
    public void setTelephoneNumber(String number) throws SharkKBException, IOException {
        addAndSerializeObjInContextPoint("Telephonenumber", number);
    }

    @Override
    public String getTelephoneNumber() throws SharkKBException {
            return (String) getAndDeserializeObjFromContextPoint("Telephonenumber");
    }

    @Override
    public void setQualifications(ProfileQualification profileQualification) throws IOException, SharkKBException {
        addAndSerializeObjInContextPoint("Qualification", profileQualification);
    }

    @Override
    public ProfileQualification getQualification() throws SharkKBException {
        return (ProfileQualification) getAndDeserializeObjFromContextPoint("Qualification");
    }

    @Override
    public void setKnownLanguages(ProfileKnownLanguages knownLanguages) throws IOException, SharkKBException {
        addAndSerializeObjInContextPoint("KnownLanguages", knownLanguages);
    }

    @Override
    public ProfileKnownLanguages getKnownLanguages() throws IOException, ClassNotFoundException {
        return (ProfileKnownLanguages) getAndDeserializeObjFromContextPoint("KnownLanguages");
    }

    @Override
    public void setProblem(ProfileProblem profileProblem) throws IOException, SharkKBException {
        addAndSerializeObjInContextPoint("Problem", profileProblem);
    }

    @Override
    public ProfileProblem getProblem() throws IOException, ClassNotFoundException {
        return (ProfileProblem) getAndDeserializeObjFromContextPoint("Problem");
    }

    @Override
    public void setCurrentPosition(ProfileCurrentPosition currentPosition) throws IOException, SharkKBException {
        addAndSerializeObjInContextPoint("CurrentPosition", currentPosition);
    }

    @Override
    public ProfileCurrentPosition getCurrentPosition() throws IOException, ClassNotFoundException {
        return (ProfileCurrentPosition) getAndDeserializeObjFromContextPoint("CurrentPosition");
    }

    @Override
    public void setSupportPossibilities(ProfileSupportPossibilities profileSupportPossibilities) throws IOException, SharkKBException {
        addAndSerializeObjInContextPoint("SupportPossibilities", profileSupportPossibilities);
    }

    @Override
    public ProfileSupportPossibilities getSupportPossibilities() throws IOException, ClassNotFoundException {
        return (ProfileSupportPossibilities) getAndDeserializeObjFromContextPoint("SupportPossibilities");
    }

}
