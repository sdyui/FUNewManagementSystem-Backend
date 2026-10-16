package com.funewmanagementsystem.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Category")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CategoryID")
    private Long id;

    @Column(name = "CategoryName", nullable = false)
    private String categoryName;

    @Column(name = "CategoryDescription", length = 1000)
    private String categoryDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ParentCategoryID")
    private Category parentCategory;

    @Column(name = "IsActive")
    private Boolean isActive; //1 = active, 0 = inactive
}