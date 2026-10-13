package com.annalogue.m2.controllers;

import com.annalogue.m2.models.UserEntity;
import com.annalogue.m2.services.ICommentService;
import com.annalogue.m2.services.IUserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class CommentController {

    private final ICommentService commentService;
    private final IUserService userService;

    public CommentController(ICommentService commentService, IUserService userService) {
        this.commentService = commentService;
        this.userService = userService;
    }

    @PostMapping("publication/{id}/addComment")
    public String addComment(@PathVariable("id") long publicationId,
                             @RequestParam(value="content") String content, Model model) {

        UserEntity currentUser = userService.getCurrentUser();
        commentService.addComment(content, currentUser.getId(), publicationId);
        return "redirect:/modules/detail";
    }

    @PostMapping("deleteComment/{id}")
    public String deleteStudent(@PathVariable("id") long commentId) {
        commentService.deleteComment(commentId);
        return "redirect:/";
    }

}
