package com.annalogue.m2.controllers;

import com.annalogue.m2.exceptions.UploadFileException;
import com.annalogue.m2.models.CommentEntity;
import com.annalogue.m2.models.PublicationEntity;
import com.annalogue.m2.models.UserEntity;
import com.annalogue.m2.services.ICommentService;
import com.annalogue.m2.services.IPublicationService;
import com.annalogue.m2.services.IUserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
public class CommentController {

    private final ICommentService commentService;
    private final IUserService userService;
    private final IPublicationService publicationService;

    public CommentController(ICommentService commentService, IUserService userService, IPublicationService publicationService) {
        this.commentService = commentService;
        this.userService = userService;
        this.publicationService = publicationService;
    }

    @PostMapping("publication/{id}/addComment")
    public String addComment(@PathVariable("id") long publicationId,
                             @RequestParam(value="content") String content,
                             @RequestParam(value="file") MultipartFile file,
                             Model model) {

        UserEntity currentUser = userService.getCurrentUser();
        CommentEntity comment = null;
        try {
            comment = commentService.addComment(content, currentUser.getId(), publicationId, file);
        } catch (UploadFileException e) {
            model.addAttribute("error", e.getMessage());
        }

        PublicationEntity publication = publicationService.findById(publicationId).orElse(null);
        if (publication == null) {
            return "redirect:/";
        }
        return "redirect:/modules/detail/" + publication.getModule().getId();
    }

    @PostMapping("deleteComment/{id}")
    public String deleteStudent(@PathVariable("id") long commentId) {
        commentService.deleteComment(commentId);
        return "redirect:/";
    }


}
