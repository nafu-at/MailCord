/*
 * Copyright 2022 NAFU_at
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package page.nafuchoco.mailcord;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import page.nafuchoco.neobot.api.ConfigLoader;
import page.nafuchoco.neobot.api.NeoBot;
import page.nafuchoco.neobot.api.module.NeoModule;
import page.nafuchoco.neobot.api.module.NeoModuleLogger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Properties;
import java.util.Timer;

public class MailCord extends NeoModule {

    private static MailCord instance;

    public static MailCord getInstance() {
        if (instance == null)
            instance = (MailCord) NeoBot.getModuleManager().getModule("MailCord");
        return instance;
    }


    private NeoModuleLogger log;
    private MailCordConfig config;
    private Timer timer;
    private AutoCheckerTask autoCheckerTask;

    @Override
    public void onEnable() {
        // Set logger
        log = getModuleLogger();

        // Load Configuration file.
        var configFile = new File(getDataFolder(), "config.yaml");
        if (!configFile.exists()) {
            try (InputStream original = getResources("config.yaml")) {
                Files.copy(original, configFile.toPath());
                getModuleLogger().info("The configuration file was not found, so a new file was created.");
                getModuleLogger().debug("Configuration file location: {}", configFile.getPath());
            } catch (IOException e) {
                getModuleLogger().error("The correct configuration file could not be retrieved from the executable.\n" +
                        "If you have a series of problems, please contact the developer.", e);
            }
        }
        config = ConfigLoader.loadConfig(configFile, MailCordConfig.class);

        // Load JDA Channel.

        Guild guild = getInstance().getLauncher().getDiscordApi().getGuildById(config.getAuthorization().getDiscord().getGuildId());
        TextChannel receiveChannel = guild.getTextChannelById(config.getAuthorization().getDiscord().getReceiveChannel());
        TextChannel sendChannel = guild.getTextChannelById(config.getAuthorization().getDiscord().getSendChannel());

        // Setup IMAP Server
        MailCordConfig.ServerAuthConfig imapConfig = config.getAuthorization().getMailServer().getImapServer();
        Properties imapProperties = new Properties();
        switch (imapConfig.getSslProtocol()) {
            case TLS:
                imapProperties.setProperty("mail.imap.ssl.enable", String.valueOf(true));
                imapProperties.setProperty("mail.imap.ssl.trust", imapConfig.getServerAddress());
                break;

            case STARTTLS:
                imapProperties.setProperty("mail.imap.starttls.enable", String.valueOf(true));
                imapProperties.setProperty("mail.imap.ssl.trust", imapConfig.getServerAddress());
                break;

            case NONE:
            default:
                break;

        }

        // Start AutoCheckerTask
        timer = new Timer(true);
        autoCheckerTask = new AutoCheckerTask(imapProperties, receiveChannel);
        timer.scheduleAtFixedRate(autoCheckerTask, 0, 15 * 60 * 1000);
    }

    @Override
    public void onDisable() {
        if (timer != null)
            timer.cancel();
    }

    public MailCordConfig getConfig() {
        return config;
    }
}
