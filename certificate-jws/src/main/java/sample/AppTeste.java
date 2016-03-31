package sample;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import br.gov.frameworkdemoiselle.certificate.signer.factory.PKCS7Factory;
import br.gov.frameworkdemoiselle.certificate.signer.pkcs7.PKCS7Signer;
import br.gov.frameworkdemoiselle.certificate.signer.pkcs7.bc.policies.ADRBCMS_2_1;
import br.gov.frameworkdemoiselle.certificate.ui.util.Utils;

public class AppTeste {

	private final static int BUFFER_SIZE = 4096;

	public static void main(String[] args) throws IOException, KeyStoreException, NoSuchProviderException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException {

		String jnlpIdentifier = "1c7913ac-c5a3-4ad7-9706-d85b4dc04519";
		String jnlpService = "http://localhost:8080/certificate-jws-web/api/filemanager";

		System.out.println("jnlp.identifier..: " + jnlpIdentifier);
		System.out.println("jnlp.service.....: " + jnlpService);
		
		//Apagar
		
		String configName = "/home/01534562567/drivers.config";
		String PIN = "****";
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
        signer.setAttached(true);
		
		
		Utils utils = new Utils();
		// //Faz o download do conteudo a ser assinado
		String conexao = jnlpService.concat("/download/")
				.concat(jnlpIdentifier);
		System.out.println("Conectando em....: " + conexao);
		byte[] zip = utils.downloadFromUrl(conexao);
		System.out.println(System.getProperty("user.home"));
		utils.writeContentToDisk(
				zip,
				System.getProperty("user.home").concat(File.separator)
						.concat("teste-resultado/").concat("resultado.zip"));

		InputStream in = new ByteArrayInputStream(zip);
		ZipInputStream zipStream = new ZipInputStream(in);
		ZipEntry entry = null;
		BufferedOutputStream dest = null;

		while ((entry = zipStream.getNextEntry()) != null) {
			System.out.println("Extracting: " + entry);
			// write the files to the disk
			int count;
			FileOutputStream stream = new FileOutputStream(entry.getName());
			byte content[] = new byte[BUFFER_SIZE];
			dest = new BufferedOutputStream(stream, BUFFER_SIZE);
			while ((count = zipStream.read(content, 0, BUFFER_SIZE)) != -1) {
				dest.write(content, 0, count);
			}
			dest.flush();
			dest.close();
			System.out.println("Assinando: " + entry);
            byte[] signed = signer.signer(content);
            // Grava o conteudo assinado no disco para verificar o resultado
            utils.writeContentToDisk(signed, System.getProperty("user.home").concat("/teste-resultado/").concat(File.separator).concat("resultado.p7s"));

			
			
		}
		zipStream.close();

	}

}