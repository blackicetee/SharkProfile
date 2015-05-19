package Profile;

import net.sharkfw.knowledgeBase.Information;
import net.sharkfw.knowledgeBase.PeerSemanticTag;
import net.sharkfw.knowledgeBase.SharkKBException;

import java.util.Date;

/**
 * Created by Mr.T on 16.04.2015.
 */
public interface Profile {
    /**This function returns the owner of the profile.
     *
     * @return The owner of the profile.
     */
    PeerSemanticTag getProfileOwner();

    /**This function returns the creator of the profile.
     *
     * @return The creator of the profile.
     */
    PeerSemanticTag getProfileCreator();

    /**This function sets a profile name.
     * A profile name is represented as an profileName object.
     * A surname, a lastname and a title can be stored in such an profileName object.
     *
     * @param profileName This is the profile name object.
     * @throws SharkKBException This message is thrown when no SharkKB is found or if there is another problem with the SharkKB.
     */
    void setName(ProfileName profileName) throws SharkKBException;//Parameter vielleicht mit SemantixTagSet? Vor und Nachname

    /**This function returns an profile name.
     * A profile name is represented as an profileName object.
     * A surname, a lastname and a title can be stored in such an profileName object.
     *
     * @return The profile name object.
     * @throws SharkKBException This message is thrown when no SharkKB is found or if there is another problem with the SharkKB.
     */
    ProfileName getName() throws SharkKBException;

    /**This function sets a profile picture.
     * A profile picture contains two elements.
     * The first element is the content(the picture itself) as byte array
     * and the second element is the content type.
     *
     * @param content This is the picture itself as byte array.
     * @param contentType This is the type of the content.
     * @throws SharkKBException
     */
    void setPicture(byte[] content, String contentType) throws SharkKBException;

    /**This function returns an Information about the profile picture.
     * Information contains content.
     * Here the content is the profile picture and the content type of this picture.
     *
     * @return The profile picture stored in an Information object.
     * @throws SharkKBException This message is thrown when no SharkKB is found or if there is another problem with the SharkKB.
     */
    Information getPicture() throws SharkKBException;

    /**This function clears the profile picture.
     * After you use that function a set profile picture from that profile is deleted.
     *
     * @throws SharkKBException This message is thrown when no SharkKB is found or if there is another problem with the SharkKB.
     */
    void clearPicture() throws SharkKBException;

    /**
     *
     * @param datum
     * @throws SharkKBException
     */
    void setBirthday(Date datum) throws SharkKBException;
    Date getBirthday() throws SharkKBException;

    //das setProfileAdress ist überflüssig, aber das get ist gut in dem Fall
    String[] getProfileAddresses();

    void setTelephoneNumber(String number) throws SharkKBException;
    String getTelephoneNumber() throws SharkKBException;

    void setQualifications(ProfileQualification profileQualification) throws SharkKBException;
    ProfileQualification getQualification() throws SharkKBException;

    //Standart für z.B. dt.dt oder eng.eng oder dt.sz suchen und überlegen, in welchem Format ich die Sprachen abspeichern möchte
    //ISO 639-1 Sprachcodes z.B. Germany = Deutschland = de, French = Französisch = fr
    void setKnownLanguages(ProfileKnownLanguages knownLanguages) throws SharkKBException;
    ProfileKnownLanguages getKnownLanguages() throws SharkKBException;
    //1,2,
    void setProblem(ProfileProblem profileProblem) throws SharkKBException;
    ProfileProblem getProblem() throws SharkKBException;

    void setCurrentPosition(ProfileCurrentPosition currentPosition) throws SharkKBException;//Nur der  Aufenthaltsort /  Aufenthaltsort+Zeitangabe bzw. Ablaufdatum
    ProfileCurrentPosition getCurrentPosition() throws SharkKBException;

    void setSupportPossibilities(ProfileSupportPossibilities profileSupportPossibilities) throws SharkKBException;
    ProfileSupportPossibilities getSupportPossibilities() throws SharkKBException;
}
