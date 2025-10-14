package ch.hearc.ig.guideresto.persistence;
import ch.hearc.ig.guideresto.business.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.sql.*;
import java.util.*;
public class RestaurantMapper extends AbstractMapper<Restaurant> {
   private static final Logger logger = LogManager.getLogger(RestaurantMapper.class);
   private static final String TABLE_NAME = "restaurants";
   
   private Map<Integer, Restaurant> cache = new HashMap<>();
   
   //requêtes SQL
   private static final String findByID =
           "SELECT r.numero, r.nom, r.adresse, r.description, r.site_web, r.fk_type, r.fk_vill " +
                   "FROM restaurants r WHERE r.numero = ?";
   private static final String findAll =
           "SELECT numero, nom, adresse, description, site_web, fk_type, fk_vill FROM restaurants";
   private static final String insertRestaurant =
           "INSERT INTO restaurants (nom, adresse, description, site_web, fk_type, fk_vill) VALUES (?,?,?,?,?,?)";
   private static final String updateRestaurant =
           "UPDATE restaurants SET nom=?, description=?, site_web=?, fk_type=?, fk_vill=? WHERE numero=?";
   private static final String deleteRestaurant =
           "DELETE FROM restaurants WHERE numero=?";
   
   //Implémentations AbstractMapper
   @Override
   public Restaurant findById(int id) {
      if (cache.containsKey(id)) return cache.get(id);
      Connection conn = ConnectionUtils.getConnection();
      try (PreparedStatement stmt = conn.prepareStatement(findByID)) {
         stmt.setInt(1, id);
         try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
               Restaurant restaurant = mapRowToRestaurant(rs);
               cache.put(id, restaurant);
               return restaurant;
            }
         }
      } catch (SQLException ex) {
         logger.error("Erreur findById : {}", ex.getMessage());
      }
      return null;
   }
   
   @Override
   public Set<Restaurant> findAll() {
      Set<Restaurant> restaurants = new HashSet<>();
      Connection conn = ConnectionUtils.getConnection();
      try (PreparedStatement stmt = conn.prepareStatement(findAll);
           ResultSet rs = stmt.executeQuery()) {
         while (rs.next()) {
            Restaurant r = mapRowToRestaurant(rs);
            restaurants.add(r);
            cache.put(r.getId(), r);
         }
      } catch (SQLException ex) {
         logger.error("Erreur findAll : {}", ex.getMessage());
      }
      return restaurants;
   }
   
   @Override
   public Restaurant create(Restaurant restaurant) {
      Connection conn = ConnectionUtils.getConnection();
      int id = getSequenceValue(); //récupérationd d'une séquence oracle
      try (PreparedStatement stmt = conn.prepareStatement(insertRestaurant)) {
         stmt.setInt(1, id);
         stmt.setString(2, restaurant.getName());
         stmt.setString(3,restaurant.getAddress());
         stmt.setString(4, restaurant.getDescription());
         stmt.setString(5, restaurant.getWebsite());
         stmt.setInt(6, restaurant.getType());
         stmt.setInt(7, restaurant.); //comment gérer les adresses ?
      }
   }
   
}
