package com.example.demo.handler;

import java.util.function.Function;
import com.example.demo.model.User;
import java.util.List;
import java.util.Arrays;
import java.time.LocalDate;

public class UserLambdaHandler implements Function<Object, List<User>> {

    @Override
    public List<User> apply(Object input) {
        // Créer une liste d'utilisateurs
        User user1 = new User("John", "Doe", 25, "Male", LocalDate.now());
        User user2 = new User("Jane", "Doe", 28, "Female", LocalDate.now());

        return Arrays.asList(user1, user2);  // Renvoie la liste d'utilisateurs
    }
}
