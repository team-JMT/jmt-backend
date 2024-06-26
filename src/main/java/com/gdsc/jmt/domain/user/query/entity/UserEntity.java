package com.gdsc.jmt.domain.user.query.entity;

import com.gdsc.jmt.domain.user.common.RoleType;
import com.gdsc.jmt.domain.user.common.SocialType;
import com.gdsc.jmt.domain.user.common.Status;
import com.gdsc.jmt.global.entity.BaseTimeEntity;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
@Entity @Table(name = "tb_user")
public class UserEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String email;
    private String profileImageUrl;  // 파일 저장 경로
    @Nullable
    @Column(unique = true)
    private String nickname;

    @Enumerated(EnumType.STRING)
    private SocialType socialType;

    @Enumerated(EnumType.STRING)
    private RoleType roleType;
    @Enumerated(EnumType.STRING)
    private Status status;
}
