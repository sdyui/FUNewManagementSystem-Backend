package com.funewmanagementsystem.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "NewsArticle")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NewsArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NewsArticleID", nullable = false)
    private Long id;

    @Column(name = "NewsTitle", length = 200, nullable = false)
    private String newsTitle;

    @Column(name = "Headline", length = 500)
    private String headline;

    @Column(name = "CreatedDate", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "NewsContent", columnDefinition = "NVARCHAR(MAX)")
    private String newsContent;

    @Column(name = "NewsSource", length = 255)
    private String newsSource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CategoryID", nullable = false)
    private Category category;

    @Column(name = "NewsStatus", nullable = false)
    private Boolean newsStatus = true;// 1 = active, 0 = inactive

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CreatedByID", nullable = false)
    private SystemAccount createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UpdatedByID")
    private SystemAccount updatedBy;

    @Column(name = "ModifiedDate")
    private LocalDateTime modifiedDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "ArticleStatus", length = 30)
    private  ArticleStatus articleStatus;

    @Column(name = "PublishDate")
    private LocalDateTime publishDate;

    @Version
    @Column
    private Long version;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "NewsTag",
        joinColumns = @JoinColumn(name = "NewsArticleID"),
        inverseJoinColumns = @JoinColumn(name = "TagID")
    )
    private Set<Tag> tags = new HashSet<>();
}
