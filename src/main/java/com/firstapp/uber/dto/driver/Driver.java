package com.firstapp.uber.dto.driver;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "drivers")
@Getter
@Setter
@NoArgsConstructor(access = lombok.AccessLevel.PUBLIC)
@AllArgsConstructor
public class Driver {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Integer id;
    @Column(name = "name")
    String name;
    @Column(name = "license_no")
    String licenseNo;
    @Column(name = "cab_id")
    Integer cabId;
    @Column(name = "avg_rating")
    Double avgRating;
    @Column(name = "ratingCount")
    Integer ratingCount;
    @Column(name="is_online")
    Boolean isOnline;
}
