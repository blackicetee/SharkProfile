package Profile.ProfileTests;

import Profile.ProfileNameImpl;
import org.junit.Assert;
import org.junit.Test;
//SharkFW/src/java/j2seTests/ApiRev1/ExchangeTests.java
//klassischer wrapper
public class ProfileNameImplTest {
    ProfileNameImpl pName = new ProfileNameImpl("Paul");

    @Test
    public void testSurname() throws Exception {
        String n = pName.getSurname();
        Assert.assertNotNull(n);
        Assert.assertEquals(n, "Paul");
        Assert.assertNotEquals(n, "Heinz");
    }

    @Test
    public void testLastName() throws Exception {
        pName.setLastName("Müller");
        String n = pName.getLastName();
        Assert.assertEquals(n, "Müller");
        Assert.assertNotEquals(n, "Martin");
        Assert.assertNotNull(n);
    }

    @Test
    public void testSetTitle() throws Exception {
        pName.setTitle("Pr. Dr. Dr.");
        String n = pName.getTitle();
        Assert.assertEquals(n, "Pr. Dr. Dr.");
        pName.setTitle("Pr.Dr.Dr.");
        n = pName.getTitle();
        Assert.assertEquals(n, "Pr.Dr.Dr.");
    }
}