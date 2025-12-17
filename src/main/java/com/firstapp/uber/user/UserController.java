package com.firstapp.uber.user;


import model.User;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/CRUD")
public class UserController {
    private final UserRepo repo;

    public UserController(UserRepo repo) {
        this.repo = repo;
    }

    @GetMapping("")
    public List<User> findAll() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public User findById(@PathVariable long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@RequestBody User user) {
        repo.create(user);
    }

    @PutMapping("/id/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@RequestBody User user){
        repo.update(user);
    }

    @DeleteMapping("/id/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id){
        repo.delete(id);
    }
}

