package klout;

import com.json.parsers.JSONParser;
import com.json.parsers.JsonParserFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

public class KloutScore {

    ArrayList<String> keys;
    HttpClient httpclient;
    HttpGet httpget;
    HttpResponse response;
    HttpEntity entity;
    InputStream instream;
    int data;
    int statusCode;
    int index;
    public KloutScore() throws FileNotFoundException, URISyntaxException {
        keys = new ArrayList<String>();
        httpclient = new DefaultHttpClient();
        index =0;
        fillWithKeys();
    }
    private void fillWithKeys() throws URISyntaxException, FileNotFoundException {
        URL fileURL = this.getClass().getResource("/klout/keyList");
        File keyFile = new File(fileURL.toURI());
        Scanner sc = new Scanner(keyFile);
        while (sc.hasNextLine()) {
            keys.add(sc.nextLine());
        }
    }
    private String getKey() {
        if (index == keys.size()) {
            index=0;
        }
        String r = keys.get(index);
        index++;
        return r;
    }

    /**
     * Obtenir le KloutScore correspondant à un id Twitter
     * @param id l'id du compte twitter sous forme de long
     * @return un klout score sous forme de double
     * @throws IOException Erreur Http
     * @throws KloutException Erreut Klout
     */
    public double getKloutScore(Long id) throws IOException, KloutException {
        String key = this.getKey();
        httpclient = new DefaultHttpClient();
        httpget = new HttpGet("http://api.klout.com/v2/identity.json/tw/"
                + id + "?key=" + key);
        response = httpclient.execute(httpget);
        entity = response.getEntity();
        instream = entity.getContent();
        data = instream.read();
        String reponse = "";

        float score = -1;
        //SI ona  bien récupéré le klout score
        statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == 200) {
            try {
                //Lecture de la réponse Http
                while (data != -1) {
                    // do something with data...
                    reponse += (char) data;
                    // System.out.print((char) data);
                    data = instream.read();
                }
                System.out.println(reponse);
                // Si la réponse http contient bien qqch
                if (!reponse.equals("")) {
                    instream.close();
                    //On parse le JSON
                    JsonParserFactory factory = JsonParserFactory.getInstance();
                    JSONParser parser = factory.newJsonParser();
                    Map jsonData = parser.parseJson(reponse);
                    //On récupère l'id Klout du user
                    String idKlout = (String) jsonData.get("id");
                    //ON requete le klout score du user en utilisant son id klout
                    httpget = new HttpGet("http://api.klout.com/v2/user.json/"
                            + idKlout + "?key=" + key);
                    response = httpclient.execute(httpget);
                    entity = response.getEntity();
                    instream = entity.getContent();
                    data = instream.read();
                    reponse = "";
                    while (data != -1) {
                        // do something with data...
                        reponse += (char) data;
                        // System.out.print((char) data);
                        data = instream.read();
                    }
                    instream.close();
                    // System.out.println(reponse);
                    if (!reponse.equals("")) {
                        reponse = reponse.substring(0,
                                reponse.indexOf("scoreDeltas") - 2)
                                + "}";
                        // System.out.println(reponse);
                        factory = JsonParserFactory.getInstance();
                        parser = factory.newJsonParser();
                        jsonData = parser.parseJson(reponse);
                        String scoreString = (String) ((Map) jsonData
                                .get("score")).get("score");
                        score = Float.parseFloat(scoreString);
                        return score;
                    }
                }
            } catch (StringIndexOutOfBoundsException siob) {
                throw new KloutException(reponse, id);
            }
        }
        else if (statusCode == 503) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            throw new KloutException("Too many requests !", id);
        }
        return score;
    }


}
