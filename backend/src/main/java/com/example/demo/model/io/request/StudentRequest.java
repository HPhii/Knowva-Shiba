package com.example.demo.model.io.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class StudentRequest {
    @NotBlank(message = "Name can not be blank")
    String name;

    @NotBlank(message = "Code can not be blank")
    @Pattern(regexp = "SE\\d{6}", message = "Student code is not valid")
    String studentCode;

    @Min(value = 0, message = "Score must be higher than 0")
    @Max(value = 10, message = "Score must be lower than 10")
    float score;
}
