package com.example.user.controller;

import java.security.Principal;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.user.model.SiteUser;
import com.example.user.model.dto.UserCreateForm;
import com.example.user.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/signup")
    public String signup(UserCreateForm userCreateForm) {
        return "signup_form";
    }

    @PostMapping("/signup")
    public String signup(@Valid UserCreateForm userCreateForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "signup_form";
        }

        if (!userCreateForm.getPassword1().equals(userCreateForm.getPassword2())) {
            bindingResult.rejectValue("password2", "passwordInCorrect", 
                    "2개의 패스워드가 일치하지 않습니다.");
            return "signup_form";
        }

        try {
            userService.create(userCreateForm.getUsername(),
                    userCreateForm.getEmail(), userCreateForm.getPassword1());
        } catch(DataIntegrityViolationException e) {
            bindingResult.reject("signupFailed", "이미 등록된 사용자입니다.");
            return "signup_form";
        } catch(Exception e) {
            bindingResult.reject("signupFailed", e.getMessage());
            return "signup_form";
        }
        return "redirect:/";
    }

    @GetMapping("/login")
    public String login() {
        return "login_form";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordForm() {
        return "forgot_password_form";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam("email") String email, Model model) {
        try {
            userService.sendTemporaryPassword(email);
            model.addAttribute("message", "임시 비밀번호가 이메일로 발송되었습니다.");
            return "login_form";
        } catch (Exception e) {
            model.addAttribute("error", "해당 이메일을 가진 사용자가 존재하지 않습니다.");
            return "forgot_password_form";
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/change-password")
    public String changePasswordForm() {
        return "change_password_form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/change-password")
    public String changePassword(Principal principal,
                                 @RequestParam("currentPassword") String currentPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 Model model) {
        try {
            userService.changePassword(principal.getName(), currentPassword, newPassword);
            model.addAttribute("message", "비밀번호가 성공적으로 변경되었습니다.");
            return "redirect:/";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "change_password_form";
        }
    }
    
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/profile")
    public String profile(Principal principal, Model model) {
        SiteUser user = userService.getUser(principal.getName());
        model.addAttribute("user", user);
        model.addAttribute("questions", user.getQuestionList()); 
        model.addAttribute("answers", user.getAnswerList());    
        model.addAttribute("comments", user.getCommentList());   
        return "profile";
    }

}
