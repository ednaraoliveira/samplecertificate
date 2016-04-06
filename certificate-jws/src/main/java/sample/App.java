/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sample;

import java.io.File;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;

import javax.swing.JOptionPane;

import br.gov.frameworkdemoiselle.certificate.signer.factory.PKCS7Factory;
import br.gov.frameworkdemoiselle.certificate.signer.pkcs7.PKCS7Signer;
import br.gov.frameworkdemoiselle.certificate.signer.pkcs7.bc.policies.ADRBCMS_2_1;
import br.gov.frameworkdemoiselle.certificate.ui.action.AbstractFrameExecute;
import br.gov.frameworkdemoiselle.certificate.ui.util.ConectionException;
import br.gov.frameworkdemoiselle.certificate.ui.util.Utils;
import br.gov.frameworkdemoiselle.certificate.ui.view.Principal;

/**
 *
 * @author 07721825741
 */
public class App extends AbstractFrameExecute {

    String jnlpIdentifier = "";
    String jnlpService = "";

    /**
     * Carrega as variaveis do arquivo jnlp
     */
    public App() {

        jnlpIdentifier = System.getProperty("jnlp.identifier");
        jnlpService = System.getProperty("jnlp.service");

        System.out.println("jnlp.identifier..: " + jnlpIdentifier);
        System.out.println("jnlp.service.....: " + jnlpService);
    }

    @Override
    public void execute(KeyStore ks, String alias, Principal principal) {
        try {

            if (jnlpIdentifier == null || jnlpIdentifier.isEmpty()) {
                JOptionPane.showMessageDialog(principal, "A variavel \"jnlp.identifier\" não está configurada.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (jnlpService == null || jnlpService.isEmpty()) {
                JOptionPane.showMessageDialog(principal, "A variavel \"jnlp.service\" não está configurada.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            /* Parametrizando o objeto doSign */
            PKCS7Signer signer = PKCS7Factory.getInstance().factoryDefault();
            signer.setCertificates(ks.getCertificateChain(alias));
            signer.setPrivateKey((PrivateKey) ks.getKey(alias, null));
            signer.setSignaturePolicy(new ADRBCMS_2_1());
            signer.setAttached(true);
            /* Realiza a assinatura do conteudo */
            System.out.println("Efetuando a  assinatura do conteudo");
            Utils utils = new Utils();
            //Faz o download do conteudo a ser assinado
    		String conexao = jnlpService.concat("/download/");
    		System.out.println("Conectando em....: " + conexao);
    		byte[] content = utils.downloadFromUrl(conexao, jnlpIdentifier);
            byte[] signed = signer.signer(content);
            // Grava o conteudo assinado no disco para verificar o resultado
            utils.writeContentToDisk(signed, System.getProperty("user.home").concat(File.separator).concat("resultado.p7s"));

            //Faz o upload do conteudo assinado
//            utils.uploadToURL(signed, jnlpService.concat("/upload/").concat(jnlpIdentifier));
            utils.uploadToURL(signed, jnlpService.concat("/upload/"),jnlpIdentifier);
            JOptionPane.showMessageDialog(principal, "O arquivo foi assinado com sucesso.", "Mensagem", JOptionPane.INFORMATION_MESSAGE);
            System.exit(0);
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | ConectionException ex) {
            JOptionPane.showMessageDialog(principal, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }

    @Override
    public void cancel(KeyStore ks, String alias, Principal principal) {
        /* Seu codigo customizado aqui... */
        System.out.println("br.gov.serpro.certificate.ui.user.App.cancel()");
        principal.setVisible(false); //you can't see me!
        principal.dispose(); //Destroy the JFrame object
    }

}
