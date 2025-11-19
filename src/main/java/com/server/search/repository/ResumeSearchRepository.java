package com.server.search.repository;

import com.server.search.document.ResumeDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ResumeSearchRepository extends ElasticsearchRepository<ResumeDocument, Long> {
}
