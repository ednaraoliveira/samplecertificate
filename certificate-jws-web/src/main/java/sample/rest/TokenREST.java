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
public class TokenREST {

    MessageDigest md = null;

    public TokenREST() {
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(TokenREST.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @GET
    @Path("generate/{info}")
    @Produces("text/plain")
    public String generate(@PathParam("info") String info) {
    	
    	Map<String, String> files = Collections.synchronizedMap(new HashMap<String, String>());
    	for (String nameFiles : info.split(",")) {
			files.put(nameFiles, null);
		}
    	
    	String token = TokenManager.put(files);
    	//MyMessage message = new MyMessage(token);
    	System.out.println(token);
        return token;
    }

}
