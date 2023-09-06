package DAO;

import Back.Files;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author Montilha
 */
public class DAOFiles extends Files{
    
    private Connection connection = null;
    private PreparedStatement pstdados = null;
    private ResultSet rsdados = null;
    private static final String path = System.getProperty("user.dir")+"/src/main/java/DAO/";
    private static final File config_file = new File(path + "configuracaobd.properties");
    private static final String sqlconsulta = "SELECT * FROM FILES order by nome";
    private static final String sqlinserir = "INSERT INTO FILES (nome, dados, id_usuario) VALUES (?, ?, ?)";
    private static final String sqlalterar = "UPDATE FILES SET nome = ? WHERE id_file = ?";
    private static final String sqlaexcluir = "DELETE FROM FILES WHERE nome = ?";
    private static final String sqldownload = "SELECT dados FROM FILES where nome = ?";
    
    Files fil = new Files();
    
    // Criação do pstdados
    private PreparedStatement SQLStatement(PreparedStatement pstdados, String acao, Files fil){
        try {
            int tipo = ResultSet.TYPE_SCROLL_SENSITIVE;
            int concorrencia = ResultSet.CONCUR_UPDATABLE;
            
            if(acao.equals("inserir")){
                pstdados = connection.prepareStatement(sqlinserir, tipo, concorrencia);
                pstdados.setString(1, fil.getNome());
                
                File arquivo = fil.getDados();
                FileInputStream fis = new FileInputStream(arquivo);
                pstdados.setBinaryStream(2, fis, arquivo.length());
                
                pstdados.setInt(3, fil.getIdUsuario());
            }
            if(acao.equals("alterar")){
                pstdados = connection.prepareStatement(sqlalterar, tipo, concorrencia);
                pstdados.setString(1, fil.getNome());
                pstdados.setInt(2, fil.getIdFile());

            }
            if(acao.equals("excluir")){
                pstdados = connection.prepareStatement(sqlaexcluir, tipo, concorrencia);
                pstdados.setString(1, fil.getNome());
            }
            if(acao.equals("consultar")){
                pstdados = connection.prepareStatement(sqlconsulta, tipo, concorrencia);
            }
            if(acao.equals("download")){
                pstdados = connection.prepareStatement(sqldownload, tipo, concorrencia);
                pstdados.setString(1, fil.getNome());
            }
            
        } catch (SQLException erro) {
            System.out.println("Erro na execução da inserção = " + erro);
        }
        catch (FileNotFoundException erro) {
            System.out.println("Erro na execução da inserção = " + erro);
        }
        
        return pstdados;
    }
    

    //Conecta com o BD
    public boolean CriaConexao() {
        try {
            JDBCUtil.init(config_file);
            connection = JDBCUtil.getConnection();
            connection.setAutoCommit(false);//configuracao necessaria para confirmacao ou nao de alteracoes no banco de dados.

            DatabaseMetaData dbmt = connection.getMetaData();
            System.out.println("Nome do BD: " + dbmt.getDatabaseProductName());
            System.out.println("Versao do BD: " + dbmt.getDatabaseProductVersion());
            System.out.println("URL: " + dbmt.getURL());
            System.out.println("Driver: " + dbmt.getDriverName());
            System.out.println("Versao Driver: " + dbmt.getDriverVersion());
            System.out.println("Usuario: " + dbmt.getUserName());

            return true;
        } catch (ClassNotFoundException erro) {
            System.out.println("Falha ao carregar o driver JDBC." + erro);
        } catch (IOException erro) {
            System.out.println("Falha ao carregar o arquivo de configuração." + erro);
        } catch (SQLException erro) {
            System.out.println("Falha na conexao, comando sql = " + erro);
        }
        return false;
    }

    //Desconecta o BD
    public boolean FechaConexao() {
        if (connection != null) {
            try {
                connection.close();
                return true;
            } catch (SQLException erro) {
                System.err.println("Erro ao fechar a conexão = " + erro);
                return false;
            }
        } else {
            return false;
        }
    }
    
    //Inserir um novo file
    public boolean Inserir(Files fil) {
        try {
            pstdados = SQLStatement(pstdados, "inserir", fil);
            int resposta = pstdados.executeUpdate();
            pstdados.close();
            //DEBUG
            System.out.println("Resposta da inserção = " + resposta);
            //FIM-DEBUG
            if (resposta == 1) {
                connection.commit();
                return true;
            } else {
                connection.rollback();
                return false;
            }
        } catch (SQLException erro) {
            System.out.println("Erro na execução da inserção = " + erro);
        }
        return false;
    }
    
    //Alterar dados de um file existente
    public boolean Alterar(Files fil) {
       try {
            pstdados = SQLStatement(pstdados, "alterar", fil);
            int resposta = pstdados.executeUpdate();
            pstdados.close();
            //DEBUG
            System.out.println("Resposta da atualização = " + resposta);
            //FIM-DEBUG
            if (resposta == 1) {
                connection.commit();
                return true;
            } else {
                connection.rollback();
                return false;
            }
        } catch (SQLException erro) {
            System.out.println("Erro na execução da atualização = " + erro);
        }
        return false;
    }

    //Exclui um file existente
    public boolean Excluir(Files fil) {
        try {
            pstdados = SQLStatement(pstdados, "excluir", fil);
            int resposta = pstdados.executeUpdate();
            pstdados.close();
            //DEBUG
            System.out.println("Resposta da exclusão = " + resposta);
            //FIM-DEBUG
            if (resposta == 1) {
                connection.commit();
                return true;
            } else {
                connection.rollback();
                return false;
            }
        } catch (SQLException erro) {
            System.out.println("Erro na execução da exclusão = " + erro);
        }
        return false;
    }

    //Consultas filtradas e geral
    public boolean ConsultarTodos(List<String> aux) {
        try {
            pstdados = SQLStatement(pstdados, "consultar", fil);
            rsdados = pstdados.executeQuery();
            aux.clear();
            while (rsdados.next()) {
                String consulta = rsdados.getInt("id_file") + " - " + rsdados.getString("nome") + " - " + rsdados.getBinaryStream("dados") + " - " + rsdados.getInt("id_usuario");
                aux.add(consulta);
            }
            return true;
        } catch (SQLException erro) {
            System.out.println("Erro ao executar consulta = " + erro);
        }
        return false;
    }
    
     public boolean ConsultarFiltrado(List<String> aux, String filtro) {
        List<String> aux2 = new ArrayList<>();
        aux.clear();
        ConsultarTodos(aux2);
        for(String dado : aux2){
            String[] elementos = dado.split(" - ");
            String consulta = elementos[3];
            if(filtro.equals(consulta)){
                aux.add(dado);
            }
        }
        return true;
    }
     
     public boolean ConsultarFiltradoNome(List<String> aux, String filtro) {
        List<String> aux2 = new ArrayList<>();
        aux.clear();
        ConsultarTodos(aux2);
        for(String dado : aux2){
            String[] elementos = dado.split(" - ");
            String consulta = elementos[3];
            if(filtro.equals(consulta)){
                aux.add(elementos[1]);
            }
        }
        return true;
    }
     
     public InputStream ConsultarFiltradoComp(List<String> aux, String filtro) {
        try{
            List<String> aux2 = new ArrayList<>();
            aux.clear();
            ConsultarTodos(aux2);
            for(String dado : aux2){
                String[] elementos = dado.split(" - ");
                String consulta = elementos[1];
                if(filtro.equals(consulta)){
                    return rsdados.getBinaryStream("dados"); // Retorna o BinaryStream do arquivo
                }
            }
        }catch (SQLException erro) {
            System.out.println("Erro ao executar consulta = " + erro);
        }
        return null;
    }
     
    //Compartilhar arquivo
    public boolean Compartilhar(Files fil, FileInputStream fis){
        try {
            int tipo = ResultSet.TYPE_SCROLL_SENSITIVE;
            int concorrencia = ResultSet.CONCUR_UPDATABLE;
            pstdados = connection.prepareStatement(sqlinserir, tipo, concorrencia);
            pstdados.setString(1, fil.getNome());
            pstdados.setBinaryStream(2, fis);
            pstdados.setInt(3, fil.getIdUsuario());
            
            int resposta = pstdados.executeUpdate();
            pstdados.close();
            //DEBUG
            System.out.println("Resposta da inserção = " + resposta);
            //FIM-DEBUG
            if (resposta == 1) {
                connection.commit();
                return true;
            } else {
                connection.rollback();
                return false;
            }
        } catch (SQLException erro) {
            System.out.println("Erro na execução da inserção = " + erro);
        }
        return false;
    }
     
    //Download 
    public boolean download(String nome, String chave) {
        try {
            // Obter o diretório padrão do usuário
            String userHome = System.getProperty("user.home");
            Path downloadPath = Paths.get(userHome, "Downloads");
            
            // Cria o caminho completo do arquivo de destino
            Path filePath = downloadPath.resolve(nome);

            pstdados = SQLStatement(pstdados, "download", fil);
            pstdados.setString(1, nome); // Set the file name parameter in the prepared statement
            rsdados = pstdados.executeQuery();

            if (rsdados.next()) {
                // Obtém o stream de entrada do banco de dados
                InputStream inputStream = rsdados.getBinaryStream("dados");
                System.out.println("Arquivo encontrado");

                // Descriptografa o arquivo
                InputStream arquivoDescrip = descrip(inputStream, chave);
                if(arquivoDescrip == null){
                    return false;
                }

                // Cria o arquivo de destino
                OutputStream outputStream = new FileOutputStream(filePath.toFile());

                // Copia os dados do arquivo descriptografado para o arquivo de destino
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = arquivoDescrip.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                // Fecha os streams e exclui o arquivo descriptografado
                outputStream.close();
                arquivoDescrip.close();

                System.out.println("Arquivo baixado com sucesso: " + filePath);
                return true;
            } else {
                System.out.println("Arquivo não encontrado no banco de dados.");
                return false;
            }
        } catch (SQLException | IOException  e) {
            e.printStackTrace();
            return false;
        }
    }
     
    // Download sem descriptografar
    public void download(String nome) {
        try {
            // Obter o diretório padrão do usuário
            String userHome = System.getProperty("user.home");
            Path downloadPath = Paths.get(userHome, "Downloads");

            // Cria o caminho completo do arquivo de destino
            Path filePath = downloadPath.resolve(nome);

            pstdados = SQLStatement(pstdados, "download", fil);
            pstdados.setString(1, nome); // Set the file name parameter in the prepared statement
            rsdados = pstdados.executeQuery();

            if (rsdados.next()) {
                // Obtém o stream de entrada do banco de dados
                InputStream inputStream = rsdados.getBinaryStream("dados");
                System.out.println("Arquivo encontrado");

                // Cria o arquivo de destino
                OutputStream outputStream = new FileOutputStream(filePath.toFile());

                // Copia os dados do arquivo para o arquivo de destino
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                // Fecha os streams
                outputStream.close();
                inputStream.close();

                System.out.println("Arquivo baixado com sucesso: " + filePath);
            } else {
                System.out.println("Arquivo não encontrado no banco de dados.");
            }
        } catch (SQLException | IOException  e) {
            e.printStackTrace();
        }
    }
    
    //Descriptografar
    private InputStream descrip(InputStream inputStream, String chave) {
        try {
            byte[] chaveAjustada = ajustarTamanhoChave(chave, 32);

            SecretKeySpec chaveSecreta = new SecretKeySpec(chaveAjustada, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, chaveSecreta);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byte[] decryptedBytes = cipher.update(buffer, 0, bytesRead);
                outputStream.write(decryptedBytes);
            }
            byte[] decryptedBytes = cipher.doFinal();
            outputStream.write(decryptedBytes);

            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (Exception e) {
            System.out.println("Erro na descriptografia do código: " + e);
            return null;
        }
    }
    
    //Ajustar chave
    public byte[] ajustarTamanhoChave(String chave, int tamanhoChave) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] chaveHash = digest.digest(chave.getBytes(StandardCharsets.UTF_8));

        return Arrays.copyOf(chaveHash, tamanhoChave);
    }
        
}
