package Profile;

/**
 * Created by Mr.T on 30.04.2015.
 */
public class ProfileProblemImpl extends ProfileEntryImpl implements ProfileProblem {
    public ProfileProblemImpl(byte[] content, String type) {
        super();
        this.setDescription(content, type);
    }

}
