package com.alura.hackatonAlura.churn;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PredictionRepository extends JpaRepository<Prediction, Long> {
    long count();
    long countByPrevision(String prevision);

    @Query("""
    select p.riskLevel, count(p)
    from Prediction p
    group by p.riskLevel
""")
    List<Object[]> countByRisk();
}
