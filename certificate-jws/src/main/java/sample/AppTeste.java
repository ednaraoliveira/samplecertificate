package sample;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import br.gov.frameworkdemoiselle.certificate.signer.factory.PKCS7Factory;
import br.gov.frameworkdemoiselle.certificate.signer.pkcs7.PKCS7Signer;
import br.gov.frameworkdemoiselle.certificate.signer.pkcs7.bc.policies.ADRBCMS_2_1;
import br.gov.frameworkdemoiselle.certificate.ui.util.Utils;

public class AppTeste {

	private final static int BUFFER_SIZE = 1024;

	public static Map<String, byte[]> files = Collections.synchronizedMap(new HashMap<String, byte[]>());
	
	
	public static void main(String[] args) throws IOException, KeyStoreException, NoSuchProviderException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException {

		String jnlpIdentifier = "5ef7b056-41e4-41b5-af63-7e88f5fe304c";
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
		
		
		Utils utils = new Utils();
		// //Faz o download do conteudo a ser assinado
		String conexao = jnlpService.concat("/download/").concat(jnlpIdentifier);
		System.out.println("Conectando em....: " + conexao);
		byte[] zip = utils.downloadFromUrl(conexao);
		System.out.println(System.getProperty("user.home"));
		utils.writeContentToDisk(zip,System.getProperty("user.home").concat(File.separator).concat("teste-resultado/").concat("resultado.zip"));

		InputStream in = new ByteArrayInputStream(zip);
		ZipInputStream zipStream = new ZipInputStream(in);
		ZipEntry entry = null;
		
		BufferedOutputStream dest = null;
		
		long tempoInicio = System.currentTimeMillis();
		while ((entry = zipStream.getNextEntry()) != null) {
			System.out.println("Extracting: " + entry);

			FileInputStream stream = new FileInputStream(entry.getName());
			ByteArrayOutputStream bos = new ByteArrayOutputStream();

			byte[] buf = new byte[BUFFER_SIZE];
            for (int readNum; (readNum = stream.read(buf)) != -1;) {
                bos.write(buf, 0, readNum); //no doubt here is 0
            }
	        byte[] content = bos.toByteArray();
	        bos.close();
	        System.out.println("Tempo para ler: "+(System.currentTimeMillis()-tempoInicio));
	        System.out.println("Assinando: " + entry);
            byte[] signed = signer.signer(content);
            System.out.println("Tempo para assinar: "+(System.currentTimeMillis()-tempoInicio));
            //Grava o conteudo assinado no disco para verificar o resultado
            utils.writeContentToDisk(content, System.getProperty("user.home").concat("/teste-resultado/").concat(File.separator).concat(entry.getName()));
            utils.writeContentToDisk(signed, System.getProperty("user.home").concat("/teste-resultado/").concat(File.separator).concat(entry.getName()+".p7s"));
            files.put(entry.getName(), signed);
		}
		zipStream.close();
		System.out.println("Tempo Total: "+(System.currentTimeMillis()-tempoInicio));
	}
}