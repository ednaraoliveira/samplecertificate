/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sample.rest;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import sample.token.TokenManager;


@Path("token")
public class TokenService {

    MessageDigest md = null;

    public TokenService() {
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(TokenService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @GET
    @Path("generate/{info}")
    @Produces("application/json")
    public MyMessage generate(@PathParam("info") String info) {
    	
    	Map<String, String> files = Collections.synchronizedMap(new HashMap<String, String>());
    	for (String nameFiles : info.split(",")) {
			files.put(nameFiles, null);
		}
    	
    	String token = TokenManager.put(files);
    	MyMessage message = new MyMessage(token);
    	System.out.println(message);
        return message;
    }

//    @POST
//    @Path("/validate")
//    public Response validate(String digest, String seed) {
//        System.out.println("br.gov.serpro.jnlp.rest.TokenService.validate()");
//        return null;
//    }

    private String toHexFormat(byte[] byteData) {
        //convert the byte to hex format
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < byteData.length; i++) {
            sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

}
