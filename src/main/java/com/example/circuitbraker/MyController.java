package com.example.circuitbraker;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MyController {

    @CircuitBreaker
    @GetMapping("/invokeMyMethod")
    public String MyMethod() {
        return "myMethod invoked";
    }

}
