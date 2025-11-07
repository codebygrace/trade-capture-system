package com.technicalchallenge.repository;

import com.technicalchallenge.model.AdditionalInfo;
import com.technicalchallenge.model.EntityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdditionalInfoRepository extends JpaRepository<AdditionalInfo, Long> {

    @Query("SELECT a FROM AdditionalInfo a WHERE a.entityType = :entityType AND a.entityId = :entityId AND a.active = true")
    List<AdditionalInfo> findActiveByEntityTypeAndEntityId(@Param("entityType") EntityType entityType, @Param("entityId") Long entityId);

    @Query("SELECT a FROM AdditionalInfo a WHERE a.entityType = :entityType AND a.entityId = :entityId AND a.fieldName = :fieldName AND a.active = true")
    AdditionalInfo findActiveByEntityTypeAndEntityIdAndFieldName(@Param("entityType") EntityType entityType, @Param("entityId") Long entityId, @Param("fieldName") String fieldName);

    List<AdditionalInfo> findByEntityTypeAndEntityIdAndActiveTrue(EntityType entityType, Long entityId);
}
