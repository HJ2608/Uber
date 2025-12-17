package com.firstapp.uber.dto.cab;

import jakarta.persistence.*;
import lombok.*;
import model.CabType;

@Entity
@Table(name = "cabs")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor
public class Cab {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Integer id;
    @Column(name = "registration_no")
    String registrationNo;
    @Column(name = "model")
    String model;
    @Column(name = "color")
    String color;
    @Enumerated(EnumType.STRING)
    @Column(name = "cab_type")
    private CabType cabType;
    @Column(name = "is_active")
    Boolean isActive;
}

