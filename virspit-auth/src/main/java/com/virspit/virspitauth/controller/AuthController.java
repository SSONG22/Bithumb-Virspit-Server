package com.virspit.virspitauth.controller;

import com.virspit.virspitauth.dto.model.Member;
import com.virspit.virspitauth.dto.request.MemberChangePwdRequestDto;
import com.virspit.virspitauth.dto.request.MemberSignInRequestDto;
import com.virspit.virspitauth.dto.request.MemberSignUpRequestDto;
import com.virspit.virspitauth.dto.response.MemberSignInResponseDto;
import com.virspit.virspitauth.service.MemberService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
public class AuthController {
    private final MemberService memberService;

    @PostMapping("/register")
    @ApiOperation("회원가입")
    public ResponseEntity<String> addNewUser(@RequestBody MemberSignUpRequestDto memberSignUpRequestDto) {
        return ResponseEntity.ok(memberService.register(memberSignUpRequestDto));
    }

    @PostMapping("/login")
    @ApiOperation("로그인")
    public MemberSignInResponseDto login(@RequestBody MemberSignInRequestDto memberSignInRequestDto) throws Exception {
        return memberService.login(memberSignInRequestDto);
    }

    @GetMapping("/verify/mail")
    @ApiOperation("회원가입시 입력한 이메일 인증")
    public ResponseEntity<String> verifyEmail(@RequestParam("useremail") String userEmail) throws Exception {
        return ResponseEntity.ok(memberService.verifyUserEmail(userEmail));
    }

    @PostMapping("/verify/mail")
    @ApiOperation("이메일에 전송된 인증번호 검증")
    public ResponseEntity<Boolean> verifyNumber(@RequestParam("useremail") String userEmail, Integer number) throws Exception{
        return ResponseEntity.ok(memberService.verifyNumber(userEmail, number));
    }

    @PostMapping("/initpwd")
    @ApiOperation("비밀번호 잃어버렸을 때 초기화 요청")
    public ResponseEntity<Boolean> findPassword(@RequestParam("useremail") String userEmail) throws Exception{
        return ResponseEntity.ok(memberService.findPasssword(userEmail));
    }

    @GetMapping("/findpwd/res")
    @ApiOperation("비밀번호 초기화 요청 후 응답")
    public ResponseEntity<Boolean> initPassword(
            @RequestParam("useremail") String userEmail, @RequestParam("key") String hash) throws Exception{
        return ResponseEntity.ok(memberService.initPassword(userEmail, hash));
    }

    @PutMapping("/changepwd")
    @ApiOperation("비밀번호 변경")
    public Member changePassword(@RequestBody MemberChangePwdRequestDto memberChangePwdRequestDto) {
        return memberService.changePassword(memberChangePwdRequestDto);
    }



}