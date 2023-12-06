package com.example.circuitbraker.controller;

import com.example.circuitbraker.models.CarModel;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class MyController {

    @PostMapping("/invokeMyMethod")
    public ResponseEntity<String> MyMethod(@Valid @RequestBody(required = true) CarModel payload) throws RuntimeException {
        throw new RuntimeException("This is a test exception");
    }

}
