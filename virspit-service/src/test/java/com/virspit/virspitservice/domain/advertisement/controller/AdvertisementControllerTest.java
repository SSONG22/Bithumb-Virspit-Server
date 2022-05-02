package com.virspit.virspitservice.domain.advertisement.controller;

import com.virspit.virspitservice.domain.advertisement.dto.request.AdvertisementRequestDto;
import com.virspit.virspitservice.domain.advertisement.dto.request.AdvertisementUpdateRequestDto;
import com.virspit.virspitservice.domain.advertisement.dto.response.AdvertisementResponseDto;
import com.virspit.virspitservice.domain.advertisement.entity.AdvertisementDoc;
import com.virspit.virspitservice.domain.advertisement.service.AdvertisementService;
import com.virspit.virspitservice.response.error.ErrorCode;
import com.virspit.virspitservice.response.error.GlobalException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@WebFluxTest(AdvertisementController.class)
class AdvertisementControllerTest {

    @MockBean
    AdvertisementService service;

    @Autowired
    private WebTestClient client;

    @DisplayName("광고 등록 테스트")
    @Test
    void insert() {
        // given
        AdvertisementRequestDto requestDto = AdvertisementRequestDto.builder()
                .productId("1")
                .build();
        AdvertisementResponseDto dto = AdvertisementResponseDto.builder()
                .id(UUID.randomUUID().toString())
                .description("description")
                .createdDate(LocalDateTime.now())
                .build();

        // when
        when(service.insert(requestDto)).thenReturn(Mono.just(dto));

        // assert
        client.post()
                .uri(uriBuilder ->
                        uriBuilder
                                .path("/advertisements")
                                .build()
                )
                .bodyValue(requestDto)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();
    }

    @DisplayName("전체 광고 목록 조회")
    @Test
    void getAll() {
        // given
        AdvertisementResponseDto dto = AdvertisementResponseDto.builder()
                .id(UUID.randomUUID().toString())
                .description("description")
                .createdDate(LocalDateTime.now())
                .build();

        Flux<AdvertisementResponseDto> advertisementResponseDtoFlux = Flux.just(dto);
        Pageable pageable = PageRequest.of(0, 3, Sort.by("createdDate").descending());

        // when
        when(service.getAll(pageable)).thenReturn(advertisementResponseDtoFlux);

        // assert
        client.get()
                .uri(uriBuilder ->
                        uriBuilder
                                .path("/advertisements")
                                .queryParam("size", 1)
                                .queryParam("page", 1)
                                .build()
                )
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();
    }

    @DisplayName("광고 id 조회 ")
    @Test
    void get() {
        // given
        AdvertisementResponseDto dto = AdvertisementResponseDto.builder()
                .id(String.valueOf(1))
                .description("description")
                .createdDate(LocalDateTime.now())
                .build();
        // when
        when(service.get(String.valueOf(1))).thenReturn(Mono.just(dto));

        // assert
        client.get()
                .uri(uriBuilder ->
                        uriBuilder
                                .path("/advertisements/1")
                                .build()
                )
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();
    }

    @DisplayName("광고 id 조회 ")
    @Test
    void get_error() {
        // when
        when(service.get(String.valueOf(1))).thenReturn(
                Mono.error(new GlobalException(String.format("id: {%s} 에 해당하는 광고가 없습니다.", 1),
                ErrorCode.ENTITY_NOT_FOUND)));

        // assert
        String result = client.get()
                .uri(uriBuilder ->
                        uriBuilder
                                .path("/advertisements/1")
                                .build()
                )
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .returnResult()
                .toString();
        System.out.println(result);
    }

    @DisplayName("광고 업데이트")
    @Test
    void update() {
        // given
        AdvertisementUpdateRequestDto dto = AdvertisementUpdateRequestDto.builder()
                .description("update")
                .build();
        AdvertisementDoc advertisement = AdvertisementDoc.builder()
                .id("1")
                .description("description")
                .build();
        // when
        when(service.update(dto, String.valueOf(1))).thenReturn(Mono.just(AdvertisementResponseDto.entityToDto(advertisement)));

        // assert
        client.put()
                .uri(uriBuilder ->
                        uriBuilder
                                .path("/advertisements/1")
                                .build()
                )
                .bodyValue(dto)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();
    }

    @DisplayName("광고 삭제")
    @Test
    void delete() {
        // when
        when(service.delete(String.valueOf(1))).thenReturn(Mono.empty());

        // assert
        client.delete()
                .uri(uriBuilder ->
                        uriBuilder
                                .path("/advertisements/1")
                                .build()
                )
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();
    }


    @DisplayName("error handler 테스트 - url 잘못 입력")
    @Test
    void error() {
        client.get()
                .uri(uriBuilder ->
                        uriBuilder
                                .path("/advertisements")
                                .build()
                )
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is4xxClientError();
    }

}