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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by nicolas on 01/03/15.
 */
public abstract class MainAbstract {
    Twitter twitter;
    String urlToken;
    String urlAccount;
    String urlProfileOut;
    ControllerI c;
    IOManager feedAndWrite;
    TokenStack tokenStack;
    FileWriter fwException;

    public void setUp(String urlToken, String urlAccount, String urlProfileOut) throws IOException {
        this.urlToken =urlToken;
        this.urlAccount = urlAccount;
        this.urlProfileOut=urlProfileOut;
        c= new Controler(this);
        feedAndWrite = null;
        //On Crée l'objet qui va gérer les entrées sorties
        try {
            feedAndWrite = new IOManagerByFile(urlToken, c, urlAccount, urlProfileOut);
        } catch (IOException e) {
            System.out.println("Problème d'écriture !");
            System.exit(0);
        }
        tokenStack = TokenStack.getInstance(c);
        tokenStack.feedListWithToken(feedAndWrite);
        tokenStack.generateTwitterAccount();
        fwException = new FileWriter(new File(urlProfileOut+"_exception"));
    }

    public void tearDown() throws IOException {
        feedAndWrite.close();
        fwException.close();
    }
    public abstract void run(String urlToken, String urlAccount, String urlProfileOut) throws IOException;

    public void displaySmthng(String m) {
        System.out.println(m);
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
