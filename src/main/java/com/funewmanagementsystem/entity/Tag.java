package com.funewmanagementsystem.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Tag")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TagID")
    private Long id;

    @Column(name = "TagName", length = 100, nullable = false, unique = true)
    private String tagName;

    @Column(name = "Note", length = 500)
    private String note;

    @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    private Set<NewsArticle> newsArticles = new HashSet<>();
}
