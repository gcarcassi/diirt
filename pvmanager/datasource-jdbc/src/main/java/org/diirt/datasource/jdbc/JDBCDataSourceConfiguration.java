/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.jdbc;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.diirt.datasource.DataSourceConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Configuration for {@link JDBCDataSource}. This object is mutable, and
 * therefore not thread-safe.
 *
 * @author carcassi
 */
public final class JDBCDataSourceConfiguration extends DataSourceConfiguration<JDBCDataSource> {
    
    // Package private so we don't need getters
    Map<String, String> connections;
    List<Channel> channels;
    int pollInterval;

    @Override
    public JDBCDataSourceConfiguration read(InputStream input) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(input);
            
            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xPath = xpathFactory.newXPath();
            
            String ver = xPath.evaluate("/jdbc/@version", document);
            if (!ver.equals("1")) {
                throw new IllegalArgumentException("Unsupported version " + ver);
            }
            
            String pollIntervalSetting = xPath.evaluate("/jdbc/channels/@pollInterval", document);
            pollInterval = Integer.parseInt(pollIntervalSetting);
            
            Map<String, String> newConnections = new HashMap<>();
            NodeList xmlConnections = (NodeList) xPath.evaluate("/jdbc/connections/connection", document, XPathConstants.NODESET);
            for (int i = 0; i < xmlConnections.getLength(); i++) {
                Node xmlConnection = xmlConnections.item(i);
                String name = xPath.evaluate("@name", xmlConnection);
                String jdbcUrl = xPath.evaluate("@jdbcUrl", xmlConnection);
                newConnections.put(name, jdbcUrl);
            }
            
            List<Channel> newChannels = new ArrayList<>();
            NodeList xmlChannelSets = (NodeList) xPath.evaluate("/jdbc/channels/channelSet", document, XPathConstants.NODESET);
            for (int i = 0; i < xmlChannelSets.getLength(); i++) {
                Node xmlChannelSet = xmlChannelSets.item(i);
                String connectionName = xPath.evaluate("@connectionName", xmlChannelSet);
                NodeList xmlChannels = (NodeList) xPath.evaluate("channel", xmlChannelSet, XPathConstants.NODESET);
                for (int j = 0; j < xmlChannels.getLength(); j++) {
                    Node xmlChannel = xmlChannels.item(j);
                    String channelPattern =  xPath.evaluate("@name", xmlChannel);
                    String query =  xPath.evaluate("query", xmlChannel);
                    String pollQuery =  xPath.evaluate("pollQuery", xmlChannel);
                    newChannels.add(new Channel(channelPattern, connectionName, query, pollQuery));
                }
            }
            
            connections = newConnections;
            channels = newChannels;
        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException ex) {
            Logger.getLogger(JDBCDataSourceConfiguration.class.getName()).log(Level.FINEST, "Couldn't load jdbc configuration", ex);
            throw new IllegalArgumentException("Couldn't load jdbc configuration", ex);
        }
        return this;
    }
    
    class Channel {
        final String channelPattern;
        final String connectionName;
        final String query;
        final String pollQuery;

        public Channel(String channelPattern, String connectionName, String query, String pollQuery) {
            this.channelPattern = channelPattern;
            this.connectionName = connectionName;
            this.query = query;
            this.pollQuery = pollQuery;
        }
        
    }

    @Override
    public JDBCDataSource create() {
        return new JDBCDataSource(this);
    }
    
}
