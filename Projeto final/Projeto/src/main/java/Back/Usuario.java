package Back;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author Montilha
 */
public class Usuario {
    protected int idUsuario;
    protected String email;
    protected String senha;
    
    public void setIdUsuario(int idUsuario) {this.idUsuario = idUsuario;}
    public int getIdUsuario() {return idUsuario;}
    
    public void setEmail(String email) {this.email = email;}
    public String getEmail() {return email;}
    
    public void setSenha(String senha) {this.senha = gerarHash(senha);}
    public String getSenha() {return senha;}
    
    //Hash da senha
    public static String gerarHash(String texto){
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(texto.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            for (byte b : hashBytes) {
                result.append(String.format("%02X", b));
            }
            return result.toString();
        } catch(NoSuchAlgorithmException erro){
            System.out.println("Erro ao gerar hash " + erro);
            return texto;
        }
    }
    
}
