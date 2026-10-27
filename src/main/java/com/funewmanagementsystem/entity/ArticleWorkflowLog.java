package com.funewmanagementsystem.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;


@Entity
@Table(name = "ArticleWorkflowLog")
@Immutable
@Getter@Setter
@NoArgsConstructor
public class ArticleWorkflowLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LogId")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NewsArticleID", nullable = false)
    private NewsArticle article;

    @Enumerated(EnumType.STRING)
    @Column(name = "FromStatus", length = 30,nullable = false)
    private ArticleStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "ToStatus", length = 30)
    private ArticleStatus toStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ActorID")
    private SystemAccount actor;

    @Column(name = "Note", length = 1000, columnDefinition = "NVARCHAR(1000)")
    private String note;

    @Column(name = "ActionAt",nullable = false)
    private LocalDateTime actionAt;
}
