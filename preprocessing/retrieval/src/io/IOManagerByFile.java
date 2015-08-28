package io;

import controler.ControllerI;
import modele.TokenCrawler;
import modele.TokenStack;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Scanner;

/**
 * Lit depuis des fichiers pour récupérer des tokens
 * Ecrit également les résultats des crawls dans des fichiers
 *
 * Created by nicolas on 27/02/15.
 */
public class IOManagerByFile implements IOManager {
    File fAccountToCrawl;
    ControllerI c;
    File fToken;
    FileWriter fProfiles;
    Scanner scAccount;
    String path;
    public IOManagerByFile(String cheminToken, ControllerI c, String cheminAccount, String cheminOutputProfile) throws IOException {
        fToken = new File(cheminToken);
        this.c=c;
        fAccountToCrawl = new File(cheminAccount);
        //fProfiles= new FileWriter(new File(cheminOutputProfile));
        scAccount=new Scanner(fAccountToCrawl);
        path=cheminOutputProfile;
    }

    @Override
    public void feedWithToken(TokenStack t) throws SourceNotFoundException, SourceIncorrectException, URISyntaxException {
        feed(t,"/modele/tokenLip6");
    }

    @Override
    public void feedWithTokenRTWizard(TokenStack t) throws SourceNotFoundException, SourceIncorrectException, URISyntaxException {
       feed(t,"/modele/tokenRTWizard");
    }

    @Override
    public void feedWithTokenResearcher(TokenStack t) throws SourceNotFoundException, SourceIncorrectException, URISyntaxException {
        feed(t,"/modele/tokenResearcher");
    }

    /**
     * Ajouter aux différentes listes les tokens en utilisant un fichier source
     * @param t la stack qui contient les listes de tokens
     * @param ressource le fichier source qui contient les tokens
     * @throws SourceNotFoundException
     * @throws SourceIncorrectException
     * @throws URISyntaxException
     */
    private void feed(TokenStack t, String ressource) throws SourceNotFoundException, SourceIncorrectException, URISyntaxException {
        Scanner sc = null;
        InputStream is = this.getClass().getResourceAsStream(ressource);
        sc = new Scanner(is);
        String access, secret, pseudo, line;
        String[] tab;
        while (sc.hasNextLine()) {
            line=sc.nextLine();
            tab =line.split("\t");
            try {
                pseudo = tab[0];
                access = tab[1];
                secret = tab[2];
                if (ressource.contains("6"))
                    t.addLip6(new TokenCrawler(pseudo, access, secret));
                else if (ressource.contains("izard"))
                    t.addRTWizard(new TokenCrawler(pseudo, access, secret));
                else
                    t.addResearcger(new TokenCrawler(pseudo, access, secret));
                c.tellNewUserAdded(pseudo);
            }
            catch (ArrayIndexOutOfBoundsException e) {
                throw new SourceIncorrectException();
            }
        }
    }

    @Override
    public String getDescriptionSource() {
        return fToken.getAbsolutePath();
    }

    @Override
    public String getNextIdToCrawl() {
        return scAccount.nextLine();
    }

    @Override
    public boolean hastNextIdToCrawl() {
        return scAccount.hasNextLine();
    }

    public void write(String m) throws IOException {
        fProfiles= new FileWriter(new File(path), true);
        fProfiles.write(m+"\t");
        fProfiles.close();
    }
    public void writeLine(String m) throws IOException {

        fProfiles= new FileWriter(new File(path), true);
        fProfiles.write("\n" + m + "\t");
        fProfiles.close();
    }
    public void close() throws IOException {
        fProfiles.close();
        c.tellSthng("Fichier de résultat fermé");
    }
}
