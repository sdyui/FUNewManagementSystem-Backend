package com.funewmanagementsystem.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "SystemAccount")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SystemAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AccountID")
    private Long id;

    @Column(name = "AccountName", length = 100, nullable = false)
    private String accountName;

    @Column(name = "AccountEmail", length = 150, nullable = false, unique = true)
    private String accountEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "AccountRole", nullable = false)
    private AccountRole accountRole;

    @Column(name = "AccountPassword", length = 255, nullable = false)
    private String accountPassword;

    @OneToMany(mappedBy = "createdBy", fetch = FetchType.LAZY)
    private List<NewsArticle> createdNewsArticles = new ArrayList<>();

    @OneToMany(mappedBy = "updatedBy", fetch = FetchType.LAZY)
    private List<NewsArticle> updatedNewsArticles = new ArrayList<>();
}
