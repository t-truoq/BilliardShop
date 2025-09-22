package swd.billiardshop.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swd.billiardshop.dto.request.AddressRequest;
import swd.billiardshop.dto.response.AddressResponse;
import swd.billiardshop.entity.Address;
import swd.billiardshop.entity.User;
import swd.billiardshop.exception.AppException;
import swd.billiardshop.exception.ErrorCode;
import swd.billiardshop.repository.AddressRepository;
import swd.billiardshop.repository.OrderRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressService {
    private final AddressRepository addressRepository;
    private final OrderRepository orderRepository;

    public List<AddressResponse> getAddressesForUser(User user) {
        return addressRepository.findByUser(user)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AddressResponse createAddress(User user, AddressRequest req) {
        // Prevent creating exact duplicate address for the same user
        boolean exists = addressRepository.existsByUserAndAddressLineAndWardAndDistrictAndCityAndProvince(
                user,
                req.getAddressLine(),
                req.getWard(),
                req.getDistrict(),
                req.getCity(),
                req.getProvince()
        );
        if (exists) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Duplicate address for user");
        }
        if (Boolean.TRUE.equals(req.getIsDefault())) {
            addressRepository.clearDefaultForUser(user);
        }
        Address a = Address.builder()
                .user(user)
                .recipientName(req.getRecipientName())
                .phone(req.getPhone())
                .addressLine(req.getAddressLine())
                .ward(req.getWard())
                .district(req.getDistrict())
                .city(req.getCity())
                .province(req.getProvince())
                .postalCode(req.getPostalCode())
                .isDefault(Boolean.TRUE.equals(req.getIsDefault()))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        Address saved = addressRepository.save(a);
        return toResponse(saved);
    }

    @Transactional
    public AddressResponse updateAddress(User user, Integer addressId, AddressRequest req) {
        Address a = addressRepository.findById(addressId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Address not found"));
        if (!a.getUser().getUserId().equals(user.getUserId())) {
            throw new AppException(ErrorCode.FORBIDDEN, "Not allowed to modify this address");
        }
    // Prevent updating to an address that duplicates another of the user's addresses
    java.util.Optional<Address> same = addressRepository.findFirstByUserAndAddressLineAndWardAndDistrictAndCityAndProvince(
            user,
            req.getAddressLine(),
            req.getWard(),
            req.getDistrict(),
            req.getCity(),
            req.getProvince()
    );
    if (same.isPresent() && !same.get().getAddressId().equals(a.getAddressId())) {
        throw new AppException(ErrorCode.INVALID_REQUEST, "Duplicate address for user");
    }
        if (Boolean.TRUE.equals(req.getIsDefault())) {
            addressRepository.clearDefaultForUser(user);
            a.setIsDefault(true);
        } else if (req.getIsDefault() != null) {
            a.setIsDefault(false);
        }
        a.setRecipientName(req.getRecipientName());
        a.setPhone(req.getPhone());
        a.setAddressLine(req.getAddressLine());
        a.setWard(req.getWard());
        a.setDistrict(req.getDistrict());
        a.setCity(req.getCity());
        a.setProvince(req.getProvince());
        a.setPostalCode(req.getPostalCode());
        a.setUpdatedAt(LocalDateTime.now());
        Address saved = addressRepository.save(a);
        return toResponse(saved);
    }

    @Transactional
    public void deleteAddress(User user, Integer addressId) {
        Address a = addressRepository.findById(addressId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Address not found"));
        if (!a.getUser().getUserId().equals(user.getUserId())) {
            throw new AppException(ErrorCode.FORBIDDEN, "Not allowed to delete this address");
        }
    // Prevent deletion if used in any order referencing the address id
    boolean used = orderRepository.existsByAddress_AddressIdAndUser(a.getAddressId(), user);
        if (used) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Address is used in existing orders and cannot be deleted");
        }
        addressRepository.delete(a);
    }

    @Transactional
    public void setDefaultAddress(User user, Integer addressId) {
        Address a = addressRepository.findById(addressId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Address not found"));
        if (!a.getUser().getUserId().equals(user.getUserId())) {
            throw new AppException(ErrorCode.FORBIDDEN, "Not allowed to modify this address");
        }
        addressRepository.clearDefaultForUser(user);
        a.setIsDefault(true);
        a.setUpdatedAt(LocalDateTime.now());
        addressRepository.save(a);
    }

    // Address component validation is handled by the frontend. Duplicate prevention is enforced here.

    private AddressResponse toResponse(Address a) {
        AddressResponse r = new AddressResponse();
        r.setAddressId(a.getAddressId());
        r.setRecipientName(a.getRecipientName());
        r.setPhone(a.getPhone());
        r.setAddressLine(a.getAddressLine());
        r.setWard(a.getWard());
        r.setDistrict(a.getDistrict());
        r.setCity(a.getCity());
        r.setProvince(a.getProvince());
        r.setPostalCode(a.getPostalCode());
        r.setIsDefault(a.getIsDefault());
        r.setCreatedAt(a.getCreatedAt());
        r.setUpdatedAt(a.getUpdatedAt());
        return r;
    }
}
