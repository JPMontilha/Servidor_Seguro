package Back;

import java.io.File;

/**
 *
 * @author Montilha
 */
public class Files {
    protected int idFile;
    protected String nome;
    protected File dados;
    protected int idUsuario;
    
    public void setIdFile(int idFile) {this.idFile = idFile;}
    public int getIdFile() {return idFile;}
    
    public String getNome() {return nome;}
    public void setNome(String nome) {this.nome = nome;}
    
    public void setDados(File dados) {this.dados = dados;}
    public File getDados() {return dados;}
    
    public void setIdUsuario(int idUsuario) {this.idUsuario = idUsuario;}
    public int getIdUsuario() {return idUsuario;}
    
}
