package ch.hearc.ig.guideresto.persistence;
import ch.hearc.ig.guideresto.business.Grade;
import ch.hearc.ig.guideresto.business.CompleteEvaluation;
import ch.hearc.ig.guideresto.business.EvaluationCriteria;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class GradeMapper extends AbstractMapper {
   private static final Logger logger = LogManager.getLogger();
   
   // --- Requêtes SQL ---
   private static final String FIND_BY_ID_QUERY =
           "SELECT numero, note, fk_comm, fk_crit FROM NOTES WHERE numero = ?";
   private static final String FIND_ALL_QUERY =
           "SELECT numero, note, fk_comm, fk_crit FROM NOTES";
   private static final String INSERT_QUERY =
           "INSERT INTO NOTES (numero, note, fk_comm, fk_crit) VALUES (?, ?, ?, ?)";
   private static final String UPDATE_QUERY =
           "UPDATE NOTES SET note = ?, fk_comm = ?, fk_crit = ? WHERE numero = ?";
   private static final String DELETE_QUERY =
           "DELETE FROM NOTES WHERE numero = ?";
   private static final String EXISTS_QUERY =
           "SELECT 1 FROM NOTES WHERE numero = ?";
   private static final String COUNT_QUERY =
           "SELECT COUNT(*) FROM NOTES";
   private static final String SEQUENCE_QUERY =
           "SELECT SEQ_NOTES.NEXTVAL FROM dual"; // adapte si ta séquence s’appelle autrement
   
   // --- Dépendances ---
   private final CompleteEvaluationMapper evaluationMapper = new CompleteEvaluationMapper();
   private final EvaluationCriteriaMapper criteriaMapper = new EvaluationCriteriaMapper();
   
   // --- CRUD ---
   @Override
   public Grade findById(int id) {
      Connection connection = ConnectionUtils.getConnection();
      
      try (PreparedStatement stmt = connection.prepareStatement(FIND_BY_ID_QUERY)) {
         stmt.setInt(1, id);
         try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
               return mapRow(rs);
            }
         }
      } catch (SQLException ex) {
         logger.error("Erreur SQL dans findById: {}", ex.getMessage());
      }
      
      return null;
   }
   
   @Override
   public Set<Grade> findAll() {
      Set<Grade> grades = new HashSet<>();
      Connection connection = ConnectionUtils.getConnection();
      
      try (PreparedStatement stmt = connection.prepareStatement(FIND_ALL_QUERY);
           ResultSet rs = stmt.executeQuery()) {
         
         while (rs.next()) {
            grades.add(mapRow(rs));
         }
         
      } catch (SQLException ex) {
         logger.error("Erreur SQL dans findAll: {}", ex.getMessage());
      }
      
      return grades;
   }
   
   @Override
   public Grade create(Grade grade) {
      Connection connection = ConnectionUtils.getConnection();
      Integer nextId = getSequenceValue();
      grade.setId(nextId);
      
      try (PreparedStatement stmt = connection.prepareStatement(INSERT_QUERY)) {
         stmt.setInt(1, grade.getId());
         stmt.setInt(2, grade.getGrade());
         stmt.setInt(3, grade.getEvaluation().getId());
         stmt.setInt(4, grade.getCriteria().getId());
         
         int rows = stmt.executeUpdate();
         if (rows > 0) {
            return grade;
         }
         
      } catch (SQLException ex) {
         logger.error("Erreur SQL dans create: {}", ex.getMessage());
      }
      
      return null;
   }
   
   @Override
   public boolean update(Grade grade) {
      Connection connection = ConnectionUtils.getConnection();
      
      try (PreparedStatement stmt = connection.prepareStatement(UPDATE_QUERY)) {
         stmt.setInt(1, grade.getGrade());
         stmt.setInt(2, grade.getEvaluation().getId());
         stmt.setInt(3, grade.getCriteria().getId());
         stmt.setInt(4, grade.getId());
         return stmt.executeUpdate() > 0;
      } catch (SQLException ex) {
         logger.error("Erreur SQL dans update: {}", ex.getMessage());
      }
      
      return false;
   }
   
   @Override
   public boolean delete(Grade grade) {
      return deleteById(grade.getId());
   }
   
   @Override
   public boolean deleteById(int id) {
      Connection connection = ConnectionUtils.getConnection();
      
      try (PreparedStatement stmt = connection.prepareStatement(DELETE_QUERY)) {
         stmt.setInt(1, id);
         return stmt.executeUpdate() > 0;
      } catch (SQLException ex) {
         logger.error("Erreur SQL dans deleteById: {}", ex.getMessage());
      }
      
      return false;
   }
   
   // --- Requêtes utilitaires ---
   
   @Override
   protected String getSequenceQuery() {
      return SEQUENCE_QUERY;
   }
   
   @Override
   protected String getExistsQuery() {
      return EXISTS_QUERY;
   }
   
   @Override
   protected String getCountQuery() {
      return COUNT_QUERY;
   }
   
   // --- Mapping ResultSet → Grade ---
   private Grade mapRow(ResultSet rs) throws SQLException {
      int id = rs.getInt("numero");
      int note = rs.getInt("note");
      int evalId = rs.getInt("fk_comm");
      int critId = rs.getInt("fk_crit");
      
      CompleteEvaluation evaluation = evaluationMapper.findById(evalId);
      EvaluationCriteria criteria = criteriaMapper.findById(critId);
      
      return new Grade(id, note, evaluation, criteria);
   }
}
