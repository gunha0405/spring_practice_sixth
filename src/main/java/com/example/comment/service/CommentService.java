package com.example.comment.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.example.DataNotFoundException;
import com.example.answer.model.Answer;
import com.example.comment.model.Comment;
import com.example.comment.repository.CommentRepository;

import com.example.question.model.Question;
import com.example.user.model.SiteUser;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;


    public Comment create(Question question, SiteUser author, String content) {
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setCreateDate(LocalDateTime.now());
        comment.setAuthor(author);
        comment.setQuestion(question);
        return this.commentRepository.save(comment);
    }

    public Comment create(Answer answer, SiteUser author, String content) {
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setCreateDate(LocalDateTime.now());
        comment.setAuthor(author);
        comment.setAnswer(answer);
        return this.commentRepository.save(comment);
    }


    public Comment getComment(Long id) {
        return this.commentRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("찾을 수 없는 댓글입니다. id=" + id));
    }


    public Comment modify(Comment comment, String content) {
        comment.setContent(content);
        comment.setModifyDate(LocalDateTime.now());
        return this.commentRepository.save(comment);
    }


    public void delete(Comment comment) {
        this.commentRepository.delete(comment);
    }
}
