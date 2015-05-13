package Profile;

import net.sharkfw.knowledgeBase.SharkKBException;

/**
 * Created by Mr.T on 22.04.2015.
 */
public interface ProfileName {
    void setSurname(String surname) throws SharkKBException;
    String getSurname();

    void setLastName(String lastName);
    String getLastName();

    void setTitle(String title);
    String getTitle();
}
