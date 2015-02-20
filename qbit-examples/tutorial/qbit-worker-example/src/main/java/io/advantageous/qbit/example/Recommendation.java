package io.advantageous.qbit.example;


/* Domain object. */
public class Recommendation {

    private final String recommendation;

    public Recommendation(String recommendation) {
        this.recommendation = recommendation;
    }


    @Override
    public String toString() {
        return "Recommendation{" +
                "recommendation='" + recommendation + '\'' +
                '}';
    }
}
