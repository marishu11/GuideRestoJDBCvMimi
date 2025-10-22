package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.BasicEvaluation;
import ch.hearc.ig.guideresto.business.Restaurant;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Mapper pour la classe BasicEvaluation (table LIKES)
 */
public class BasicEvaluationMapper extends AbstractMapper<BasicEvaluation> {
   
   private static final Logger logger = LogManager.getLogger();
   
   // --- Requêtes SQL ---
   private static final String FIND_BY_ID_QUERY =
           "SELECT numero, appreciation, date_eval, adresse_ip, fk_rest FROM LIKES WHERE numero = ?";
   private static final String FIND_ALL_QUERY =
           "SELECT numero, appreciation, date_eval, adresse_ip, fk_rest FROM LIKES";
   private static final String INSERT_QUERY =
           "INSERT INTO LIKES (numero, appreciation, date_eval, adresse_ip, fk_rest) VALUES (?, ?, ?, ?, ?)";
   private static final String UPDATE_QUERY =
           "UPDATE LIKES SET appreciation = ?, date_eval = ?,adresse_ip = ?, fk_rest = ? WHERE numero = ?";
   private static final String DELETE_QUERY =
           "DELETE FROM LIKES WHERE numero = ?";
   private static final String EXISTS_QUERY =
           "SELECT 1 FROM LIKES WHERE numero = ?";
   private static final String COUNT_QUERY =
           "SELECT COUNT(*) FROM LIKES";
   private static final String SEQUENCE_QUERY =
           "SELECT SEQ_EVAL.NEXTVAL FROM dual"; // adapte le nom de la séquence
   
   // --- Dépendances ---
   private final RestaurantMapper restaurantMapper = new RestaurantMapper();
   
   // --- CRUD ---
   
   @Override
   public BasicEvaluation findById(int id) {
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
   public Set<BasicEvaluation> findAll() {
      Set<BasicEvaluation> evaluations = new HashSet<>();
      Connection connection = ConnectionUtils.getConnection();
      
      try (PreparedStatement stmt = connection.prepareStatement(FIND_ALL_QUERY);
           ResultSet rs = stmt.executeQuery()) {
         
         while (rs.next()) {
            evaluations.add(mapRow(rs));
         }
         
      } catch (SQLException ex) {
         logger.error("Erreur SQL dans findAll: {}", ex.getMessage());
      }
      
      return evaluations;
   }
   
   @Override
   public BasicEvaluation create(BasicEvaluation eval) {
      Connection connection = ConnectionUtils.getConnection();
      Integer nextId = getSequenceValue();
      eval.setId(nextId);
      
      try (PreparedStatement stmt = connection.prepareStatement(INSERT_QUERY)) {
         stmt.setInt(1, eval.getId());
         stmt.setDate(2, new java.sql.Date(eval.getVisitDate().getTime()));
         stmt.setInt(3, eval.getRestaurant().getId());
         stmt.setInt(4, eval.getLikeRestaurant() != null && eval.getLikeRestaurant() ? 1 : 0);
         stmt.setString(5, eval.getIpAddress());
         
         int rows = stmt.executeUpdate();
         if (rows > 0) {
            return eval;
         }
         
      } catch (SQLException ex) {
         logger.error("Erreur SQL dans create: {}", ex.getMessage());
      }
      
      return null;
   }
   
   @Override
   public boolean update(BasicEvaluation eval) {
      Connection connection = ConnectionUtils.getConnection();
      
      try (PreparedStatement stmt = connection.prepareStatement(UPDATE_QUERY)) {
         stmt.setDate(1, new java.sql.Date(eval.getVisitDate().getTime()));
         stmt.setInt(2, eval.getRestaurant().getId());
         stmt.setInt(3, eval.getLikeRestaurant() != null && eval.getLikeRestaurant() ? 1 : 0);
         stmt.setString(4, eval.getIpAddress());
         stmt.setInt(5, eval.getId());
         
         return stmt.executeUpdate() > 0;
      } catch (SQLException ex) {
         logger.error("Erreur SQL dans update: {}", ex.getMessage());
      }
      
      return false;
   }
