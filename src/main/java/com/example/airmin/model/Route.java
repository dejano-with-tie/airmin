package com.example.airmin.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.BatchSize;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.Objects;

@Getter
@Setter
@ToString
@Entity
@NoArgsConstructor
@BatchSize(size = 100)
public class Route {

    @Id
    @GeneratedValue
    private Long id;
    @Column(nullable = false)
    private Double price;
    @Column(length = 3)
    private String airlineCode;
    private Long airlineId;
    private Boolean codeShare;
    private Integer numberOfStops;
    /**
     * List of 3-letter codes separated by space, describing plane type (equipment)
     */
    private String equipment;
    @ManyToOne(optional = false)
    private Airport source;
    @ManyToOne(optional = false)
    private Airport destination;

    public Route(final Double price, final Airport source, final Airport destination) {
        this.price = price;
        this.source = source;
        this.destination = destination;
    }

    @Override public int hashCode() {
        return Objects.hash(id, price, source, destination);
    }

    @Override public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Route)) return false;
        final Route route = (Route) o;
        return Objects.equals(id, route.id) &&
                Objects.equals(price, route.price) &&
                Objects.equals(source, route.source) &&
                Objects.equals(destination, route.destination);
    }
}
