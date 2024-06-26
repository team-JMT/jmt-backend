package com.gdsc.jmt.domain.restaurant.command.service;

import com.gdsc.jmt.domain.category.query.entity.CategoryEntity;
import com.gdsc.jmt.domain.category.query.repository.CategoryRepository;
import com.gdsc.jmt.domain.group.entity.GroupEntity;
import com.gdsc.jmt.domain.group.repository.GroupRepository;
import com.gdsc.jmt.domain.restaurant.command.dto.request.CreateRecommendRestaurantRequest;
import com.gdsc.jmt.domain.restaurant.command.dto.request.CreateRecommendRestaurantRequestFromClient;
import com.gdsc.jmt.domain.restaurant.command.dto.request.ReportRecommendRestaurantRequest;
import com.gdsc.jmt.domain.restaurant.command.dto.request.UpdateRecommendRestaurantRequest;
import com.gdsc.jmt.domain.restaurant.command.dto.response.CreatedRestaurantResponse;
import com.gdsc.jmt.domain.restaurant.query.entity.*;
import com.gdsc.jmt.domain.restaurant.query.repository.*;
import com.gdsc.jmt.domain.restaurant.util.KakaoSearchDocument;
import com.gdsc.jmt.domain.restaurant.util.KakaoSearchDocumentRequest;
import com.gdsc.jmt.domain.user.query.entity.UserEntity;
import com.gdsc.jmt.domain.user.query.repository.UserRepository;
import com.gdsc.jmt.global.exception.ApiException;
import com.gdsc.jmt.global.jwt.dto.UserInfo;
import com.gdsc.jmt.global.messege.CategoryMessage;
import com.gdsc.jmt.global.messege.DefaultMessage;
import com.gdsc.jmt.global.messege.RestaurantMessage;
import com.gdsc.jmt.global.messege.UserMessage;
import com.gdsc.jmt.global.service.S3FileService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RestaurantService {
    private final RestaurantRepository restaurantRepository;
    private final RecommendRestaurantRepository recommendRestaurantRepository;
    private final RestaurantPhotoRepository restaurantPhotoRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;
    private final ReportReasonRepository reportReasonRepository;
    private final GroupRepository groupRepository;

    private final S3FileService s3FileService;

    @Transactional
    public CreatedRestaurantResponse createRecommendRestaurant(CreateRecommendRestaurantRequestFromClient request, String email) {
        CreateRecommendRestaurantRequest createRecommendRestaurantRequest = new CreateRecommendRestaurantRequest(request);
        RecommendRestaurantEntity recommendRestaurant = validateCreation(email, createRecommendRestaurantRequest);

        Long id = recommendRestaurantRepository.save(recommendRestaurant).getId();
        if(request.getPictures() != null) {
            uploadImages(recommendRestaurant, request.getPictures());
        }
        return new CreatedRestaurantResponse(request.getRestaurantLocationId(), id);
    }

    @Transactional
    public Long createRestaurantLocation(KakaoSearchDocumentRequest kakaoSearchDocumentRequest) {
        Optional<RestaurantEntity> checkExisting = restaurantRepository.findByKakaoSubId(kakaoSearchDocumentRequest.getId());
        if(checkExisting.isPresent()) {
            throw new ApiException(RestaurantMessage.RESTAURANT_LOCATION_CONFLICT);
        }

        RestaurantEntity restaurant = restaurantRepository.save(kakaoSearchDocumentRequest.createRestaurantEntity());
        return restaurant.getId();
    }

    @Transactional
    public void updateRecommendRestaurant(UpdateRecommendRestaurantRequest request, String email) {
        // 내용 변경
        RecommendRestaurantEntity recommendRestaurant = validateIsWriteRecommendRestaurantByUser(request.getId(), email);
        Optional<CategoryEntity> findCategoryResult = categoryRepository.findById(request.getCategoryId());
        if(findCategoryResult.isEmpty()) {
            throw new ApiException(CategoryMessage.CATEGORY_FIND_FAIL);
        }
        recommendRestaurant.update(request, findCategoryResult.get());
        recommendRestaurantRepository.save(recommendRestaurant);
    }

    @Transactional
    public void removeRecommendRestaurant(final Long id, final String email) {
        RecommendRestaurantEntity recommendRestaurant = validateIsWriteRecommendRestaurantByUser(id, email);
        recommendRestaurantRepository.delete(recommendRestaurant);
    }

    @Transactional
    public void reportRecommendRestaurant(final Long recommendRestaurantId, final UserInfo userInfo, final ReportRecommendRestaurantRequest request) {
        Optional<RecommendRestaurantEntity> findResult = recommendRestaurantRepository.findById(recommendRestaurantId);
        Optional<UserEntity> findUserResult = userRepository.findByEmail(userInfo.getEmail());
        Optional<ReportReasonEntity> findReportReasonResult = reportReasonRepository.findById(request.getReportReasonId());

        if(findResult.isEmpty()) {
            throw new ApiException(RestaurantMessage.RECOMMEND_RESTAURANT_NOT_FOUND);
        }

        if(findResult.isPresent() && findUserResult.isPresent() && findReportReasonResult.isPresent()) {
            ReportEntity reportEntity = ReportEntity.builder()
                    .reportRestaurant(findResult.get())
                    .reportUser(findResult.get().getUser())
                    .reporterUser(findUserResult.get())
                    .reportReason(findReportReasonResult.get())
                    .reportReasonText(request.getReportReason())
                    .build();

            reportRepository.save(reportEntity);
        }
    }

    private void uploadImages(RecommendRestaurantEntity recommendRestaurant, List<MultipartFile> images) {
        List<RestaurantPhotoEntity> photoEntities = new ArrayList<>();
        for(MultipartFile image : images) {
            try {
                String imageUrl = s3FileService.upload(image,"restaurantPhoto");
                RestaurantPhotoEntity photoEntity = RestaurantPhotoEntity.builder()
                        .imageUrl(imageUrl)
                        .imageSize(image.getSize())
                        .build();
                restaurantPhotoRepository.save(photoEntity);
                photoEntities.add(photoEntity);
            }
            catch (IOException e) {
                throw new ApiException(RestaurantMessage.RESTAURANT_IMAGE_UPLOAD_FAIL);
            }
        }
        recommendRestaurant.initPictures(photoEntities);
        recommendRestaurantRepository.save(recommendRestaurant);
    }

    private RecommendRestaurantEntity validateCreation(final String email, CreateRecommendRestaurantRequest createRecommendRestaurantRequest) {
        RestaurantEntity restaurant = validateRestaurant(createRecommendRestaurantRequest.getRestaurantLocationId());
        CategoryEntity category = validateCategory(createRecommendRestaurantRequest.getCategoryId());
        GroupEntity group = validateGroup(createRecommendRestaurantRequest.getGroupId());
        UserEntity user = validateUser(email);
        validateConflict(restaurant);

        return RecommendRestaurantEntity.builder()
                .user(user)
                .restaurant(restaurant)
                .category(category)
                .introduce(createRecommendRestaurantRequest.getIntroduce())
                .canDrinkLiquor(createRecommendRestaurantRequest.getCanDrinkLiquor())
                .goWellWithLiquor(createRecommendRestaurantRequest.getGoWellWithLiquor())
                .recommendMenu(createRecommendRestaurantRequest.getRecommendMenu())
                .group(group)
                .build();
    }

    private GroupEntity validateGroup(final Long groupId) {
        Optional<GroupEntity> group = groupRepository.findById(groupId);
        if(group.isEmpty())
            throw new ApiException(DefaultMessage.INTERNAL_SERVER_ERROR);
        return group.get();
    }

    private RestaurantEntity validateRestaurant(final Long restaurantLocationId) {
        Optional<RestaurantEntity> restaurant = restaurantRepository.findById(restaurantLocationId);
        if(restaurant.isEmpty())
            throw new ApiException(DefaultMessage.INTERNAL_SERVER_ERROR);
        return  restaurant.get();
    }

    private CategoryEntity validateCategory(final Long categoryId) {
        Optional<CategoryEntity> category = categoryRepository.findById(categoryId);
        if(category.isEmpty())
            throw new ApiException(DefaultMessage.INTERNAL_SERVER_ERROR);
        return category.get();
    }

    private void validateConflict(RestaurantEntity restaurant) {
        Optional<RecommendRestaurantEntity> recommendRestaurant = recommendRestaurantRepository.findByRestaurant(restaurant);
        if(recommendRestaurant.isPresent())
            throw new ApiException(DefaultMessage.INTERNAL_SERVER_ERROR);
    }

    private UserEntity validateUser(String email) {
        Optional<UserEntity> result = userRepository.findByEmail(email);
        if(result.isEmpty())
            throw new ApiException(DefaultMessage.INTERNAL_SERVER_ERROR);
        return  result.get();
    }

    private RecommendRestaurantEntity validateIsWriteRecommendRestaurantByUser(Long id, String email) {
        Optional<UserEntity> findUserResult = userRepository.findByEmail(email);
        Optional<RecommendRestaurantEntity> findRecommendRestaurantResult = recommendRestaurantRepository.findById(id);

        if(findRecommendRestaurantResult.isEmpty()) {
            throw new ApiException(RestaurantMessage.RECOMMEND_RESTAURANT_NOT_FOUND);
        }

        if(findUserResult.isEmpty()) {
            throw new ApiException(UserMessage.USER_NOT_FOUND);
        }

        RecommendRestaurantEntity recommendRestaurant = findRecommendRestaurantResult.get();
        if(!recommendRestaurant.getUser().getId().equals(findUserResult.get().getId())) {
            throw new ApiException(RestaurantMessage.RECOMMEND_RESTAURANT_NOT_MATCH_OWNER);
        }
        return recommendRestaurant;
    }
}
