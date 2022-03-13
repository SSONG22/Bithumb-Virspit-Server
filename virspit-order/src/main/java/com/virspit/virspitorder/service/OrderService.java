package com.virspit.virspitorder.service;

import com.virspit.virspitorder.dto.request.OrderMemoRequestDto;
import com.virspit.virspitorder.dto.response.OrdersResponseDto;
import com.virspit.virspitorder.dto.response.ProductResponseDto;
import com.virspit.virspitorder.entity.Orders;
import com.virspit.virspitorder.feign.MemberServiceFeignClient;
import com.virspit.virspitorder.feign.ProductServiceFeignClient;
import com.virspit.virspitorder.response.error.ErrorCode;
import com.virspit.virspitorder.response.error.exception.BusinessException;
import com.virspit.virspitorder.repository.OrderRepository;
import com.virspit.virspitorder.response.result.SuccessResponse;
import com.virspit.virspitorder.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.groundx.caver_ext_kas.rest_client.io.swagger.client.ApiException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Transactional
@RequiredArgsConstructor
@Service
@Slf4j
public class OrderService {

    private static final String TOPIC_NAME = "order";

    private final NftService nftService;
    private final OrderRepository orderRepository;
    private final MemberServiceFeignClient memberServiceFeignClient;
    private final ProductServiceFeignClient productServiceFeignClient;
    private final KafkaOrderProducer kafkaOrderProducer;

    @Transactional(readOnly = true)
    public List<OrdersResponseDto> getAll(String startDate, String endDate, Pageable pageable) {
        StringUtils.validateInputDate(startDate, endDate);
        if (startDate != null) {
            return findAllByDate(startDate, endDate, pageable);
        }
        return findAll(pageable);
    }

    private List<OrdersResponseDto> findAllByDate(String startDate, String endDate, Pageable pageable) {
        LocalDateTime endDateTime = LocalDateTime.now();

        if (endDate != null) {
            endDateTime = StringUtils.parse(endDate, false);
        }

        return orderRepository.findByOrderDateBetween(
                StringUtils.parse(startDate, true),
                endDateTime,
                pageable)
                .stream()
                .map(doc -> OrdersResponseDto.entityToDto(doc,
                        Optional.ofNullable(productServiceFeignClient.findByProductId(doc.getProductId()))
                                .map(SuccessResponse::getData)
                                .orElse(null),
                        Optional.ofNullable(memberServiceFeignClient.findByMemberId(doc.getMemberId()))
                                .orElse(null)))
                .collect(Collectors.toList());
    }

    private List<OrdersResponseDto> findAll(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .stream()
                .map(doc -> OrdersResponseDto.entityToDto(doc,
                        Optional.ofNullable(productServiceFeignClient.findByProductId(doc.getProductId()))
                                .map(SuccessResponse::getData)
                                .orElse(null),
                        Optional.ofNullable(memberServiceFeignClient.findByMemberId(doc.getMemberId()))
                                .orElse(null)))
                .collect(Collectors.toList());
    }

    // todo queryDSL
    @Transactional(readOnly = true)
    public List<OrdersResponseDto> getAllByMember(Long memberId, String startDate, String endDate, Pageable pageable) {
        StringUtils.validateInputDate(startDate, endDate);

        if (startDate == null && endDate == null) {
            return orderRepository.findByMemberId(memberId, pageable)
                    .stream()
                    .map(doc -> OrdersResponseDto.entityToDto(doc,
                            Optional.ofNullable(productServiceFeignClient.findByProductId(doc.getProductId()))
                                    .map(SuccessResponse::getData)
                                    .orElse(null),
                            Optional.ofNullable(memberServiceFeignClient.findByMemberId(doc.getMemberId()))
                                    .orElse(null)))
                    .collect(Collectors.toList());
        }
        if (startDate == null || endDate == null) {
            throw new BusinessException("startDate, endDate 를 정확히 입력해주세요.", ErrorCode.INVALID_INPUT_VALUE);
        }
        return orderRepository.findByMemberIdAndOrderDateBetween(
                memberId,
                StringUtils.parse(startDate, true),
                StringUtils.parse(endDate, false),
                pageable)
                .stream()
                .map(doc -> OrdersResponseDto.entityToDto(doc,
                        Optional.ofNullable(productServiceFeignClient.findByProductId(doc.getProductId()))
                                .map(SuccessResponse::getData)
                                .orElse(null),
                        Optional.ofNullable(memberServiceFeignClient.findByMemberId(doc.getMemberId()))
                                .orElse(null)))
                .collect(Collectors.toList());
    }

    public OrdersResponseDto order(Long memberId, Long productId) throws ApiException {
        String memberWalletAddress = memberServiceFeignClient.findWalletByMemberId(memberId);
        if (memberWalletAddress.isBlank() || memberWalletAddress == null) {
            throw new BusinessException("member wallet 정보를 가져오지 못했습니다.", ErrorCode.ENTITY_NOT_FOUND);
        }

        ProductResponseDto product = Optional.ofNullable(productServiceFeignClient.findByProductId(productId))
                .map(SuccessResponse::getData)
                .orElse(null);
        if (product == null) {
            throw new BusinessException("product 정보를 가져오지 못했습니다.", ErrorCode.ENTITY_NOT_FOUND);
        }

        if (!nftService.payToAdminFeesByCustomer(product.getPrice(), memberWalletAddress)) {
            throw new BusinessException("클레이 지불 과정에서 오류가 발생했습니다.", ErrorCode.INTERNAL_SERVER_ERROR);
        }

        String tokenId = nftService.issueToken(memberWalletAddress, product.getNftInfo().getMetadataUri(), product.getNftInfo().getContractAlias());
        if (tokenId == null) {
            nftService.rollBackSendKlay(product.getPrice(), memberWalletAddress);
            throw new BusinessException("KAS API - 토큰 발행이 되지 않았습니다.", ErrorCode.INTERNAL_SERVER_ERROR);
        }

        Orders orders = new Orders(memberId, productId, memberWalletAddress, tokenId);
        Orders saved = orderRepository.save(orders);
        OrdersResponseDto dto = OrdersResponseDto.entityToDto(
                saved,
                product,
                Optional.ofNullable(memberServiceFeignClient.findByMemberId(saved.getMemberId())).
                        orElse(null));
        // kafka send
        kafkaOrderProducer.sendOrder(TOPIC_NAME, dto);
        return dto;
    }

    public OrdersResponseDto updateMemo(OrderMemoRequestDto requestDto) {
        Orders orders = orderRepository.findById(requestDto.getOrderId())
                .orElseThrow(() -> new BusinessException("해당 orderId가 없습니다.", ErrorCode.ENTITY_NOT_FOUND));
        orders.updateMemo(requestDto.getMemo());
        return OrdersResponseDto.entityToDto(orders, Optional.ofNullable(productServiceFeignClient.findByProductId(orders.getProductId()))
                        .map(SuccessResponse::getData)
                        .orElse(null),
                Optional.ofNullable(memberServiceFeignClient.findByMemberId(orders.getMemberId()))
                        .orElse(null));
    }
}
