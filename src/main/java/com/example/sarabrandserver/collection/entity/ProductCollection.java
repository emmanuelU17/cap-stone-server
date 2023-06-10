package com.example.sarabrandserver.collection.entity;


import com.example.sarabrandserver.product.entity.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static jakarta.persistence.FetchType.EAGER;

/**
 * Class replicates the collection or season product replicates.
 * Look for better understanding <a href="https://www.samawoman.com/">...</a>
 * */
@Table(name = "product_collection")
@Entity
@NoArgsConstructor
@Getter
@Setter
public class ProductCollection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_collection_id", nullable = false, unique = true)
    private Long productCollectionId;

    @Column(name = "collection", nullable = false, unique = true, length = 32)
    private String collection;

    @Column(name = "created_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createAt;

    @Column(name = "modified_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modifiedAt;

    @Column(name = "deleted_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date deletedAt;

    @OneToMany(cascade = CascadeType.ALL, fetch = EAGER, mappedBy = "productCollection", orphanRemoval = true)
    private Set<Product> products = new HashSet<>();

}
