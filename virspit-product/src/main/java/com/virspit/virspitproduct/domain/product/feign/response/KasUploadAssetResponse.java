package com.virspit.virspitproduct.domain.product.feign.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class KasUploadAssetResponse {
    private String contentType;
    private String filename;
    private String uri;
}