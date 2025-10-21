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
           "INSERT INTO restaurants (numero, nom, adresse, description, site_web, fk_type, fk_vill) VALUES (?,?,?,?,?,?,?)";
   private static final String updateRestaurant =
           "UPDATE restaurants SET nom=?, adresse=?,description=?, site_web=?, fk_type=?, fk_vill=? WHERE numero=?";
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
      //"INSERT INTO restaurants (numero, nom, adresse, description, site_web, fk_type, fk_vill) VALUES (?,?,?,?,?,?)";
      int id = getSequenceValue(); //récupérationd d'une séquence oracle
      try (PreparedStatement stmt = conn.prepareStatement(insertRestaurant)) {
         stmt.setInt(1, id);
         stmt.setString(2, restaurant.getName());
         stmt.setString(3, restaurant.getAddress().getStreet());
         stmt.setString(4, restaurant.getDescription());
         stmt.setString(5, restaurant.getWebsite());
         stmt.setInt(6, restaurant.getType().getId());
         stmt.setInt(7, restaurant.getAddress().getCity().getId());
         
         int rows = stmt.executeUpdate();
         if (rows > 0) {
            restaurant.setId(id);
            cache.put(id, restaurant);
            return restaurant;
         }
      } catch (SQLException ex) {
         logger.error("Erreur insertRestaurant : {}", ex.getMessage());
      }
      return null;
   }
   
   @Override
   public boolean update(Restaurant restaurant) {
      Connection conn = ConnectionUtils.getConnection();
      //"UPDATE restaurants SET nom=?, adresse=?,description=?, site_web=?, fk_type=?, fk_vill=? WHERE numero=?";
      try (PreparedStatement stmt = conn.prepareStatement(updateRestaurant)) {
         stmt.setString(1, restaurant.getName());
         stmt.setString(2, restaurant.getAddress().getStreet());
         stmt.setString(3, restaurant.getDescription());
         stmt.setString(4, restaurant.getWebsite());
         stmt.setInt(5, restaurant.getType().getId());
         stmt.setInt(6, restaurant.getAddress().getCity().getId());
         
         int rows = stmt.executeUpdate();
         if (rows > 0) {
            cache.put(restaurant.getId(), restaurant);
            return true;
         }
      } catch (SQLException ex) {
         logger.error("Erreur updateRestaurant : {}", ex.getMessage());
      }
      return false;
   }
   
   @Override
   public boolean delete(Restaurant restaurant) {
      return deleteById(restaurant.getId());
   }
   
   @Override
   public boolean deleteById(int id) {
      Connection conn = ConnectionUtils.getConnection();
      
      try (PreparedStatement stmt = conn.prepareStatement(deleteRestaurant)) {
         stmt.setInt(1, id);
         int rows = stmt.executeUpdate();
         if (rows > 0) {
            cache.remove(id);
            return true;
         }
      } catch (SQLException ex) {
         logger.error("Erreur deleteById : {}", ex.getMessage());
      }
      return false;
   }
   
   //requêtes pour abstractMapper
   @Override
   protected String getSequenceQuery() {
      return "SELECT seq_restaurants.nextval FROM dual";
   }
   
   @Override
   protected String getExistsQuery() {
      return "SELECT 1 FROM restaurants WHERE id = ?";
   }
   
   @Override
   protected String getCountQuery() {
      return "SELECT COUNT(*) FROM restaurants";
   }
   
   //Méthodes internes
   private Restaurant mapRowToRestaurant(ResultSet rs) throws SQLException {
      int id = rs.getInt("numero");
      String name = rs.getString("nom");
      String adresse = rs.getString("adresse");
      String description = rs.getString("description");
      String website = rs.getString("site_web");
      int typeId = rs.getInt("fk_type");
      int cityId = rs.getInt("fk_vill");
      
      //todo ajouter les appels à d'autres mappers
      City city = new CityMapper().findById(cityId);
      RestaurantType type = new RestaurantTypeMapper().findById(typeId);
      
      return new Restaurant(id, name, adresse, description, website, city, type);
   }
   
   //Cache
   @Override
   protected boolean isCacheEmpty() {
      return cache.isEmpty();
   }
   
   @Override
   protected void resetCache() {
      cache.clear();
   }
   
   @Override
   protected void addToCache(Restaurant r){
      cache.put(r.getId(), r);
   }
   
   @Override
   protected void removeFromCache(Integer id){
      cache.remove(id);
   }
}
