package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.CompleteEvaluation;
import ch.hearc.ig.guideresto.business.Restaurant;
import ch.hearc.ig.guideresto.business.Grade;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Mapper JDBC pour CompleteEvaluation
 * Table : COMMENTAIRES
 */
public class CompleteEvaluationMapper extends AbstractMapper<CompleteEvaluation> {
   
   private static final Logger logger = LogManager.getLogger(CompleteEvaluationMapper.class);
   
   // --- SQL Queries ---
   private static final String FIND_BY_ID =
           "SELECT numero, date_eval, commentaire, nom_utilisateur, fk_rest FROM COMMENTAIRES WHERE numero = ?";
   private static final String FIND_ALL =
           "SELECT numero, date_eval, commentaire, nom_utilisateur, fk_rest FROM COMMENTAIRES";
   private static final String INSERT =
           "INSERT INTO COMMENTAIRES (numero, date_eval, commentaire, nom_utilisateur, fk_rest) VALUES (?, ?, ?, ?, ?)";
   private static final String UPDATE =
           "UPDATE COMMENTAIRES SET date_eval = ?, commentaire = ?, nom_utilisateur = ?, fk_rest = ? WHERE numero = ?";
   private static final String DELETE =
           "DELETE FROM COMMENTAIRES WHERE numero = ?";
   private static final String EXISTS =
           "SELECT 1 FROM COMMENTAIRES WHERE numero = ?";
   private static final String COUNT =
           "SELECT COUNT(*) FROM COMMENTAIRES";
   private static final String SEQUENCE =
           "SELECT SEQ_EVAL.NEXTVAL FROM dual"; // adapte si le nom diffère
   
   // --- Dépendances ---
   private final RestaurantMapper restaurantMapper = new RestaurantMapper();
   private final GradeMapper gradeMapper = new GradeMapper(); // pour charger les notes liées
   
   // --- CRUD ---
   
   @Override
   public CompleteEvaluation findById(int id) {
      Connection conn = ConnectionUtils.getConnection();
      try (PreparedStatement stmt = conn.prepareStatement(FIND_BY_ID)) {
         stmt.setInt(1, id);
         try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
               CompleteEvaluation eval = mapRow(rs);
               
               // Charger les grades associés
               Set<Grade> grades = gradeMapper.findByEvaluationId(eval.getId());
               eval.setGrades(grades);
               
               return eval;
            }
         }
      } catch (SQLException ex) {
         logger.error("Erreur SQL dans findById: {}", ex.getMessage());
      }
      return null;
   }
   
   @Override
   public Set<CompleteEvaluation> findAll() {
      Set<CompleteEvaluation> evaluations = new HashSet<>();
      Connection conn = ConnectionUtils.getConnection();
      try (PreparedStatement stmt = conn.prepareStatement(FIND_ALL);
           ResultSet rs = stmt.executeQuery()) {
         
         while (rs.next()) {
            CompleteEvaluation eval = mapRow(rs);
            Set<Grade> grades = gradeMapper.findByEvaluationId(eval.getId());
            eval.setGrades(grades);
            evaluations.add(eval);
         }
         
      } catch (SQLException ex) {
         logger.error("Erreur SQL dans findAll: {}", ex.getMessage());
      }
      return evaluations;
   }
   
   @Override
   public CompleteEvaluation create(CompleteEvaluation eval) {
      Connection conn = ConnectionUtils.getConnection();
      Integer nextId = getSequenceValue();
      eval.setId(nextId);
      
      try (PreparedStatement stmt = conn.prepareStatement(INSERT)) {
         stmt.setInt(1, eval.getId());
         stmt.setDate(2, new java.sql.Date(eval.getVisitDate().getTime()));
         stmt.setInt(3, eval.getRestaurant().getId());
         stmt.setString(4, eval.getComment());
         stmt.setString(5, eval.getUsername());
         
         int rows = stmt.executeUpdate();
         if (rows > 0) {
            // enregistrer aussi les grades
            for (Grade g : eval.getGrades()) {
               g.setEvaluation(eval);
               gradeMapper.create(g);
            }
            return eval;
         }
         
      } catch (SQLException ex) {
         logger.error("Erreur SQL dans create: {}", ex.getMessage());
      }
      
      return null;
   }
   
   @Override
   public boolean update(CompleteEvaluation eval) {
      Connection conn = ConnectionUtils.getConnection();
      try (PreparedStatement stmt = conn.prepareStatement(UPDATE)) {
         stmt.setDate(1, new java.sql.Date(eval.getVisitDate().getTime()));
         stmt.setInt(2, eval.getRestaurant().getId());
         stmt.setString(3, eval.getComment());
         stmt.setString(4, eval.getUsername());
         stmt.setInt(5, eval.getId());
         
         int rows = stmt.executeUpdate();
         if (rows > 0) {
            // mettre à jour les grades aussi
            for (Grade g : eval.getGrades()) {
               gradeMapper.update(g);
            }
            return true;
         }
         
      } catch (SQLException ex) {
         logger.error("Erreur SQL dans update: {}", ex.getMessage());
      }
      return false;
   }
   
   @Override
   public boolean delete(CompleteEvaluation eval) {
      return deleteById(eval.getId());
   }
   
   @Override
   public boolean deleteById(int id) {
      Connection conn = ConnectionUtils.getConnection();
      
      try {
         // supprimer d'abord les grades
         gradeMapper.deleteByEvaluationId(id);
         
         try (PreparedStatement stmt = conn.prepareStatement(DELETE)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
         }
         
      } catch (SQLException ex) {
         logger.error("Erreur SQL dans deleteById: {}", ex.getMessage());
      }
      return false;
   }
   
   // --- AbstractMapper requirements ---
   @Override
   protected String getSequenceQuery() {
      return SEQUENCE;
   }
   
   @Override
   protected String getExistsQuery() {
      return EXISTS;
   }
   
   @Override
   protected String getCountQuery() {
      return COUNT;
   }
   
   // --- Helper ---
   private CompleteEvaluation mapRow(ResultSet rs) throws SQLException {
      int id = rs.getInt("numero");
      Date visitDate = rs.getDate("date_eval");
      String comment = rs.getString("commentaire");
      String username = rs.getString("nom_utilisateur");
      int restaurantId = rs.getInt("fk_rest");
      
      Restaurant restaurant = restaurantMapper.findById(restaurantId);
      
      return new CompleteEvaluation(id, visitDate, restaurant, comment, username);
   }
}
