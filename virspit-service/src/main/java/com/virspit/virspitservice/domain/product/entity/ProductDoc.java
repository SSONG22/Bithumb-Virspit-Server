package com.virspit.virspitservice.domain.product.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@ToString
@Builder
@AllArgsConstructor
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(collection = "products")
public class ProductDoc {
    @Id
    private String id;

    private String title;

    private String description;

    private Long teamPlayerId;

    private String teamPlayerName;

    private String teamPlayerType;

    private Long sportsId;

    private String sportsName;

    private Integer price;

    private Integer remainedCount;

    private LocalDateTime startDateTime;

    private Boolean exhibition;

    private String nftImageUrl;

    private String detailImageUrl;

    private String contractAlias;

    private String metadataUri;

    private LocalDateTime createdDateTime;

    private LocalDateTime updatedDateTime;


    public void setId(final String id) {
        this.id = id;
    }

}
