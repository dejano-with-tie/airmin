package com.example.airmin.model;

import com.vladmihalcea.hibernate.type.basic.ZoneIdType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Objects;

@Getter
@Setter
@ToString
@Entity
@TypeDef(typeClass = ZoneIdType.class, defaultForType = ZoneId.class)
@NoArgsConstructor
public class Airport {
    /**
     * Used by graph search algorithm. It is nasty, I know
     */
    @Transient
    private double distance = Double.POSITIVE_INFINITY;
    @Id
    @GeneratedValue
    private Long id;
    @Column(nullable = false, unique = true)
    private Long externalId;
    private String name;
    /**
     * Location identifier defined by International Air Transport Association (IATA)
     */
    @Column(length = 3)
    private String iataCode;
    /**
     * Location indicator
     */
    @Column(length = 4)
    private String icaoCode;
    @Column(precision = 6, scale = 4)
    private Double latitude;
    @Column(precision = 6, scale = 4)
    private Double longitude;
    private Integer altitude;
    @Column(length = 10)
    @Convert(converter = ZoneOffsetConverter.class)
    private ZoneOffset zoneOffset;
    @Column(length = 60)
    private ZoneId zoneId;
    @Column(length = 120)
    private String type;
    /**
     * From where this was imported
     */
    private String dataSource;
    @ToString.Exclude
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private City city;
    @OneToMany(mappedBy = "source")
    @ToString.Exclude
    private Collection<Route> departures;
    @ToString.Exclude
    @OneToMany(mappedBy = "destination")
    private Collection<Route> arrivals;

    public Airport(final Long externalId, final String name, final City city) {
        this.externalId = externalId;
        this.name = name;
        this.city = city;
    }

    @Override public int hashCode() {
        return Objects.hash(id, externalId);
    }

    @Override public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Airport)) return false;
        final Airport airport = (Airport) o;
        return Objects.equals(id, airport.id) &&
                Objects.equals(externalId, airport.externalId);
    }
}
