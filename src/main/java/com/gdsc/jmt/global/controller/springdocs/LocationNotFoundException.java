package com.gdsc.jmt.global.controller.springdocs;

import com.gdsc.jmt.global.messege.LocationMessage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class LocationNotFoundException {
    @Schema(description = "", nullable = true)
    String data = null;

    @Schema(description = "", example = "위치 정보를 찾을 수 없습니다.")
    String message = LocationMessage.LOCATION_NOT_FOUND.getMessage();

    @Schema(description = "", example = "LOCATION_NOT_FOUND")
    String code = LocationMessage.LOCATION_NOT_FOUND.toString();
}
