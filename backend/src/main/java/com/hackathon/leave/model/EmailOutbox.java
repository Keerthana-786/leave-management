package com.hackathon.leave.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_outbox")
public class EmailOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "to_email", nullable = false)
    private String toEmail;

    @Column(nullable = false)
    private String subject;

    @Lob
    @Column(nullable = false)
    private String body;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private boolean sent = false;

    public EmailOutbox() {
    }

    public EmailOutbox(Long id, String toEmail, String subject, String body, LocalDateTime createdAt, boolean sent) {
        this.id = id;
        this.toEmail = toEmail;
        this.subject = subject;
        this.body = body;
        this.createdAt = createdAt;
        this.sent = sent;
    }

    public static EmailOutboxBuilder builder() {
        return new EmailOutboxBuilder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToEmail() {
        return toEmail;
    }

    public void setToEmail(String toEmail) {
        this.toEmail = toEmail;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    public static class EmailOutboxBuilder {
        private Long id;
        private String toEmail;
        private String subject;
        private String body;
        private LocalDateTime createdAt;
        private boolean sent = false;

        public EmailOutboxBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public EmailOutboxBuilder toEmail(String toEmail) {
            this.toEmail = toEmail;
            return this;
        }

        public EmailOutboxBuilder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public EmailOutboxBuilder body(String body) {
            this.body = body;
            return this;
        }

        public EmailOutboxBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public EmailOutboxBuilder sent(boolean sent) {
            this.sent = sent;
            return this;
        }

        public EmailOutbox build() {
            return new EmailOutbox(id, toEmail, subject, body, createdAt, sent);
        }
    }
}
