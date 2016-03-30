package sample;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import br.gov.frameworkdemoiselle.certificate.ui.util.ConectionException;
import br.gov.frameworkdemoiselle.certificate.ui.util.Utils;

public class AppTeste {

	private final int BUFFER_SIZE = 4096;

	public static void main(String[] args) throws IOException {

		String jnlpIdentifier = "ed247156-5e03-45bb-b734-26befe29ee58";
		String jnlpService = "http://localhost:8080/certificate-jws-web/api/filemanager";

		System.out.println("jnlp.identifier..: " + jnlpIdentifier);
		System.out.println("jnlp.service.....: " + jnlpService);

		Utils utils = new Utils();
		// //Faz o download do conteudo a ser assinado
		String conexao = jnlpService.concat("/download/").concat(jnlpIdentifier);
		System.out.println("Conectando em....: " + conexao);
		byte[] content = utils.downloadFromUrl(conexao);
		System.out.println(System.getProperty("user.home"));
		utils.writeContentToDisk(content, System.getProperty("user.home").concat(File.separator).concat("teste-resultado/").concat("resultado.zip"));
		
		InputStream in = new ByteArrayInputStream(content);
		ZipInputStream zip = new ZipInputStream(in);
		ZipEntry entry = null;
		
		while ((entry = zip.getNextEntry()) != null) {
			System.out.println("Extracting: " +entry);
            int count;
//            byte data[] = new byte[BUFFER];
//            // write the files to the disk
//            FileOutputStream fos = new 
//	      FileOutputStream(entry.getName());
//            dest = new 
//              BufferedOutputStream(fos, BUFFER);
//            while ((count = zis.read(data, 0, BUFFER)) 
//              != -1) {
//               dest.write(data, 0, count);
//            }
//            dest.flush();
//            dest.close();			
		}
		
		
	}

	public static int listaArquivos(String UrlToDownload) {
		ByteArrayOutputStream outputStream = null;
		int qtd = 0;
		try {
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet(UrlToDownload);
			HttpResponse response = client.execute(request);

			HttpEntity entity = response.getEntity();

			qtd = Integer.parseInt(EntityUtils.toString(entity));

		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}
		return qtd;
	}

	public byte[] downloadFromUrl(String UrlToDownload) {
		ByteArrayOutputStream outputStream = null;
		try {
			System.out
					.println("br.gov.serpro.certificate.ui.util.Utils.downloadFromUrl()");
			URL url = new URL(UrlToDownload);
			outputStream = new ByteArrayOutputStream();
			byte[] chunk = new byte[BUFFER_SIZE];
			int bytesRead;
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			int responseCode = con.getResponseCode();
			System.out.println("Response Code...: " + responseCode);
			if (responseCode != HttpURLConnection.HTTP_OK) {
				Logger.getLogger(Utils.class.getName()).log(Level.SEVERE,
						"Server returned non-OK code: {0}", responseCode);
				throw new ConectionException("Server returned non-OK code: "
						+ responseCode);
			} else {
				InputStream stream = con.getInputStream();

				while ((bytesRead = stream.read(chunk)) > 0) {
					outputStream.write(chunk, 0, bytesRead);
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return outputStream.toByteArray();
	}

}
