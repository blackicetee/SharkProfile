package Profile.ProfileTests;

import Profile.*;
import net.sharkfw.knowledgeBase.*;
import net.sharkfw.knowledgeBase.geom.SharkGeometry;
import net.sharkfw.knowledgeBase.geom.inmemory.InMemoSharkGeometry;
import net.sharkfw.knowledgeBase.inmemory.InMemoSharkKB;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.NoSuchElementException;

public class ProfileImplTest {
    private SharkKB kb = new InMemoSharkKB();
    private ProfileFactory profileFactory;
    private String[] aliceAddresses = {"mail://alice@wonderland.net", "mail://alice@wizards.net", "http://www.sharksystem.net/alice.html"};
    private PeerSemanticTag createAlice() throws SharkKBException{
        return this.kb.createPeerSemanticTag("Alice",
                        "http://www.sharksystem.net/alice.html",
                        aliceAddresses);

    }

    @Before
    public void setUp() throws SharkKBException {
        profileFactory = new ProfileFactoryImpl(kb);
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
        String identifier = "mainpic";
        profile.setPicture(examplepicture, profilbild, identifier);
        Information i = profile.getPicture(identifier);
        Assert.assertEquals("I am a picture", i.getContentAsString());
        Assert.assertEquals("ProfilePicture", i.getContentType());
        profile.clearPicture(identifier);
        i = profile.getPicture(identifier);
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
        profile.setTelephoneNumber("0151-28764539", "My Mobile");
        Assert.assertEquals("This telephonenumbers sould be equal", "0151-28764539", profile.getTelephoneNumber("My Mobile"));
    }

    @Test
    public void testQualifications() throws Exception {
        PeerSemanticTag alice = createAlice();
        Profile profile = profileFactory.createProfile(alice, alice);
        String[] qualifications = {"jave ee development", "linux shell programming", "developing with sharkFW"};
        ProfileQualification pq = new ProfileQualificationImpl(Serializer.serialize(qualifications), "Qualifications");
        profile.setQualifications(pq, "programming qualifications");
        String[] newQualifications = (String[]) Serializer.deserialize(profile.getQualification("programming qualifications").getDescription());
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

    /**Trying to create an ProfileProblem Object and save it persistent in a Profile.
     * A ProfileProblem consists of some Data which should describe a "Problem",
     * this Problem is a Problem of the ProfileOwner (he owns the profile).
     * Every Time this test is proceeded he fails because there are several wrong issues.
     * If we try to get the SpatialSemanticTag(SST) we can get the SST name and the SSTSemanticIdentifiers,
     * but we can`t get the SharkGeometry out of it because then a NullPointerException is thrown.
     * The SpatialSemanticTag describes a location where the Problem occurred.
     * Similar to SST the TimeSemanticTag does not work correctly either.
     * We initialize the TimeSemanticTag with the System Time in Milliseconds and the day-length,
     * but if we want to get the stored information we always get 0 from both get functions.
     * I think there could be a bug in Shark XMLSerializer so maybe somebody could check this.
     *
     * @throws SharkKBException This message is thrown when no SharkKB is found or if there is another problem with the SharkKB.
     */
    @Test
    public void testProblem() throws SharkKBException {
        PeerSemanticTag alice = createAlice();
        Profile profile = profileFactory.createProfile(alice, alice);
        String problem = "The Bridge is broken";
        byte[] content = new byte[0];
        try {
            content = Serializer.serialize(problem);
        } catch (IOException e) {
            throw new SharkKBException(e.getMessage());
        }
        ProfileProblem pp = new ProfileProblemImpl(content, "bridgeProblem");
        String wkt = "Here";
        SharkGeometry geom = InMemoSharkGeometry.createGeomByWKT(wkt);
        String[] sis = {"https://en.wikipedia.org/wiki/Bridge", "http://www.sharksystem.net/brokenBridge.png"};
        SpatialSemanticTag sst = kb.createSpatialSemanticTag("locationOfTheBrokenBridge", sis, geom);
        pp.setLocation(sst);
        long now = System.currentTimeMillis();
        long dayLength = 24 * 60 * 60 * 1000;
        TimeSemanticTag bridgeBrokenSince = kb.createTimeSemanticTag(now, dayLength);
        System.out.println(bridgeBrokenSince.getFrom());
        System.out.println(bridgeBrokenSince.getDuration());
        pp.setTimeFrom(bridgeBrokenSince);
        profile.setProblem(pp, "broken bridge");
        ProfileProblem pp1 = profile.getProblem("broken bridge");
        SpatialSemanticTag newsst = pp1.getLocation();
        System.out.println(newsst.getName());
        Assert.assertEquals("locationOfTheBrokenBridge", newsst.getName());
        for (int i = 0; i < newsst.getSI().length; i++) {
            Assert.assertEquals(sis[i], newsst.getSI()[i]);
            System.out.println(newsst.getSI()[i]);
        }
        //Assert.assertEquals(wkt, newsst.getGeometry().getWKT());
        TimeSemanticTag newtst = pp1.getTimeFrom();
        System.out.println(newtst.getFrom());
        System.out.println(newtst.getDuration());
        //Dokumentieren des Fehlers, dann Max schicken und pushen

    }

    @Test
    public void testCurrentPosition() throws Exception {

    }

    @Test
    public void testSupportPossibilities() throws Exception {

    }
}