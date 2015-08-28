package modele;

import controler.ControllerI;
import io.IOManager;
import io.SourceIncorrectException;
import io.SourceNotFoundException;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;

import java.net.URISyntaxException;
import java.util.LinkedList;

/**
 * Gère les différentes listes de tokens utilisables pour crawler les comptes Twitter
 *
 * Created by nicolas on 27/02/15.
 */
public class TokenStack {
    /**
     * Liste de tokens de users enregistrés à plusieurs apps : une développée avec Max (lip6), une avec Adrien (RTWizard) et une autre avec Max (Researcher)
     */
    private LinkedList<TokenCrawler> listOfTokenLip6;
    private LinkedList<TokenCrawler> listOfTokenRTWizard;
    private LinkedList<TokenCrawler> listOfTokenResearcher;

    /**
     * Les tokens de ces apps
     */
    private String appConsumerKeyLip6 = "1PpYYO3tyjfyk03h4ywwg";
    private String appConsumerSecretLip6 = "8vOVdwPM5RttySuXjRJDVLjtJE4TlgEWgPff1iRGrI";

    private String appConsumerKeyRTWizard = "m0GS3JwNiGEacsoSbMfA";
    private String appConsumerSecretRTWizard = "AF76LOX9DLEPogILirRZNlETSdypiDhRLVx80MYqVQ";

    private String appConsumerKeyResearcher = "qm3dS89iIRjhgWlPbKG1gg";
    private String appConsumerSecretResearcher = "avdTgWprK4kLWIXxUT6JDAaRMYgixhYVizGlUym9TgU";

    /**
     * Comptes Twitter utilisables pour crawler
     */
    private LinkedList<Twitter> twitterAccountList;
    private  ConfigurationBuilder cb;

    /**
     * Gestion de l'objet en Singleton
     */
    private static TokenStack t = null;

    private ControllerI c;
    private int index=0;

    public boolean addLip6(TokenCrawler tokenCrawler) {
        return listOfTokenLip6.add(tokenCrawler);
    }
    public boolean addRTWizard(TokenCrawler tokenCrawler) {
        return listOfTokenRTWizard.add(tokenCrawler);
    }
    public boolean addResearcger(TokenCrawler tokenCrawler) {
        return listOfTokenResearcher.add(tokenCrawler);
    }

    private TokenStack(ControllerI c) {
        listOfTokenLip6 = new LinkedList<TokenCrawler>();
        listOfTokenRTWizard = new LinkedList<TokenCrawler>();
        listOfTokenResearcher = new LinkedList<TokenCrawler>();
        twitterAccountList = new LinkedList<Twitter>();
        this.c=c;
    }

    /**
     * Récupérer l'objet en singleton
     * @param c le controleur de l'appli
     * @return une instance unique de TokenStack
     */
    public static TokenStack getInstance(ControllerI c) {
        if (t == null) {
            t = new TokenStack(c);
        }
        return t;
    }

    /**
     * Ajouter les tokens aux différentes listes de tokens (lip6, rtwiard, researcher)
     * @param f le gestionnaire d'entrée sortie qui va lire les tokens dans un fichier
     * @return vrai si tout s'est bien passé, faux sinon
     */
    public boolean feedListWithToken(IOManager f) {
        try {
            f.feedWithToken(this);
            f.feedWithTokenResearcher(this);
            f.feedWithTokenRTWizard(this);
        } catch (SourceNotFoundException e) {
            c.tellError("The source you gave does not exist. It was impossible to obtain tokens.");
            return false;
        } catch (SourceIncorrectException e) {
            c.tellError("File not formatted correctly. Use \\t instead of your current separator in file " + f.getDescriptionSource());
            return false;
        } catch (URISyntaxException e) {
            c.tellError("Jar not correctly formatted, tokenList not found.");
        }
        return true;
    }

    /**
     *  A partir des listes de tokens, remplit la liste de comptes twitter utilisables pour crawler
     */
    public void generateTwitterAccount() {
        twitterAccountList.clear();
        for (TokenCrawler token : listOfTokenLip6) {
            Twitter twitterAccount = null;
            try {
                twitterAccount = generateMethod(token, appConsumerKeyLip6, appConsumerSecretLip6);
                twitterAccountList.add(twitterAccount);
            } catch (TwitterException e) {
                c.tellError("Credential not verified : " + token);
            }
        }
        for (TokenCrawler token : listOfTokenResearcher) {
            Twitter twitterAccount = null;
            try {
                twitterAccount = generateMethod(token, appConsumerKeyResearcher, appConsumerSecretResearcher);
                twitterAccountList.add(twitterAccount);
            } catch (TwitterException e) {
                c.tellError("Credential not verified : " + token);
            }
        }
        for (TokenCrawler token : listOfTokenRTWizard) {
            Twitter twitterAccount = null;
            try {
                twitterAccount = generateMethod(token, appConsumerKeyRTWizard, appConsumerSecretRTWizard);
                twitterAccountList.add(twitterAccount);
            } catch (TwitterException e) {
                c.tellError("Credential not verified : " + token);
            }
        }
    }

    private Twitter generateMethod(TokenCrawler t, String appConsumerKey, String appConsumerSecret) throws TwitterException {
        cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true);
        cb.setOAuthConsumerKey(appConsumerKey);
        cb.setOAuthConsumerSecret(appConsumerSecret);
        cb.setOAuthAccessToken(t.getToken());
        cb.setOAuthAccessTokenSecret(t.getSecret());
        Twitter twitterAccount = new TwitterFactory(cb.build()).getInstance();
        User u = twitterAccount.verifyCredentials();
        return twitterAccount;
    }

    /**
     * Obtenir le prochain compte Twitter disponible pour crawler des profils
     * @return
     */
    public boolean hasNextTwitterAccount() {
        return twitterAccountList.size() > 0;
    }
    public Twitter nextTwitterAccount() {
        if (index == twitterAccountList.size()) {
            index = 0;
            try {
                c.tellPause("" + (((15000 + 1000 * 60 * 15) / 1000) / 60));
                Thread.sleep(Math.abs(1000 * 60 * 15 + 15000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Twitter twitterAccount = twitterAccountList.get(index);
        index++;
        return twitterAccount;
    }


}
