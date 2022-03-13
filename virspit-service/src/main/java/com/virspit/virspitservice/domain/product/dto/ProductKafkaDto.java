package com.virspit.virspitservice.domain.product.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.virspit.virspitservice.domain.product.entity.ProductDoc;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Optional;

@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
public class ProductKafkaDto {
    @ApiModelProperty("상품 ID")
    private String id;

    @ApiModelProperty("상품 제목")
    private String title;

    @ApiModelProperty("상품 설명")
    private String description;

    @ApiModelProperty("종목 정보")
    private SportsInfo sportsInfo;

    @ApiModelProperty("팀/선수 정보")
    private TeamPlayerInfo teamPlayerInfo;

    @ApiModelProperty("상품 가격(Klay)")
    private Integer price;

    @ApiModelProperty("상품 수량")
    private Integer remainedCount;

    @ApiModelProperty(value = "상품 판매 시작 일", example = "2021-09-26 17:00:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startDateTime;

    @ApiModelProperty("상품 진열 여부")
    private Boolean exhibition;

    @ApiModelProperty("NFT 이미지 주소")
    private String nftImageUrl;

    @ApiModelProperty("상품 상세 이미지 주소")
    private String detailImageUrl;

    @ApiModelProperty("상품 NFT 정보")
    private NftInfo nftInfo;

    @ApiModelProperty(value = "상품 업데이트 일", example = "2021-09-26 17:00:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedDateTime;

    @ApiModelProperty(value = "상품 등록 일", example = "2021-09-26 17:00:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdDateTime;

    @ApiModelProperty(value = "product 이벤트", example = "DELETE or UPDATE")
    private Event event;

    public ProductDoc kafkaToEntity(final ProductKafkaDto productDto) {
        return ProductDoc.builder()
                .id(productDto.getId())
                .title(productDto.getTitle())
                .description(productDto.getDescription())
                .price(productDto.getPrice())
                .remainedCount(productDto.getRemainedCount())
                .startDateTime(productDto.getStartDateTime())
                .exhibition(productDto.getExhibition())
                .nftImageUrl(productDto.getNftImageUrl())
                .detailImageUrl(productDto.getDetailImageUrl())
                .metadataUri(Optional.ofNullable(productDto.getNftInfo())
                        .map(NftInfo::getMetadataUri).orElse(null))
                .contractAlias(Optional.ofNullable(productDto.getNftInfo())
                        .map(NftInfo::getContractAlias).orElse(null))
                .teamPlayerId(Optional.ofNullable(productDto.getTeamPlayerInfo())
                        .map(TeamPlayerInfo::getId).orElse(null))
                .teamPlayerName(Optional.ofNullable(productDto.getTeamPlayerInfo())
                        .map(TeamPlayerInfo::getName).orElse(null))
                .teamPlayerType(Optional.ofNullable(productDto.getTeamPlayerInfo())
                        .map(TeamPlayerInfo::getType).orElse(null))
                .sportsId(Optional.ofNullable(productDto.getSportsInfo())
                        .map(SportsInfo::getId).orElse(null))
                .sportsName(Optional.ofNullable(productDto.getSportsInfo())
                        .map(SportsInfo::getName).orElse(null))
                .createdDateTime(productDto.getCreatedDateTime())
                .updatedDateTime(productDto.getUpdatedDateTime())
                .build();
    }
}
