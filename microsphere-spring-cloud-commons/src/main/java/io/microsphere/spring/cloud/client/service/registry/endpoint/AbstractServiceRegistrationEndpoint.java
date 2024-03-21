package io.microsphere.spring.cloud.client.service.registry.endpoint;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.boot.web.server.WebServer;
import org.springframework.cloud.client.serviceregistry.AbstractAutoServiceRegistration;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;
import org.springframework.context.ApplicationListener;

/**
 * Abstract Endpoint for Service Registration
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class AbstractServiceRegistrationEndpoint implements SmartInitializingSingleton, ApplicationListener<WebServerInitializedEvent> {

    @Value("${spring.application.name}")
    protected String applicationName;

    @Autowired
    private ObjectProvider<Registration> registrationProvider;

    @Autowired
    private ObjectProvider<ServiceRegistry> serviceRegistryProvider;

    @Autowired
    private ObjectProvider<AbstractAutoServiceRegistration> autoServiceRegistrationProvider;

    protected Registration registration;

    protected ServiceRegistry serviceRegistry;

    protected AbstractAutoServiceRegistration serviceRegistration;

    protected int port;

    protected static boolean running;

    @Override
    public void afterSingletonsInstantiated() {
        this.registration = registrationProvider.getIfAvailable();
        this.serviceRegistry = serviceRegistryProvider.getIfAvailable();
        this.serviceRegistration = autoServiceRegistrationProvider.getIfAvailable();
    }

    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        WebServer webServer = event.getWebServer();
        this.port = webServer.getPort();
        this.running = serviceRegistration == null ? true : serviceRegistration.isRunning();
    }

    protected boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}
