package ca.ubc.cs.cpsc210.meetup.model;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ca.ubc.cs.cpsc210.meetup.util.LatLon;

/*
 * A place represents a location at which people can meet
 */
public class Place extends Location {

    // The name of the place
    private String name;
   // The category of the place
    private String category;
    // The price of a place, with 0 being the cheapest
    private int price;

    // Tags describing place
    private Set<String> tags;

    /**
     * Constructor
     * @param placeName tThe name of the place
     */
    public Place(String placeName) {
        this(placeName, new LatLon());

      // ******************  study notes please ignore this

      /*  super(new LatLon());
        this.name = placeName;*/

        // this(placeName, new LatLon()); // does the same thing


         // this constructor constructs a place without a latlon, that's why
        // im passing in a dummy latlon
        // constructors of the same class can be used to re-use code, otherwise you might have the same
        // initalization code in 2 places and might forget to update one of them later
        // CourseTime does special initalization on its constructor parameters, if coursetime had another
        // constructor rather than copying and pasting the logic, we'd wanna reuse it
    }

    /**
     * Constructor
     * @param placeName The name of the place
     * @param latLon The latitude and longitude of the place
     */


    public Place(String placeName, LatLon latLon) {
        super(latLon);
        name = placeName;
    }

    public Place(String placeName, LatLon latLon, String category, int price) {
        super(latLon);
        name = placeName;
        this.category = category;
        this.price = price;
    }




    /**
     * Add a tag describing what can be done in the place
     * @param tag The tag to add, non-null
     */
    public void addTag(String tag) {
        tags.add(tag);
    }

    /**
     * Determine if this place has the specified tag
     * @param tag The tag to look for, non-null
     * @return true if found, false otherwise
     */
    public boolean containsTag(String tag) {
        return tags.contains(tag);
    }


	// ************** Getters ****************

    public String getName() {
        return name;
    }

   public String getCategory() {
       return category;
   }

    public int getPrice() {
        return price;
    }

}