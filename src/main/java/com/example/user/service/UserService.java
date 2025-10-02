package com.example.user.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.DataNotFoundException;
import com.example.user.model.SiteUser;
import com.example.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    public SiteUser create(String username, String email, String password) {
        SiteUser user = new SiteUser();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        this.userRepository.save(user);
        return user;
    }

    public SiteUser getUser(String username) {
        return this.userRepository.findByUsername(username)
                .orElseThrow(() -> new DataNotFoundException("user not found"));
    }


    public void sendTemporaryPassword(String email) {
        SiteUser user = this.userRepository.findByEmail(email)
                .orElseThrow(() -> new DataNotFoundException("user not found"));

        String tempPassword = UUID.randomUUID().toString().substring(0, 8);

        user.setPassword(passwordEncoder.encode(tempPassword));
        this.userRepository.save(user);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("[사이트] 임시 비밀번호 안내");
        message.setText("안녕하세요, 요청하신 임시 비밀번호는: " + tempPassword + " 입니다.\n로그인 후 반드시 비밀번호를 변경해주세요.");
        mailSender.send(message);
    }

    public void changePassword(String username, String currentPassword, String newPassword) {
        SiteUser user = getUser(username);

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        this.userRepository.save(user);
    }
}
