package org.oplearn.project.dto.response;

/**
 * Một nhánh trong cây duyệt phân cấp (Ngôn ngữ → Thời kỳ → Thể thơ → Tác giả).
 * - id: khoá của nhánh khi có (genreId, authorId); null với ngôn ngữ/thời kỳ (định danh bằng label).
 * - label: nhãn hiển thị (tên ngôn ngữ/thời kỳ/thể thơ/tác giả).
 * - count: số bài thơ thuộc nhánh này.
 */
public record FacetItemResponse(Long id, String label, long count) {
}
