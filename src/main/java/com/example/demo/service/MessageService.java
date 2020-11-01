package com.example.demo.service;

import com.example.demo.domain.Comment;
import com.example.demo.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MessageService {

    Comment createComment(String text);

    Notification createNotificationForComment(Long commentId);

    Page<Comment> findAllComments(Pageable pageable);

    Page<Notification> findAllNotification(Pageable pageable);

    void removeAllComments();


}
