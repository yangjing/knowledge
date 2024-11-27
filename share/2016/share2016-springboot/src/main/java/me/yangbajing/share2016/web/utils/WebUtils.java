package me.yangbajing.share2016.web.utils;

import me.yangbajing.share2016.data.domain.ErrResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Created by Yang Jing (yangbajing@gmail.com) on 2016-05-06.
 */
public class WebUtils {

    public static <T> ResponseEntity<T> responseOptional(Optional<T> result) {
        return responseOptional(result, () -> new ErrResponse(404, "not found"));
    }

    public static <T> ResponseEntity<T> responseOptional(Optional<T> result, Supplier<ErrResponse> other) {
        return result
                .map(bo -> new ResponseEntity(bo, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity(other.get(), HttpStatus.OK));
    }

}
