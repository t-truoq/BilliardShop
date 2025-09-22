package swd.billiardshop.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import swd.billiardshop.dto.request.AddressRequest;
import swd.billiardshop.dto.response.AddressResponse;
import swd.billiardshop.entity.User;
import swd.billiardshop.service.AddressService;
import swd.billiardshop.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/user/addresses")
@RequiredArgsConstructor
@Tag(name = "Address Management", description = "APIs for managing user addresses")
@SecurityRequirement(name = "bearerAuth")
public class AddressController {
    private final AddressService addressService;
    private final UserService userService;

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) throw new RuntimeException("Unauthenticated");
        return userService.getUserEntityByUsername(auth.getName());
    }

    @GetMapping
    @Operation(
            summary = "Get user addresses",
            description = "Retrieve all addresses for the currently authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved addresses",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AddressResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthenticated - User not logged in",
                    content = @Content
            )
    })
    public ResponseEntity<List<AddressResponse>> listForCurrentUser() {
        User user = currentUser();
        return ResponseEntity.ok(addressService.getAddressesForUser(user));
    }

    @PostMapping
    @Operation(
            summary = "Create new address",
            description = "Create a new address for the currently authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Address created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AddressResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthenticated - User not logged in",
                    content = @Content
            )
    })
    public ResponseEntity<AddressResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Address details to create",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AddressRequest.class)
                    )
            )
            @RequestBody AddressRequest req
    ) {
        User user = currentUser();
        return ResponseEntity.ok(addressService.createAddress(user, req));
    }

    @PutMapping("/{addressId}")
    @Operation(
            summary = "Update address",
            description = "Update an existing address for the currently authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Address updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AddressResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthenticated - User not logged in",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Address not found or not belongs to user",
                    content = @Content
            )
    })
    public ResponseEntity<AddressResponse> update(
            @Parameter(
                    description = "ID of the address to update",
                    required = true,
                    example = "1"
            )
            @PathVariable Integer addressId,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated address details",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AddressRequest.class)
                    )
            )
            @RequestBody AddressRequest req
    ) {
        User user = currentUser();
        return ResponseEntity.ok(addressService.updateAddress(user, addressId, req));
    }

    @DeleteMapping("/{addressId}")
    @Operation(
            summary = "Delete address",
            description = "Delete an existing address for the currently authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Address deleted successfully",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthenticated - User not logged in",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Address not found or not belongs to user",
                    content = @Content
            )
    })
    public ResponseEntity<Void> delete(
            @Parameter(
                    description = "ID of the address to delete",
                    required = true,
                    example = "1"
            )
            @PathVariable Integer addressId
    ) {
        User user = currentUser();
        addressService.deleteAddress(user, addressId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{addressId}/set-default")
    @Operation(
            summary = "Set default address",
            description = "Set an existing address as the default address for the currently authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Default address set successfully",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthenticated - User not logged in",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Address not found or not belongs to user",
                    content = @Content
            )
    })
    public ResponseEntity<Void> setDefault(
            @Parameter(
                    description = "ID of the address to set as default",
                    required = true,
                    example = "1"
            )
            @PathVariable Integer addressId
    ) {
        User user = currentUser();
        addressService.setDefaultAddress(user, addressId);
        return ResponseEntity.ok().build();
    }
}