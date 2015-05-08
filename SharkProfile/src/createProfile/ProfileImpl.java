package createProfile;

import net.sharkfw.knowledgeBase.*;

import java.io.InputStream;
import java.lang.Override;
import java.util.Date;

/**
 * Created by Mr.T on 16.04.2015.
 */
public class ProfileImpl implements Profile {
    private ContextPoint cp;
    ProfileImpl(SharkKB kb) {
        //this.cp = kb.createContextPoint();
    }

    ProfileImpl(ContextPoint cp) {

    }
    @Override
    public void setName(ProfileNameImpl profileNameImpl) {

    }

    @Override
    public ProfileNameImpl getName() throws SharkKBException {
        return null;
    }

    @Override
    public void nameUsed(boolean used) {

    }

    @Override
    public void setPicture(String filename, String contentType) throws SharkKBException {

    }
    //Interface von Information anschauen
    //Bei jeder Methode überlegen wie ich sie Implementieren würde bis zu nächster Woche
    @Override
    public void setPicture(byte[] content, String contentType) throws SharkKBException {
        Information i = cp.addInformation(content);
        i.setContentType(contentType);
    }

    @Override
    public void setPicture(InputStream picture, long len, String contentType) throws SharkKBException {

    }

    @Override
    public Information getPicture() throws SharkKBException {
        return null;
    }

    @Override
    public void clearPicture() {

    }

    @Override
    public void pictureUsed(boolean used) {

    }

    @Override
    public void setBirthday(Date datum) {

    }

    @Override
    public Date getBirthday() {
        return null;
    }

    @Override
    public void birthdayUsed(boolean used) {

    }

    @Override
    public PeerSemanticTag getProfileAddresses() {
        return null;
    }

    @Override
    public void profileAddressesUsed(boolean used) {

    }

    @Override
    public void setTelephoneNumber(String number) {

    }

    @Override
    public String getTelephoneNumber() {
        return null;
    }

    @Override
    public void telephoneNumberUsed(boolean used) {

    }

    @Override
    public void setQualifications(ProfileQualification profileQualification) {

    }

    @Override
    public ProfileQualification getQualification() {
        return null;
    }

    @Override
    public void qualificationUsed(boolean used) {

    }

    @Override
    public void setKnownLanguages(String[] knownLanguages) {

    }

    @Override
    public String[] getKnownLanguages() {
        return null;
    }

    @Override
    public void knownLanguagesUsed(boolean used) {

    }

    @Override
    public void setProblem(ProfileProblem profileProblem) {

    }

    @Override
    public ProfileProblem getProblem() {
        return null;
    }

    @Override
    public void problemUsed(boolean used) {

    }

    @Override
    public void setCurrentPosition(SpatialSemanticTag currentPosition) {

    }

    @Override
    public void setCurrentPosition(SpatialSemanticTag currentPosition, TimeSemanticTag fromTo) {

    }

    @Override
    public SpatialSemanticTag getCurrentPosition() {
        return null;
    }

    @Override
    public void currentPositionUsed(boolean used) {

    }

    @Override
    public void setPosition(SpatialSemanticTag sst) {

    }

    @Override
    public SpatialSemanticTag getPosition() throws SharkKBException {
        return null;
    }

    @Override
    public void positionUsed(boolean used) {

    }

    @Override
    public void setSupportPossibilities(ProfileSupportPossibilities profileSupportPossibilities) {

    }

    @Override
    public ProfileSupportPossibilities getSupportPossibilities() {
        return null;
    }

    @Override
    public void supportPossibilitiesUsed(boolean used) {

    }
}
