import java.util.ResourceBundle;

public class LanguageBundle {
    private ResourceBundle bundle;

    public LanguageBundle(String language) {
        this.bundle = ResourceBundle.getBundle("languages/Bundle_" + language, new UTF8Control());
    }

    public String getString(String key) {
        return bundle.getString(key);
    }
}
