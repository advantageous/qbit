package io.advantageous.qbit.example;

/* Domain object. */
public class User {

    private final String userName;
    //private final int numRecommendations;

    public User(String userName){//, int numRecommendations) {
        this.userName = userName;
        //this.numRecommendations = numRecommendations;
    }

    public String getUserName() {
        return userName;
    }
//
//
//    public int getNumRecommendations() {
//        return numRecommendations;
//    }

}
