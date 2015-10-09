package io.advantageous.qbit.jms;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class JmsServiceBuilder {


    private String initialContextFactory ="org.apache.activemq.jndi.ActiveMQInitialContextFactory";
    private String connectionFactoryName = "ConnectionFactory";
    private String userName=null;
    private String password = null;
    private String host = "localhost";
    private int port = 61616;
    private boolean startConnection = true;
    private boolean transacted = false;
    private int acknowledgeMode = Session.AUTO_ACKNOWLEDGE;
    private Map<String, Object> jndiSettings;
    private String providerURLPattern = "tcp://#host#:#port#";
    private String providerURL =null;
    private ConnectionFactory connectionFactory;
    private String destinationPrefix = "dynamicQueues/";
    private String defaultDestination;
    private Supplier<Connection> connectionSupplier;
    private Function<String, Destination> destinationSupplier;
    private int defaultTimeout=100;

    private Context context;

    public static JmsServiceBuilder newJmsServiceBuilder() {
        return new JmsServiceBuilder();
    }

    public String getProviderURLPattern() {
        return providerURLPattern;
    }

    public String getInitialContextFactory() {
        return initialContextFactory;
    }


    public String getUserName() {
        return userName;
    }


    public String getPassword() {
        return password;
    }

    public String getHost() {
        return host;
    }


    public int getPort() {
        return port;
    }


    public boolean isStartConnection() {
        return startConnection;
    }

    public boolean isTransacted() {
        return transacted;
    }


    public int getAcknowledgeMode() {
        return acknowledgeMode;
    }


    public String getProviderURL() {

        if (providerURL == null) {
            providerURL = getProviderURLPattern().replace("#host#", getHost())
                    .replace("#port#",  Integer.toString(getPort()));
        }
        return providerURL;
    }

    public Map<String, Object> getJndiSettings() {
        if (jndiSettings==null) {
            jndiSettings = new LinkedHashMap<>();
        }

        return jndiSettings;
    }

    public Context getContext() {
        if (context == null) {
            try {
                context = new InitialContext(createProperties());
            } catch (NamingException e) {
                throw new IllegalStateException("Unable to create context", e);
            }
        }
        return context;
    }

    public String getConnectionFactoryName() {
        return connectionFactoryName;
    }

    public String getDestinationPrefix() {
        return destinationPrefix;
    }

    public String getDefaultDestination() {
        return defaultDestination;
    }

    public Supplier<Connection> getConnectionSupplier() {
        final boolean startConnection = isStartConnection();

        if (connectionSupplier==null) {
            if (getUserName()==null) {
                connectionSupplier = () -> {
                    try {
                        final Connection connection = getConnectionFactory().createConnection();
                        if (startConnection) {
                            connection.start();
                        }
                        return connection;
                    } catch (JMSException e) {
                        throw new IllegalStateException("Unable to create context", e);
                    }
                };
            }else {
                final String userName = getUserName();
                final String password = getPassword();

                connectionSupplier = () -> {
                    try {
                        final Connection connection = getConnectionFactory().createConnection(userName, password);
                        if (startConnection) {
                            connection.start();
                        }
                        return connection;
                    } catch (JMSException e) {
                        throw new IllegalStateException("Unable to create context for user " + userName, e);
                    }
                };
            }
        }
        return connectionSupplier;
    }

    public Function<String, Destination> getDestinationSupplier() {

        final Context context = getContext();
        final String prefix = getDestinationPrefix();

        if (destinationSupplier == null) {
            destinationSupplier = destinationName -> {

                try {
                    return (Destination) context.lookup(prefix + destinationName);
                } catch (NamingException e) {
                    throw new IllegalStateException("Unable to lookup destination " + prefix + destinationName , e);

                }
            };
        }
        return destinationSupplier;
    }

    public int getDefaultTimeout() {
        return defaultTimeout;
    }

    public JmsServiceBuilder setDefaultTimeout(int defaultTimeout) {
        this.defaultTimeout = defaultTimeout;
        return this;
    }

    public JmsServiceBuilder setDestinationSupplier(final Function<String, Destination> destinationSupplier) {
        this.destinationSupplier = destinationSupplier;
        return this;
    }

    public JmsServiceBuilder setConnectionSupplier(Supplier<Connection> connectionSupplier) {
        this.connectionSupplier = connectionSupplier;
        return this;
    }

    public JmsServiceBuilder setDestinationPrefix(String destinationPrefix) {
        this.destinationPrefix = destinationPrefix;
        return this;
    }

    public JmsServiceBuilder setDefaultDestination(String defaultDestination) {
        this.defaultDestination = defaultDestination;
        return this;
    }

    public ConnectionFactory getConnectionFactory() {
        if (connectionFactory == null) {
            try {
                connectionFactory = (ConnectionFactory) getContext().lookup(getConnectionFactoryName());
            } catch (NamingException e) {
                throw new IllegalStateException("Unable to create JMS connection factory", e);
            }
        }
        return connectionFactory;
    }

    public JmsServiceBuilder setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
        return this;
    }
    public JmsServiceBuilder setConnectionFactoryName(String connectionFactoryName) {
        this.connectionFactoryName = connectionFactoryName;
        return this;
    }


    public JmsServiceBuilder setContext(Context context) {
        this.context = context;
        return this;
    }

    public JmsServiceBuilder setJndiSettings(Map<String, Object> jndiSettings) {
        this.jndiSettings = jndiSettings;
        return this;
    }


    public JmsServiceBuilder addJndiSetting(final String key, final Object value) {
        this.getJndiSettings().put(key, value);
        return this;
    }


    public JmsServiceBuilder setAcknowledgeMode(int acknowledgeMode) {
        this.acknowledgeMode = acknowledgeMode;
        return this;
    }
    public JmsServiceBuilder setProviderURL(String providerURL) {
        this.providerURL = providerURL;
        return this;
    }

    public JmsServiceBuilder setProviderURLPattern(String providerURLPattern) {
        this.providerURLPattern = providerURLPattern;
        return this;
    }

    public JmsServiceBuilder setInitialContextFactory(String initialContextFactory) {
        this.initialContextFactory = initialContextFactory;
        return this;
    }


    public JmsServiceBuilder setPassword(String password) {
        this.password = password;
        return this;
    }

    public JmsServiceBuilder setHost(String host) {
        this.host = host;
        return this;
    }

    public JmsServiceBuilder setPort(int port) {
        this.port = port;
        return this;
    }


    public JmsServiceBuilder setStartConnection(boolean startConnection) {
        this.startConnection = startConnection;
        return this;
    }

    public JmsServiceBuilder setTransacted(boolean transacted) {
        this.transacted = transacted;
        return this;
    }


    public JmsServiceBuilder setUserName(String userName) {
        this.userName = userName;
        return this;
    }


    private Hashtable<Object,  Object> createProperties () {
        Hashtable<Object,  Object> properties = new Hashtable<>();

        properties.put(Context.INITIAL_CONTEXT_FACTORY, getInitialContextFactory());
        properties.put(Context.PROVIDER_URL, getProviderURL());

        if (getJndiSettings()!=null) {
            getJndiSettings().entrySet().forEach(entry -> properties.put(entry.getKey(), entry.getValue()));
        }
        return properties;
    }

    public JmsService build() {
        return new JmsService(
                 getConnectionSupplier(), getDestinationSupplier(), isTransacted(),
                getAcknowledgeMode(), isStartConnection(), getDefaultDestination(), getDefaultTimeout());
    }
}
