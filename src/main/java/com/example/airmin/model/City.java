package com.example.airmin.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "city", uniqueConstraints = @UniqueConstraint(columnNames = {"name", "country"}))
@Data
@NoArgsConstructor
public class City {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 120)
    private String country;

    @Column(nullable = false)
    private String description;

    public City(final String name, final String country, final String description) {
        this.name = name;
        this.description = description;
        this.country = country;
    }

    @ToString.Exclude
    @OneToMany(mappedBy = "city")
    private List<Airport> airports;

    @ToString.Exclude
    @OneToMany(mappedBy = "city", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private Collection<Comment> comments;

    @Override public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof City)) return false;
        final City city = (City) o;
        return Objects.equals(id, city.id) &&
                name.equalsIgnoreCase(city.getName()) &&
                country.equalsIgnoreCase(city.getCountry());
    }

    @Override public int hashCode() {
        return Objects.hash(id, name.toLowerCase(), country.toLowerCase());
    }
}
