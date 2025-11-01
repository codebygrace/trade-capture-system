package com.technicalchallenge.service.validation;

import com.technicalchallenge.dto.TradeDTO;
import com.technicalchallenge.model.ApplicationUser;
import com.technicalchallenge.repository.ApplicationUserRepository;
import org.springframework.stereotype.Service;

/**
 * Service that validates whether a given user is allowed to perform a specific operation on a {@link TradeDTO}.
 * The validation logic is based on the user’s profile type and, for TRADER_SALES, on the relationship between the user
 * and the trade data.
 */
@Service
public class UserPrivilegeValidator {

    private final ApplicationUserRepository applicationUserRepository;

    public UserPrivilegeValidator(ApplicationUserRepository applicationUserRepository) {
        this.applicationUserRepository = applicationUserRepository;
    }

    /**
     * Method to check whether a given user has the right privileges to act on a given trade
     * @param userId loginId of the user whose privileges are being checked
     * @param operation action being attempted by the user on the trade
     * @param tradeDTO trade data involved in the operation
     * @return true if the user is authorised to carry out the given operation and false otherwise
     */
    public boolean validateUserPrivileges(String userId, String operation, TradeDTO tradeDTO) {

        // checks for null conducted first to prevent NullPointExceptions occurring

        if (userId == null || operation == null  || tradeDTO == null)  {
            return false;
        }

        ApplicationUser user = applicationUserRepository.findByLoginId(userId).orElse(null);

        if (user == null || user.getUserProfile() == null || user.getUserProfile().getUserType() == null ) {
            return false;
        }

        String userType = user.getUserProfile().getUserType();

        // equalsIgnoreCase has been used for the following IF statements to account for any case variations that may occur in userId

        // SUPERUSER has full system access
        if (userType.equalsIgnoreCase("SUPERUSER")) {
                return true;
        }

        // TRADER_SALES only has full access to trades they're responsible for so traderUserName must match userId
        if (tradeDTO.getTraderUserName() == null ) {
            return false;
        }
        if (userType.equalsIgnoreCase("TRADER_SALES") && tradeDTO.getTraderUserName().equalsIgnoreCase(userId)) {
            return true;
        }

        // MIDDLE_OFFICE can only view and amend trades
        if (userType.equalsIgnoreCase("MO")) {
            return operation.equalsIgnoreCase("AMEND") || operation.equalsIgnoreCase("VIEW");
        }

        // SUPPORT can only view trades
        if (userType.equalsIgnoreCase("SUPPORT")) {
            return operation.equalsIgnoreCase("VIEW");
        }

        // Prevents actions by any other type of user outside the given scope
        return false;
    }
}
