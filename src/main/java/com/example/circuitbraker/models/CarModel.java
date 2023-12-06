package com.example.circuitbraker.models;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@Data
public class CarModel {

    @NotBlank(message = "model is mandatory")
    private String model;
    @NotBlank(message = "make is mandatory")
    private String make;
    @NotBlank(message = "year is mandatory")
    private String year;

    public CarModel(String model, String make, String year) {
        this.model = model;
        this.make = make;
        this.year = year;
    }

}
