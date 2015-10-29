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

/**
 * This class is used to create a JMS client (called JmsService) that can send and receive messages via JMS.
 * @author rick hightower
 */
public class JmsServiceBuilder {


    /**
     * JNDI name of initial factory. Default works with ActiveMQ.
     */
    private String initialContextFactory ="org.apache.activemq.jndi.ActiveMQInitialContextFactory";


    /**
     * JNDI Lookup name of ConnectionFactory.
     */
    private String connectionFactoryName = "ConnectionFactory";

    /**
     * JMS user name.
     */
    private String userName=null;


    /**
     * JMS password.
     */
    private String password = null;

    /**
     * Host for JMS broker.
     */
    private String host = "localhost";


    /**
     * Port for JMS broker.
     */
    private int port = 61616;


    /** Are we just looking up the connection or should we start it to? */
    private boolean startConnection = true;


    /** Should access to the destination be transacted? */
    private boolean transacted = false;


    /** Session acknowledgement mode from `java.jms.Session`. */
    private int acknowledgeMode = Session.AUTO_ACKNOWLEDGE;

    /** Additional JNDI setting, usually not needed. */
    private Map<String, Object> jndiSettings;

    /** Pattern to create JNDI URL connect string from host and port. Default works with ActiveMQ.
     * Replaces #host# with host and #port# with port. */
    private String providerURLPattern = "failover:tcp://#host#:#port#";

    /** Provider URL to JMS Broker which if not set will be constructed based on host, port and providerURLPattern. */
    private String providerURL =null;

    /**
     * JMS Connection factory. If not set, will be looked up in JNDI.
     */
    private ConnectionFactory connectionFactory;

    /**
     * JNDI Prefix for destination lookup. Default will lookup destination from ActiveMQ `dynamicQueues` which
     * will create the queue on the fly.
     */
    private String destinationPrefix = "dynamicQueues/";


    /** Name of our default destination which can be a queue or topic. */
    private String defaultDestination;


    /** Supplies a JMS connection. Allows you to get the connection in the most convenient way possible.
     *  If this is setup, a connection will not be looked up in JNDI.
     *  By implementing this, you are saying, I know how to get the connection from my JMS provider.
     * */
    private Supplier<Connection> connectionSupplier;

    /** Supplies a JMS destination given the name of a destination.
     * Allows you to get the destination in the most convenient way possible.
     *  If this is setup, a destination will not be looked up in JNDI.
     *  By implementing this, you are saying, I know how to get the destination from my JMS provider.
     * */
    private Function<String, Destination> destinationSupplier;

    /**
     * Default timeout in milliseconds.
     */
    private int defaultTimeout=10;


    /**
     * Initial JNDI context, if set, it will not be created by using the `initialContextFactory`.
     */
    private Context context;

    /**
     * Create a new JMS Service Builder.
     * @return JmsServiceBuilder
     */
    public static JmsServiceBuilder newJmsServiceBuilder() {
        return new JmsServiceBuilder();
    }

    /**
     *
     * @return providerURLPattern
     */
    public String getProviderURLPattern() {
        return providerURLPattern;
    }

    /**
     *
     * @return initialContextFactory
     */
    public String getInitialContextFactory() {
        return initialContextFactory;
    }


    /**
     *
     * @return userName
     */
    public String getUserName() {
        return userName;
    }


    /**
     *
     * @return password
     */
    public String getPassword() {
        return password;
    }


    /**
     *
     * @return host
     */
    public String getHost() {
        return host;
    }


    /**
     *
     * @return port
     */
    public int getPort() {
        return port;
    }


    /**
     *
     * @return startConnection
     */
    public boolean isStartConnection() {
        return startConnection;
    }

    /**
     *
     * @return transacted
     */
    public boolean isTransacted() {
        return transacted;
    }



    /**
     *
     * @see JmsServiceBuilder#acknowledgeMode
     * @return acknowledgeMode
     */
    public int getAcknowledgeMode() {
        return acknowledgeMode;
    }


    /**
     * If null, will build based on host, port and provider url pattern.
     * @see JmsServiceBuilder#providerURL
     * @return providerURL
     */
    public String getProviderURL() {

        if (providerURL == null) {
            providerURL = getProviderURLPattern().replace("#host#", getHost())
                    .replace("#port#",  Integer.toString(getPort()));
        }
        return providerURL;
    }

    /**
     * @see JmsServiceBuilder#jndiSettings
     * @return jndi settings
     */
    public Map<String, Object> getJndiSettings() {
        if (jndiSettings==null) {
            jndiSettings = new LinkedHashMap<>();
        }

        return jndiSettings;
    }

    /**
     * Gets the initial JNDI context, if not set uses the jndi settings and `initialContextFactory` to create
     * a JNDI initial context.
     *
     * @see JmsServiceBuilder#context
     * @see JmsServiceBuilder#initialContextFactory
     * @see JmsServiceBuilder#jndiSettings
     *
     * @return JNDI initial context.
     */
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

    /**
     *
     * @see JmsServiceBuilder#connectionFactory
     * @return connection factory
     */
    public String getConnectionFactoryName() {
        return connectionFactoryName;
    }


    /**
     *
     * @see JmsServiceBuilder#destinationPrefix
     * @return destination prefix
     */
    public String getDestinationPrefix() {
        return destinationPrefix;
    }

    /**
     * @see JmsServiceBuilder#defaultDestination
     * @return default JMS Destination Queue or Topic
     */
    public String getDefaultDestination() {
        return defaultDestination;
    }

    /**
     * If the user name is set, use the user name and password to create the JMS connection.
     * @see JmsServiceBuilder#connectionSupplier
     * @return connection supplier
     */
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

    /**
     * @see JmsServiceBuilder#destinationSupplier
     * @return destination
     */
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

    /**
     * @see JmsServiceBuilder#defaultTimeout
     * @return defaultTimeout
     */
    public int getDefaultTimeout() {
        return defaultTimeout;
    }

    /**
     *
     * @param defaultTimeout default timeout
     * @return this
     */
    public JmsServiceBuilder setDefaultTimeout(int defaultTimeout) {
        this.defaultTimeout = defaultTimeout;
        return this;
    }


    /**
     * @see JmsServiceBuilder#connectionFactory
     * @return connection factory
     */
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


    /**
     *
     * @param destinationSupplier destinationSupplier
     * @return this
     */
    public JmsServiceBuilder setDestinationSupplier(final Function<String, Destination> destinationSupplier) {
        this.destinationSupplier = destinationSupplier;
        return this;
    }

    /**
     *
     * @param connectionSupplier connection supplier
     * @return this
     */
    public JmsServiceBuilder setConnectionSupplier(Supplier<Connection> connectionSupplier) {
        this.connectionSupplier = connectionSupplier;
        return this;
    }

    /**
     *
     * @param destinationPrefix destination prefix
     * @return this
     */
    public JmsServiceBuilder setDestinationPrefix(String destinationPrefix) {
        this.destinationPrefix = destinationPrefix;
        return this;
    }

    /**
     *
     * @param defaultDestination defaultDestination
     * @return this
     */
    public JmsServiceBuilder setDefaultDestination(String defaultDestination) {
        this.defaultDestination = defaultDestination;
        return this;
    }

    /**
     *
     * @param connectionFactory connectionFactory
     * @return this
     */
    public JmsServiceBuilder setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
        return this;
    }

    /**
     *
     * @param connectionFactoryName connectionFactoryName
     * @return this
     */
    public JmsServiceBuilder setConnectionFactoryName(String connectionFactoryName) {
        this.connectionFactoryName = connectionFactoryName;
        return this;
    }


    /**
     *
     * @param context context
     * @return this
     */
    public JmsServiceBuilder setContext(Context context) {
        this.context = context;
        return this;
    }

    /**
     *
     * @param jndiSettings jndiSettings
     * @return this
     */
    public JmsServiceBuilder setJndiSettings(Map<String, Object> jndiSettings) {
        this.jndiSettings = jndiSettings;
        return this;
    }


    /**
     *
     * @param key key for JNDI setting
     * @param value value for JNDI setting
     * @return this
     */
    public JmsServiceBuilder addJndiSetting(final String key, final Object value) {
        this.getJndiSettings().put(key, value);
        return this;
    }


    /**
     *
     * @param acknowledgeMode Session acknowledge Mode
     * @return this
     */
    public JmsServiceBuilder setAcknowledgeMode(int acknowledgeMode) {
        this.acknowledgeMode = acknowledgeMode;
        return this;
    }

    /**
     *
     * @param providerURL providerURL
     * @return this
     */
    public JmsServiceBuilder setProviderURL(String providerURL) {
        this.providerURL = providerURL;
        return this;
    }

    /**
     *
     * @param providerURLPattern providerURLPattern
     * @return this
     */
    public JmsServiceBuilder setProviderURLPattern(String providerURLPattern) {
        this.providerURLPattern = providerURLPattern;
        return this;
    }

    /**
     *
     * @param initialContextFactory initialContextFactory
     * @return this
     */
    public JmsServiceBuilder setInitialContextFactory(String initialContextFactory) {
        this.initialContextFactory = initialContextFactory;
        return this;
    }


    /**
     *
     * @param password password
     * @return this
     */
    public JmsServiceBuilder setPassword(String password) {
        this.password = password;
        return this;
    }

    /**
     *
     * @param host host
     * @return this
     */
    public JmsServiceBuilder setHost(String host) {
        this.host = host;
        return this;
    }

    /**
     *
     * @param port port
     * @return this
     */
    public JmsServiceBuilder setPort(int port) {
        this.port = port;
        return this;
    }


    /**
     *
     * @param startConnection startConnection
     * @return this
     */
    public JmsServiceBuilder setStartConnection(boolean startConnection) {
        this.startConnection = startConnection;
        return this;
    }

    /**
     *
     * @param transacted transacted
     * @return this
     */
    public JmsServiceBuilder setTransacted(boolean transacted) {
        this.transacted = transacted;
        return this;
    }


    /**
     *
     * @param userName user name
     * @return this
     */
    public JmsServiceBuilder setUserName(String userName) {
        this.userName = userName;
        return this;
    }


    /**
     * Used to construct properties for JMS JNDI context.
     * Populates with
     *
     * ```
     *  properties.put(Context.INITIAL_CONTEXT_FACTORY, getInitialContextFactory());
     *  properties.put(Context.PROVIDER_URL, getProviderURL());
     * ```
     * Then adds all of the setting in jndi settings.
     * @return this
     */
    private Hashtable<Object,  Object> createProperties () {
        Hashtable<Object,  Object> properties = new Hashtable<>();

        properties.put(Context.INITIAL_CONTEXT_FACTORY, getInitialContextFactory());
        properties.put(Context.PROVIDER_URL, getProviderURL());

        if (getJndiSettings()!=null) {
            getJndiSettings().entrySet().forEach(entry -> properties.put(entry.getKey(), entry.getValue()));
        }
        return properties;
    }

    /**
     * Build JMS Service
     * @return new JMS Service
     */
    public JmsService build() {
        return new JmsService(
                 getConnectionSupplier(), getDestinationSupplier(), isTransacted(),
                getAcknowledgeMode(), isStartConnection(), getDefaultDestination(), getDefaultTimeout());
    }
}
