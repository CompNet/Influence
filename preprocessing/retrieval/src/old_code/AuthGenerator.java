package old_code;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;

import modele.TokenCrawler;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class AuthGenerator {

	static String url;
	static String utilisateur = "joris";
	static String motDePasse = "falip";
	int index;
	LinkedList<TokenCrawler> list;

	public AuthGenerator(String s) {
		index = 0;
		list = new LinkedList<TokenCrawler>();
		// on addLip6 tous les tokens dispos dans list
		Connection connexion = null;
		Statement statement = null;
		ResultSet resultat = null;

		try {
			url = s;
			connexion = DriverManager.getConnection(url, utilisateur,
					motDePasse);
			statement = connexion.createStatement();

			resultat = statement.executeQuery("select * from account;");
			String access, secret, pseudo;

			while (resultat.next()) {
				pseudo = resultat.getString("pseudo");
				access = resultat.getString("token");
				secret = resultat.getString("secret");

				list.add(new TokenCrawler(pseudo, access, secret));
				System.out.println("compte ajout�");
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (resultat != null) {
				try {
					resultat.close();
				} catch (SQLException ignore) {
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException ignore) {
				}
			}
			if (connexion != null) {
				try {
					connexion.close();
				} catch (SQLException ignore) {
				}
			}
		}
	}

	public Twitter generator() {
		if (index == list.size()) {
			index = 0;
			try {
				System.out
						.println("pause de "
								+ (((15000 + 1000 * 60 * 15) / 1000) / 60)
								+ "minutes");
				Thread.sleep(Math.abs(1000 * 60 * 15 + 15000));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		System.out.println("new user " + list.get(index).getPseudo());

		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true);
		cb.setOAuthConsumerKey("1PpYYO3tyjfyk03h4ywwg");
		cb.setOAuthConsumerSecret("8vOVdwPM5RttySuXjRJDVLjtJE4TlgEWgPff1iRGrI");
		cb.setOAuthAccessToken(list.get(index).getToken());
		cb.setOAuthAccessTokenSecret(list.get(index).getSecret());
		index++;

		return new TwitterFactory(cb.build()).getInstance();
	}

}