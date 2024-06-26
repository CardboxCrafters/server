package com.mycompany.myapp.web.dto;

import lombok.*;

public class NamecardResponseDto {
    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class OCRResponseDto {
        private String name;
        private String mobile;
        private String email;
        private String company;
        private String department;
        private String position;
        private String tel;
        private String fax;
        private String homepage;
        private String address;
    }

    @Builder @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class NamecardDTO{
        private String name;
        private String company;
        private String department;
        private String position;
        private String mobile;
        private String email;
        private String tel;
        private String fax;
        private String homepage;
        private String address;
        private String categoryName;
        private Double latitude;
        private Double longitude;
    }

    @Builder @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class NamecardPreviewDto{
        private String category;
        private Long namecardId;
        private String name;
        private String position;
        private String department;
        private String company;
        private String namecardUrl;
    }
}
