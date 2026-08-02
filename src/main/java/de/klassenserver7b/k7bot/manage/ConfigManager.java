/* (C)2026 */
package de.klassenserver7b.k7bot.manage;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.klassenserver7b.k7bot.exceptions.InvalidConfigException;
import io.github.cdimascio.dotenv.Dotenv;
import net.vieiro.toml.TOML;
import net.vieiro.toml.TOMLParser;

public class ConfigManager {

	public static final String BOT_TOKEN_ENV_NAME = "BOT_TOKEN";

	@SuppressWarnings("FieldCanBeLocal")
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private final Dotenv env;
	private final TOML toml;

	public ConfigManager() throws InvalidConfigException {
		this.env = Dotenv.configure().ignoreIfMissing().ignoreIfMalformed().load();
		String config_loc = env.get("CONFIG_FILE", "resources/k7bot.toml");

		TOML toml;

		try {
			toml = TOMLParser.parseFromFilename(config_loc);
		} catch (IOException ex) {
			if (!new File(config_loc).exists()) {
				log.info("Creating config file at {}", config_loc);
				try {
					String defaultConfig = new BufferedReader(new InputStreamReader(Objects.requireNonNull(
							this.getClass().getClassLoader().getResourceAsStream("defaultConfig.toml"),
							"Check that maven config forces existence of defaultConfig.toml"))).readAllAsString();
					Files.writeString(new File(config_loc).toPath(), defaultConfig, StandardOpenOption.CREATE);
					toml = TOMLParser.parseFromString(defaultConfig);
				} catch (IOException e) {
					throw (InvalidConfigException) new InvalidConfigException(
							String.format("Couldn't create config file at %s", config_loc)).initCause(e);
				}
			} else {
				throw (InvalidConfigException) new InvalidConfigException(
						String.format("Couldn't parse config file at %s", config_loc)).initCause(ex);
			}
		}

		this.toml = toml;
	}

	public void validate() throws InvalidConfigException {

		String botToken = this.getEnvVar("BOT_TOKEN");
		if (botToken == null || botToken.isBlank()) {
			throw new InvalidConfigException("BOT_TOKEN environment unset");
		}

		String[] sections = botToken.split("\\.");

		if ((sections.length != 2 && sections.length != 3) || (sections[0].isBlank() || sections[1].isBlank())) {
			throw new InvalidConfigException("Invalid structure of Bot Token");
		}

		for (int i = 0; i < 2; i++) {
			Base64.Decoder decoder = Base64.getUrlDecoder();

			try {
				var _ = decoder.decode(sections[0]);
				var _ = decoder.decode(sections[1]);
			} catch (IllegalArgumentException e) {
				throw new InvalidConfigException("BOT_TOKEN is not a valid JWT");
			}
		}

		if (!toml.getErrors().isEmpty()) {
			String errors = String.join("\n", toml.getErrors());
			throw new InvalidConfigException("Invalid TOML File:\n\n" + errors);
		}
	}

	public String getEnvVar(String name) {
		return env.get(name);
	}

	@SuppressWarnings("unused")
	public String getEnvVar(String name, String defaultValue) {
		return env.get(name, defaultValue);
	}

	public TOML getToml() {
		return toml;
	}
}
