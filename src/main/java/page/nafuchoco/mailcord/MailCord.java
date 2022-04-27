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

import net.dv8tion.jda.api.entities.TextChannel;
import page.nafuchoco.neojukepro.core.Main;
import page.nafuchoco.neojukepro.core.guild.NeoGuild;
import page.nafuchoco.neojukepro.core.module.NeoModule;
import page.nafuchoco.neojukepro.core.module.NeoModuleLogger;

import java.util.Properties;
import java.util.Timer;

public class MailCord extends NeoModule {

    private static MailCord instance;

    public static MailCord getInstance() {
        if (instance == null)
            instance = (MailCord) Main.getLauncher().getModuleManager().getModule("MailCord");
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
        ConfigLoader configLoader = new ConfigLoader("config.yml");
        configLoader.reloadConfig();
        config = configLoader.getConfig();

        // Load JDA Channel.
        NeoGuild guild = getNeoJukePro().getGuildRegistry().getNeoGuild(config.getAuthorization().getDiscord().getGuildId());
        TextChannel receiveChannel = guild.getJDAGuild().getTextChannelById(config.getAuthorization().getDiscord().getReceiveChannel());
        TextChannel sendChannel = guild.getJDAGuild().getTextChannelById(config.getAuthorization().getDiscord().getSendChannel());

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
