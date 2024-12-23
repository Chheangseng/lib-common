package tcs.system.lib_common.page;

import java.util.Objects;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PageableUtil {
  public static Pageable pageable(int page, int pageSize, String field, String sortDirection) {
    if (page == 0) {
      return Pageable.unpaged(Sort.by(Sort.Direction.fromString(sortDirection), field));
    }
    if (Objects.isNull(field) || Objects.isNull(sortDirection)) {
      return PageRequest.of(page - 1, pageSize);
    }
    return PageRequest.of(
        page - 1, pageSize, Sort.by(Sort.Direction.fromString(sortDirection), field));
  }
  public static Pageable pageable(int page, int pageSize) {
    return PageRequest.of(page - 1, pageSize);
  }
}
