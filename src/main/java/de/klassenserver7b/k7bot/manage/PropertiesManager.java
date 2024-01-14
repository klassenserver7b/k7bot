package de.klassenserver7b.k7bot.manage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Properties;

public class PropertiesManager {

	private HashMap<String, Boolean> apienabled;
	private Properties prop;
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	public PropertiesManager() {
		apienabled = new HashMap<>();
		prop = null;
	}

	public boolean loadProps() {

		prop = new Properties();

		try (FileInputStream in = new FileInputStream("resources/bot.properties");) {

			prop.load(in);
			return true;

		} catch (IOException e) {

			log.error("No valid config File found! generating a new one");
			File f = new File("resources/bot.properties");

			if (!f.exists()) {
				generateConfigFile(f);
			}
			return false;

		}
	}

	public void checkAPIProps() {

		this.apienabled.put("hypixel", prop.getProperty("hypixel-api-key") != null);

		this.apienabled.put("github", prop.getProperty("github-oauth-token") != null);

		this.apienabled.put("vplan", (prop.getProperty("vplanpw") != null) && prop.getProperty("schoolID") != null);

		this.apienabled.put("lernsax", (prop.getProperty("lsaxemail") != null)
				&& (prop.getProperty("lsaxtoken") != null) && (prop.getProperty("lsaxappid") != null));

		this.apienabled.put("gourmetta",
				(prop.getProperty("gourmettauserid") != null) && (prop.getProperty("gourmettapassword") != null));

		this.apienabled.put("discogs", prop.getProperty("discogs-token") != null);

	}

	public boolean isBotTokenValid() {
		String token = prop.getProperty("token");

		if (token != null && !token.isBlank()) {

			if (token.split("\\.").length == 3) {
				return true;
			}
		}

		return false;
	}

	public void generateConfigFile(File f) {

		try (BufferedWriter stream = Files.newBufferedWriter(Path.of("resources/bot.properties"),
				Charset.forName("UTF-8"), StandardOpenOption.TRUNCATE_EXISTING);) {

			f.createNewFile();

			Properties prop = new Properties();

			prop.setProperty("token", "");
			prop.setProperty("canary-token", "");
			prop.setProperty("hypixel-api-key", "");
			prop.setProperty("github-oauth-token", "");
			prop.setProperty("ownerId", "");
			prop.setProperty("shardCount", "");
			prop.setProperty("vplanpw", "");
			prop.setProperty("schoolID", "");
			prop.setProperty("lsaxemail", "");
			prop.setProperty("lsaxtoken", "");
			prop.setProperty("lsaxappid", "");
			prop.setProperty("gourmettauserid", "");
			prop.setProperty("gourmettapassword", "");
			prop.setProperty("discogs-token", "");
			prop.setProperty("vppwold", "");
			prop.setProperty("spotify-cookie", "");
			prop.setProperty("votinglimit", "");

			prop.store(stream, "Bot-Configfile\n 'token' is required!");
			stream.close();

		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}

	}

	public void setProperty(String id, String value) {
		prop.setProperty(id, value);

		flushConfig();
	}

	private void flushConfig() {

		try (BufferedWriter stream = Files.newBufferedWriter(Path.of("resources/bot.properties"),
				Charset.forName("UTF-8"), StandardOpenOption.TRUNCATE_EXISTING)) {

			prop.store(stream, "Bot-Configfile\n 'token' is required!");
			stream.close();

		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}

	}

	public String getProperty(String key) {
		return prop.getProperty(key);
	}

	public Properties getProperties() {
		return this.prop;
	}

	public boolean isApiEnabled(String api) {
		return apienabled.get(api);
	}

	public HashMap<String, Boolean> getEnabledApis() {
		return this.apienabled;
	}
}
