package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.EvaluationCriteria;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class EvaluationCriteriaMapper extends AbstractMapper<EvaluationCriteria> {
   
   @Override
   public EvaluationCriteria findById(int id) {
      String query = "SELECT numero, nom, description FROM CRITERES_EVALUATION WHERE numero = ?";
      Connection connection = ConnectionUtils.getConnection();
      
      try (PreparedStatement stmt = connection.prepareStatement(query)) {
         stmt.setInt(1, id);
         
         try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
               return mapRow(rs);
            }
         }
      } catch (SQLException ex) {
         logger.error("Erreur lors de findById pour CRITERES_EVALUATION : {}", ex.getMessage());
      }
      return null;
   }
   
   @Override
   public Set<EvaluationCriteria> findAll() {
      String query = "SELECT numero, nom, description FROM CRITERES_EVALUATION";
      Set<EvaluationCriteria> criteres = new HashSet<>();
      Connection connection = ConnectionUtils.getConnection();
      
      try (PreparedStatement stmt = connection.prepareStatement(query);
           ResultSet rs = stmt.executeQuery()) {
         
         while (rs.next()) {
            criteres.add(mapRow(rs));
         }
      } catch (SQLException ex) {
         logger.error("Erreur lors de findAll pour CRITERES_EVALUATION : {}", ex.getMessage());
      }
      return criteres;
   }
   
   @Override
   public EvaluationCriteria create(EvaluationCriteria object) {
      String query = "INSERT INTO CRITERES_EVALUATION (numero, nom, description) VALUES (?, ?, ?)";
      Connection connection = ConnectionUtils.getConnection();
      
      try (PreparedStatement stmt = connection.prepareStatement(query)) {
         int newId = getSequenceValue();
         object.setId(newId);
         
         stmt.setInt(1, newId);
         stmt.setString(2, object.getName());
         stmt.setString(3, object.getDescription());
         
         stmt.executeUpdate();
         return object;
      } catch (SQLException ex) {
         logger.error("Erreur lors de create pour CRITERES_EVALUATION : {}", ex.getMessage());
      }
      return null;
   }
   
   @Override
   public boolean update(EvaluationCriteria object) {
      String query = "UPDATE CRITERES_EVALUATION SET nom = ?, description = ? WHERE numero = ?";
      Connection connection = ConnectionUtils.getConnection();
      
      try (PreparedStatement stmt = connection.prepareStatement(query)) {
         stmt.setString(1, object.getName());
         stmt.setString(2, object.getDescription());
         stmt.setInt(3, object.getId());
         
         return stmt.executeUpdate() > 0;
      } catch (SQLException ex) {
         logger.error("Erreur lors de update pour CRITERES_EVALUATION : {}", ex.getMessage());
         return false;
      }
   }
   
   @Override
   public boolean delete(EvaluationCriteria object) {
      return deleteById(object.getId());
   }
   
   @Override
   public boolean deleteById(int id) {
      String query = "DELETE FROM CRITERES_EVALUATION WHERE numero = ?";
      Connection connection = ConnectionUtils.getConnection();
      
      try (PreparedStatement stmt = connection.prepareStatement(query)) {
         stmt.setInt(1, id);
         return stmt.executeUpdate() > 0;
      } catch (SQLException ex) {
         logger.error("Erreur lors de deleteById pour CRITERES_EVALUATION : {}", ex.getMessage());
         return false;
      }
   }
   
   @Override
   protected String getSequenceQuery() {
      // ⚠️ Adapter le nom exact de ta séquence Oracle
      return "SELECT SEQ_CRITERES_EVALUATION.NEXTVAL FROM DUAL";
   }
   
   @Override
   protected String getExistsQuery() {
      return "SELECT 1 FROM CRITERES_EVALUATION WHERE numero = ?";
   }
   
   @Override
   protected String getCountQuery() {
      return "SELECT COUNT(*) FROM CRITERES_EVALUATION";
   }
   
   /**
    * Mapping d'une ligne du ResultSet vers un objet EvaluationCriteria
    */
   private EvaluationCriteria mapRow(ResultSet rs) throws SQLException {
      int id = rs.getInt("numero");
      String nom = rs.getString("nom");
      String description = rs.getString("description");
      return new EvaluationCriteria(id, nom, description);
   }
}
