package com.learning.etl.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response details for ETL document ingestion")
public class DocumentIngestResponse {

    @Schema(description = "Name of the ingested file", example = "document.pdf")
    private String fileName;

    @Schema(description = "Total number of semantic chunks generated and stored in vector store", example = "12")
    private int totalChunks;

    @Schema(description = "Status or confirmation message", example = "Document parsed, chunked, and indexed successfully")
    private String message;
}
