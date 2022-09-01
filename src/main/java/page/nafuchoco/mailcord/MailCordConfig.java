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

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
public class MailCordConfig {
    @JsonProperty("authorization")
    private AuthorizationConfig authorization;

    @JsonProperty("debugMode")
    private boolean debugMode;

    @Getter
    @ToString
    public static class AuthorizationConfig {
        @JsonProperty("discord")
        private DiscordConfig discord;
        @JsonProperty("mailServer")
        private List<MailServerConfig> mailServer;
    }

    @Getter
    @ToString
    public static class DiscordConfig {
        @JsonProperty("guildId")
        private long guildId;
    }

    @Getter
    @ToString
    public static class MailServerConfig {
        @JsonProperty("address")
        private String address;
        @JsonProperty("smtp")
        private ServerAuthConfig smtpServer;
        @JsonProperty("imap")
        private ServerAuthConfig imapServer;
    }

    @Getter
    @ToString
    public static class ServerAuthConfig {
        @JsonProperty("serverAddress")
        private String serverAddress;
        @JsonProperty("port")
        private int port;
        @JsonProperty("ssl")
        private SSLProtocol sslProtocol;
        @JsonProperty("username")
        private String username;
        @JsonProperty("password")
        private String password;
        @JsonProperty("channel")
        private String channel;
    }

    public enum SSLProtocol {
        NONE, STARTTLS, TLS
    }
}
