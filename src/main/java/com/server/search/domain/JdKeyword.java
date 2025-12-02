package com.server.search.domain;

import com.server.global.entity.BaseEntity;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "jd_keywords")
public class JdKeyword extends BaseEntity {
    @Id
    private Long jdId;

    @ElementCollection
    @CollectionTable(name = "jd_keywords_list", joinColumns = @JoinColumn(name = "jd_id"))
    @Column(name = "keyword")
    private List<String> keywords;

    public static JdKeyword of(Long jdId, List<String> keywords) {
        JdKeyword entity = new JdKeyword();
        entity.jdId = jdId;
        entity.keywords = keywords;
        return entity;
    }
}
