package com.technicalchallenge.service.validation;

import com.technicalchallenge.dto.TradeDTO;
import com.technicalchallenge.model.ApplicationUser;
import com.technicalchallenge.model.UserProfile;
import com.technicalchallenge.repository.ApplicationUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserPrivilegeValidatorTest {

    @Mock
    ApplicationUserRepository applicationUserRepository;

    @InjectMocks
    UserPrivilegeValidator userPrivilegeValidator;

    private TradeDTO tradeDTO;
    private ApplicationUser user;
    private UserProfile userProfile;

    @BeforeEach
    void setUp() {
        tradeDTO = new TradeDTO();
        user = new ApplicationUser();
    }

    @Test
    @DisplayName("Null userID should return false when privileges are validated")
    void testNullUserIdReturnsFalse() {

        boolean result = userPrivilegeValidator.validateUserPrivileges(null, "CREATE", tradeDTO);
        assertFalse(result);
        verify(applicationUserRepository, never()).findByLoginId(anyString());
    }

    @Test
    @DisplayName("Null operation should return false when privileges are validated")
    void testNullOperationReturnsFalse() {

        boolean result = userPrivilegeValidator.validateUserPrivileges("joey", null, tradeDTO);
        assertFalse(result);
        verify(applicationUserRepository, never()).findByLoginId(anyString());
    }

    @Test
    @DisplayName("Null TradeDTO should return false when privileges are validated")
    void testNullTradeDTOReturnsFalse() {

        boolean result = userPrivilegeValidator.validateUserPrivileges("joey", "CREATE", null);
        assertFalse(result);
        verify(applicationUserRepository, never()).findByLoginId(anyString());
    }

    @Test
    @DisplayName("SUPERUSER has all privileges")
    void testSuperuserAllOperationsReturnsTrue() {

        // Given
        userProfile = new UserProfile();
        userProfile.setUserType("SUPERUSER");
        user.setUserProfile(userProfile);
        when(applicationUserRepository.findByLoginId("SU_Sam")).thenReturn(Optional.of(user));

        // When
        boolean createResult = userPrivilegeValidator.validateUserPrivileges("SU_Sam", "CREATE", tradeDTO);
        boolean amendResult = userPrivilegeValidator.validateUserPrivileges("SU_Sam", "AMEND", tradeDTO);
        boolean viewResult = userPrivilegeValidator.validateUserPrivileges("SU_Sam", "VIEW", tradeDTO);
        boolean deleteResult = userPrivilegeValidator.validateUserPrivileges("SU_Sam", "DELETE", tradeDTO);

        // Then
        assertTrue(createResult);
        assertTrue(amendResult);
        assertTrue(viewResult);
        assertTrue(deleteResult);
    }

    @Test
    @DisplayName("TRADER_SALES has privileges for their trades")
    void testTraderSalesOperationsReturnsTrue() {

        // Given
        userProfile = new UserProfile();
        userProfile.setUserType("TRADER_SALES");
        user.setUserProfile(userProfile);
        when(applicationUserRepository.findByLoginId("TS_Tom")).thenReturn(Optional.of(user));
        tradeDTO.setTraderUserName("TS_Tom");

        // When
        boolean createResult = userPrivilegeValidator.validateUserPrivileges("TS_Tom", "CREATE", tradeDTO);
        boolean amendResult = userPrivilegeValidator.validateUserPrivileges("TS_Tom", "AMEND", tradeDTO);
        boolean viewResult = userPrivilegeValidator.validateUserPrivileges("TS_Tom", "VIEW", tradeDTO);
        boolean deleteResult = userPrivilegeValidator.validateUserPrivileges("TS_Tom", "DELETE", tradeDTO);

        // Then
        assertTrue(createResult);
        assertTrue(amendResult);
        assertTrue(viewResult);
        assertTrue(deleteResult);
    }

    @Test
    @DisplayName("TRADER_SALES does not has privileges for trades owned by another trader")
    void testTraderSalesOperationsReturnsFalse() {

        // Given
        userProfile = new UserProfile();
        userProfile.setUserType("TRADER_SALES");
        user.setUserProfile(userProfile);
        when(applicationUserRepository.findByLoginId("TS_Tom")).thenReturn(Optional.of(user));
        tradeDTO.setTraderUserName("TS_Joey"); // trade owned by another trader

        // When
        boolean createResult = userPrivilegeValidator.validateUserPrivileges("TS_Tom", "CREATE", tradeDTO);
        boolean amendResult = userPrivilegeValidator.validateUserPrivileges("TS_Tom", "AMEND", tradeDTO);
        boolean viewResult = userPrivilegeValidator.validateUserPrivileges("TS_Tom", "VIEW", tradeDTO);
        boolean deleteResult = userPrivilegeValidator.validateUserPrivileges("TS_Tom", "DELETE", tradeDTO);

        // Then
        assertFalse(createResult);
        assertFalse(amendResult);
        assertFalse(viewResult);
        assertFalse(deleteResult);
    }

    @Test
    @DisplayName("MIDDLE_OFFICE can only amend and view trades")
    void testMiddleOfficeOperationsOnlyAmendAndViewReturnsTrue() {

        // Given
        userProfile = new UserProfile();
        userProfile.setUserType("MO");
        user.setUserProfile(userProfile);
        when(applicationUserRepository.findByLoginId("MO_Molly")).thenReturn(Optional.of(user));
        tradeDTO.setTraderUserName("TS_Joey");

        // When
        boolean createResult = userPrivilegeValidator.validateUserPrivileges("MO_Molly", "CREATE", tradeDTO);
        boolean amendResult = userPrivilegeValidator.validateUserPrivileges("MO_Molly", "AMEND", tradeDTO);
        boolean viewResult = userPrivilegeValidator.validateUserPrivileges("MO_Molly", "VIEW", tradeDTO);
        boolean deleteResult = userPrivilegeValidator.validateUserPrivileges("Mo_Molly", "DELETE", tradeDTO);

        // Then
        assertFalse(createResult);
        assertTrue(amendResult);
        assertTrue(viewResult);
        assertFalse(deleteResult);
    }

    @Test
    @DisplayName("SUPPORT can only view trades")
    void testSupportOnlyViewReturnsTrue() {

        // Given
        userProfile = new UserProfile();
        userProfile.setUserType("SUPPORT");
        user.setUserProfile(userProfile);
        when(applicationUserRepository.findByLoginId("ST_Sue")).thenReturn(Optional.of(user));
        tradeDTO.setTraderUserName("TS_Joey");

        // When
        boolean createResult = userPrivilegeValidator.validateUserPrivileges("ST_Sue", "CREATE", tradeDTO);
        boolean amendResult = userPrivilegeValidator.validateUserPrivileges("ST_Sue", "AMEND", tradeDTO);
        boolean viewResult = userPrivilegeValidator.validateUserPrivileges("ST_Sue", "VIEW", tradeDTO);
        boolean deleteResult = userPrivilegeValidator.validateUserPrivileges("ST_Sue", "DELETE", tradeDTO);

        // Then
        assertFalse(createResult);
        assertFalse(amendResult);
        assertTrue(viewResult);
        assertFalse(deleteResult);
    }

    @Test
    @DisplayName("OTHER roles cannot act on trades")
    void testOtherRoleReturnsFalse() {

        // Given
        userProfile = new UserProfile();
        userProfile.setUserType("OTHER");
        user.setUserProfile(userProfile);
        when(applicationUserRepository.findByLoginId("OR_Oliver")).thenReturn(Optional.of(user));
        tradeDTO.setTraderUserName("TS_Joey");

        // When
        boolean createResult = userPrivilegeValidator.validateUserPrivileges("OR_Oliver", "CREATE", tradeDTO);
        boolean amendResult = userPrivilegeValidator.validateUserPrivileges("OR_Oliver", "AMEND", tradeDTO);
        boolean viewResult = userPrivilegeValidator.validateUserPrivileges("OR_Oliver", "VIEW", tradeDTO);
        boolean deleteResult = userPrivilegeValidator.validateUserPrivileges("OR_Oliver", "DELETE", tradeDTO);

        // Then
        assertFalse(createResult);
        assertFalse(amendResult);
        assertFalse(viewResult);
        assertFalse(deleteResult);
    }

}
