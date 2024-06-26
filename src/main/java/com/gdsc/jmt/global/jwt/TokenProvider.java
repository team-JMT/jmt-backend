package com.gdsc.jmt.global.jwt;

import com.gdsc.jmt.global.jwt.dto.TokenResponse;
import com.gdsc.jmt.domain.user.common.RoleType;
import com.gdsc.jmt.global.jwt.dto.UserInfo;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

@Component
public class TokenProvider {
    private static final Logger logger = LoggerFactory.getLogger(TokenProvider.class);
    private static final String AUTHORITIES_KEY = "auth";
    private static final String BEARER_TYPE = "bearer";
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 30;
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 7;

    private final Key key;

    @Autowired
    public TokenProvider(@Value("${jwt.secret}") String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public TokenResponse generateJwtToken(String email, RoleType role) {
        long now = (new Date()).getTime();

        Map<String, Object> payloads = Map.of(
                "email", email,
                AUTHORITIES_KEY, role);


        Date accessTokenExpiresIn = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);
        String accessToken = Jwts.builder() // payload "email": "email"
                .setClaims(payloads)           // payload "aggregatedId : "userAggregateId" , "auth": "ROLE_USER"
                .setExpiration(accessTokenExpiresIn)        // payload "exp": 1516239022 (예시)
                .signWith(key, SignatureAlgorithm.HS512)    // header "alg": "HS512"
                .compact();

        String refreshToken = Jwts.builder()
                .setExpiration(new Date(now + REFRESH_TOKEN_EXPIRE_TIME))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();

        return new TokenResponse(BEARER_TYPE, accessToken, refreshToken, accessTokenExpiresIn.getTime());
    }

    public TokenResponse generateJwtTokenForTest(String email, RoleType role, long expireTime) {
        long now = (new Date()).getTime();

        Map<String, Object> payloads = Map.of(
                "email", email,
                AUTHORITIES_KEY, role);


        Date accessTokenExpiresIn = new Date(now + expireTime);
        String accessToken = Jwts.builder() // payload "email": "email"
                .setClaims(payloads)           // payload "aggregatedId : "userAggregateId" , "auth": "ROLE_USER"
                .setExpiration(accessTokenExpiresIn)        // payload "exp": 1516239022 (예시)
                .signWith(key, SignatureAlgorithm.HS512)    // header "alg": "HS512"
                .compact();

        String refreshToken = Jwts.builder()
                .setExpiration(new Date(now + expireTime))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();

        return new TokenResponse(BEARER_TYPE, accessToken, refreshToken, accessTokenExpiresIn.getTime());
    }

    public Authentication getAuthentication(String accessToken) {
        Claims claims = parseClaims(accessToken);

        if (claims.get(AUTHORITIES_KEY) == null) {
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        }

        // 클레임에서 권한 정보 가져오기
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .toList();

        // UserDetails 객체를 만들어서 Authentication 리턴
        UserDetails principal = new UserInfo(claims.get("email").toString(), authorities);
        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            //TODO : 나중에 되면 sentry 해보기
            logger.warn("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            logger.warn("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            logger.warn("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            logger.warn("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }

    public Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    public Long getExpiration(String token) {
        Date expiration = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getExpiration();

        // 현재 시간
        Long now = new Date().getTime();
        return (expiration.getTime() - now);
    }
}
