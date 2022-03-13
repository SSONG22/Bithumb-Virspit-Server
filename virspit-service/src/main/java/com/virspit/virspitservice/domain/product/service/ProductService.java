package com.virspit.virspitservice.domain.product.service;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.virspit.virspitservice.domain.common.PageSupport;
import com.virspit.virspitservice.domain.product.dto.ProductDto;
import com.virspit.virspitservice.domain.product.dto.ProductKafkaDto;
import com.virspit.virspitservice.domain.product.entity.ProductDoc;

import com.virspit.virspitservice.domain.product.entity.QProductDoc;
import com.virspit.virspitservice.domain.product.repository.ProductDocRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Range;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

import static com.virspit.virspitservice.domain.product.entity.QProductDoc.productDoc;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ProductService {
    private final ProductDocRepository productRepository;

    @Transactional
    public Mono<ProductDto> insert(ProductKafkaDto productDto) {
        Mono<ProductDto> result = productRepository.save(productDto.kafkaToEntity(productDto))
                .map(ProductDto::entityToDto);
        return result;
    }

    public Flux<ProductDto> getAllProducts() {
        return productRepository.findAll()
                .map(ProductDto::entityToDto);
    }

    public Mono<PageSupport> getAllProducts(Pageable pageable, String type, Long sportsId) {

        return productRepository.findAll(productDoc.teamPlayerType.eq(type),
                productDoc.sportsId.eq(sportsId),
                new OrderSpecifier<>(Order.DESC, productDoc.createdDateTime))
                .collectList()
                .map(list -> new PageSupport<>(
                        list
                                .stream()
                                .map(p -> ProductDto.entityToDto((ProductDoc) p))
                                .skip(pageable.getPageNumber() * pageable.getPageSize())
                                .limit(pageable.getPageSize())
                                .collect(Collectors.toList()),
                        pageable.getPageNumber(), pageable.getPageSize(), list.size()));
    }

    public Mono<ProductDto> getProduct(final String id) {
        return productRepository.findById(id)
                .map(ProductDto::entityToDto);
    }

    public Flux<ProductDto> getProductsInPriceRange(final int minPrice, final int maxPrice) {
        return productRepository.findByPriceBetween(Range.closed(minPrice, maxPrice))
                .map(ProductDto::entityToDto);
    }

    public Flux<ProductDto> getProductsBy(String search) {
        return productRepository.findByTitleLikeOrderByCreatedDateDesc(search)
                .map(ProductDto::entityToDto);
    }

    @Transactional
    public Mono<Void> deleteProduct(final String id) {
        return productRepository.deleteById(id);
    }

    public Flux<ProductDto> getFavorites(List<String> ids) {
        return productRepository.findAllById(ids)
                .map(ProductDto::entityToDto);
    }
}
