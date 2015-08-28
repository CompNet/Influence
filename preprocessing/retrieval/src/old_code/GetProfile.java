package old_code;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

public class GetProfile {
	
	public static void main(String[] args) throws SQLException {
		Twitter twitter;
		String url = args[0];
		Connection connexion = DriverManager.getConnection(
				url+"?characterEncoding=UTF-8", "root",
				"mapardg11");
		Statement instruction = connexion.createStatement();
		ResultSet resultat = null;
		PreparedStatement statement = connexion
				.prepareStatement("INSERT INTO profile_info VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		Date dt;
		User u;
		Long id = new Long(0);
		String currentTime;
		
		AuthGenerator auth = new AuthGenerator(url);
		boolean out=false;
		
		while (true) {
			twitter = auth.generator();
			for (int i = 0; i < 180; i++) {
				resultat = instruction
						.executeQuery("SELECT id from profile_to_crawl where crawle="
								+ false + " LIMIT 0,1");
				if (!resultat.next()) {
					out = true;
					break;
				} else {
					try {
						id = resultat.getLong("id");
						u = twitter.showUser(id);
						statement.setLong(1, id);
						statement.setString(2, u.getName());
						dt = u.getCreatedAt();
						currentTime = sdf.format(dt);
						statement.setString(3, currentTime);
						statement.setString(4, u.getScreenName());
						statement.setString(5, u.getDescription());
						statement.setBoolean(6, u.isProfileUseBackgroundImage());
						statement.setBoolean(7, u.isVerified());
						statement.setBoolean(8, u.isContributorsEnabled());
						statement.setBoolean(9,
								((u.getURL() != null) && (new String("").equals(u
										.getURL()))));
						statement.setLong(10, u.getFollowersCount());
						statement.setLong(11, u.getFriendsCount());
						statement.setLong(12, u.getStatusesCount());
						statement.setLong(13, u.getListedCount());
						statement.setLong(14, u.getFavouritesCount());
						statement.executeUpdate();
						instruction.executeUpdate("UPDATE profile_to_crawl SET crawle=" + true +" WHERE id="+id);
					} catch (TwitterException e) {
						twitterExceptionHandler(e, id, statement);
					} catch (SQLException e) {
						if (e.getMessage().contains("Incorrect string value")) {
							statement.executeUpdate("DELETE FROM profile_to_crawl WHERE id=" + id + ";");
						}
						else {
							appender(e, id);
							System.exit(0);
						}
					}
				}
			}
			if (out) {
				resultat.close();
				break;
			}
		}

	}
	
	public static void twitterExceptionHandler(TwitterException e, long id,
			Statement statement) throws SQLException {
		int code = e.getStatusCode();
		if (code == 404) {
			System.out.println("Utilisateur [" + id + "] inexistant.");
			statement.executeUpdate("DELETE FROM profile_to_crawl WHERE id=" + id + ";");
		} else if (code == 401) {
			System.out.println("Utilisateur [" + id + "] bloqu�.");
			System.out.println(e);
			statement.executeUpdate("DELETE FROM profile_to_crawl WHERE id=" + id + ";");
		} else if (code == 429) {
			try {
				Thread.sleep((e.getRetryAfter() + 1) * 1000);
				System.out.println("Application limit : retry after ["
						+ e.getRetryAfter() + "] seconds.");
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		} else if (e.getMessage().contains("suspended")) {
			System.out.println("Utilisateur [" + id + "] bloqu�.");
			statement.executeUpdate("DELETE FROM profile_to_crawl WHERE id=" + id + ";");
		}
		else {
			appender(e, id);
		}
	}
	
	public static void appender(Exception e, long id) {
		FileWriter fw;
		PrintWriter pw;
		try {
			fw = new FileWriter("exception.txt", true);
			pw = new PrintWriter(fw);
			pw.print(Integer.valueOf(String.valueOf(id)));
			pw.append('\n');
			e.printStackTrace(pw);
			pw.append('\n');
			fw.close();
			pw.checkError();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

}
