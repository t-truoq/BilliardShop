package swd.billiardshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swd.billiardshop.entity.Media;

public interface MediaRepository extends JpaRepository<Media, Integer> {
}
