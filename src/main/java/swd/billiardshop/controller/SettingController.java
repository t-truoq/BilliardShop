package swd.billiardshop.controller;

import org.springframework.web.bind.annotation.*;
import swd.billiardshop.entity.Setting;
import swd.billiardshop.service.SettingService;
import java.util.List;

@RestController
@RequestMapping("/settings")
public class SettingController {
    private final SettingService settingService;

    public SettingController(SettingService settingService) {
        this.settingService = settingService;
    }

    @GetMapping
    public List<Setting> getAllSettings() {
        return settingService.getAllSettings();
    }
}
