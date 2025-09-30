package com.example.question.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.question.model.Question;
import com.example.question.repository.QuestionRepository;
import com.example.question.service.QuestionService;

import lombok.RequiredArgsConstructor;


@Controller
@RequiredArgsConstructor
public class QuestionController {

	private final QuestionService questionService;

    @GetMapping("/question/list")
    public String list(Model model) {
    	List<Question> questionList = this.questionService.getList();
        model.addAttribute("questionList", questionList);
        return "question_list";
    }
}
