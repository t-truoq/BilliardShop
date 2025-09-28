package swd.billiardshop.service;

import org.springframework.stereotype.Service;
import swd.billiardshop.enums.ShipmentStatus;
import swd.billiardshop.exception.AppException;
import swd.billiardshop.exception.ErrorCode;
import swd.billiardshop.entity.Shipment;

import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import swd.billiardshop.dto.ghn.GHNShippingFeeRequest;
import swd.billiardshop.dto.ghn.GHNShippingFeeResponse;
import swd.billiardshop.dto.ghn.GHNCreateOrderResponse;
import swd.billiardshop.dto.ghn.GHNCreateOrderRequest;
import swd.billiardshop.dto.response.CartItemResponse;
import swd.billiardshop.dto.response.ShipmentResponse;
import swd.billiardshop.entity.Order;
import swd.billiardshop.repository.ShipmentRepository;
import swd.billiardshop.configuration.GHNConfig;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ShipmentService {

    @Autowired
    private GHNClientService ghnClientService;

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private LocationMappingService locationMappingService;

    @Autowired
    private GHNConfig ghnConfig;

    @Autowired
    private swd.billiardshop.repository.OrderItemRepository orderItemRepository;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    public BigDecimal calculateShippingFee(String shippingMethod, swd.billiardshop.entity.Address address, List<CartItemResponse> items) {
        GHNShippingFeeRequest req = new GHNShippingFeeRequest();
        req.setFromDistrictId(ghnConfig.getShopDistrictId());
        req.setFromWardCode(String.valueOf(ghnConfig.getShopWardId()));

        try {
            // mapping to
            // toDistrict/ward must come from location mapping service; fail fast if not mapped
            swd.billiardshop.entity.LocationMapping mapping = locationMappingService.mapAddressToGHN(address);
            if (mapping == null) {
                // throw so callers (e.g. preview) can decide; preview will catch and default to zero
                throw new AppException(ErrorCode.LOCATION_NOT_MAPPED,
                        "Địa chỉ giao hàng chưa được ánh xạ tới mã khu vực GHN. Vui lòng kiểm tra địa chỉ hoặc liên hệ quản trị viên.");
            }

            req.setToDistrictId(mapping.getGhnDistrictId());
            req.setToWardCode(mapping.getGhnWardCode());
            req.setWeight(calculateTotalWeight(items));
            req.setLength(20); req.setWidth(15); req.setHeight(10);
            // Set both service_id and service_type_id for compatibility
            req.setServiceId(getServiceId(shippingMethod));
            req.setServiceTypeId(getServiceTypeId(shippingMethod));

            GHNShippingFeeResponse resp = ghnClientService.calculateShippingFee(req);
            if (resp != null && resp.getTotal() != null) {
                return BigDecimal.valueOf(resp.getTotal());
            }
        } catch (AppException e) {
            // Re-throw AppException để caller có thể handle (ví dụ: preview sẽ catch và return 0)
            throw e;
        } catch (Exception e) {
            log.error("Error calculating shipping fee for address: {}", address, e);
        }

        return BigDecimal.ZERO;
    }

    public String getEstimatedDelivery(String shippingMethod) {
        return ghnClientService != null ? switch (shippingMethod.toLowerCase()) {
            case "ghn_express" -> "1-2 ngày làm việc";
            case "ghn_standard" -> "2-3 ngày làm việc";
            case "ghn_saving" -> "3-5 ngày làm việc";
            default -> "2-5 ngày làm việc";
        } : null;
    }

    public ShipmentResponse createShipment(Order order) {
        // For now create a simple shipment record with order info and return response.
        Shipment s = new Shipment();
        s.setOrder(order);
        s.setCarrier(swd.billiardshop.enums.Carrier.GHN);
        s.setStatus(swd.billiardshop.enums.ShipmentStatus.PENDING);
        s.setPickupAddress(ghnConfig.getShopAddress());
        s.setDeliveryAddress(order.getShippingAddress());
        s.setWeight(BigDecimal.ZERO);
        s.setShippingCost(order.getShippingCost());
        s.setCreatedAt(LocalDateTime.now());
        s.setUpdatedAt(LocalDateTime.now());

        // Try to map address to GHN ids
        swd.billiardshop.entity.Address addr = order.getAddress();
        swd.billiardshop.entity.LocationMapping mapping = null;

        try {
            mapping = locationMappingService.mapAddressToGHN(addr);
        } catch (Exception e) {
            log.warn("Failed to map address to GHN for order {}: {}", order.getOrderNumber(), e.getMessage());
        }

        if (mapping == null) {
            // Persist a pending shipment record and skip calling GHN so order confirmation does not fail.
            s.setCarrierResponse("{\"reason\":\"LOCATION_NOT_MAPPED\", \"message\": \"Address not mapped to GHN (district/ward missing)\"}");
            s = shipmentRepository.save(s);
            return buildShipmentResponse(s);
        }

        // build typed GHNCreateOrderRequest with items/weight
        GHNCreateOrderRequest ghnReq = new GHNCreateOrderRequest();
        ghnReq.setFromName("Billiard Shop");
        ghnReq.setFromPhone(ghnConfig.getShopPhone());
        ghnReq.setFromAddress(ghnConfig.getShopAddress());
        ghnReq.setToName(addr != null ? addr.getRecipientName() : null);
        ghnReq.setToPhone(addr != null ? addr.getPhone() : null);
        if (addr != null) {
            String fullAddr = addr.getAddressLine();
            if (addr.getWard() != null) fullAddr += ", " + addr.getWard();
            if (addr.getDistrict() != null) fullAddr += ", " + addr.getDistrict();
            if (addr.getCity() != null) fullAddr += ", " + addr.getCity();
            ghnReq.setToAddress(fullAddr);
        }
        if (mapping != null) {
            ghnReq.setToDistrictId(mapping.getGhnDistrictId());
            ghnReq.setToWardCode(mapping.getGhnWardCode());
        }
        ghnReq.setPaymentTypeId(1);
        ghnReq.setContent("Order " + order.getOrderNumber());

        // items: translate order items to GHN item objects (use simple maps inside the DTO's items list)
        java.util.List<java.util.Map<String, Object>> items = new java.util.ArrayList<>();
        int totalWeightGrams = 0;
        if (order.getOrderId() != null) {
            java.util.List<swd.billiardshop.entity.OrderItem> orderItems = orderItemRepository.findByOrder(order);
            if (orderItems != null) {
                for (swd.billiardshop.entity.OrderItem oi : orderItems) {
                    java.util.Map<String, Object> it = new java.util.HashMap<>();
                    it.put("name", oi.getProductName());
                    it.put("code", oi.getProductSku());
                    it.put("quantity", oi.getQuantity());
                    it.put("price", oi.getUnitPrice() != null ? oi.getUnitPrice().longValue() : 0L);

                    // per-item weight: prefer product.weight (assumed grams), fallback to 500g
                    int perItemWeight = 500;
                    try {
                        if (oi.getProduct() != null && oi.getProduct().getWeight() != null) {
                            perItemWeight = oi.getProduct().getWeight().intValue();
                        }
                    } catch (Exception ignored) {}
                    it.put("weight", perItemWeight);

                    // dimensions: try to parse product.dimensions like "20x15x10"
                    int len = 20, wid = 15, hei = 10;
                    try {
                        if (oi.getProduct() != null && oi.getProduct().getDimensions() != null) {
                            String dims = oi.getProduct().getDimensions();
                            String[] parts = dims.split("[^0-9]+");
                            java.util.List<Integer> nums = new java.util.ArrayList<>();
                            for (String p : parts) {
                                if (p == null || p.isBlank()) continue;
                                try { nums.add(Integer.parseInt(p)); } catch (Exception ex) {}
                            }
                            if (nums.size() >= 1) len = nums.get(0);
                            if (nums.size() >= 2) wid = nums.get(1);
                            if (nums.size() >= 3) hei = nums.get(2);
                        }
                    } catch (Exception ignored) {}
                    it.put("length", len);
                    it.put("width", wid);
                    it.put("height", hei);

                    items.add(it);
                    totalWeightGrams += oi.getQuantity() * perItemWeight;
                }
            }
        }
        @SuppressWarnings({"unchecked","rawtypes"})
        java.util.List<Object> ghnItems = (java.util.List) items;
        ghnReq.setItems(ghnItems);
        ghnReq.setWeight(totalWeightGrams);
        ghnReq.setLength(20);
        ghnReq.setWidth(15);
        ghnReq.setHeight(10);
    ghnReq.setServiceId(getServiceId(order.getShippingMethod()));
    ghnReq.setServiceTypeId(getServiceTypeId(order.getShippingMethod())); // backup

        // set COD amount when applicable: assume unpaid (PENDING) orders require COD
        try {
            if (order.getPaymentStatus() != null && order.getPaymentStatus() == swd.billiardshop.enums.PaymentStatus.PENDING) {
                if (order.getTotalAmount() != null) ghnReq.setCodAmount(order.getTotalAmount().longValue());
            }
        } catch (Exception ignored) {}

        GHNCreateOrderResponse ghResp = ghnClientService.createShippingOrder(ghnReq);
        if (ghResp != null) {
            // persist GHN info only when API returned values
            if (ghResp.getOrderCode() != null) {
                s.setTrackingNumber(ghResp.getOrderCode());
            }
            if (ghResp.getTotalFee() != null) {
                s.setShippingCost(java.math.BigDecimal.valueOf(ghResp.getTotalFee()));
            }
            if (ghResp.getExpectedDeliveryTime() != null) {
                try {
                    java.time.LocalDateTime dt = java.time.LocalDateTime.parse(ghResp.getExpectedDeliveryTime());
                    s.setEstimatedDelivery(dt);
                } catch (Exception e) {
                    // leave null if not parseable
                }
            }

            // store raw carrier response for audit/debug
            try {
                s.setCarrierResponse(objectToJson(ghResp));
            } catch (Exception ignored) {}
        } else {
            // On GHN failure, record a carrier response explaining the failure and keep trackingNumber null
            try {
                s.setCarrierResponse("{\"reason\":\"GHN_API_FAILED\",\"message\":\"GHN API did not return a successful response\"}");
            } catch (Exception ignored) {}
        }

        s = shipmentRepository.save(s);
        return buildShipmentResponse(s);
    }

    private String objectToJson(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (Exception e) {
            return null;
        }
    }

    public void cancelShipment(Integer orderId) {
        Optional<Shipment> shipmentOpt = shipmentRepository.findByOrderOrderId(orderId);
        if (shipmentOpt.isPresent()) {
            Shipment shipment = shipmentOpt.get();
            shipment.setStatus(swd.billiardshop.enums.ShipmentStatus.RETURNED);
            shipment.setUpdatedAt(LocalDateTime.now());
            shipmentRepository.save(shipment);
        }
    }

    public ShipmentResponse getShipmentByOrderId(Integer orderId) {
        return shipmentRepository.findByOrderOrderId(orderId)
                .map(this::buildShipmentResponse)
                .orElse(null);
    }

    public ShipmentResponse trackShipment(String trackingNumber) {
        return shipmentRepository.findByTrackingNumber(trackingNumber)
                .map(this::buildShipmentResponse)
                .orElse(null);
    }

    private ShipmentResponse buildShipmentResponse(Shipment shipment) {
        ShipmentResponse r = new ShipmentResponse();
        r.setTrackingCode(shipment.getTrackingNumber());
        r.setCarrier(shipment.getCarrier() != null ? shipment.getCarrier().name() : null);
        r.setStatus(shipment.getStatus() != null ? shipment.getStatus().name() : null);
        r.setShippedAt(shipment.getShippedAt());
        return r;
    }

    private int calculateTotalWeight(List<CartItemResponse> items) {
        return items.stream().mapToInt(ci -> ci.getQuantity() * 500).sum();
    }

    private Integer getServiceTypeId(String shippingMethod) {
        if (shippingMethod == null) return 1;
        return switch (shippingMethod.toLowerCase()) {
            case "ghn_express" -> 2;
            case "ghn_saving" -> 3;
            default -> 1;
        };
    }

    private Integer getServiceId(String shippingMethod) {
        if (shippingMethod == null) return 53320; // GHN standard service id
        return switch (shippingMethod.toLowerCase()) {
            case "ghn_express" -> 53321;
            case "ghn_saving" -> 53319;
            default -> 53320;
        };
    }

    public ShipmentResponse updateShipmentFromGHN(String trackingNumber) {
        Optional<Shipment> shipmentOpt = shipmentRepository.findByTrackingNumber(trackingNumber);

        if (shipmentOpt.isEmpty()) {
            log.warn("Shipment not found with tracking number: {}", trackingNumber);
            return null;
        }

        Shipment shipment = shipmentOpt.get();

        // Get latest info from GHN
        GHNClientService.GHNOrderDetailResponse ghnResponse = ghnClientService.getOrderDetail(trackingNumber);

        if (ghnResponse != null && ghnResponse.getData() != null) {
            GHNClientService.GHNOrderDetailResponse.GHNOrderData orderData = ghnResponse.getData();

            // Update shipment status based on GHN status
            ShipmentStatus newStatus = mapGHNStatusToShipmentStatus(orderData.getStatus());
            if (newStatus != null) {
                shipment.setStatus(newStatus);
            }

            // Update other fields if available
            if (orderData.getPickTime() != null && !orderData.getPickTime().isEmpty()) {
                try {
                    // Parse GHN datetime format (adjust pattern as needed)
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    shipment.setShippedAt(LocalDateTime.parse(orderData.getPickTime(), formatter));
                } catch (Exception e) {
                    log.warn("Could not parse pick_time: {}", orderData.getPickTime(), e);
                }
            }

            if (orderData.getDeliverTime() != null && !orderData.getDeliverTime().isEmpty()) {
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    shipment.setShippedAt(LocalDateTime.parse(orderData.getDeliverTime(), formatter));
                } catch (Exception e) {
                    log.warn("Could not parse deliver_time: {}", orderData.getDeliverTime(), e);
                }
            }

            shipment.setUpdatedAt(LocalDateTime.now());
            shipment = shipmentRepository.save(shipment);

            log.info("Updated shipment {} from GHN with status: {}", trackingNumber, orderData.getStatus());
        }

        return buildShipmentResponse(shipment);
    }

    private ShipmentStatus mapGHNStatusToShipmentStatus(String ghnStatus) {
        if (ghnStatus == null) return null;

        return switch (ghnStatus.toLowerCase()) {
            case "ready_to_pick", "picking" -> ShipmentStatus.PENDING;
            case "picked" -> ShipmentStatus.PICKED_UP;
            case "storing", "transporting" -> ShipmentStatus.IN_TRANSIT;
            case "sorting", "delivering" -> ShipmentStatus.OUT_FOR_DELIVERY;
            case "delivered" -> ShipmentStatus.DELIVERED;
            case "return", "return_transporting", "return_sorting", "returned" -> ShipmentStatus.RETURNED;
            case "exception", "damage" -> ShipmentStatus.EXCEPTION;
            case "lost" -> ShipmentStatus.LOST;
            case "cancel" -> ShipmentStatus.CANCELLED;
            case "failed" -> ShipmentStatus.FAILED;
            default -> null;
        };

    }
}