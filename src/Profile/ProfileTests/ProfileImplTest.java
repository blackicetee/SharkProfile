package Profile.ProfileTests;

import Profile.Profile;
import Profile.ProfileFactory;
import Profile.ProfileFactoryImpl;
import Profile.ProfileName;
import Profile.ProfileNameImpl;
import net.sharkfw.knowledgeBase.Information;
import net.sharkfw.knowledgeBase.PeerSemanticTag;
import net.sharkfw.knowledgeBase.SharkKB;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.knowledgeBase.inmemory.InMemoSharkKB;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.NoSuchElementException;

public class ProfileImplTest {
    private SharkKB kb = new InMemoSharkKB();
    private ProfileFactory profileFactory = new ProfileFactoryImpl(kb);
    private PeerSemanticTag createAlice() throws SharkKBException{
        return this.kb.createPeerSemanticTag("Alice",
                        "http://www.sharksystem.net/alice.html",
                        "mail://alice@wonderland.net");
    }

    @Test
    public void testProfileName() throws SharkKBException, IOException, ClassNotFoundException {
        PeerSemanticTag alice = createAlice();
        Profile profile = profileFactory.createProfile(alice, alice);
        ProfileName profileName = new ProfileNameImpl();
        profileName.setSurname("Alice");
        profileName.setLastName("Alpha");
        profileName.setTitle("Prof.");
        profile.setName(profileName);
        ProfileName profileName1 = profile.getName();
        Assert.assertEquals("Alice", profileName1.getSurname());
        Assert.assertEquals("Alpha", profileName1.getLastName());
        Assert.assertEquals("Prof.", profileName1.getTitle());
    }

    @Test(expected=NoSuchElementException.class)
    public void testProfilePicture() throws Exception {
        PeerSemanticTag alice = createAlice();
        Profile profile = profileFactory.createProfile(alice, alice);
        byte[] examplepicture = "I am a picture".getBytes();
        String profilbild = "ProfilePicture";
        profile.setPicture(examplepicture, profilbild);
        Information i = profile.getPicture();
        Assert.assertEquals("I am a picture", i.getContentAsString());
        Assert.assertEquals("ProfilePicture", i.getContentType());
        profile.clearPicture();
        i = profile.getPicture();
    }

    @Test
    public void testProfileBirthday() throws Exception {
        PeerSemanticTag alice = createAlice();
        Profile profile = profileFactory.createProfile(alice, alice);

    }

    @Test
    public void testGetProfileAddresses() throws Exception {

    }

    @Test
    public void testSetTelephoneNumber() throws Exception {

    }

    @Test
    public void testGetTelephoneNumber() throws Exception {

    }

    @Test
    public void testSetQualifications() throws Exception {

    }

    @Test
    public void testGetQualification() throws Exception {

    }

    @Test
    public void testSetKnownLanguages() throws Exception {

    }

    @Test
    public void testGetKnownLanguages() throws Exception {

    }

    @Test
    public void testSetProblem() throws Exception {

    }

    @Test
    public void testGetProblem() throws Exception {

    }

    @Test
    public void testSetCurrentPosition() throws Exception {

    }

    @Test
    public void testSetCurrentPosition1() throws Exception {

    }

    @Test
    public void testGetCurrentPosition() throws Exception {

    }

    @Test
    public void testSetPosition() throws Exception {

    }

    @Test
    public void testGetPosition() throws Exception {

    }

    @Test
    public void testSetSupportPossibilities() throws Exception {

    }

    @Test
    public void testGetSupportPossibilities() throws Exception {

    }
}