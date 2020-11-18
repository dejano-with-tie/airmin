package com.example.airmin.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.NaturalId;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue
    private Long id;
    @NaturalId
    @Column(nullable = false)
    private String username;
    @ToString.Exclude
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private String firstName;
    @Column(nullable = false)
    private String lastName;
    @OrderColumn
    @ElementCollection()
    @Enumerated(EnumType.STRING)
    private Set<Role> roles = new HashSet<>();
    @OneToMany(mappedBy = "author")
    @ToString.Exclude
    private Collection<Comment> comments;

    public User(final String username, final String password, final String firstName, final String lastName) {
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @Override public int hashCode() {
        return Objects.hash(id);
    }

    @Override public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        final User user = (User) o;
        return Objects.equals(id, user.id);
    }
}






