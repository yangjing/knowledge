package me.yangbajing.share2016.business.service;

import me.yangbajing.share2016.data.domain.TodoStatus;
import me.yangbajing.share2016.data.model.Todo;
import me.yangbajing.share2016.data.repo.TodoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Created by Yang Jing (yangbajing@gmail.com) on 2016-05-06.
 */
@Service
public class TodServiceImpl implements TodoService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private TodoRepository todoRepository;

    @Override
    public List<Todo> findAll(Optional<TodoStatus> status) {
        return status.map(todoRepository::findByStatus).orElseGet(() -> todoRepository.findAll());
    }

    @Override
    public Todo create(Todo todo) {
        return todoRepository.insert(todo);
    }

    @Override
    public Optional<Todo> findOne(String id) {
        return Optional.ofNullable(todoRepository.findOne(id));
    }

    @Override
    public Todo updateOne(Todo todo) {
        return todoRepository.save(todo);
    }

    @Override
    public void removeOne(String id) {
        todoRepository.delete(id);
    }

    @Override
    public List<Todo> updateMutil(List<Todo> todos) {
        return todoRepository.save(todos);
    }

    @Override
    public int updateMutilStatus(Optional<List<String>> idsOpt, TodoStatus status) {
        Query query = idsOpt
                .map(list -> Query.query(Criteria.where("_id").in(list)))
                .orElseGet(() -> new Query());
        return mongoTemplate
                .updateMulti(query, Update.update("status", status), Todo.class)
                .getN();
    }

    @Override
    public Optional<Todo> updateOneStatus(String id, TodoStatus status) {
        return findOne(id)
                .map(todo -> {
                    todo.setStatus(status);
                    return todoRepository.save(todo);
                });
    }

}
