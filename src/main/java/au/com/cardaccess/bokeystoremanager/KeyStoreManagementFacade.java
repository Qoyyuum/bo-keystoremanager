package au.com.cardaccess.bokeystoremanager;

import org.slf4j.Logger;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

@Component
@ManagedResource(description = "JMX bean for keystore management", objectName = "jmix.data:type=KeyStoreBean")
public class KeyStoreManagementFacade {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(KeyStoreManagementFacade.class);
    private final String KEYSTORE = "keys.p12";
    private final char[] STOREPASS = "password1234".toCharArray();
    private final String BASEPATH = "./";

    private final String KEY_SUBJECT = "/C=BN/L=Bandar Seri Begawan/O=Ali Baba Sdn Bhd/OU=Alakazam/CN=";
    private String KEY_DOMAIN = ".example.keystore.com.bn";

    @ManagedOperation
    @ManagedAttribute(description = "List entities in keystore")
    public List<String> listEntities() throws CertificateException, IOException, NoSuchAlgorithmException, KeyStoreException {
        KeyStore keystore = KeyStore.getInstance("pkcs12");
        keystore.load(new FileInputStream(BASEPATH + KEYSTORE), STOREPASS);
        Enumeration<String> enumeration = keystore.aliases();
        List<String> aliases = new ArrayList<>();
        while(enumeration.hasMoreElements()) {
            String alias = enumeration.nextElement();
            X509Certificate cert = (X509Certificate)keystore.getCertificate(alias);
            Date expiryDate = cert.getNotAfter();

            aliases.add(alias + " - Expires: " + expiryDate);
        }

        return aliases;
    }

//    @ManagedOperation
//    @ManagedAttribute(description = "List entities in Keystore")
//    public List<String> listEntities() throws IOException, RuntimeException {
//        String command = "keytool -list -v -keystore " + KEYSTORE + " -storepass " + STOREPASS;
//        try {
//            Process process = Runtime.getRuntime().exec(command);
//            BufferedReader keystorereader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//            List<String> entities = new ArrayList<>();
//            String line;
//            while ((line = keystorereader.readLine()) != null) {
//                entities.add(line);
//            }
//            keystorereader.close();
//            return entities;
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }

    private String folderFriendlyName(String name) {
        return name.replaceAll("\\s", "_").toLowerCase();
    }

//    @ManagedOperation
//    @ManagedAttribute(description = "Create a keypair")
//    private boolean createKeyPair(String entityName, String keyType) {
//        String BaseKeyPath = folderFriendlyName(entityName) + "/keys/";
//        File keysFolder = new File(BASEPATH + BaseKeyPath);
//        keysFolder.mkdir();
//        File keyTypeFolder = new File(BASEPATH + BaseKeyPath + keyType);
//        keyTypeFolder.mkdir();
//        String[] command = {"openssl", "req", "-new", "-newkey", "rsa:2048", "-nodes", "-keyout", keyTypeFolder + "/" + keyType + ".key" , " -out " , keyTypeFolder + "/" + keyType + ".csr" , "-subj", "'" + KEY_SUBJECT + keyType + "." + folderFriendlyName(entityName) + KEY_DOMAIN + "'"};
////        String command = "openssl req -new -newkey rsa:2048 -nodes -keyout " + keyTypeFolder + "/" + keyType + ".key" + " -out " + keyTypeFolder + "/" + keyType + ".csr" + " -subj '" + KEY_SUBJECT + keyType + "." + folderFriendlyName(entityName) + KEY_DOMAIN + "'";
//        log.info(Arrays.toString(command));
//        try {
//            Process process = new ProcessBuilder(command).start();
////            Process process = Runtime.getRuntime().exec(command);
//            BufferedReader outputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
//
//            String line;
//            StringBuilder output = new StringBuilder();
//            StringBuilder errorOutput = new StringBuilder();
//
//            while ((line = outputReader.readLine()) != null) {
//                output.append(line).append("\n");
//            }
//
//            while ((line = errorReader.readLine()) != null) {
//                errorOutput.append(line).append("\n");
//            }
//
//            outputReader.close();
//            errorReader.close();
//
//            int exitCode = process.waitFor();
//            if (exitCode == 0) {
//                log.info("Successfully made the keypair");
//                return true;
//            }
//            else {
//                log.info("Error Output:\n" + errorOutput.toString());
//                return false;
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//    }

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

    @ManagedOperation
    @ManagedAttribute(description = "Delete an entity in keystore")
    public void deleteCertificate(String alias) throws IOException, RuntimeException {
        String command = "keytool -delete -alias " + alias + " -keystore keystore.jks";
        try {
            Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @ManagedOperation
    @ManagedAttribute(description = "Generate key pair for new entity")
    public static PublicKey generateKeyPair(String alias) throws Exception {

        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);

        KeyPair pair = generator.generateKeyPair();

        PublicKey publicKey = pair.getPublic();

        return publicKey;
    }

    @ManagedOperation
    @ManagedAttribute(description = "Saves the signed certificate into the keystore as a new entry")
    public static void setKeyEntry(String alias, PrivateKey privateKey, X509Certificate certificate) throws Exception {

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null);

        X509Certificate[] chain = new X509Certificate[1];
        chain[0] = certificate;

        keyStore.setKeyEntry(alias, privateKey, new char[0], chain);
    }
}