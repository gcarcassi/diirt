/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.pods.web;

import org.diirt.pods.web.common.MessageValueEvent;
import org.diirt.pods.web.common.MessageConnectionEvent;
import org.diirt.pods.web.common.Message;
import org.diirt.pods.web.common.MessageWriteCompletedEvent;
import org.diirt.pods.web.common.MessageWrite;
import org.diirt.pods.web.common.MessageSubscribe;
import org.diirt.pods.web.common.MessageDecoder;
import org.diirt.pods.web.common.MessageErrorEvent;
import org.diirt.pods.web.common.MessageUnsubscribe;
import org.diirt.pods.web.common.MessageEncoder;
import org.diirt.pods.web.common.MessageResume;
import org.diirt.pods.web.common.MessagePause;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpSession;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.diirt.datasource.CompositeDataSource;
import org.diirt.datasource.DataSource;
import org.diirt.pods.common.ChannelTranslation;
import org.diirt.pods.common.ChannelTranslator;
import org.diirt.util.config.Configuration;
import org.diirt.datasource.PVManager;
import org.diirt.datasource.PVReader;
import org.diirt.datasource.PVReaderEvent;
import org.diirt.datasource.PVReaderListener;
import org.diirt.datasource.PVWriter;
import org.diirt.datasource.PVWriterEvent;
import org.diirt.datasource.PVWriterListener;
import static org.diirt.pods.common.ExpressionLanguage.*;
import org.diirt.datasource.formula.FormulaAst;
import org.diirt.datasource.loc.LocalDataSource;
import org.diirt.pods.common.ChannelRequest;
import org.diirt.pods.common.UserInfo;
import org.diirt.pods.web.common.MessageDecodeException;
import org.diirt.util.time.TimeDuration;

/**
 *
 * @author carcassi
 */
@ServerEndpoint(value = "/socket", decoders = {MessageDecoder.class}, encoders = {MessageEncoder.class}, configurator = WSEndpointConfigurator.class)
public class WSEndpoint {
    
    // TODO: understand lifecycle of whole web application and put
    // configuration there, including closing datasources.
    static {
        ChannelTranslator temp = null;
        try (InputStream input = Configuration.getFileAsStream("pods/web/mappings.xml", new WSEndpoint(), "mappings.default.xml")) {
            temp = ChannelTranslator.loadTranslator(input);
        } catch (Exception ex) {
            Logger.getLogger(WSEndpoint.class.getName()).log(Level.SEVERE, "Couldn't load DIIRT_HOME/pods/web/mappings", ex);
        }
        channelTranslator = temp;
    }
    
    private static Logger log = Logger.getLogger(WSEndpoint.class.getName());
    private static final ChannelTranslator channelTranslator;
    
    // XXX: need to understand how state can actually be used
    private final Map<Integer, PVReader<?>> channels = new ConcurrentHashMap<>();
    private int defaultMaxRate;
    
    private String currentUser;
    private String remoteAddress;
    private DataSource sessionDataSource;
    
    private static DataSource prepareSessionDataSource() {
        DataSource defaultDataSource = PVManager.getDefaultDataSource();
        if (defaultDataSource instanceof CompositeDataSource) {
            CompositeDataSource composite = (CompositeDataSource) defaultDataSource;
            CompositeDataSource session = composite.createSessionDataSource();
            session.putDataSource("loc", new LocalDataSource());
            return session;
        } else {
            return defaultDataSource;
        }
    }

    @OnMessage
    public void onMessage(Session session, Message message) {
        switch (message.getMessage()) {
            case SUBSCRIBE:
                onSubscribe(session, (MessageSubscribe) message);
                return;
            case UNSUBSCRIBE:
                onUnsubscribe(session, (MessageUnsubscribe) message);
                return;
            case PAUSE:
                onPause(session, (MessagePause) message);
                return;
            case RESUME:
                onResume(session, (MessageResume) message);
                return;
            case WRITE:
                onWrite(session, (MessageWrite) message);
                return;
            default:
                sendError(session, message.getId(), "Message '" + message.getMessage() + "' not supported on this server");
        }
    }

    private void onSubscribe(final Session session, final MessageSubscribe message) {
        if (channels.get(message.getId()) != null) {
            sendError(session, message.getId(), "Subscription with id '" + message.getId() + "' already exists");
            return;
        }
        
        // TODO: add maxRate check during parsing
        int maxRate = defaultMaxRate;
        if (message.getMaxRate() >= 20) {
            maxRate = message.getMaxRate();
        }
        
        connect(message.isReadOnly(), message.getChannel(), session, message, maxRate);
    }

    private void connect(boolean readOnly, String formula, final Session session, final MessageSubscribe message, int maxRate) {
        UserInfo userInfo = UserInfo.from(currentUser, remoteAddress);
        PVReader<?> reader;
        if (readOnly) {
            reader = PVManager.read(formula(formula, readOnly, channelTranslator, userInfo))
                    .readListener(new ReadOnlyListener(session, message))
                    .timeout(TimeDuration.ofSeconds(1.0), "Still connecting...")
                    .from(sessionDataSource)
                    .option("channelTranslator", channelTranslator)
                    .option("userInfo", userInfo)
                    .maxRate(TimeDuration.ofMillis(maxRate));
        } else {
            ReadWriteListener readWriteListener = new ReadWriteListener(session, message);
            reader = PVManager.readAndWrite(formula(formula, readOnly, channelTranslator, userInfo))
                    .readListener(readWriteListener)
                    .writeListener(readWriteListener)
                    .timeout(TimeDuration.ofSeconds(1.0), "Still connecting...")
                    .from(sessionDataSource)
                    .option("channelTranslator", channelTranslator)
                    .option("userInfo", userInfo)
                    .asynchWriteAndMaxReadRate(TimeDuration.ofMillis(maxRate));
        }
        channels.put(message.getId(), reader);
    }

    private void onUnsubscribe(Session session, MessageUnsubscribe message) {
        PVReader<?> channel = channels.remove(message.getId());
        if (channel != null) {
            channel.close();
        } else {
            sendError(session, message.getId(), "Subscription with id '" + message.getId() + "' does not exist");
        }
    }

    private void onPause(Session session, MessagePause message) {
        PVReader<?> channel = channels.get(message.getId());
        if (channel != null) {
            channel.setPaused(true);
        }
    }

    private void onResume(Session session, MessageResume message) {
        PVReader<?> channel = channels.get(message.getId());
        if (channel != null) {
            channel.setPaused(false);
        }
    }

    private void onWrite(Session session, MessageWrite message) {
        PVReader<?> channel = channels.get(message.getId());
        if (channel instanceof PVWriter) {
            @SuppressWarnings("unchecked")
            PVWriter<Object> channelWriter = (PVWriter<Object>) channel;
            channelWriter.write(message.getValue());
        } else {
            if (channel == null) {
                sendError(session, message.getId(), "Channel id '" + message.getId() + "' is not open");
            } else {
                sendError(session, message.getId(), "Channel id '" + message.getId() + "' is read-only");
            }
        }
    }

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        // Read the maxRate parameter
        String maxRate = session.getPathParameters().get("maxRate");
        if (maxRate != null) {
            try {
                defaultMaxRate = Integer.parseInt(maxRate);
                if (defaultMaxRate < 20) {
                    sendError(session, -1, "maxRate must be greater than 20");
                    defaultMaxRate = 1000;
                }
            } catch (NumberFormatException ex) {
                sendError(session, -1, "maxRate must be an integer");
            }
        } else {
            defaultMaxRate = 1000;
        }
        
        // Retrive user and remote host for security purposes
        HttpSession httpSession = (HttpSession) config.getUserProperties().get("session");
        remoteAddress = (String) httpSession.getAttribute("remoteHost");
        if (session.getUserPrincipal() != null) {
            currentUser = session.getUserPrincipal().getName();
        } else {
            currentUser = null;
        }
        
        // Prepare session DataSource
        sessionDataSource = prepareSessionDataSource();
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        for (Map.Entry<Integer, PVReader<?>> entry : channels.entrySet()) {
            PVReader<?> channel = entry.getValue();
            channel.close();
        }
        closed = true;
        sessionDataSource.close();
        sessionDataSource = null;
    }
    
    private volatile boolean closed = false;

    @OnError
    public void onError(Session session, Throwable cause) {
        if (cause instanceof MessageDecodeException) {
            MessageDecodeException de = (MessageDecodeException) cause;
            sendError(session, de.getId(), cause.getMessage());
        } else {
            log.log(Level.WARNING, "Unhandled exception", cause);
        }
    }
    
    public void sendError(Session session, int id, String message) {
        session.getAsyncRemote().sendObject(new MessageErrorEvent(id, message));
    }

    private class ReadOnlyListener implements PVReaderListener<Object> {

        private final Session session;
        private final MessageSubscribe message;

        public ReadOnlyListener(Session session, MessageSubscribe message) {
            this.session = session;
            this.message = message;
        }

        @Override
        public void pvChanged(PVReaderEvent<Object> event) {
            try {
                if (closed) {
                    log.log(Level.SEVERE, "Getting event after channel was closed for " + event.getPvReader().getName());
                    event.getPvReader().close();
                    return;
                }
                if (event.isConnectionChanged()) {
                    session.getAsyncRemote().sendObject(new MessageConnectionEvent(message.getId(), event.getPvReader().isConnected(), false));
                }
                if (event.isValueChanged()) {
                    session.getAsyncRemote().sendObject(new MessageValueEvent(message.getId(), event.getPvReader().getValue()));
                }
                if (event.isExceptionChanged()) {
                    session.getAsyncRemote().sendObject(new MessageErrorEvent(message.getId(), event.getPvReader().lastException().getMessage()));
                }
            } catch (RuntimeException ex) {
                log.log(Level.SEVERE, "Error while preparing event for " + event.getPvReader().getName(), ex);
            }
        }
    }
    
    private static boolean readConnected(Object channel) {
        @SuppressWarnings("unchecked")
        PVReader<Object> reader = (PVReader<Object>) channel;
        return reader.isConnected();
    }

    private class ReadWriteListener implements PVReaderListener<Object>, PVWriterListener<Object> {

        private final Session session;
        private final MessageSubscribe message;

        public ReadWriteListener(Session session, MessageSubscribe message) {
            this.session = session;
            this.message = message;
        }

        @Override
        public void pvChanged(PVReaderEvent<Object> event) {
            try {
                if (closed) {
                    log.log(Level.SEVERE, "Getting event after channel was closed for " + event.getPvReader().getName());
                    event.getPvReader().close();
                    return;
                }
                if (event.isValueChanged()) {
                    session.getAsyncRemote().sendObject(new MessageValueEvent(message.getId(), event.getPvReader().getValue()));
                }
                if (event.isExceptionChanged()) {
                    session.getAsyncRemote().sendObject(new MessageErrorEvent(message.getId(), event.getPvReader().lastException().getMessage()));
                }
            } catch (RuntimeException ex) {
                log.log(Level.SEVERE, "Error while preparing event for " + event.getPvReader().getName(), ex);
            }
        }

        @Override
        public void pvChanged(PVWriterEvent<Object> event) {
            try {
                if (closed) {
                    log.log(Level.SEVERE, "Getting event after channel was closed for " + event.getPvWriter());
                    event.getPvWriter().close();
                    return;
                }
                if (event.isConnectionChanged()) {
                    session.getAsyncRemote().sendObject(new MessageConnectionEvent(message.getId(), readConnected(event.getPvWriter()), event.getPvWriter().isWriteConnected()));
                }
                if (event.isWriteSucceeded()) {
                    session.getAsyncRemote().sendObject(new MessageWriteCompletedEvent(message.getId()));
                }
                if (event.isWriteFailed()) {
                    session.getAsyncRemote().sendObject(new MessageWriteCompletedEvent(message.getId(), event.getPvWriter().lastWriteException().getMessage()));
                }
                if (event.isExceptionChanged()) {
                    session.getAsyncRemote().sendObject(new MessageErrorEvent(message.getId(), event.getPvWriter().lastWriteException().getMessage()));
                }
            } catch (RuntimeException ex) {
                log.log(Level.SEVERE, "Error while preparing event for " + event.getPvWriter(), ex);
            }
        }
    }
}
