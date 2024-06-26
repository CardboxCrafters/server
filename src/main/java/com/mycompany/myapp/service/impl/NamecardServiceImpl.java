package com.mycompany.myapp.service.impl;

import com.mycompany.myapp.converter.AddressConverter;
import com.mycompany.myapp.converter.NamecardConverter;
import com.mycompany.myapp.domain.*;
import com.mycompany.myapp.repository.AddressRepository;
import com.mycompany.myapp.repository.CategoryRepository;
import com.mycompany.myapp.repository.NamecardRepository;
import com.mycompany.myapp.service.NamecardService;
import com.mycompany.myapp.service.S3Service;
import com.mycompany.myapp.util.HttpRequestUtil;
import com.mycompany.myapp.web.dto.NamecardRequestDto;
import com.mycompany.myapp.web.dto.NamecardResponseDto;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NamecardServiceImpl implements NamecardService {
    private final NamecardConverter namecardConverter;
    private final NamecardRepository namecardRepository;
    private final CategoryRepository categoryRepository;
    private final AddressConverter addressConverter;
    private final AddressRepository addressRepository;
    private final S3Service s3Service;
    private final HttpRequestUtil httpRequestUtil;

    @Transactional
    @Override
    public void createCroppedNamecard(User user, NamecardRequestDto.NamecardDto request, MultipartFile image) throws IOException {
        //이미지 크롭
        byte[] croppedImageBytes = HttpRequestUtil.cropImage(image);

        // S3에 이미지 업로드
        String fileName = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
        String imageUrl = s3Service.uploadFile(croppedImageBytes, fileName);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new NoSuchElementException("Category not found"));

        NameCard nameCard = namecardConverter.createNamecard(user, request, category, imageUrl);
        namecardRepository.save(nameCard);

        Address address = addressConverter.createAddress(request, nameCard);
        addressRepository.save(address);
    }

    @Transactional
    @Override
    public void createNamecard(User user, NamecardRequestDto.NamecardDto request, MultipartFile image) throws IOException {
        byte[] fileBytes = image.getBytes();

        // S3에 이미지 업로드
        String fileName = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
        String imageUrl = s3Service.uploadFile(fileBytes, fileName);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new NoSuchElementException("Category not found"));

        NameCard nameCard = namecardConverter.createNamecard(user, request, category, imageUrl);
        namecardRepository.save(nameCard);

        Address address = addressConverter.createAddress(request, nameCard);
        addressRepository.save(address);
    }

    @Override
    public NamecardResponseDto.OCRResponseDto postOCR(MultipartFile image) {
        try {
            // image를 Base64로 인코딩
            String base64Image = Base64.encodeBase64String(image.getBytes());
            return httpRequestUtil.postOCR(base64Image);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public NamecardResponseDto.NamecardDTO getNamecard(Long namecardId){
        NameCard namecard = namecardRepository.findById(namecardId)
                .orElseThrow(() -> new NoSuchElementException("Namecard not found."));

        Address address = addressRepository.findOneByNameCard(namecard);

        return namecardConverter.getNamecard(namecard, address);
    }

    @Override
    public List<NamecardResponseDto.NamecardPreviewDto> getNamecardByCategory(User user, Optional<Long> categoryId){
        List<NameCard> nameCardList;

        if (categoryId.isPresent()) {
            Category category = categoryRepository.findById(categoryId.get())
                    .orElseThrow(() -> new NoSuchElementException("Category not found."));

            nameCardList = namecardRepository.findByCategory(category);
        } else {
            nameCardList = namecardRepository.findByUserAndIsUserFalse(user);
        }

        Collections.sort(nameCardList, Comparator.comparing(NameCard::getName));

        return nameCardList.stream().map(
                namecardConverter::toNamecardPage
        ).collect(Collectors.toList());
    }

    @Override
    public List<NamecardResponseDto.NamecardPreviewDto> searchNamecard(User user, String keyword){
        List<NameCard> nameCardList;

        nameCardList = namecardRepository.findByNameContaining(keyword);

        Collections.sort(nameCardList, Comparator.comparing(NameCard::getName));

        return nameCardList.stream().map(
                namecardConverter::toNamecardPage
        ).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteNamecard(Long namecardId){
        NameCard nameCard = namecardRepository.findById(namecardId)
                .orElseThrow(() -> new NoSuchElementException("Namecard not found"));

        List<Address> addresses = addressRepository.findByNameCard(nameCard);
        addressRepository.deleteAll(addresses);

        namecardRepository.delete(nameCard);
    }

    @Override
    @Transactional
    public void updateNamecard(NamecardRequestDto.NamecardDto request, Long namecardId){
        NameCard namecard = namecardRepository.findById(namecardId)
                .orElseThrow(() -> new NoSuchElementException("Namecard not found"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new NoSuchElementException("Category not found"));

        namecard.updateNamecard(request, category);

        namecardRepository.save(namecard);
    }
}
