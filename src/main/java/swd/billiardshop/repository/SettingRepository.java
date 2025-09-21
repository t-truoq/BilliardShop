package swd.billiardshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swd.billiardshop.entity.Setting;

public interface SettingRepository extends JpaRepository<Setting, Integer> {
}
