package database.models;

public class Review {
    public final float rating;
    public final String comment;

    public Review(float rating, String comment) {
        this.rating = rating;
        this.comment = comment;
    }

    public String toString() {
        return "Rating: " + this.rating + "Comment: [" + comment + " ]";
    }
}