package com.example.comment.controller;

import java.security.Principal;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.answer.model.Answer;
import com.example.answer.service.AnswerService;
import com.example.comment.model.Comment;
import com.example.comment.model.dto.CommentForm;
import com.example.comment.service.CommentService;
import com.example.question.model.Question;
import com.example.question.service.QuestionService;
import com.example.user.model.SiteUser;
import com.example.user.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/comment")
@RequiredArgsConstructor
public class CommentController {
	
	private final CommentService commentService;
	private final QuestionService questionService;
	private final AnswerService answerService; 
	private final UserService userService;
	

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/create/question/{id}")
	public String createQuestionCommentForm(CommentForm commentForm) {
		return "comment_form";
	}
	
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/create/question/{id}")
	public String createQuestionComment(@PathVariable("id") Long id,
	                                    @Valid CommentForm commentForm,
	                                    BindingResult bindingResult,
	                                    Principal principal) {
		Question question = this.questionService.getQuestion(id);
		SiteUser user = this.userService.getUser(principal.getName());

		if (bindingResult.hasErrors()) {
		    return "comment_form";
		}
		Comment c = this.commentService.create(question, user, commentForm.getContent());
		return String.format("redirect:/question/detail/%s", c.getQuestionId());
	}
	
	
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/create/answer/{id}")
	public String createAnswerCommentForm(CommentForm commentForm) {
		return "comment_form";
	}
	
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/create/answer/{id}")
	public String createAnswerComment(@PathVariable("id") Long id,
	                                  @Valid CommentForm commentForm,
	                                  BindingResult bindingResult,
	                                  Principal principal) {
		Answer answer = this.answerService.getAnswer(id);
		SiteUser user = this.userService.getUser(principal.getName());

		if (bindingResult.hasErrors()) {
		    return "comment_form";
		}
		Comment c = this.commentService.create(answer, user, commentForm.getContent());
		return String.format("redirect:/question/detail/%s#answer_%s",
		                     c.getQuestionId(),   
		                     answer.getId());    
	}
	
	
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/modify/{id}")
	public String modifyCommentForm(CommentForm commentForm, @PathVariable("id") Long id, Principal principal) {
		Comment comment = this.commentService.getComment(id);
		commentForm.setContent(comment.getContent());
		return "comment_form";
	}
	
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/modify/{id}")
	public String modifyComment(@Valid CommentForm commentForm,
	                            BindingResult bindingResult,
	                            Principal principal,
	                            @PathVariable("id") Long id) {
		if (bindingResult.hasErrors()) {
            return "comment_form";
        }
		Comment comment = this.commentService.getComment(id);
		Comment c = this.commentService.modify(comment, commentForm.getContent());
        return String.format("redirect:/question/detail/%s", c.getQuestionId());
	}
	
	
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/delete/{id}")
	public String deleteComment(Principal principal, @PathVariable("id") Long id) {
		Comment comment = this.commentService.getComment(id);
		this.commentService.delete(comment);
        return String.format("redirect:/question/detail/%s", comment.getQuestionId());
	}
	
}
