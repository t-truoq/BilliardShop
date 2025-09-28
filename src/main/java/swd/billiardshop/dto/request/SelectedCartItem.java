package swd.billiardshop.dto.request;

import lombok.Data;

@Data
public class SelectedCartItem {
    private Integer cartItemId;
    private Integer quantity; // requested quantity
}
