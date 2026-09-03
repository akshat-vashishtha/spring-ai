package com.learning.service;

import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import com.learning.config.RetrievalProperties;
import com.learning.dto.KnowledgeDocumentRequest;
import com.learning.dto.KnowledgeSearchResult;
import com.learning.validation.RequestValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KnowledgeService {

    private final VectorStore vectorStore;
    private final RetrievalProperties retrievalProperties;
    private final RequestValidator requestValidator;

    public String addDocument(KnowledgeDocumentRequest request) {
        requestValidator.validateKnowledgeDocument(request);

        Map<String, Object> metadata = request.metadata() == null
                ? Map.of()
                : Map.copyOf(request.metadata());

        Document document = Document.builder()
                .text(request.content())
                .metadata(metadata)
                .build();

        vectorStore.add(List.of(document));
        return document.getId();
    }

    public List<KnowledgeSearchResult> search(
            String query,
            Integer requestedTopK,
            Double requestedSimilarityThreshold) {
        int topK = requestedTopK != null ? requestedTopK : retrievalProperties.getTopK();
        double similarityThreshold = requestedSimilarityThreshold != null
                ? requestedSimilarityThreshold
                : retrievalProperties.getSimilarityThreshold();

        requestValidator.validateSearch(query, topK, similarityThreshold);

        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(similarityThreshold)
                .build();

        return vectorStore.similaritySearch(searchRequest).stream()
                .map(document -> new KnowledgeSearchResult(
                        document.getId(),
                        document.getText(),
                        document.getScore(),
                        document.getMetadata()))
                .toList();
    }
}
