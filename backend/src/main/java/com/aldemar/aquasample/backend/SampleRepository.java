package com.aldemar.aquasample.backend;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SampleRepository extends JpaRepository<SampleRecord, Long> {
    Optional<SampleRecord> findByCode(String code);

    @Query("select s from SampleRecord s "
            + "where (:center is null or lower(s.center) = lower(:center)) "
            + "and (:train is null or lower(s.train) = lower(:train)) "
            + "and (:line is null or lower(s.line) = lower(:line)) "
            + "and (:status is null or s.reviewStatus = :status) "
            + "order by s.timestamp desc")
    List<SampleRecord> search(@Param("center") String center, @Param("train") String train,
                              @Param("line") String line, @Param("status") SampleStatus status);
}
