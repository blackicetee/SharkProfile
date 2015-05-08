package createProfile.createProfileTests;

import createProfile.ProfileProblemImpl;
import org.junit.Assert;

public class ProfileProblemImplTest {
    ProfileProblemImpl problem = new ProfileProblemImpl();
    @org.junit.Test
    public void testSetUniqueName() throws Exception {
        problem.setUniqueName("Insurance");
        String n = problem.getUniqueName();
        Assert.assertEquals("Insurance", n);
    }

    @org.junit.Test
    public void testGetUniqueName() throws Exception {

    }

    @org.junit.Test
    public void testSetDescription() throws Exception {

    }

    @org.junit.Test
    public void testGetDescription() throws Exception {

    }

    @org.junit.Test
    public void testSetLocation() throws Exception {

    }

    @org.junit.Test
    public void testGetLocation() throws Exception {

    }

    @org.junit.Test
    public void testSetTimeFrom() throws Exception {

    }

    @org.junit.Test
    public void testGetTimeFrom() throws Exception {

    }

    @org.junit.Test
    public void testSetTimeTo() throws Exception {

    }

    @org.junit.Test
    public void testGetTimeTo() throws Exception {

    }

    @org.junit.Test
    public void testSetContentType() throws Exception {

    }

    @org.junit.Test
    public void testGetContentType() throws Exception {

    }
}