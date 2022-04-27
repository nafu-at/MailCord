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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import page.nafuchoco.neojukepro.core.module.NeoModuleLogger;

import java.io.*;
import java.nio.file.Files;

public class ConfigLoader {
    private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());

    private final NeoModuleLogger log;
    private final String filename;
    private final File configFile;
    private MailCordConfig config;

    public ConfigLoader(String filename) {
        log = MailCord.getInstance().getModuleLogger();
        this.filename = filename;
        configFile = new File(filename);
    }

    public void reloadConfig() {
        if (!configFile.exists()) {
            try (InputStream original = ClassLoader.getSystemResourceAsStream(filename)) {
                Files.copy(original, configFile.toPath());
                log.info("The configuration file was not found, so a new file was created.");
                log.debug("Configuration file location: {}", configFile.getPath());
            } catch (IOException e) {
                log.error("The correct configuration file could not be retrieved from the executable.\n" +
                        "If you have a series of problems, please contact the developer.", e);
            }
        }

        try (FileInputStream configInput = new FileInputStream(configFile)) {
            config = MAPPER.readValue(configInput, MailCordConfig.class);
            log.info("The configuration file has been successfully loaded.");
        } catch (FileNotFoundException e) {
            log.error("The configuration file could not be found. Do not delete the configuration file after starting the program.\n" +
                    "If you don't know what it is, please report it to the developer.", e);
        } catch (IOException e) {
            log.error("An error occurred while loading the configuration file.", e);
        }
    }

    public MailCordConfig getConfig() {
        return config;
    }
}
