package swd.billiardshop.dto.request;

import lombok.Data;
import swd.billiardshop.enums.Role;

@Data
public class AdminChangeRoleRequest {
    private Role role;
}
