package com.gdsc.jmt.domain.restaurant.command.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class CreateRecommendRestaurantRequestFromClient {
    // 기본 식당 정보
    @Schema(description = "식당 이름", example = "마라탕")
    private String name;

    // 사용자가 입력한 맛집 정보
    @Schema(description = "식당 소개글", example = "마제소바")
    private String introduce;
    @Schema(description = "식당 카테고리 ID", example = "1")
    private Long categoryId;

    @Schema(description = "사진 데이터", nullable = true)
    @Nullable
    private List<MultipartFile> pictures;

    @Schema(description = "주류 유무", example = "true")
    private Boolean canDrinkLiquor;

    @Schema(description = "어울리는 술", example = "위스키")
    private String goWellWithLiquor;

    @Schema(description = "추천 메뉴", example = "#마제소바#라멘")
    private String recommendMenu;

    @Schema(description = "맛집 위치정보 ID", example = "1")
    private Long restaurantLocationId;

    @Schema(description = "그룹 ID", example = "1")
    private Long groupId;
}
