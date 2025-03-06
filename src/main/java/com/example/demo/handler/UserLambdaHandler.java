package com.example.demo.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.example.demo.model.User;
import java.time.LocalDate;
import java.util.List;
import java.util.Arrays;

public class UserLambdaHandler implements RequestHandler<Object, List<User>> {

    // La méthode handleRequest avec les deux paramètres requis par AWS Lambda
    @Override
    public List<User> handleRequest(Object input, Context context) {
        // Créer une liste d'utilisateurs
        User user1 = new User("John", "Doe", 25, "Male", LocalDate.now());
        User user2 = new User("Jane", "Doe", 28, "Female", LocalDate.now());

        return Arrays.asList(user1, user2);  // Renvoie la liste d'utilisateurs
    }
}