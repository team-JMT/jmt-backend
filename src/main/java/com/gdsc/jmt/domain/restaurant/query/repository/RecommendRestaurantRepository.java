package com.gdsc.jmt.domain.restaurant.query.repository;

import com.gdsc.jmt.domain.restaurant.query.entity.RecommendRestaurantEntity;
import com.gdsc.jmt.domain.restaurant.query.entity.RestaurantEntity;
import com.gdsc.jmt.domain.restaurant.query.entity.calculate.RecommendRestaurantWithDistanceDTO;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import org.springframework.lang.Nullable;

public interface RecommendRestaurantRepository extends JpaRepository<RecommendRestaurantEntity, Long>, JpaSpecificationExecutor<RecommendRestaurantEntity> {
    Optional<RecommendRestaurantEntity> findByRestaurant(RestaurantEntity restaurant);

    @Query("select NEW com.gdsc.jmt.domain.restaurant.query.entity.calculate.RecommendRestaurantWithDistanceDTO(reRestaurant, ST_DISTANCE_SPHERE(reRestaurant.restaurant.location, ST_GeomFromText(:userLocation))) " +
            "from RecommendRestaurantEntity reRestaurant " +
            "where reRestaurant.user.id = :userId")
    Page<RecommendRestaurantWithDistanceDTO> findByUserId(Long userId, String userLocation, Pageable pageable);

    @NotNull
    @Override
    @EntityGraph(attributePaths = {"restaurant"})
    Page<RecommendRestaurantEntity> findAll(Specification<RecommendRestaurantEntity> spec, Pageable pageable);
}
