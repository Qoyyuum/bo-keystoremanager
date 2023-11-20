package au.com.cardaccess.bokeystoremanager.entity;

import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Component
@ManagedResource(description = "JMX bean for keystore management", objectName = "jmix.data:type=KeyStoreBean")
public class KeyStoreManagementFacade {
    private final String KEYSTORE = "keys.p12";
    private final String STOREPASS = "password1234";

    @ManagedOperation
    @ManagedAttribute(description = "Testing")
    public String helloBrunei() {
        return "Hello Brunei";
    }
    @ManagedOperation
    @ManagedAttribute(description = "List entities in Keystore")
    public List<String> listEntities() throws IOException, RuntimeException {
        String command = "keytool -list -v -keystore " + KEYSTORE + " -storepass " + STOREPASS;
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader keystorereader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            List<String> entities = new ArrayList<>();
            String line;
            while ((line = keystorereader.readLine()) != null) {
                entities.add(line);
            }
            keystorereader.close();
            return entities;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

//    @EventListener
//    public void onApplicationContextRefreshed(final ContextRefreshedEvent event) {
//
//    }
//
//    @EventListener
//    public void onApplicationStarted(final ApplicationStartedEvent event) {
//
//    }

    @ManagedOperation
    @ManagedAttribute(description = "Delete an entity in keystore")
    public void deleteCertificate(String alias) throws IOException {
        String command = "keytool -delete -alias " + alias + " -keystore keystore.jks";
        try {
            Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}