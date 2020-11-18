package com.example.airmin.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
public class Comment {

    @Id
    @GeneratedValue
    private Long id;
    @Column(nullable = false)
    private String content;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime modifiedAt;
    @ManyToOne(optional = false)
    private User author;
    @ManyToOne(optional = false)
    private City city;

    public Comment(final String content, final User author, final City city) {
        this.content = content;
        this.author = author;
        this.city = city;
    }

    @Override public int hashCode() {
        return Objects.hash(id, content, createdAt, modifiedAt, author, city);
    }

    @Override public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Comment)) return false;
        final Comment comment = (Comment) o;
        return Objects.equals(id, comment.id) &&
                Objects.equals(content, comment.content) &&
                Objects.equals(createdAt, comment.createdAt) &&
                Objects.equals(modifiedAt, comment.modifiedAt) &&
                Objects.equals(author, comment.author) &&
                Objects.equals(city, comment.city);
    }
}
