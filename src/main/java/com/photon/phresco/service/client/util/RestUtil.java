package com.photon.phresco.service.client.util;

import java.io.InputStream;
import java.util.List;

import com.photon.phresco.configuration.ConfigReader;
import com.photon.phresco.configuration.Configuration;
import com.photon.phresco.exception.PhrescoException;

public final class RestUtil {

    private static final String CONFIG_FILE = "phresco-env-config.xml";
    
    private RestUtil() {
    }
    
    public static String getServerPath() throws PhrescoException {
        String phrescoServerUrl = "";
        try {
            InputStream stream = null;
            stream = RestUtil.class.getClassLoader()
                    .getResourceAsStream(CONFIG_FILE);
            ConfigReader configReader = new ConfigReader(stream);
            String environment = System.getProperty("SERVER_ENVIRONMENT");
            if (environment == null || environment.isEmpty()) {
                environment = configReader.getDefaultEnvName();
            }
            List<Configuration> configurations = configReader
                    .getConfigurations(environment, "WebService");
            for (Configuration configuration : configurations) {
                String protocol = configuration.getProperties().getProperty(
                        "protocol");
                String host = configuration.getProperties().getProperty("host");
                String port = configuration.getProperties().getProperty("port");
                String context = configuration.getProperties().getProperty(
                        "context");
                String additionalContext = configuration.getProperties()
                        .getProperty("additional_context");
                phrescoServerUrl = protocol + "://" + host + ":" + port + "/"
                        + context + additionalContext;
            }
        } catch (Exception e) {
            throw new PhrescoException(e);
        }
        return phrescoServerUrl;
    }
}
