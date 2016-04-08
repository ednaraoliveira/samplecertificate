package sample.rest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import sample.token.TokenManager;
import br.gov.frameworkdemoiselle.certificate.signer.factory.PKCS7Factory;
import br.gov.frameworkdemoiselle.certificate.signer.pkcs7.PKCS7Signer;
import br.gov.frameworkdemoiselle.certificate.util.ZipBytes;

@Path("filemanager")
public class DownloadAndUploadREST {

	private final String SERVER_DOWNLOAD_LOCATION_FOLDER = "file/source/";
	private final String SERVER_UPLOAD_LOCATION_FOLDER = "file/signature/";
	private final String SIGNATURE_EXTENSION = ".p7s";
	private final String SIGNATURE_ZIP = ".zip";
	private final int FILE_BUFFER_SIZE = 4096;

	@Context
	ServletContext context;

	@Context
	HttpHeaders headers;
	
	@POST
	@Path("download")
	@Produces("application/zip")
	public Response download() throws IOException {
		System.out.println("br.gov.serpro.jnlp.rest.FileManagerService.download()");
		String downloadLocation = context.getRealPath("").concat(File.separator).concat(SERVER_DOWNLOAD_LOCATION_FOLDER);
		byte[] content = null;
		ResponseBuilder response = null;
		Map<String, byte[]> files = Collections.synchronizedMap(new HashMap<String, byte[]>());
		
		String token = headers.getRequestHeader("authorization").get(0).replace("Token ", "");

		//Buscar arquivos associados ao Token
		for (Map.Entry<String, String> filesToSign : TokenManager.get(token).entrySet()) {
			java.nio.file.Path path = Paths.get(downloadLocation.concat(filesToSign.getKey()));
			content = Files.readAllBytes(path);
			files.put(filesToSign.getKey(), content);
		}
		
		byte[] zipFiles = ZipBytes.compressing(files);
		
		response = Response.ok((Object) zipFiles);
		response.header("Content-Type", "application/zip");
		response.header("Content-Disposition", "attachment; filename=" + token+ ".zip");

		return response.build();

	}

	@POST
	@Path("upload")
	@Consumes("application/zip")
	public Response upload(InputStream payload) {
		String uploadLocation = context.getRealPath("")	.concat(File.separator).concat(SERVER_UPLOAD_LOCATION_FOLDER);
		System.out.println("br.gov.serpro.jnlp.rest.DownloadAndUploadService.upload()");
		
		Map<String, byte[]> signatures = Collections.synchronizedMap(new HashMap<String, byte[]>());

		String token = headers.getRequestHeader("authorization").get(0).replace("Token ", "");
		
		try {

			File directory = new File(uploadLocation);
			if (!directory.exists()) {
				if (directory.mkdirs()) {
					System.out.println("Multiple directories are created.");
				} else {
					System.out
							.println("Failed to create multiple directories.");
				}
			}

			ByteArrayOutputStream ba = new ByteArrayOutputStream();
			byte[] buffer = new byte[FILE_BUFFER_SIZE];

			int bytesRead = -1;
			System.out.println("Recebendo os dados...");

			while ((bytesRead = payload.read(buffer)) != -1) {
				ba.write(buffer, 0, bytesRead);
			}
			ba.flush();
			ba.close();
			System.out.println("Dados recebidos.");

			Calendar calendar = new GregorianCalendar();
			DateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmssSSS");

			java.nio.file.Path path = Paths.get(uploadLocation.concat(df.format(calendar.getTime())).concat(SIGNATURE_ZIP));
			Files.write(path, ba.toByteArray(), StandardOpenOption.CREATE);
			
			signatures = ZipBytes.decompressing(ba.toByteArray());
			for (Map.Entry<String, byte[]> entry : signatures.entrySet()) {
				path = Paths.get(uploadLocation.concat(entry.getKey()).concat(SIGNATURE_EXTENSION));
				Files.write(path, entry.getValue(), StandardOpenOption.CREATE);
				TokenManager.get(token).put(entry.getKey(),entry.getKey().concat(SIGNATURE_EXTENSION));
			}

		} catch (IOException ex) {
			Logger.getLogger(DownloadAndUploadREST.class.getName()).log(Level.SEVERE, null, ex);
		}
		check(token);
		return Response.status(Status.NO_CONTENT).build();
	}

	private boolean check(String token) {
		String downloadLocation = context.getRealPath("").concat(File.separator).concat(SERVER_DOWNLOAD_LOCATION_FOLDER);
		String uploadLocation = context.getRealPath("").concat(File.separator).concat(SERVER_UPLOAD_LOCATION_FOLDER);
		
		byte[] file = null;
		byte[] signature = null;

		Iterator entries = TokenManager.get(token).entrySet().iterator();
		while (entries.hasNext()) {
			Entry thisEntry = (Entry) entries.next();
			String nameFile = (String) thisEntry.getKey();
			String nameSignature = (String) thisEntry.getValue();
			
			System.out.println("validar aquivo.. :" + nameFile);
			java.nio.file.Path pathFile = Paths.get(downloadLocation.concat(nameFile));
			java.nio.file.Path pathSignature = Paths.get(uploadLocation.concat(nameSignature));
			
			try {
				file = Files.readAllBytes(pathFile);
				signature = Files.readAllBytes(pathSignature);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		PKCS7Signer signer = PKCS7Factory.getInstance().factoryDefault();
		signer.check(file, signature);

		return true;

	}

}
