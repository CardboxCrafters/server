package com.mycompany.myapp.web.controller;

import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.exception.CustomExceptions;
import com.mycompany.myapp.exception.ResponseMessage;
import com.mycompany.myapp.exception.StatusCode;
import com.mycompany.myapp.repository.UserRepository;
import com.mycompany.myapp.service.UserService;
import com.mycompany.myapp.util.JwtUtil;
import com.mycompany.myapp.web.controller.base.BaseController;
import com.mycompany.myapp.web.dto.NamecardRequestDto;
import com.mycompany.myapp.web.dto.NamecardResponseDto;
import com.mycompany.myapp.web.dto.UserRequestDto;
import com.mycompany.myapp.web.dto.UserResponseDto;
import com.mycompany.myapp.web.dto.base.DefaultRes;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Api(tags = "유저 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController extends BaseController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @ApiOperation(value = "Get User API")
    @ApiResponse(code = 200, message = "내 정보 불러오기 성공")
    @GetMapping("")
    public ResponseEntity getUser(){
        try {
            logger.info("Received request: method={}, path={}, description={}", "GET", "/api/user", "Get User API");
            User user = userService.getCurrentUser();

            UserResponseDto.UserDto res = userService.getUser(user);

            return new ResponseEntity( DefaultRes.res(StatusCode.OK, ResponseMessage.GET_USER_SUCCESS, res), HttpStatus.OK);
        } catch (CustomExceptions.Exception e) {
            return handleApiException(e, HttpStatus.BAD_REQUEST);
        }
    }

    @ApiOperation(value = "Update User API")
    @ApiResponse(code = 200, message = "내 정보 수정하기 성공")
    @PutMapping("")
    public ResponseEntity updateUser(@RequestBody UserRequestDto.UpdateUserDto request){
        try {
            logger.info("Received request: method={}, path={}, description={}", "PUT", "/api/user", "Update User API");
            User user = userService.getCurrentUser();

            userService.updateUser(user, request);

            return new ResponseEntity( DefaultRes.res(StatusCode.OK, ResponseMessage.UPDATE_USER_SUCCESS), HttpStatus.OK);
        } catch (CustomExceptions.Exception e) {
            return handleApiException(e, HttpStatus.BAD_REQUEST);
        }
    }

    @ApiOperation(value = "Withdraw User API")
    @ApiResponse(code = 200, message = "회원 탈퇴 성공")
    @PatchMapping("")
    public ResponseEntity withdrawUser(){
        try {
            logger.info("Received request: method={}, path={}, description={}", "PATCH", "/api/user", "Withdraw User API");
            User user = userService.getCurrentUser();

            userService.withdrawUser(user);

            return new ResponseEntity( DefaultRes.res(StatusCode.OK, ResponseMessage.WITHDRAW_USER_SUCCESS), HttpStatus.OK);
        } catch (CustomExceptions.Exception e) {
            return handleApiException(e, HttpStatus.BAD_REQUEST);
        }
    }

    @ApiOperation(value = "Send SMS Certification API")
    @ApiResponse(code = 200, message = "SMS 인증 문자 전송 성공")
    @PostMapping("/sms-certification/send")
    public ResponseEntity sendSMS(@RequestBody UserRequestDto.SendSmsCertificationDto request){
        try {
            logger.info("Received request: method={}, path={}, description={}", "POST", "api/user/sms-certification/send", "Send SMS Certification API");

            userService.sendSms(request);

            return new ResponseEntity( DefaultRes.res(StatusCode.OK, ResponseMessage.SEND_CERTIFICATION_SUCCESS), HttpStatus.OK);
        } catch (CustomExceptions.Exception e) {
            return handleApiException(e, HttpStatus.BAD_REQUEST);
        }
    }

    @ApiOperation(value = "Confirm SMS Certification API")
    @ApiResponse(code = 200, message = "SMS 문자 인증 성공")
    @PostMapping("/sms-certification/confirm")
    public ResponseEntity confrimSMS(@RequestBody UserRequestDto.ConfirmSmsCertificationDto request){
        try {
            logger.info("Received request: method={}, path={}, description={}", "POST", "api/user/sms-certification/confirm", "Confirm SMS Certification API");

            Long userId = userService.verifyAndRegisterUser(request);
            String accessToken = jwtUtil.generateAccessToken(userId);
            String refreshToken = jwtUtil.generateRefreshToken(userId);
            System.out.println(refreshToken);
            System.out.println(accessToken);

            userService.saveRefreshToken(refreshToken);

            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + accessToken);
            Map<String, Object> response = new HashMap<>();
            response.put("accessToken", accessToken);

            return new ResponseEntity( DefaultRes.res(StatusCode.OK, ResponseMessage.CONFIRM_CERTIFICATION_SUCCESS, response), HttpStatus.OK);
        } catch (CustomExceptions.Exception e) {
            return handleApiException(e, HttpStatus.BAD_REQUEST);
        }
    }

    @ApiOperation(value = "Reissue Access Token API")
    @ApiResponse(code = 200, message = "Access Toekn 재발급 성공")
    @GetMapping("/reissue")
    public ResponseEntity reissue(){
        try {
            logger.info("Received request: method={}, path={}, description={}", "GET", "/api/user/reissue", "Reissue Access Token API");
            User user = userService.getCurrentUser();

            String newAccessToken = userService.reissueAccessToken(user);
            Map<String, Object> res = new HashMap<>();
            res.put("accessToken", newAccessToken);

            return new ResponseEntity( DefaultRes.res(StatusCode.OK, ResponseMessage.REISSUE_ACCESS_TOKEN_SUCCESS, res), HttpStatus.OK);
        } catch (CustomExceptions.Exception e) {
            return handleApiException(e, HttpStatus.BAD_REQUEST);
        }
    }
}
