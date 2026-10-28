package com.oriole.wisepen.ai.asset.repository;

import com.mongodb.client.result.UpdateResult;
import com.oriole.wisepen.ai.asset.domain.entity.VersionBundleBaseEntity;
import com.oriole.wisepen.ai.asset.enums.AssetResourceType;
import com.oriole.wisepen.ai.asset.enums.AssetUploadStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
public class CustomVersionBundleAssetRepository {

    private final MongoTemplate mongoTemplate;

    public boolean promotePendingUpload(VersionBundleBaseEntity<?> versionBundle,
                                        String assetId,
                                        String pendingObjectKey,
                                        AssetResourceType assetResourceType,
                                        Long actualSize) {
        Update update = new Update()
                .set("assets.$.objectKey", pendingObjectKey)
                .set("assets.$.assetResourceType", assetResourceType)
                .set("assets.$.size", actualSize)
                .set("assets.$.uploadStatus", AssetUploadStatus.AVAILABLE)
                .unset("assets.$.pendingUpload")
                .set("updateTime", LocalDateTime.now());
        return updatePendingAsset(versionBundle, assetId, pendingObjectKey, update);
    }

    public boolean clearPendingUpload(VersionBundleBaseEntity<?> versionBundle,
                                      String assetId,
                                      String pendingObjectKey) {
        Update update = new Update()
                .set("assets.$.uploadStatus", AssetUploadStatus.AVAILABLE)
                .unset("assets.$.pendingUpload")
                .set("updateTime", LocalDateTime.now());
        return updatePendingAsset(versionBundle, assetId, pendingObjectKey, update);
    }

    public boolean removePendingAsset(VersionBundleBaseEntity<?> versionBundle,
                                      String assetId,
                                      String pendingObjectKey) {
        Query query = pendingAssetQuery(versionBundle, assetId, pendingObjectKey);
        Query assetCondition = Query.query(Criteria.where("id").is(assetId)
                .and("pendingUpload.objectKey").is(pendingObjectKey));
        Update update = new Update()
                .pull("assets", assetCondition.getQueryObject())
                .set("updateTime", LocalDateTime.now());
        return mongoTemplate.updateFirst(query, update, versionBundle.getClass()).getModifiedCount() > 0;
    }

    private boolean updatePendingAsset(VersionBundleBaseEntity<?> versionBundle,
                                       String assetId,
                                       String pendingObjectKey,
                                       Update update) {
        UpdateResult updateResult = mongoTemplate.updateFirst(
                pendingAssetQuery(versionBundle, assetId, pendingObjectKey),
                update,
                versionBundle.getClass()
        );
        return updateResult.getModifiedCount() > 0;
    }

    private Query pendingAssetQuery(VersionBundleBaseEntity<?> versionBundle,
                                    String assetId,
                                    String pendingObjectKey) {
        return Query.query(Criteria.where("_id").is(versionBundle.getId())
                .and("assets").elemMatch(Criteria.where("id").is(assetId)
                        .and("pendingUpload.objectKey").is(pendingObjectKey)));
    }
}
