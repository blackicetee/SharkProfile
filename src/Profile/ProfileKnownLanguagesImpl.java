package Profile;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mr.T on 13.05.2015.
 */
public class ProfileKnownLanguagesImpl implements ProfileKnownLanguages, Serializable {
    private String[] languages = null;
    /**This is a Table of all viable ISO-639-1 language-codes
     *ISO	Language
     *aa	Afar
     *ab	Abkhazian
     *af	Afrikaans
     *am	Amharic
     *ar	Arabic
     *as	Assamese
     *ay	Aymara
     *az	Azerbaijani
     *ba	Bashkir
     *be	Byelorussian
     *bg	Bulgarian
     *bh	Bihari
     *bi	Bislama
     *bn	Bengali
     *bo	Tibetan
     *br	Breton
     *ca	Catalan
     *co	Corsican
     *cs	Czech
     *cy	Welch
     *da	Danish
     *de	German
     *dz	Bhutani
     *el	Greek
     *en	English
     *eo	Esperanto
     *es	Spanish
     *et	Estonian
     *eu	Basque
     *fa	Persian
     *fi	Finnish
     *fj	Fiji
     *fo	Faeroese
     *fr	French
     *fy	Frisian
     *ga	Irish
     *gd	Scots Gaelic
     *gl	Galician
     *gn	Guarani
     *gu	Gujarati
     *ha	Hausa
     *hi	Hindi
     *he	Hebrew
     *hr	Croatian
     *hu	Hungarian
     *hy	Armenian
     *ia	Interlingua
     *id	Indonesian
     *ie	Interlingue
     *ik	Inupiak
     *in	former Indonesian
     *is	Icelandic
     *it	Italian
     *iu	Inuktitut (Eskimo)
     *iw	former Hebrew
     *ja	Japanese
     *ji	former Yiddish
     *jw	Javanese
     *ka	Georgian
     *kk	Kazakh
     *kl	Greenlandic
     *km	Cambodian
     *kn	Kannada
     *ko	Korean
     *ks	Kashmiri
     *ku	Kurdish
     *ky	Kirghiz
     *la	Latin
     *ln	Lingala
     *lo	Laothian
     *lt	Lithuanian
     *lv	Latvian, Lettish
     *mg	Malagasy
     *mi	Maori
     *mk	Macedonian
     *ml	Malayalam
     *mn	Mongolian
     *mo	Moldavian
     *mr	Marathi
     *ms	Malay
     *mt	Maltese
     *my	Burmese
     *na	Nauru
     *ne	Nepali
     *nl	Dutch
     *no	Norwegian
     *oc	Occitan
     *om	(Afan) Oromo
     *or	Oriya
     *pa	Punjabi
     *pl	Polish
     *ps	Pashto, Pushto
     *pt	Portuguese
     *qu	Quechua
     *rm	Rhaeto-Romance
     *rn	Kirundi
     *ro	Romanian
     *ru	Russian
     *rw	Kinyarwanda
     *sa	Sanskrit
     *sd	Sindhi
     *sg	Sangro
     *sh	Serbo-Croatian
     *si	Singhalese
     *sk	Slovak
     *sl	Slovenian
     *sm	Samoan
     *sn	Shona
     *so	Somali
     *sq	Albanian
     *sr	Serbian
     *ss	Siswati
     *st	Sesotho
     *su	Sudanese
     *sv	Swedish
     *sw	Swahili
     *ta	Tamil
     *te	Tegulu
     *tg	Tajik
     *th	Thai
     *ti	Tigrinya
     *tk	Turkmen
     *tl	Tagalog
     *tn	Setswana
     *to	Tonga
     *tr	Turkish
     *ts	Tsonga
     *tt	Tatar
     *tw	Twi
     *ug	Uigur
     *uk	Ukrainian
     *ur	Urdu
     *uz	Uzbek
     *vi	Vietnamese
     *vo	Volapuk
     *wo	Wolof
     *xh	Xhosa
     *yi	Yiddish
     *yo	Yoruba
     *za	Zhuang
     *zh	Chinese
     *zu	Zulu
     */
    private String[] ISOLanguages = {
            "aa", "ab", "af", "am", "ar", "as", "ay", "az", "ba", "be",
            "bg", "bh", "bi", "bn", "bo", "br", "ca", "co", "cs", "cy",
            "da", "de", "dz", "el", "en", "eo", "es", "et", "eu", "fa",
            "fi", "fj", "fo", "fr", "fy", "ga", "gd", "gl", "gn", "gu",
            "ha", "hi", "he", "hr", "hu", "hy", "ia", "id", "ie", "ik",
            "in", "is", "it", "iu", "iw", "ja", "ji", "jw", "ka", "kk",
            "kl", "km", "kn", "ko", "ks", "ku", "ky", "la", "ln", "lo",
            "lt", "lv", "mg", "mi", "mk", "ml", "mn", "mo", "mr", "ms",
            "mt", "my", "na", "ne", "nl", "no", "oc", "om", "or", "pa",
            "pl", "ps", "pt", "qu", "rm", "rn", "ro", "ru", "rw", "sa",
            "sd", "sg", "sh", "si", "sk", "sl", "sm", "sn", "so", "sq",
            "sr", "ss", "st", "su", "sv", "sw", "ta", "te", "tg", "th",
            "ti", "tk", "tl", "tn", "to", "tr", "ts", "tt", "tw", "ug",
            "uk", "ur", "uz", "vi", "vo", "wo", "xh", "yi", "yo", "za",
            "zh", "zu"};

    public ProfileKnownLanguagesImpl(String[] languages) {
        this.languages = languages;
        filterKnownLanguagesInISOForm();
    }

    private String[] makeListToStringArray(List<String> li) {
        String[] correctLanguages = new String[li.size()];
        correctLanguages = li.toArray(correctLanguages);
        return correctLanguages;
    }

    private void filterKnownLanguagesInISOForm() {
        List<String> listOfLanguagesInISOForm = new ArrayList<String>();
        if (languages != null) {
            for (int i = 0; i < languages.length; i++) {
                for (int j = 0; j < ISOLanguages.length; j++) {
                    if (languages[i] == ISOLanguages[j]) {
                        listOfLanguagesInISOForm.add(languages[i]);
                    }
                }
            }
        }
        if (listOfLanguagesInISOForm != null) {
            languages = makeListToStringArray(listOfLanguagesInISOForm);
        }
        else {
            System.out.println("All of your given languages are in the wrong form!");
            System.out.println("Please just insert ISO-639-1 language-codes like \"en\" for english.");
            System.out.println("A documentation of all possible ISO-639-1 language-codes is in the file \"ProfileKnownLanguagesImpl\"");
        }

    }

    @Override
    public String[] getKnownLanguages() {
        return languages;
    }
}
