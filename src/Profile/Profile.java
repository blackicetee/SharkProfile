package Profile;

import net.sharkfw.knowledgeBase.*;

import java.io.IOException;
import java.util.Date;

/**
 * Created by Mr.T on 16.04.2015.
 */
public interface Profile {
    //interface profileNameImpl{getlastName(); getSurname(); getTitle()}
    void setName(ProfileName profileName) throws SharkKBException, IOException;//Parameter vielleicht mit SemantixTagSet? Vor und Nachname
    ProfileName getName() throws SharkKBException, IOException, ClassNotFoundException;

    void setPicture(byte[] content, String contentType) throws SharkKBException;
    //Wenn Information einen Content Type zurück gibt ist es ok, wenn nicht dann genau so wie bei ProfileNameImpl
    //Im Information Interface existiert eine Funktion getContentType welche den ContentType liefert
    //Package knowledgeBase Interface Information = java.lang.String getContentType();
    Information getPicture() throws SharkKBException;
    void clearPicture() throws SharkKBException;

    void setBirthday(Date datum) throws IOException, SharkKBException;
    Date getBirthday() throws IOException, ClassNotFoundException, SharkKBException;

    //das setProfileAdress ist überflüssig, aber das get ist gut in dem Fall
    String[] getProfileAddresses();

    void setTelephoneNumber(String number) throws SharkKBException, IOException;
    String getTelephoneNumber() throws SharkKBException;

    void setQualifications(ProfileQualification profileQualification) throws IOException, SharkKBException;
    ProfileQualification getQualification() throws IOException, ClassNotFoundException, SharkKBException;

    //Standart für z.B. dt.dt oder eng.eng oder dt.sz suchen und überlegen, in welchem Format ich die Sprachen abspeichern möchte
    //ISO 639-1 Sprachcodes z.B. Germany = Deutschland = de, French = Französisch = fr
    void setKnownLanguages(ProfileKnownLanguages knownLanguages) throws IOException, SharkKBException;
    ProfileKnownLanguages getKnownLanguages() throws IOException, ClassNotFoundException;
    //1,2,
    void setProblem(ProfileProblem profileProblem) throws IOException, SharkKBException;
    ProfileProblem getProblem() throws IOException, ClassNotFoundException;

    void setCurrentPosition(ProfileCurrentPosition currentPosition) throws IOException, SharkKBException;//Nur der  Aufenthaltsort /  Aufenthaltsort+Zeitangabe bzw. Ablaufdatum
    ProfileCurrentPosition getCurrentPosition() throws IOException, ClassNotFoundException;

    void setSupportPossibilities(ProfileSupportPossibilities profileSupportPossibilities) throws IOException, SharkKBException;
    ProfileSupportPossibilities getSupportPossibilities() throws IOException, ClassNotFoundException;


}
