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

import jakarta.mail.NoSuchProviderException;
import jakarta.mail.Session;
import jakarta.mail.Store;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.Properties;

@Slf4j
public class MailClient {
    private final String address;
    private final Session session;
    private final Store imapStore;
    private final MailCordConfig.ServerAuthConfig imapServer;
    private final TextChannel receiveChannel;
    private final Store smtpStore;
    private final MailCordConfig.ServerAuthConfig smtpServer;
    private final TextChannel sendChannel;

    public MailClient(String address,
                      Properties properties,
                      String receiveChannel,
                      String sendChannel,
                      MailCordConfig.ServerAuthConfig smtpServer,
                      MailCordConfig.ServerAuthConfig imapServer) throws NoSuchProviderException {
        this(address, properties, imapServer, smtpServer, receiveChannel, sendChannel, false);
    }

    public MailClient(String address,
                      Properties properties,
                      MailCordConfig.ServerAuthConfig imapServer,
                      MailCordConfig.ServerAuthConfig smtpServer,
                      String receiveChannel,
                      String sendChannel,
                      boolean debug) throws NoSuchProviderException {
        var instance = MailCord.getInstance();
        this.address = address;
        this.imapServer = imapServer;
        this.smtpServer = smtpServer;
        this.receiveChannel = instance.getLauncher().getDiscordApi().getTextChannelById(receiveChannel);
        this.sendChannel = instance.getLauncher().getDiscordApi().getTextChannelById(sendChannel);

        session = Session.getDefaultInstance(properties);
        imapStore = session.getStore("imap");
        session.setDebug(debug);

        smtpStore = null; //Why? Because i don't implement SMTP.
    }

    public String getAddress() {
        return address;
    }

    public Session getSession() {
        return session;
    }

    public Store getImapStore() {
        return imapStore;
    }

    public MailCordConfig.ServerAuthConfig getImapServer() {
        return imapServer;
    }

    public TextChannel getReceiveChannel() {
        return receiveChannel;
    }

    public Store getSmtpStore() {
        return smtpStore;
    }

    public MailCordConfig.ServerAuthConfig getSmtpServer() {
        return smtpServer;
    }

    public TextChannel getSendChannel() {
        return sendChannel;
    }
}
