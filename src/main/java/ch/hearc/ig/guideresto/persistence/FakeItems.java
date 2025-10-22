package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.*;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author cedric.baudet
 */
public class FakeItems {

    private static Set<RestaurantType> types;
    private static Set<Restaurant> restaurants;
    private static Set<EvaluationCriteria> criterias;
    private static Set<City> cities;

    public static Set<Restaurant> getAllRestaurants() {
       try {
          return new LinkedHashSet<>(new RestaurantMapper().findAll());
       } catch (SQLException e) {
          e.printStackTrace();
          return new LinkedHashSet<>();
       }
    }

    public static Set<EvaluationCriteria> getEvaluationCriterias() {
       try {
          return new LinkedHashSet<>(new EvaluationCriteriaMapper().findAll());
       } catch (SQLException e) {
          e.printStackTrace();
          return new LinkedHashSet<>();
       }
    }

    public static Set<RestaurantType> getRestaurantTypes() {
       try {
          return new LinkedHashSet<>(new RestaurantTypeMapper().findAll());
       } catch (SQLException e) {
          e.printStackTrace();
          return new LinkedHashSet<>();
       }
    }

    public static Set<City> getCities() {
       try {
          return new LinkedHashSet<>(new CityMapper().findAll());
       } catch (SQLException e) {
          e.printStackTrace();
          return new LinkedHashSet<>();
       }
    }

}
