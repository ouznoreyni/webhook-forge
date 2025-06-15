package sn.noreyni.common.response;

public record PaginationMeta(
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {

    public static PaginationMeta of(int page, int size, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / size);
        boolean hasNext = page < totalPages - 1;
        boolean hasPrevious = page > 0;

        return new PaginationMeta(page, size, totalElements, totalPages, hasNext, hasPrevious);
    }
}