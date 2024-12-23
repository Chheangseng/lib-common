package tcs.system.lib_common.page;

import java.util.List;
import lombok.*;
import org.springframework.data.domain.Page;

@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
public class PaginationEntityResponse <T>{
    private List<T> contents;
    private int page;
    private int pageSize;
    private int totalPages;
    private long total;
    private boolean hasNext;
    public PaginationEntityResponse(Page<T> page) {
        this(
                page.getContent(),
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.hasNext());
    }

    public PaginationEntityResponse(
            List<T> contents, int page, int pageSize, int totalPages, long total, boolean hasNext) {
        this.contents = contents;
        this.page = page;
        this.totalPages = totalPages;
        this.pageSize = pageSize;
        this.total = total;
        this.hasNext = hasNext;
    }
}
