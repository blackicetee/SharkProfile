package Profile;

/**
 * Created by Mr.T on 30.04.2015.
 */
public class ProfileSupportPossibilitiesImpl extends ProfileEntryImpl implements ProfileSupportPossibilities {
    public  ProfileSupportPossibilitiesImpl(byte[] content, String type) {
        super();
        this.setDescription(content, type);
    }
}
