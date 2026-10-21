package com.oriole.wisepen.web.app.repository;

import com.oriole.wisepen.web.app.domain.entity.SingleHtmlPageEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SingleHtmlPageRepository extends MongoRepository<SingleHtmlPageEntity, String> {
    void deleteByResourceIdIn(List<String> resourceIds);
}
