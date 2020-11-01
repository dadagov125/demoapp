package com.example.demo.service.impl;

import com.example.demo.domain.Comment;
import com.example.demo.domain.Notification;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.service.BusinessLogic;
import com.example.demo.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    CommentRepository commentRepository;
    @Autowired
    NotificationRepository notificationRepository;

    @Transactional()
    @Override
    public Comment createComment(String text) {

        Comment comment = new Comment();
        comment.setText(text);
        comment.setTime(LocalDateTime.now().toInstant(ZoneOffset.UTC));

        Comment savedComment = commentRepository.save(comment);

        BusinessLogic.doSomeWorkOnCommentCreation();

        createNotificationForComment(savedComment.getId());

        return comment;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Notification createNotificationForComment(Long commentId) {

        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new RuntimeException("Comment is not found"));

        Notification notification = new Notification();

        notification.setComment(comment);
        notification.setTime(LocalDateTime.now().toInstant(ZoneOffset.UTC));

        Notification savedNotification = notificationRepository.save(notification);

        try {
            BusinessLogic.doSomeWorkOnNotification();
            savedNotification.setDelivered(true);
        } catch (Exception ex) {
            savedNotification.setDelivered(false);
        }

        return savedNotification;
    }

    @Override
    public Page<Comment> findAllComments(Pageable pageable) {
        return commentRepository.findAll(pageable);
    }

    @Override
    public Page<Notification> findAllNotification(Pageable pageable) {
        return notificationRepository.findAll(pageable);
    }

    @Override
    public void removeAllComments() {
        notificationRepository.deleteAll();
        commentRepository.deleteAll();
    }


}
