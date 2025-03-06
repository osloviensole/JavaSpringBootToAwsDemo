package com.example.demo.handler;

import com.example.demo.model.User;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

@Component
public class UserHandler implements Supplier<List<User>> {

    @Override
    public List<User> get() {
        return Arrays.asList(
                new User("Jean", "Dupont", 30, "Homme", LocalDate.now()),
                new User("Alice", "Martin", 25, "Femme", LocalDate.now()),
                new User("Mohamed", "Ali", 40, "Homme", LocalDate.now())
        );
    }
}