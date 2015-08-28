package controler;

import main.MainAbstract;
import modele.TokenCrawler;

/**
 * Created by nicolas on 27/02/15.
 */
public class Controler implements ControllerI{
    MainAbstract main;
    public Controler(MainAbstract m) {
        this.main=m;
    }
    @Override
    public void tellPause(String m) {
        main.displaySmthng(m);
    }

    @Override
    public void tellNewUserAdded(String m) {
        main.displaySmthng(m + " added");
    }

    @Override
    public void tellNewUserCrawling(String m) {
        main.displaySmthng(m);
    }

    @Override
    public void tellError(String m) {
        main.displaySmthng(m);
    }

    @Override
    public void tellInitOk() {
        main.displaySmthng("Init ok... Crawling should start now...");
    }

    @Override
    public void tellTokenAdded(TokenCrawler t) {
        main.displaySmthng("Token added :" + t);
    }

    public void tellSthng(String m) {
        main.displaySmthng(m);
    }
}
