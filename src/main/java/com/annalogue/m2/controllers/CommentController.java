package com.annalogue.m2.controllers;

import com.annalogue.m2.exceptions.UploadFileException;
import com.annalogue.m2.models.CommentEntity;
import com.annalogue.m2.models.PublicationEntity;
import com.annalogue.m2.models.UserEntity;
import com.annalogue.m2.services.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class CommentController {

    private final ICommentService commentService;
    private final IUserService userService;
    private final IPublicationService publicationService;
    private final INotifyService notifyService;
    private final ICorrectionService correctionService;

    public CommentController(ICommentService commentService, IUserService userService, IPublicationService publicationService, INotifyService notifyService, ICorrectionService correctionService) {
        this.commentService = commentService;
        this.userService = userService;
        this.publicationService = publicationService;
        this.notifyService = notifyService;
        this.correctionService = correctionService;
    }

    @PostMapping("/publications/{id}/add-comment")
    public String addCommentToPublication(@PathVariable("id") long publicationId,
                                          @RequestParam("content") String content,
                                          @RequestParam(value = "file", required = false) MultipartFile file,
                                          RedirectAttributes redirectAttributes) {

        UserEntity currentUser = userService.getCurrentUser();
        try {
            commentService.addCommentToPublication(content, currentUser.getId(), publicationId, file);
        } catch (UploadFileException e) {
            redirectAttributes.addFlashAttribute("error", notifyService.getMessage(e.getMessage()));
        }

        return publicationService.findById(publicationId)
                .map(p -> "redirect:/modules/" + p.getModule().getId())
                .orElse("redirect:/");
    }

    @PostMapping("/modules/{id}/add-comment-correction")
    public String addCommentToCorrection(@PathVariable("id") long correctionId,
                                         @RequestParam(value = "content") String content,
                                         @RequestParam(value = "file", required = false) MultipartFile file,
                                         RedirectAttributes redirectAttributes) {

        UserEntity currentUser = userService.getCurrentUser();
        try {
            commentService.addCommentToCorrection(content, currentUser.getId(), correctionId, file);
        } catch (UploadFileException e) {
            redirectAttributes.addFlashAttribute("error", notifyService.getMessage(e.getMessage()));
        }

        return correctionService.findById(correctionId)
                .map(correction -> "redirect:/modules/" + correction.getPublication().getModule().getId())
                .orElse("redirect:/");
    }

    @PostMapping("/publications/{pubId}/comments/{commentId}/delete")
    public String deleteComment(@PathVariable("pubId") long publicationId,
                                @PathVariable("commentId") long commentId,
                                RedirectAttributes redirectAttributes) {

        try {
            commentService.deleteComment(commentId);
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return publicationService.findById(publicationId)
                .map(p -> "redirect:/modules/" + p.getModule().getId())
                .orElse("redirect:/");
    }

}
