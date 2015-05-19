package Profile;

import java.io.Serializable;

/**
 * Created by Mr.T on 30.04.2015.
 */
public class ProfileQualificationImpl extends ProfileEntryImpl implements ProfileQualification {
    public ProfileQualificationImpl(byte[] content, String type) {
        super();
        this.setDescription(content, type);
    }
}
