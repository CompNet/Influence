package main;

import controler.Controler;
import controler.ControllerI;
import io.IOManager;
import io.IOManagerByFile;
import klout.KloutException;
import klout.KloutScore;
import modele.TokenStack;
import org.apache.commons.lang.ArrayUtils;
import twitter4j.IDs;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Crawle les comptes Twitter et récupère les features
 *
 * Created by nicolas on 27/02/15.
 */
public class Main extends MainAbstract {

    FileWriter log;

    /**
     * Lance le crawl
     * Premier argument : le fichier contenant les tokens
     * Second argument : le fichier contenant les IDs de compte à crawler
     * Troisième argument : le fichier de sortie contenant les résultats
    */
    public static void main(String[] args) throws IOException {
        Main main = new Main();


        Twitter twitter;


        if (args.length < 3) {
            System.out.println("java app.jar fileWithToken fileWithAccounts fileToWriteAccounts");
            System.exit(0);
        }
        String urlToken = args[0];
        String urlAccount = args[1];
        String urlProfileOut = args[2];

        ControllerI c = new Controler(main);
        IOManager feedAndWrite = null;
        //On Crée l'objet qui va gérer les entrées sorties
        try {
            feedAndWrite = new IOManagerByFile(urlToken, c, urlAccount, urlProfileOut);
        } catch (IOException e) {
            System.out.println("Problème d'écriture !");
            System.exit(0);
        }

        //Pour formater la date correctement
        java.util.Date dt;
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss");
        String currentTime;

        //Le gestionnaire de token
        TokenStack tokenStack = TokenStack.getInstance(c);
        tokenStack.feedListWithToken(feedAndWrite);
        tokenStack.generateTwitterAccount();

        //L'objet qui nous permet de récupérer les klout scores
        KloutScore ksf = null;
        try {
            ksf = new KloutScore();
        } catch (URISyntaxException e) {
            System.out.println("Jar not correctly formatted. Klout file not found.");
            System.exit(0);
        }
        boolean out = false;

        //On écrit les exceptions dans un fichier séparé
        FileWriter fwException = new FileWriter(new File(urlProfileOut+"_exception"));

        String id;
        User u;
        //Tant qu'il y a des ids de compte à crawler
        while (feedAndWrite.hastNextIdToCrawl()) {
            //On récupère un compte Twitter pour crawler
            //On peut faire 15 requêtes avec ce compte
            twitter = tokenStack.nextTwitterAccount();
            for (int i = 0; i < 15; i++) {
                if (feedAndWrite.hastNextIdToCrawl()) {
                    //On récupère le prochain id à crawler
                    id = feedAndWrite.getNextIdToCrawl();
                    try {
                        //On récupère toutes les features
                        u = twitter.showUser(id);
                        feedAndWrite.writeLine(id + "");
                        feedAndWrite.write(u.getName());
                        dt = u.getCreatedAt();
                        currentTime = sdf.format(dt);
                        feedAndWrite.write(currentTime);
                        feedAndWrite.write(u.getScreenName());
                        feedAndWrite.write(u.getDescription());
                        feedAndWrite.write(u.isProfileUseBackgroundImage()+"");
                        feedAndWrite.write(u.isVerified() +"");
                        feedAndWrite.write(u.isContributorsEnabled()+"");
                        feedAndWrite.write(((u.getURL() != null) && (new String("").equals(u.getURL()))) +"");
                        feedAndWrite.write(u.getFollowersCount()+"");
                        feedAndWrite.write(u.getFriendsCount()+"");
                        feedAndWrite.write(u.getStatusesCount()+"");
                        feedAndWrite.write(u.getListedCount()+"");
                        feedAndWrite.write(u.getFavouritesCount()+"");
                        IDs followers = twitter.getFollowersIDs(id,-1);
                        IDs followees = twitter.getFriendsIDs(id, -1);

                        List<Long> frs= new ArrayList<Long>(Arrays.asList(ArrayUtils.toObject(followers.getIDs())));
                        List<Long> fes =  new ArrayList<Long>(Arrays.asList(ArrayUtils.toObject(followees.getIDs())));
                        feedAndWrite.write(main.stdDeviation(frs)+"");
                        feedAndWrite.write(main.stdDeviation(fes)+"");
                        frs.retainAll(fes);
                        feedAndWrite.write(frs.size()+"");

                        try {
                            feedAndWrite.write(ksf.getKloutScore(u.getId())+"");
                        } catch (KloutException e) {
                            fwException.write("KloutException; " +id +";"+e.getMessage()+"\n");
                            System.out.println("Klout error for user " + id);
                        }

                    } catch (TwitterException e) {
                        if (e.getErrorCode() == 32) {
                            try {
                                Thread.sleep(1000 * 60 * 5);
                            } catch (InterruptedException e1) {
                            }
                            main.displaySmthng("Code 32 - Id to crawl " + id);
                            tokenStack.generateTwitterAccount();
                        }
                        fwException.write("TwitterException; " +id +";"+e.getErrorCode()+";"+e.getMessage()+"\n");
                    } catch (IOException e) {
                        System.out.println("Problème d'écriture !");
                        System.exit(0);
                    }

                } else
                    break;
            }
        }
        feedAndWrite.close();
        fwException.close();
    }/**/

    @Override
    public void run(String urlToken, String urlAccount, String urlProfileOut) throws IOException {

    }

    public void displaySmthng(String m) {
        try {
            log = new FileWriter(new File("log"), true);
            log.write(m+"\n");
            log.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public double stdDeviation(List<Long> listOfIds) {
        //On divise par 1000 car JAVA gère mal les chiffres trop grands
        double normalizer = 1000;
        int size = listOfIds.size();
        double sum=new Long(0);
        for (Long l :  listOfIds)
            sum+=(l / normalizer);
        double mean = sum / size;
        //System.out.println("Mean : " + mean + "; Size : " + size + "; Sum : " + sum);
        double var=0;
        for (Long l :  listOfIds)
            var+=(mean-(l/normalizer))*(mean-(l/normalizer));
        var = var /size;
        //System.out.println("Var : " + var + "; Size : " + size + "; Sum : " + sum);
        return Math.sqrt((double)var);
    }
}
