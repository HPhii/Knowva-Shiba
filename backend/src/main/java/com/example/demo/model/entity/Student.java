package com.example.demo.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY) //Post thì sẽ bị ẩn, Get các thứ thì được
    long id;

    String name;

    @Column(unique = true)
    String studentCode;

    float score;

    @ManyToOne
    @JoinColumn(name = "account_id")
    Account account;

    @JsonIgnore
    boolean isDeleted = false;
}
