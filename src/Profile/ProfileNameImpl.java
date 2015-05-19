package Profile;

import java.io.Serializable;

/**
 * Created by Mr.T on 22.04.2015.
 */
public class ProfileNameImpl implements ProfileName, Serializable {
    private String surname = "";
    private String lastName = "";
    private String title = "";

    public ProfileNameImpl(String sname) {
        this.surname = sname;
    }

    @Override
    public void setSurname(String surname) {
        this.surname = surname;
    }

    @Override
    public String getSurname() {
        return surname;
    }

    @Override
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public String getLastName() {
        return lastName;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getTitle() {
        return title;
    }
}
