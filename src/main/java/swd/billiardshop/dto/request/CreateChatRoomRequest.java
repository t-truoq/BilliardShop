package swd.billiardshop.dto.request;

public class CreateChatRoomRequest {
    private Integer customerId;
    private Integer staffId;

    // Getters and setters
    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }
    public Integer getStaffId() { return staffId; }
    public void setStaffId(Integer staffId) { this.staffId = staffId; }
}