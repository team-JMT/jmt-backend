package com.gdsc.jmt.domain.user.query.controller;

import com.gdsc.jmt.domain.user.query.controller.springdocs.CheckDuplicateNicknameSpringDocs;
import com.gdsc.jmt.domain.user.query.service.UserQueryService;
import com.gdsc.jmt.global.controller.FirstVersionRestController;
import com.gdsc.jmt.global.dto.JMTApiResponse;
import com.gdsc.jmt.global.messege.UserMessage;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "사용자 정보 조회 관련 컨트롤러")
@FirstVersionRestController
@RequiredArgsConstructor
public class UserQueryController {
    private final UserQueryService userQueryService;

    @GetMapping("/user/{nickname}")
    @CheckDuplicateNicknameSpringDocs
    public JMTApiResponse<?> checkDuplicateUserNickname(@PathVariable("nickname") String nickname) {
        boolean isDuplicated = userQueryService.checkDuplicateUserNickname(nickname);
        if (isDuplicated) {
            return JMTApiResponse.createResponseWithMessage(null, UserMessage.NICKNAME_IS_DUPLICATED);
        }
        return JMTApiResponse.createResponseWithMessage(nickname, UserMessage.NICKNAME_IS_AVAILABLE);
    }
}