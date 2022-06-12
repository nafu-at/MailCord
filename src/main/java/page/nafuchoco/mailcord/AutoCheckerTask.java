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

import jakarta.mail.*;
import jakarta.mail.search.FlagTerm;
import net.dv8tion.jda.api.entities.TextChannel;
import page.nafuchoco.neobot.api.module.NeoModuleLogger;

import java.util.Arrays;
import java.util.Properties;
import java.util.TimerTask;

public class AutoCheckerTask extends TimerTask {

    private final NeoModuleLogger log;
    private final Properties imapProperties;
    private final TextChannel receiveChannel;

    public AutoCheckerTask(Properties imapProperties, TextChannel receiveChannel) {
        log = MailCord.getInstance().getModuleLogger();
        this.imapProperties = imapProperties;
        this.receiveChannel = receiveChannel;
    }

    @Override
    public void run() {
        log.debug("Checking mail...");
        MailCord mailCord = MailCord.getInstance();
        MailCordConfig.ServerAuthConfig imapConfig = mailCord.getConfig().getAuthorization().getMailServer().getImapServer();

        Session session = Session.getDefaultInstance(imapProperties);
        try {
            Store store = session.getStore("imap");
            session.setDebug(mailCord.getConfig().isDebugMode());
            store.connect(imapConfig.getServerAddress(), imapConfig.getPort(), imapConfig.getUsername(), imapConfig.getPassword());
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            inbox.addMessageCountListener(new MessageReceiveEventListener(receiveChannel));

            // Fetch unseen messages from inbox folder
            Message[] messages = inbox.search(
                    new FlagTerm(new Flags(Flags.Flag.SEEN), false));

            // Sort messages from recent to oldest
            Arrays.sort(messages, (m1, m2) -> {
                try {
                    return m2.getSentDate().compareTo(m1.getSentDate());
                } catch (MessagingException e) {
                    throw new RuntimeException(e);
                }
            });

            // Send a message to Discord
            for (Message message : messages) {
                receiveChannel.sendMessageEmbeds(MailEmbedBuilder.buildMailEmbed(message)).queue();
                message.setFlag(Flags.Flag.SEEN, true);
            }
        } catch (NoSuchProviderException e1) {
            log.error("", e1);
        } catch (AuthenticationFailedException e2) {
            log.error("Attempted to connect to the mail server, but the authentication did not complete.", e2);
        } catch (MessagingException e3) {
            log.error("", e3);
        }
    }
}
