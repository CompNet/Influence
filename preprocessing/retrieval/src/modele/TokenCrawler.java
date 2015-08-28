package modele;

public class TokenCrawler {

	public TokenCrawler(String pseudo, String token, String secret) {
		super();
		this.token = token;
		this.secret = secret;
		this.pseudo = pseudo;
	}

	String token, secret, pseudo;

	public String getToken() {
		return token;
	}

	public String getSecret() {
		return secret;
	}
	
	public String getPseudo() {
		return pseudo;
	}

    @Override
    public String toString() {
        return "TokenCrawler{" +
                "token='" + token + '\'' +
                ", secret='" + secret + '\'' +
                ", pseudo='" + pseudo + '\'' +
                '}';
    }
}
