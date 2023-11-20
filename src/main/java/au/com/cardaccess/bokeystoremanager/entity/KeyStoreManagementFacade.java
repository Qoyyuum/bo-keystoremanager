package au.com.cardaccess.bokeystoremanager.entity;

import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Component
@ManagedResource(description = "JMX bean for keystore management", objectName = "jmix.data:type=KeyStoreBean")
public class KeyStoreManagementFacade {
    private final String KEYSTORE = "keys.p12";
    private final String STOREPASS = "password1234";

    private final String KEY_SUBJECT = "/C=BN/L=Bandar Seri Begawan/O=Ali Baba Sdn Bhd/OU=Alakazam/CN=";
    private String KEY_DOMAIN = ".example.keystore.com.bn";

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

    private String folderFriendlyName(String name) {
        return name.replaceAll("\\s", "_").toLowerCase();
    }

    @ManagedOperation
    @ManagedAttribute(description = "Create a keypair")
    private boolean createKeyPair(String entityName, String keyType) {
        File keysFolder = new File(folderFriendlyName(entityName) + "/keys");
        keysFolder.mkdir();
        File keyFile = new File(folderFriendlyName(entityName) + "/keys/" + keyType + ".key");
        File csrFile = new File(folderFriendlyName(entityName) + "/keys/" + keyType + ".csr");
//        String[] command = {"openssl", "req", "-new", "-newkey", "rsa:2048", "-nodes", "-keyout", folderFriendlyName(entityName) + "/keys/" + keyType};
        String command = "openssl req -new -newkey rsa:2048 -nodes -keyout" + folderFriendlyName(entityName) + "/keys/" + keyType + ".key" + " -out " + folderFriendlyName(entityName) + "/keys/" + keyType + ".csr" + " -subj " + KEY_SUBJECT + keyType + "." + folderFriendlyName(entityName) + KEY_DOMAIN;
        try {
            Process process = Runtime.getRuntime().exec(command);
            int exitCode = process.waitFor();
//            BufferedReader opensslreader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//            List<String> entities = new ArrayList<>();
//            String line;
//            while ((line = opensslreader.readLine()) != null) {
//                entities.add(line);
//            }
//            opensslreader.close();
            if (exitCode == 0) {
                return true;
            }
            else {
                return false;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

//    @ManagedOperation
//    @ManagedAttribute(description = "Create a new entity for this keystore")
//    public String createEntity(String entityName, String keyType) {
//        String folderName = folderFriendlyName(entityName);
//        File folder = new File(folderName);
//        if (folder.mkdir()) {
//            if (createKeyPair(entityName, keyType)) {
//                return entityName + " keys are created in " + folderName + " successfully!";
//            }
//            else {
//                return entityName + " keys are NOT created. It could already exist in " + folderName;
//            }
//        }
//        else {
//            return entityName + " folder has NOT been created...";
//        }
//    }

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