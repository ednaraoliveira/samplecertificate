package sample.rest;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.jws.Oneway;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import sample.token.TokenManager;

@Path("filemanager")
public class DownloadAndUploadService {

	private final String SERVER_DOWNLOAD_LOCATION_FOLDER = "file/source/";
	private final String SERVER_UPLOAD_LOCATION_FOLDER = "file/signature/";
	private final String SIGNATURE_EXTENSION = ".p7s";
	private final int FILE_BUFFER_SIZE = 4096;

	@Context
	ServletContext context;
	
	@GET
	@Path("downloadantigo/{fileID}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response downloadAntigo(@PathParam("fileID") String fileID) {
		byte[] data = null;
		String fileName = null;
		ResponseBuilder response = null;
		
		Map<String, String> files = TokenManager.get(fileID);
		for (String name : files.keySet()) {
			fileName = name;
		}
		
		System.out.println("br.gov.serpro.jnlp.rest.FileManagerService.download()");
		System.out.println("fileId..:" + fileID);
		System.out.println("arquivo selecionado...: " + fileName);

		
		if (fileName != null) {
			TokenManager.destroy(fileID);

			try {
				String downloadLocation = context.getRealPath("")
						.concat(File.separator)
						.concat(SERVER_DOWNLOAD_LOCATION_FOLDER);
				System.out.println(downloadLocation);

				// Carrega o arquivo utilizando new io
				java.nio.file.Path path = Paths.get(downloadLocation.concat(fileName));
				data = Files.readAllBytes(path);

			} catch (IOException ex) {
				Logger.getLogger(DownloadAndUploadService.class.getName()).log(Level.SEVERE, null, ex);
			}

			response = Response.ok((Object) data);
			response.header("Content-Disposition",	"attachment;filename=classes.jar");
		}
		else{
			response = Response.status(406);			
		}
		
		return response.build();

	}	
	
	@GET
	@Path("download/{fileID}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response download(@PathParam("fileID") String fileID) throws IOException {
		System.out.println("br.gov.serpro.jnlp.rest.FileManagerService.download()");
		
		byte[] data = null;
		Map<String, String> files = TokenManager.get(fileID);
		ResponseBuilder response = null;
		String downloadLocation = context.getRealPath("").concat(File.separator).concat(SERVER_DOWNLOAD_LOCATION_FOLDER);
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ZipOutputStream zipOut = new ZipOutputStream(out);

		for (String fileName : files.keySet()) {

			//Lendo os arquivos
			try {
				// Carrega o arquivo utilizando new io
				java.nio.file.Path path = Paths.get(downloadLocation.concat(fileName));
				data = Files.readAllBytes(path);

			} catch (IOException ex) {
				Logger.getLogger(DownloadAndUploadService.class.getName()).log(Level.SEVERE, null, ex);
			}
			
			System.out.println("Adding " + fileName);
			
			zipOut.putNextEntry(new ZipEntry(fileName));
			zipOut.write(data);
			zipOut.setLevel(0);
			zipOut.closeEntry();
			
			System.out.println("Finishedng file " + fileName);
		}
		
		zipOut.close();
		out.close();
		
		data = out.toByteArray();

		response = Response.ok((Object) data);
		response.header("Content-Disposition", "attachment; filename=" + fileID +".zip");
		return response.build();

	}

	@POST
	@Path("upload")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	public Response upload(InputStream payload) {
		try {
			System.out
					.println("br.gov.serpro.jnlp.rest.DownloadAndUploadService.upload()");

			String uploadLocation = context.getRealPath("")
					.concat(File.separator)
					.concat(SERVER_UPLOAD_LOCATION_FOLDER);

			File directory = new File(uploadLocation);
			if (!directory.exists()) {
				if (directory.mkdirs()) {
					System.out.println("Multiple directories are created.");
				} else {
					System.out
							.println("Failed to create multiple directories.");
				}
			}

			DataInputStream dis = new DataInputStream(payload);
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

			java.nio.file.Path path = Paths.get(uploadLocation.concat(
					df.format(calendar.getTime())).concat(SIGNATURE_EXTENSION));
			Files.write(path, ba.toByteArray(), StandardOpenOption.CREATE);

		} catch (IOException ex) {
			Logger.getLogger(DownloadAndUploadService.class.getName()).log(
					Level.SEVERE, null, ex);
		}
		return Response.status(Status.OK).build();
	}
}
