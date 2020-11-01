package com.example.demo.web.rest;


import com.example.demo.domain.Comment;
import com.example.demo.domain.Notification;
import com.example.demo.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class MessageController {

    @Autowired
    MessageService messageService;

    @GetMapping("/comments")
    public ResponseEntity<Page<Comment>> getComments(Pageable pageable) {
        Page<Comment> page = messageService.findAllComments(pageable);
        return ResponseEntity.ok(page);

    }

    @PostMapping("/comments")
    public ResponseEntity<Comment> createComment(@RequestBody String text) {
        Comment comment = messageService.createComment(text);
        return ResponseEntity.ok(comment);
    }

    @GetMapping("/notifications")
    public ResponseEntity<Page<Notification>> getNotifications(Pageable pageable) {
        Page<Notification> page = messageService.findAllNotification(pageable);
        return ResponseEntity.ok(page);

    }


    @GetMapping("/clean")
    public String cleanTables() {
        messageService.removeAllComments();

        return "Ok";
    }


}



