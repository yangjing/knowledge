package me.yangbajing.share2016.data.repo;

import me.yangbajing.share2016.data.domain.TodoStatus;
import me.yangbajing.share2016.data.model.Todo;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Created by Yang Jing (yangbajing@gmail.com) on 2016-05-06.
 */
public interface TodoRepository extends MongoRepository<Todo, String> {
    List<Todo> findByStatus(TodoStatus status);
}
