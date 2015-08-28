package io;

import java.io.File;

/**
 * Created by nicolas on 27/02/15.
 */
public class ConfigurationFileManager {
    File f;
    String cheminToken;
    String cheminAccount;
    public ConfigurationFileManager(String chemin) {
        f = new File(chemin);
    }


}
