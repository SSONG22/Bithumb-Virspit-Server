package com.virspit.virspitorder.controller;

import com.virspit.virspitorder.dto.response.OrdersResponseDto;
import com.virspit.virspitorder.entity.Orders;
import com.virspit.virspitorder.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    protected OrderService ordersService;

    @DisplayName("전체 주문 목록 테스트")
    @Test
    void allListByDate() throws Exception {
        Orders order1 = Orders.builder()
                .id(1l)
                .memberId(1l)
                .productId(1l)
                .orderDate(LocalDateTime.now())
                .build();
        List<Orders> orders = List.of(order1);

        String startDate = "2020-10-12 12:00:00";
        String endDate = "2021-10-12 12:00:00";

        Pageable page = PageRequest.of(0, 2, Sort.by("orderDate").descending());
        given(ordersService.getAll(startDate, endDate, page))
                .willReturn(orders.stream()
                        .map(OrdersResponseDto::entityToDto)
                        .collect(Collectors.toList()));

        mvc.perform(get("/orders")
                .param("startDate", startDate)
                .param("endDate", endDate)
                .param("page", String.valueOf(1))
                .param("size", String.valueOf(2))
        ).andExpect(status().isOk());

        verify(ordersService).getAll(startDate, endDate, page);
    }
}