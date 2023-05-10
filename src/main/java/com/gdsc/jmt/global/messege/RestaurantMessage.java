package com.gdsc.jmt.global.messege;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RestaurantMessage implements ResponseMessage {

    RESTAURANT_LOCATION_FAIL("위치정보가 잘못되었습니다." , HttpStatus.BAD_REQUEST),
    RESTAURANT_LOCATION_CONFLICT("맛집 위치정보가 이미 등록이 되어있습니다." , HttpStatus.CONFLICT),
    RESTAURANT_LOCATION_CREATED("맛집 위치정보가 등록되었습니다." , HttpStatus.CREATED),

    RECOMMEND_RESTAURANT_CONFLICT("맛집이 이미 등록이 되어있습니다." , HttpStatus.CONFLICT),
    RECOMMEND_RESTAURANT_REGISTERABLE("해당 맛집을 등록할 수 있습니다." , HttpStatus.OK),
    RESTAURANT_CREATED("맛집이 등록되었습니다." , HttpStatus.CREATED),
    RESTAURANT_FIND_ALL("맛집 리스트가 조회되었습니다.", HttpStatus.OK),

    RESTAURANT_LOCATION_NOT_FOUND("맛집 위치정보가 등록되지 않았습니다." , HttpStatus.NOT_FOUND),
    RESTAURANT_LOCATION_FIND("맛집 위치 정보를 조회하였습니다." , HttpStatus.OK),
    DETAIL_RESTAURANT_FIND_SUCCESS("맛집 세부 정보를 조회하였습니다.", HttpStatus.OK),
    RECOMMEND_RESTAURANT_NOT_FOUND("맛집 세부 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);

    private final String message;
    private final HttpStatus status;
}
