package main;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by nicolas on 01/03/15.
 */
public class MainGetTweets extends MainAbstract{
    FileWriter log;
    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.out.println("java app.jar fileWithToken fileWithAccounts fileToWriteAccounts");
            System.exit(0);
        }
        else {
            String urlToken = args[0];
            String urlAccount = args[1];
            String urlProfileOut = args[2];
            MainAbstract m = new MainGetTweets();
            m.run(urlToken, urlAccount, urlProfileOut);
        }
    }
    public void run (String urlToken, String urlAccount, String urlProfileOut) throws IOException {

        Twitter twitter;

        this.setUp(urlToken, urlAccount, urlProfileOut);

                //Pour formater la date correctement
        java.util.Date dt;
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss");
        String currentTime;
        String id;
        Status s;
        Long l;
        //Tant qu'il y a des ids de compte à crawler
        while (feedAndWrite.hastNextIdToCrawl()) {
            //On récupère un compte Twitter pour crawler
            //On peut faire 15 requêtes avec ce compte
            if (tokenStack.hasNextTwitterAccount())
                twitter = tokenStack.nextTwitterAccount();
            else
                break;
            for (int i = 0; i < 180; i++) {
                if (feedAndWrite.hastNextIdToCrawl()) {
                    //On récupère le prochain id à crawler
                    id = feedAndWrite.getNextIdToCrawl();
                    try {
                        l=new Long(id);
                        s = twitter.showStatus(l);
                        dt = s.getCreatedAt();
                        currentTime = sdf.format(dt);
                        feedAndWrite.writeLine(id + "");
                        feedAndWrite.write(currentTime + "");
                        feedAndWrite.write(s.getFavoriteCount()+ "");
                        feedAndWrite.write(s.getRetweetCount() + "");
                        feedAndWrite.write(s.getInReplyToScreenName() + "");
                        feedAndWrite.write(s.getGeoLocation() + "");
                    } catch (TwitterException e) {
                        if (e.getErrorCode() == 32) {
                            fwException.write("TwitterException; " + id + ";" + e.getErrorCode() + ";" + e.getMessage() + "\n");
                            try {
                                Thread.sleep(1000 * 60 * 15);
                            } catch (InterruptedException e1) {
                            }
                            tokenStack.generateTwitterAccount();
                            if (tokenStack.hasNextTwitterAccount())
                                twitter = tokenStack.nextTwitterAccount();
                            else
                                break;
                        }
                    } catch (IOException e) {
                        System.out.println("Problème d'écriture !");
                        System.exit(0);
                    }
                    catch (java.lang.NumberFormatException e) {
                        fwException.write("NumberException; " + id + "\n");
                    }

                } else
                    break;
            }
        }
        this.tearDown();
    }

    public void displaySmthng(String m) {
        try {
            log = new FileWriter(new File("log"), true);
            log.write(m+"\n");
            log.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }/**/
}
