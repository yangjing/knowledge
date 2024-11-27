package me.yangbajing.share2016.data.domain;

import java.util.List;
import java.util.Optional;

/**
 * Created by Yang Jing (yangbajing@gmail.com) on 2016-05-07.
 */
public class UpdateStatusRequest {
    private List<String> ids;
    private TodoStatus status;

    public List<String> getIds() {
        return ids;
    }

    public void setIds(List<String> ids) {
        this.ids = ids;
    }

    public TodoStatus getStatus() {
        return status;
    }

    public void setStatus(TodoStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "UpdateStatusRequest{" +
                "ids=" + ids +
                ", status=" + status +
                '}';
    }
}
