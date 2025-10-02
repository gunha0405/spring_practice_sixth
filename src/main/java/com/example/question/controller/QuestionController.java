package com.example.question.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import com.example.answer.model.Answer;
import com.example.answer.model.dto.AnswerForm;
import com.example.answer.service.AnswerService;
import com.example.category.model.Category;
import com.example.category.service.CategoryService;
import com.example.question.model.Question;
import com.example.question.model.dto.QuestionForm;
import com.example.question.repository.QuestionRepository;
import com.example.question.service.QuestionService;
import com.example.user.model.SiteUser;
import com.example.user.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@Controller
@RequestMapping("/question")
@RequiredArgsConstructor
public class QuestionController {

	private final QuestionService questionService;
	private final AnswerService answerService;
	private final UserService userService;
	private final CategoryService categoryService;

	@GetMapping("/list")
    public String list(Model model, @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "kw", defaultValue = "") String kw) {
        Page<Question> paging = this.questionService.getList(page, kw);
        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);
        return "question_list";
    }
    
	@GetMapping("/list/recent-answers")
	public String listByRecentAnswers(Model model,
	                                  @RequestParam(value = "page", defaultValue = "0") int page) {
	    Page<Question> paging = questionService.getQuestionsByLatestAnswer(page);
	    model.addAttribute("paging", paging);
	    model.addAttribute("sortType", "recent-answers");
	    return "question_list";
	}

	@GetMapping("/list/recent-comments")
	public String listByRecentComments(Model model,
	                                   @RequestParam(value = "page", defaultValue = "0") int page) {
	    Page<Question> paging = questionService.getQuestionsByLatestComment(page);
	    model.addAttribute("paging", paging);
	    model.addAttribute("sortType", "recent-comments");
	    return "question_list";
	}

	
    
	@GetMapping("/detail/{id}")
	public String detail(Model model,
	                     @PathVariable("id") Long id,
	                     @RequestParam(value = "page", defaultValue = "0") int page,
	                     @RequestParam(value = "sort", defaultValue = "new") String sort) {
	    Question question = this.questionService.getQuestion(id);
	    Page<Answer> paging = this.answerService.getAnswersByQuestion(question, page, sort);

	    model.addAttribute("question", question);
	    model.addAttribute("answerPaging", paging);
	    model.addAttribute("answerForm", new AnswerForm());
	    model.addAttribute("sort", sort);
	    return "question_detail";
	}
    
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/create")
    public String questionCreate(QuestionForm questionForm, Model model) {
    	List<Category> categories = this.categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        return "question_form";
    }
    
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create")
    public String questionCreate(@Valid QuestionForm questionForm, BindingResult bindingResult, Principal principal, Model model) {
        if (bindingResult.hasErrors()) {
        	model.addAttribute("categories", categoryService.getAllCategories());
            return "question_form";
        }
        SiteUser siteUser = this.userService.getUser(principal.getName());
        this.questionService.create(questionForm.getSubject(), questionForm.getContent(), questionForm.getCategoryId(), siteUser);
        return "redirect:/question/list";
    }
    
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify/{id}")
    public String questionModify(QuestionForm questionForm, @PathVariable("id") Long id, Principal principal) {
        Question question = this.questionService.getQuestion(id);
        if(!question.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }
        questionForm.setSubject(question.getSubject());
        questionForm.setContent(question.getContent());
        return "question_form";
    }
    
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{id}")
    public String questionModify(@Valid QuestionForm questionForm, BindingResult bindingResult, 
            Principal principal, @PathVariable("id") Long id) {
        if (bindingResult.hasErrors()) {
            return "question_form";
        }
        Question question = this.questionService.getQuestion(id);
        if (!question.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }
        this.questionService.modify(question, questionForm.getSubject(), questionForm.getContent());
        return String.format("redirect:/question/detail/%s", id);
    }
    
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete/{id}")
    public String questionDelete(Principal principal, @PathVariable("id") Long id) {
        Question question = this.questionService.getQuestion(id);
        if (!question.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제권한이 없습니다.");
        }
        this.questionService.delete(question);
        return "redirect:/";
    }
    
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/vote/{id}")
    public String questionVote(Principal principal, @PathVariable("id") Long id) {
        Question question = this.questionService.getQuestion(id);
        SiteUser siteUser = this.userService.getUser(principal.getName());
        this.questionService.vote(question, siteUser);
        return String.format("redirect:/question/detail/%s", id);
    }
}
