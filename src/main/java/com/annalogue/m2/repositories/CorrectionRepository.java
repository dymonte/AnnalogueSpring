package com.annalogue.m2.repositories;

import com.annalogue.m2.models.CommentEntity;
import com.annalogue.m2.models.CorrectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CorrectionRepository extends JpaRepository<CorrectionEntity, Long>  {
}
