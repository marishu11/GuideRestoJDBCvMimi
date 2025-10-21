package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.City;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;
public class CityMapper extends AbstractMapper<City>{
   private static final Logger logger = LogManager.getLogger();
   
   // --- Requêtes SQL ---
   private static final String FIND_BY_ID_QUERY = "SELECT numero, code_postal, nom_ville FROM VILLES WHERE numero = ?";
   private static final String FIND_ALL_QUERY = "SELECT numero, code_postal, nom_ville FROM VILLES";
   private static final String INSERT_QUERY = "INSERT INTO VILLES (numero, code_postal, nom_ville) VALUES (?, ?, ?)";
   private static final String UPDATE_QUERY = "UPDATE VILLES SET code_postal = ?, nom_ville = ? WHERE numero = ?";
   private static final String DELETE_QUERY = "DELETE FROM VILLES WHERE numero = ?";
   private static final String EXISTS_QUERY = "SELECT 1 FROM VILLES WHERE numero = ?";
   private static final String COUNT_QUERY = "SELECT COUNT(*) FROM VILLES";
   private static final String SEQUENCE_QUERY = "SELECT SEQ_VILLES.NEXTVAL FROM dual";
   
   // --- Méthodes CRUD ---
   
   @Override
   public City findById(int id) {
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
   public Set<City> findAll() {
      Set<City> cities = new HashSet<>();
      Connection connection = ConnectionUtils.getConnection();
      try (PreparedStatement stmt = connection.prepareStatement(FIND_ALL_QUERY);
           ResultSet rs = stmt.executeQuery()) {
         
         while (rs.next()) {
            cities.add(mapRow(rs));
         }
      } catch (SQLException ex) {
         logger.error("Erreur SQL dans findAll: {}", ex.getMessage());
      }
      return cities;
   }
   
   @Override
   public City create(City city) {
      Connection connection = ConnectionUtils.getConnection();
      Integer nextId = getSequenceValue();
      city.setId(nextId);
      
      try (PreparedStatement stmt = connection.prepareStatement(INSERT_QUERY)) {
         stmt.setInt(1, city.getId());
         stmt.setString(2, city.getZipCode());
         stmt.setString(3, city.getCityName());
         int rows = stmt.executeUpdate();
         
         if (rows > 0) {
            return city;
         }
      } catch (SQLException ex) {
         logger.error("Erreur SQL dans create: {}", ex.getMessage());
      }
      return null;
   }
   
   @Override
   public boolean update(City city) {
      Connection connection = ConnectionUtils.getConnection();
      try (PreparedStatement stmt = connection.prepareStatement(UPDATE_QUERY)) {
         stmt.setString(1, city.getZipCode());
         stmt.setString(2, city.getCityName());
         stmt.setInt(3, city.getId());
         return stmt.executeUpdate() > 0;
      } catch (SQLException ex) {
         logger.error("Erreur SQL dans update: {}", ex.getMessage());
      }
      return false;
   }
   
   @Override
   public boolean delete(City city) {
      return deleteById(city.getId());
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
   
   // --- Méthodes requises par AbstractMapper ---
   
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
   
   // --- Mapping ResultSet → City ---
   private City mapRow(ResultSet rs) throws SQLException {
      int id = rs.getInt("numero");
      String zip = rs.getString("code_postal");
      String name = rs.getString("nom_ville");
      return new City(id, zip, name);
   }
}
