package me.yangbajing.share2016.data.model;

import me.yangbajing.share2016.data.domain.TodoStatus;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Created by Yang Jing (yangbajing@gmail.com) on 2016-05-06.
 */
@Document
public class Todo {
    @Id
    private String id;

    private String text;

    private TodoStatus status;

    @LastModifiedDate
    private LocalDateTime lastModifiedDate;

    @CreatedDate
    private LocalDateTime createdDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public TodoStatus getStatus() {
        return status;
    }

    public void setStatus(TodoStatus status) {
        this.status = status;
    }

    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public String toString() {
        return "Todo{" +
                "id='" + id + '\'' +
                ", text='" + text + '\'' +
                ", status=" + status +
                ", lastModifiedDate=" + lastModifiedDate +
                ", createdDate=" + createdDate +
                '}';
    }
}
