package com.gdsc.jmt.global.config;

import com.gdsc.jmt.global.http.AppleRestServerAPI;
import com.gdsc.jmt.global.http.KakaoRestServerAPI;
import com.gdsc.jmt.global.http.NaverRestServerAPI;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

@Configuration
public class RetrofitConfig {
    private static final String APPLE_URL = "https://appleid.apple.com/";

    private static final String KAKAO_URL = "https://dapi.kakao.com/v2/";

    private static final String NAVER_URL = "https://naveropenapi.apigw.ntruss.com/";

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder().connectTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    @Bean(name = "appleRetrofit")
    public Retrofit appleRetrofit(OkHttpClient client) {
        return new Retrofit.Builder().baseUrl(APPLE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
    }

    @Bean(name = "kakaoRetrofit")
    public Retrofit kakaoRetrofit(OkHttpClient client) {
        return new Retrofit.Builder().baseUrl(KAKAO_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
    }

    @Bean(name = "naverRetrofit")
    public Retrofit naverRetrofit(OkHttpClient client) {
        return new Retrofit.Builder().baseUrl(NAVER_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
    }

    @Bean
    public AppleRestServerAPI appleRestServerAPI(@Qualifier("appleRetrofit") Retrofit retrofit) {
        return retrofit.create(AppleRestServerAPI.class);
    }

    @Bean
    public KakaoRestServerAPI kakaoRestServerAPI(@Qualifier("kakaoRetrofit") Retrofit retrofit) {
        return retrofit.create(KakaoRestServerAPI.class);
    }

    @Bean
    public NaverRestServerAPI naverRestServerAPI(@Qualifier("naverRetrofit") Retrofit retrofit) {
        return retrofit.create(NaverRestServerAPI.class);
    }
}
