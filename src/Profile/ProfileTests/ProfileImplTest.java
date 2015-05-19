package Profile.ProfileTests;

import Profile.*;
import net.sharkfw.knowledgeBase.*;
import net.sharkfw.knowledgeBase.geom.SharkGeometry;
import net.sharkfw.knowledgeBase.geom.inmemory.InMemoSharkGeometry;
import net.sharkfw.knowledgeBase.inmemory.InMemoSharkKB;
import net.sharkfw.system.L;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.NoSuchElementException;

public class ProfileImplTest {
    private SharkKB kb = new InMemoSharkKB();
    private ProfileFactory profileFactory = new ProfileFactoryImpl(kb);
    private String[] aliceAddresses = {"mail://alice@wonderland.net", "mail://alice@wizards.net", "http://www.sharksystem.net/alice.html"};
    private PeerSemanticTag createAlice() throws SharkKBException{
        return this.kb.createPeerSemanticTag("Alice",
                        "http://www.sharksystem.net/alice.html",
                        aliceAddresses);
    }

    @Test
    public void testProfileName() throws SharkKBException, IOException, ClassNotFoundException {
        PeerSemanticTag alice = createAlice();
        Profile profile = profileFactory.createProfile(alice, alice);
        ProfileName profileName = new ProfileNameImpl("Alice");
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
        PeerSemanticTag alice = createAlice();
        Profile profile = profileFactory.createProfile(alice, alice);
        String[] addresses = profile.getProfileAddresses();
        for (int i = 0; i < addresses.length; i++) {
            Assert.assertEquals("This addresses sould be equal", aliceAddresses[i], addresses[i]);
        }

    }

    @Test
    public void testTelephoneNumber() throws Exception {
        PeerSemanticTag alice = createAlice();
        Profile profile = profileFactory.createProfile(alice, alice);
        profile.setTelephoneNumber("0151-28764539");
        Assert.assertEquals("This telephonenumbers sould be equal", "0151-28764539", profile.getTelephoneNumber());
    }

    @Test
    public void testQualifications() throws Exception {
        PeerSemanticTag alice = createAlice();
        Profile profile = profileFactory.createProfile(alice, alice);
        String[] qualifications = {"jave ee development", "linux shell programming", "developing with sharkFW"};
        ProfileQualification pq = new ProfileQualificationImpl(Serializer.serialize(qualifications), "Qualifications");
        profile.setQualifications(pq);
        String[] newQualifications = (String[]) Serializer.deserialize(profile.getQualification().getDescription());
        for (int i = 0; i < newQualifications.length; i++) {
            Assert.assertEquals(qualifications[i], newQualifications[i]);
            //System.out.println(newQualifications[i]);
        }
    }

    @Test
    public void testKnownLanguages() throws Exception {
        PeerSemanticTag alice = createAlice();
        Profile profile = profileFactory.createProfile(alice, alice);
        String[] l = {"aa", "english", "informatic", "de", "en"};
        String[] languagesExpected = {"aa", "de", "en"};
        ProfileKnownLanguages pkl = new ProfileKnownLanguagesImpl(l);
        profile.setKnownLanguages(pkl);
        String[] newL = profile.getKnownLanguages().getKnownLanguages();
        for (int i = 0; i < newL.length; i++) {
            Assert.assertEquals(languagesExpected[i], newL[i]);
        }
    }

    @Test
    public void testProblem() throws Exception {
        PeerSemanticTag alice = createAlice();
        Profile profile = profileFactory.createProfile(alice, alice);
        String problem = "The Bridge is broken";
        byte[] content = Serializer.serialize(problem);
        ProfileProblem pp = new ProfileProblemImpl(content, "bridgeProblem");
        String wkt = "";
        SharkGeometry geom = InMemoSharkGeometry.createGeomByWKT(wkt);
        String[] sis = {"https://en.wikipedia.org/wiki/Bridge", "http://www.sharksystem.net/brokenBridge.png"};
        SpatialSemanticTag brokenBridge = kb.createSpatialSemanticTag("locationOfTheBrokenBridge", sis, geom);
        pp.setLocation(brokenBridge);
        long now = System.currentTimeMillis();
        long dayLength = 24 * 60 * 60 * 1000;
        TimeSemanticTag bridgeBrokenSince = kb.createTimeSemanticTag(now, dayLength);
        System.out.println(L.semanticTag2String(bridgeBrokenSince));
        System.out.println(L.semanticTag2String(brokenBridge));
        pp.setTimeFrom(bridgeBrokenSince);
        profile.setProblem(pp);
    }

    @Test
    public void testCurrentPosition() throws Exception {

    }

    @Test
    public void testSupportPossibilities() throws Exception {

    }
}