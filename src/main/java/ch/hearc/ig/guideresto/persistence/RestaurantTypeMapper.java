package ch.hearc.ig.guideresto.persistence;
import ch.hearc.ig.guideresto.business.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.*;
public class RestaurantTypeMapper extends AbstractMapper<RestaurantType>{
   private static final Logger logger = LogManager.getLogger(RestaurantTypeMapper.class);
   private static final String TABLE_NAME = "types_restaurants";
   
   private final Map<Integer, RestaurantType> cache = new HashMap<>();
   
   // --- Requêtes SQL ---
   private static final String FIND_BY_ID =
           "SELECT numero, libelle, description FROM types_restaurants WHERE numero = ?";
   
   private static final String FIND_ALL =
           "SELECT numero, libelle, description FROM types_restaurants";
   
   private static final String INSERT =
           "INSERT INTO types_restaurants (numero, libelle, description) VALUES (?, ?, ?)";
   
   private static final String UPDATE =
           "UPDATE types_restaurants SET libelle = ?, description = ? WHERE numero = ?";
   
   private static final String DELETE =
           "DELETE FROM types_restaurants WHERE numero = ?";
   
   
   // --- Implémentations AbstractMapper ---
   @Override
   public RestaurantType findById(int id) {
      if (cache.containsKey(id)) return cache.get(id);
      
      Connection conn = ConnectionUtils.getConnection();
      try (PreparedStatement stmt = conn.prepareStatement(FIND_BY_ID)) {
         stmt.setInt(1, id);
         try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
               RestaurantType type = mapRowToRestaurantType(rs);
               cache.put(id, type);
               return type;
            }
         }
      } catch (SQLException ex) {
         logger.error("Erreur findById RestaurantType : {}", ex.getMessage());
      }
      return null;
   }
   
   @Override
   public Set<RestaurantType> findAll() {
      Set<RestaurantType> types = new HashSet<>();
      Connection conn = ConnectionUtils.getConnection();
      
      try (PreparedStatement stmt = conn.prepareStatement(FIND_ALL);
           ResultSet rs = stmt.executeQuery()) {
         
         while (rs.next()) {
            RestaurantType t = mapRowToRestaurantType(rs);
            types.add(t);
            cache.put(t.getId(), t);
         }
      } catch (SQLException ex) {
         logger.error("Erreur findAll RestaurantType : {}", ex.getMessage());
      }
      return types;
   }
   
   @Override
   public RestaurantType create(RestaurantType type) {
      Connection conn = ConnectionUtils.getConnection();
      int id = getSequenceValue();
      
      try (PreparedStatement stmt = conn.prepareStatement(INSERT)) {
         stmt.setInt(1, id);
         stmt.setString(2, type.getLabel());
         stmt.setString(3, type.getDescription());
         
         int rows = stmt.executeUpdate();
         if (rows > 0) {
            type.setId(id);
            cache.put(id, type);
            return type;
         }
      } catch (SQLException ex) {
         logger.error("Erreur create RestaurantType : {}", ex.getMessage());
      }
      return null;
   }
   
   @Override
   public boolean update(RestaurantType type) {
      Connection conn = ConnectionUtils.getConnection();
      
      try (PreparedStatement stmt = conn.prepareStatement(UPDATE)) {
         stmt.setString(1, type.getLabel());
         stmt.setString(2, type.getDescription());
         stmt.setInt(3, type.getId());
         
         int rows = stmt.executeUpdate();
         if (rows > 0) {
            cache.put(type.getId(), type);
            return true;
         }
      } catch (SQLException ex) {
         logger.error("Erreur update RestaurantType : {}", ex.getMessage());
      }
      return false;
   }
   
   @Override
   public boolean delete(RestaurantType type) {
      return deleteById(type.getId());
   }
   
   @Override
   public boolean deleteById(int id) {
      Connection conn = ConnectionUtils.getConnection();
      
      try (PreparedStatement stmt = conn.prepareStatement(DELETE)) {
         stmt.setInt(1, id);
         int rows = stmt.executeUpdate();
         if (rows > 0) {
            cache.remove(id);
            return true;
         }
      } catch (SQLException ex) {
         logger.error("Erreur deleteById RestaurantType : {}", ex.getMessage());
      }
      return false;
   }
   
   // --- Requêtes pour AbstractMapper ---
   @Override
   protected String getSequenceQuery() {
      return "SELECT seq_types_restaurants.nextval FROM dual";
   }
   
   @Override
   protected String getExistsQuery() {
      return "SELECT 1 FROM types_restaurants WHERE numero = ?";
   }
   
   @Override
   protected String getCountQuery() {
      return "SELECT COUNT(*) FROM types_restaurants";
   }
   
   // --- Mapping d'une ligne SQL vers un objet RestaurantType ---
   private RestaurantType mapRowToRestaurantType(ResultSet rs) throws SQLException {
      int id = rs.getInt("numero");
      String label = rs.getString("libelle");
      String description = rs.getString("description");
      
      return new RestaurantType(id, label, description);
   }
   
   // --- Cache ---
   @Override
   protected boolean isCacheEmpty() {
      return cache.isEmpty();
   }
   
   @Override
   protected void resetCache() {
      cache.clear();
   }
   
   @Override
   protected void addToCache(RestaurantType type) {
      cache.put(type.getId(), type);
   }
   
   @Override
   protected void removeFromCache(Integer id) {
      cache.remove(id);
   }
}
