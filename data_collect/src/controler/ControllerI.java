package controler;

import modele.TokenCrawler;

/**
 * Created by nicolas on 27/02/15.
 */
public interface ControllerI {
    public void tellPause(String m);
    public void tellNewUserAdded(String m);
    public void tellNewUserCrawling(String m);
    public void tellError(String m);
    public void tellInitOk();
    public void tellTokenAdded(TokenCrawler t);
    public void tellSthng(String m);
}
