package sample;

import java.io.File;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import br.gov.frameworkdemoiselle.certificate.signer.factory.PKCS7Factory;
import br.gov.frameworkdemoiselle.certificate.signer.pkcs7.PKCS7Signer;
import br.gov.frameworkdemoiselle.certificate.signer.pkcs7.bc.policies.ADRBCMS_2_1;
import br.gov.frameworkdemoiselle.certificate.ui.util.Utils;
import br.gov.frameworkdemoiselle.certificate.util.ZipBytes;

public class AppTeste {

	public static Map<String, byte[]> files = Collections.synchronizedMap(new HashMap<String, byte[]>());
	public static Map<String, byte[]> signatures = Collections.synchronizedMap(new HashMap<String, byte[]>());
	
	public static void main(String[] args) throws IOException, KeyStoreException, NoSuchProviderException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException {

		String jnlpIdentifier = "091f5457-1cf2-4157-ae15-051e8639da12";
		String jnlpService = "http://localhost:8080/certificate-jws-web/api/filemanager";

		System.out.println("jnlp.identifier..: " + jnlpIdentifier);
		System.out.println("jnlp.service.....: " + jnlpService);
		
		//Apagar
		
		String configName = "/home/01534562567/drivers.config";
		String PIN = "qwaszx12!";
		Certificate[] certificates = null;
		Provider p = new sun.security.pkcs11.SunPKCS11(configName);
		Security.addProvider(p);

		KeyStore keyStore = KeyStore.getInstance("PKCS11", "SunPKCS11-Provedor");
		keyStore.load(null, PIN.toCharArray());

		String alias = (String) keyStore.aliases().nextElement();
		PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias,	PIN.toCharArray());

        /* Parametrizando o objeto doSign */
        PKCS7Signer signer = PKCS7Factory.getInstance().factoryDefault();
        signer.setCertificates(keyStore.getCertificateChain(alias));
        signer.setPrivateKey((PrivateKey) keyStore.getKey(alias, null));
        signer.setSignaturePolicy(new ADRBCMS_2_1());
        signer.setAttached(false);
        
        String conexao = jnlpService.concat("/download/");
        byte[] zip = Utils.downloadFromUrl(conexao, jnlpIdentifier);
        
        //Pegando arquivos do ZIP
        files = ZipBytes.decompressing(zip);
        Utils.writeContentToDisk(zip, System.getProperty("user.home").concat("/teste-resultado/").concat(File.separator).concat("files.zip"));
        
        //Assinando os arquivos
        for (Map.Entry<String, byte[]> entry : files.entrySet()) {
            Utils.writeContentToDisk(entry.getValue(), System.getProperty("user.home").concat("/teste-resultado/").concat(File.separator).concat(entry.getKey()));
            
            System.out.println("Assinando: " + entry.getKey());
            byte[] signed = signer.signer(entry.getValue());
            signatures.put(entry.getKey(), signed);
        }
        
        byte[] uploadZip = ZipBytes.compressing(signatures);
        Utils.writeContentToDisk(uploadZip, System.getProperty("user.home").concat("/teste-resultado/").concat(File.separator).concat("resultado.zip"));

        Utils.uploadToURL(uploadZip, jnlpService.concat("/upload/"), jnlpIdentifier);
        
	}
}