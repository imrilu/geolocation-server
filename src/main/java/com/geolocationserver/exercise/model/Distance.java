package com.geolocationserver.exercise.model;

import javafx.util.Pair;
import lombok.*;
import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity(name="distances")
public class Distance {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", updatable=false)
    private long id;
    private double distance;
    private String source;
    private String destination;
    private int hits;

    private static Pair<Long,Integer> maxHitsIdentifier = null;

    public Distance(double distance, String source, String destination, int hits) {
        this.distance = distance;
        this.source = source;
        this.destination = destination;
        this.hits = hits;
        if (maxHitsIdentifier == null)
            maxHitsIdentifier = new Pair(id, hits);
    }

    public void incrementHits() {
        hits++;
        if (maxHitsIdentifier.getValue() < hits)
            maxHitsIdentifier = new Pair(id, hits);
    }

    public static long getMaxHitsId() {
        if (maxHitsIdentifier == null)
            return -1;
        return maxHitsIdentifier.getKey();
    }

}