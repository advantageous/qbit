package io.advantageous.qbit.admin;


import io.advantageous.qbit.server.EndpointServerBuilder;
import io.advantageous.qbit.server.ServiceEndpointServer;
import io.advantageous.qbit.service.health.HealthServiceAsync;
import io.advantageous.qbit.service.health.HealthServiceBuilder;

public class AdminBuilder {


    private int port= 7777;
    private String host;
    private EndpointServerBuilder endpointServerBuilder;
    private ServiceEndpointServer serviceEndpointServer;
    private String name;
    private Admin admin;
    private HealthServiceAsync healthService;
    private HealthServiceBuilder healthServiceBuilder;

    public static AdminBuilder adminBuilder() {
        return new AdminBuilder();
    }

    public HealthServiceAsync getHealthService() {
        if (healthService==null) {
            healthService = getHealthServiceBuilder().buildAndStart();
        }
        return healthService;
    }

    public AdminBuilder setHealthService(HealthServiceAsync healthService) {
        this.healthService = healthService;
        return this;
    }

    public HealthServiceBuilder getHealthServiceBuilder() {
        if (healthServiceBuilder == null) {
            healthServiceBuilder = HealthServiceBuilder.healthServiceBuilder();
        }
        return healthServiceBuilder;
    }

    public AdminBuilder setHealthServiceBuilder(HealthServiceBuilder healthServiceBuilder) {
        this.healthServiceBuilder = healthServiceBuilder;
        return this;
    }

    public Admin getAdmin() {
        if (admin == null) {
            admin = new Admin(getHealthService());
        }
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    public String getName() {
        return name;
    }

    public AdminBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public ServiceEndpointServer getServiceEndpointServer() {

        if (serviceEndpointServer==null) {
            serviceEndpointServer = getEndpointServerBuilder().build();
        }
        return serviceEndpointServer;
    }

    public void setServiceEndpointServer(ServiceEndpointServer serviceEndpointServer) {
        this.serviceEndpointServer = serviceEndpointServer;
    }

    public int getPort() {
        return port;
    }

    public AdminBuilder setPort(int port) {
        this.port = port;
        return this;

    }

    public String getHost() {
        return host;
    }

    public AdminBuilder setHost(String host) {
        this.host = host;
        return this;
    }

    public EndpointServerBuilder getEndpointServerBuilder() {
        if (endpointServerBuilder == null) {
            endpointServerBuilder =  EndpointServerBuilder.endpointServerBuilder().setPort(this.getPort());
            if (host!=null && !"".equals(host)) {
                endpointServerBuilder.setHost(host);
            }
        }
        return endpointServerBuilder;
    }

    public AdminBuilder setEndpointServerBuilder(EndpointServerBuilder endpointServerBuilder) {
        this.endpointServerBuilder = endpointServerBuilder;
        return this;
    }

    public ServiceEndpointServer build() {
        ServiceEndpointServer serviceEndpointServer = getServiceEndpointServer();
        if (name==null) {
            serviceEndpointServer.initServices(getAdmin());
        } else {
            serviceEndpointServer.addServiceObject(name, getAdmin());
        }
        return serviceEndpointServer;
    }

}
