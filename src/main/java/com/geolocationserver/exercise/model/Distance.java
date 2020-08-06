package com.geolocationserver.exercise.model;

import lombok.*;
import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity(name="distances")
public class Distance {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable=false)
    private long id;
    private double distance;
    private String source;
    private String destination;

    public Distance(double distance, String source, String destination) {
        this.distance = distance;
        this.source = source;
        this.destination = destination;
    }
}