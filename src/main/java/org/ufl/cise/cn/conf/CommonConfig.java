package org.ufl.cise.cn.conf;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ufl.cise.cn.exceptions.BitTorrentPrototypeException;
import java.io.*;
import java.util.Properties;

public enum CommonConfig {

    NumberOfPreferredNeighbors,
    UnchokingInterval,
    OptimisticUnchokingInterval,
    FileName,
    FileSize,
    PieceSize;

    public static final String CONFIG_FILE_NAME = "Common.cfg";
    private static Properties commonProperties;
    private static final Log LOG = LogFactory.getLog(CommonConfig.class);

    public static void loadProperty() throws BitTorrentPrototypeException {
        if (commonProperties == null) {
            try {
                commonProperties = new java.util.Properties();
                commonProperties.load(new FileInputStream(new File(CONFIG_FILE_NAME)));
            } catch (IOException exception) {
                LOG.error(exception.getMessage());
                throw new BitTorrentPrototypeException(exception.getMessage());
            }
        }
    }

    public static String getProperty(String key) throws BitTorrentPrototypeException {
        if (commonProperties == null) {
            synchronized (CommonConfig.class) {
                loadProperty();
            }
        }
        return commonProperties.getProperty(key);
    }
}