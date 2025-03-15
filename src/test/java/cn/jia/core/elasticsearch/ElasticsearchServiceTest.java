package cn.jia.core.elasticsearch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ElasticsearchServiceTest {

    @Mock
    private ElasticsearchOperations elasticsearchOperations;

    @InjectMocks
    private ElasticsearchService elasticsearchService;

    @BeforeEach
    void setUp() {
        // 初始化 Mockito 注解
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Test case for searchMatch method with valid input.
     */
    @Test
    void testSearchMatch_ValidInput() {
        // Arrange
        String index = "test-index";
        String field = "name";
        String value = "John";
        Class<String> clazz = String.class;

        // Mock the behavior of elasticsearchOperations.search
        SearchHits<String> mockSearchHits = mock(SearchHits.class);
        when(elasticsearchOperations.search(any(NativeQuery.class), eq(clazz), any(IndexCoordinates.class)))
                .thenReturn(mockSearchHits);

        // Act
        SearchHits<String> result = elasticsearchService.searchMatch(index, field, value, clazz);

        // Assert
        assertNotNull(result);
        verify(elasticsearchOperations, times(1)).search(any(NativeQuery.class), eq(clazz), any(IndexCoordinates.class));
    }

    /**
     * Test case for searchMatch method with invalid index.
     */
    @Test
    void testSearchMatch_InvalidIndex() {
        // Arrange
        String index = "invalid-index";
        String field = "name";
        String value = "John";
        Class<String> clazz = String.class;

        // Mock the behavior of elasticsearchOperations.search to throw an exception
        when(elasticsearchOperations.search(any(NativeQuery.class), eq(clazz), any(IndexCoordinates.class)))
                .thenThrow(new RuntimeException("Index not found"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            elasticsearchService.searchMatch(index, field, value, clazz);
        });

        assertEquals("Index not found", exception.getMessage());
        verify(elasticsearchOperations, times(1)).search(any(NativeQuery.class), eq(clazz), any(IndexCoordinates.class));
    }

    /**
     * Test case for delete method with valid input.
     */
    @Test
    void testDelete_ValidInput() {
        // Arrange
        String id = "123";
        String index = "test-index";

        // Mock the behavior of elasticsearchOperations.delete
        when(elasticsearchOperations.delete(eq(id), any(IndexCoordinates.class)))
                .thenReturn("Deleted successfully");

        // Act
        String result = elasticsearchService.delete(id, index);

        // Assert
        assertEquals("Deleted successfully", result);
        verify(elasticsearchOperations, times(1)).delete(eq(id), any(IndexCoordinates.class));
    }

    /**
     * Test case for delete method with invalid document ID.
     */
    @Test
    void testDelete_InvalidDocumentId() {
        // Arrange
        String id = "invalid-id";
        String index = "test-index";

        // Mock the behavior of elasticsearchOperations.delete to throw an exception
        when(elasticsearchOperations.delete(eq(id), any(IndexCoordinates.class)))
                .thenThrow(new RuntimeException("Document not found"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            elasticsearchService.delete(id, index);
        });

        assertEquals("Document not found", exception.getMessage());
        verify(elasticsearchOperations, times(1)).delete(eq(id), any(IndexCoordinates.class));
    }
}
