package createProfile;

import net.sharkfw.knowledgeBase.*;

import java.io.InputStream;
import java.util.Date;

/**
 * Created by Mr.T on 16.04.2015.
 */
public interface Profile {
    //interface profileNameImpl{getlastName(); getSurname(); getTitle()}
    void setName(ProfileNameImpl profileNameImpl);//Parameter vielleicht mit SemantixTagSet? Vor und Nachname
    ProfileNameImpl getName() throws SharkKBException;
    void nameUsed(boolean used);

    void setPicture(String filename, String contentType) throws SharkKBException;
    void setPicture(byte[] content, String contentType) throws SharkKBException;
    void setPicture(InputStream picture, long len, String contentType) throws SharkKBException;
    //Wenn Information einen Content Type zurück gibt ist es ok, wenn nicht dann genau so wie bei ProfileNameImpl
    //Im Information Interface existiert eine Funktion getContentType welche den ContentType liefert
    //Package knowledgeBase Interface Information = java.lang.String getContentType();
    Information getPicture() throws SharkKBException;
    void clearPicture();
    void pictureUsed(boolean used);

    void setBirthday(Date datum);
    Date getBirthday();
    void birthdayUsed(boolean used);

    //das setProfileAdress ist überflüssig, aber das get ist gut in dem Fall
    PeerSemanticTag getProfileAddresses();
    void profileAddressesUsed(boolean used);

    void setTelephoneNumber(String number);
    String getTelephoneNumber();
    void telephoneNumberUsed(boolean used);

    void setQualifications(ProfileQualification profileQualification);
    ProfileQualification getQualification();
    void qualificationUsed(boolean used);

    //Standart für z.B. dt.dt oder eng.eng oder dt.sz suchen und überlegen, in welchem Format ich die Sprachen abspeichern möchte
    //ISO 639-1 Sprachcodes z.B. Germany = Deutschland = de, French = Französisch = fr
    void setKnownLanguages(String[] knownLanguages);
    String[] getKnownLanguages();
    void knownLanguagesUsed(boolean used);
    //1,2,
    void setProblem(ProfileProblem profileProblem);
    ProfileProblem getProblem();
    void problemUsed(boolean used);

    void setCurrentPosition(SpatialSemanticTag currentPosition);//Nur der  Aufenthaltsort
    void setCurrentPosition(SpatialSemanticTag currentPosition, TimeSemanticTag fromTo);// Aufenthaltsort+Zeitangabe bzw. Ablaufdatum
    SpatialSemanticTag getCurrentPosition();
    void currentPositionUsed(boolean used);

    void setPosition(SpatialSemanticTag sst);
    SpatialSemanticTag getPosition() throws SharkKBException;
    void positionUsed(boolean used); //Möglichkeit das Position nicht freigeschaltet ist

    void setSupportPossibilities(ProfileSupportPossibilities profileSupportPossibilities);
    ProfileSupportPossibilities getSupportPossibilities();
    void supportPossibilitiesUsed(boolean used);


}
