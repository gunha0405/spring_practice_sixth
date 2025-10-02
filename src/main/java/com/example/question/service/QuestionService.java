package com.example.question.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.DataNotFoundException;
import com.example.category.model.Category;
import com.example.category.repository.CategoryRepository;
import com.example.question.model.Question;
import com.example.question.repository.QuestionRepository;
import com.example.user.model.SiteUser;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final CategoryRepository categoryRepository;

    public List<Question> getList() {
        return this.questionRepository.findAll();
    }
    
    public Question getQuestion(Long id) {  
        Optional<Question> question = this.questionRepository.findById(id);
        if (question.isPresent()) {
            return question.get();
        } else {
            throw new DataNotFoundException("question not found");
        }
    }
    
    public void create(String subject, String content, Long categoryId, SiteUser author) {
        Optional<Category> category = this.categoryRepository.findById(categoryId);
    	Question q = new Question();
        q.setSubject(subject);
        q.setContent(content);
        q.setAuthor(author);
        q.setCategory(category.get());
        q.setCreateDate(LocalDateTime.now());
        this.questionRepository.save(q);
    }
    
    public Page<Question> getList(int page, String kw) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("createDate"));
        Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));
        return this.questionRepository.findAllByKeyword(kw, pageable);
    }
    
    public void modify(Question question, String subject, String content) {
        question.setSubject(subject);
        question.setContent(content);
        question.setModifyDate(LocalDateTime.now());
        this.questionRepository.save(question);
    }
    
    public void delete(Question question) {
        this.questionRepository.delete(question);
    }
    
    public void vote(Question question, SiteUser siteUser) {
        question.getVoter().add(siteUser);
        this.questionRepository.save(question);
    }
    
    public Page<Question> getQuestionsByLatestAnswer(int page) {
        Pageable pageable = PageRequest.of(page, 10);
        return questionRepository.findAllOrderByLatestAnswer(pageable);
    }

    public Page<Question> getQuestionsByLatestComment(int page) {
        Pageable pageable = PageRequest.of(page, 10);
        return questionRepository.findAllOrderByLatestComment(pageable);
    }
}
