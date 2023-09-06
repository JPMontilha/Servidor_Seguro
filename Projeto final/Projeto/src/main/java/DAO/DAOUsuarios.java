package DAO;

import Back.Usuario;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Montilha
 */
public class DAOUsuarios {

    private static String bytesToHex(byte[] hashBytes) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private Connection connection = null;
    private PreparedStatement pstdados = null;
    private ResultSet rsdados = null;
    private static final String path = System.getProperty("user.dir")+"/src/main/java/DAO/";
    private static final File config_file = new File(path + "configuracaobd.properties");
    private static final String sqlconsultaclientes = "SELECT * FROM Usuario WHERE email = ? and senha = ?";
    private static final String sqlconsultaclientescomp = "SELECT * FROM Usuario WHERE email = ?";
    private static final String sqlinserir = "INSERT INTO USUARIO (email, senha) VALUES (?, ?)";
    private static final String sqlalterar = "UPDATE USUARIO SET senha = ? WHERE email = ?";
//    private static final String sqlaexcluir = "DELETE FROM USUARIO WHERE id_usuaario = ?";
    
    Usuario use = new Usuario();
    
    // Criação do pstdados
    private PreparedStatement SQLStatement(PreparedStatement pstdados, String acao, Usuario use){
        try {
            int tipo = ResultSet.TYPE_SCROLL_SENSITIVE;
            int concorrencia = ResultSet.CONCUR_UPDATABLE;
            
            if(acao.equals("inserir")){
                pstdados = connection.prepareStatement(sqlinserir, tipo, concorrencia);
                pstdados.setString(1, use.getEmail());                
                pstdados.setString(2, use.getSenha());
            }
            if(acao.equals("alterar")){
                pstdados = connection.prepareStatement(sqlalterar, tipo, concorrencia);
                pstdados.setString(2, use.getEmail());                
                pstdados.setString(1, use.getSenha());

            }
//            if(acao.equals("excluir")){
//                pstdados = connection.prepareStatement(sqlaexcluir, tipo, concorrencia);
//                pstdados.setInt(1, use.getIdUsuario());
//            }
            if(acao.equals("consultar")){
                pstdados = connection.prepareStatement(sqlconsultaclientes, tipo, concorrencia);
                pstdados.setString(1, use.getEmail());                
                pstdados.setString(2, use.getSenha());
            }
            if(acao.equals("consultarcomp")){
                pstdados = connection.prepareStatement(sqlconsultaclientescomp, tipo, concorrencia);
                pstdados.setString(1, use.getEmail());                
            }
            
        } catch (SQLException erro) {
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
    
    //Inserir um novo usuario
    public boolean Inserir(Usuario use) {
        try {
            pstdados = SQLStatement(pstdados, "inserir", use);
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
    
    //Alterar dados de um usuario existente
    public boolean Alterar(Usuario use) {
       try {
            pstdados = SQLStatement(pstdados, "alterar", use);
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
    
    //Consultas
    public boolean Consultar(List<String> aux, Usuario use) {
        try {
            pstdados = SQLStatement(pstdados, "consultar", use);
            rsdados = pstdados.executeQuery();
            aux.clear();
            while (rsdados.next()) {
                String consulta = rsdados.getInt("id_usuario") + " - " + rsdados.getString("email") + " - " + rsdados.getString("senha");
                aux.add(consulta);
            }
            return true;
        } catch (SQLException erro) {
            System.out.println("Erro ao executar consulta = " + erro);
        }
        return false;
    }
    
    public int ConsultarComp(List<String> aux, Usuario use) {
        int idUsuario = 0;

        try {
            pstdados = SQLStatement(pstdados, "consultarcomp", use);
            rsdados = pstdados.executeQuery();
            aux.clear();
            while (rsdados.next()) {
                idUsuario = rsdados.getInt("id_usuario");

                String consulta = idUsuario + " - " + rsdados.getString("email") + " - " + rsdados.getString("senha");
                aux.add(consulta);
            }
        } catch (SQLException erro) {
            System.out.println("Erro ao executar consulta = " + erro);
        }

        return idUsuario;
    }

    
}
