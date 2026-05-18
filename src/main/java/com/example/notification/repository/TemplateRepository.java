package com.example.notification.repository;

import com.example.notification.domain.entity.Template;
import com.example.notification.domain.enums.Channel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TemplateRepository extends JpaRepository<Template, UUID> {
    Optional<Template> findByNameAndChannel(String name, Channel channel);
}
