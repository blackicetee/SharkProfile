package Profile;

import java.util.List;

/**
 * Created by Mr.T on 13.05.2015.
 */
public class ProfileKnownLanguagesImpl implements ProfileKnownLanguages {
    private String[] languages = null;

    private String[] makeListToStringArray(List<String> li) {
        String[] correctLanguages = new String[li.size()];
        correctLanguages = li.toArray(correctLanguages);
        return correctLanguages;
    }

    private void filterKnownLanguagesInISOForm(String[] languages) {
        List<String> listOfLanguagesInISOForm = null;
        if (languages != null) {
            for (int i = 0; i < languages.length; i++) {
                if (languages[i] == "de") {
                    listOfLanguagesInISOForm.add("de");
                }
            }
        }
        this.languages = makeListToStringArray(listOfLanguagesInISOForm);
    }

    @Override
    public void setKnownLanguages(String[] languages) {
        filterKnownLanguagesInISOForm(languages);
    }

    @Override
    public String[] getKnownLanguages() {
        return languages;
    }
}
