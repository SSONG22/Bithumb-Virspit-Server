package com.virspit.virspitservice.domain.product.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.virspit.virspitservice.domain.product.entity.ProductDoc;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Range;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.querydsl.ReactiveQuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ProductDocRepository extends ReactiveMongoRepository<ProductDoc, String>,
        ReactiveQuerydslPredicateExecutor<ProductDoc> {

    Mono<ProductDoc> findByTitle(String title);

    @Query("{ id: { $exists: true }}")
    Flux<ProductDoc> findAll(Pageable page);

    @Query("{title:{$regex: ?0}}")
    Flux<ProductDoc> findByTitleLikeOrderByCreatedDateDesc(String title);

    @Query("{title:{$regex: ?0}}, id: { $exists: true }}")
    Flux<ProductDoc> findByTitleLikePagingBy(String name, Pageable page);

    Flux<ProductDoc> findByPriceBetween(Range<Integer> priceRange);

    Flux<ProductDoc> findBySportsIdAndTeamPlayerTypeOrderByCreatedDateTimeDesc(Long sportsId, String teamPlayerType);

    Flux<ProductDoc> findAll(Predicate sportsType, Predicate sportsId, OrderSpecifier orderSpecifier);
}
