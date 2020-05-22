package database.models;
import java.util.List;

public class InstructorRow {
    private String name;
    private Float rating;
    private Integer rmp_id;
    private List<String> reviews;

    public InstructorRow(String name, Float rating, Integer rmp_id, List<String> reviews) {
        this.name = name;
        this.rating = rating;
        this.rmp_id = rmp_id;
        this.reviews = reviews;
    }

    public String toString() {
        return this.name + " ,rating: " + this.rating + " reviews: [" +
        this.reviews.toString() + "]";
    }
}
