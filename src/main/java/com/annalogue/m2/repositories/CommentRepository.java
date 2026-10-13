package com.annalogue.m2.repositories;

import com.annalogue.m2.models.CommentEntity;
import com.annalogue.m2.models.PublicationEntity;
import com.annalogue.m2.models.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Long> {

    public UserEntity findByUserId(long userId);
    public UserEntity findByUserEntity(UserEntity userEntity);

    public PublicationEntity findByPublicationId(long publicationId);
    public PublicationEntity findByPublicationEntity(long publicationEntity);
}
