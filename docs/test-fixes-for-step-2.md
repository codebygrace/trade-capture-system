# Test Fixes

## Summary 

14 test failures were discovered in the following tests:

| Test                   | Failures |
|------------------------|----------|
| BookServiceTest        | 3        |
| TradeControllerTest    | 6        |
| TradeLegControllerTest | 1        |
| TradeServiceTest       | 4        |


## Details of Test Fixes

### BookServiceTest

#### Fixed NullPointerException in testFindBookById()
- **Problem:** `testFindBookById()` was throwing a NullPointerException
- **Root Cause:** `BookService.getBookById()` depends on `BookMapper` and this was not mocked
- **Solution:** Added a mock for `BookMapper` using `@Mock` annotation and added `when(bookMapper.toDto(book)).thenReturn(bookDTO)`
- **Impact:** Enables proper verification of expected behaviour when a Book is found

#### Fixed NullPointerException in testFindBookByNonExistentId()
- **Problem:** `testFindBookByNonExistentId()` was throwing a NullPointerException
- **Root Cause:** `BookService.getBookById()` depends on `BookMapper` and this was not mocked
- **Solution:** Added a mock for `BookMapper` using `@Mock` annotation
- **Impact:** Enables proper verification of expected behaviour when a Book is not found

#### Fixed NullPointerException in testSaveBook()
- **Problem:** `testSaveBook()` was throwing a NullPointerException
- **Root Cause:** `BookService.saveBook()` depends on `BookMapper` and this was not mocked. Also, `BookService.saveBook()` calls the method `populateReferenceDataByName()` which depends on `CostCenterRepository` and this was not mocked and `BookDTO` test data was missing `costCenterName`
- **Solution:** Added the following mocks, data and stubs:
  1. a mock for `BookMapper` using `@Mock` annotation
  2. a mock for `CostCenterRepository` using `@Mock` annotation
  3. CostCentre object and `bookDTO.setCostCenterName("Cost Center")`
  4. `when(bookMapper.toEntity(bookDTO)).thenReturn(book)`
  5. `when(costCenterRepository.findAll()).thenReturn(List.of(costCenter))`
  6. `when(bookMapper.toDto(book)).thenReturn(bookDTO)`
- **Impact:** Enables proper verification of expected behaviour when a Book is saved

### TradeControllerTest

#### Corrected expected status in testCreateTrade()
- **Problem:** `testCreateTrade()` threw an AssertionError for status value. Expected:`200`, Actual:`201`
- **Root Cause:** Incorrect expected status used in assertion. It should be `201` as defined in TradeController
- **Solution:** Changed `.andExpect(status().isOk())` to `.andExpect(status().isCreated())`
- **Impact:** Enables proper verification of expected behaviour which is returning 201 status when a trade is created

#### Fixed createTrade book validation
- **Problem:** `testCreateTradeValidationFailure_MissingBook()` was failing. Expected:`400`, Actual:`201`
- **Root Cause:** `bookName` in `TradeDTO` was missing `@NotNull` annotation
- **Solution:** Added `@NotNull` to `bookName` in `TradeDTO` and implemented `GlobalExceptionHandler.handleMethodArgumentNotValidException()` to return `400` and error message, and updated `testCreateTradeValidationFailure_MissingBook()` with `.andExpect(jsonPath("$.bookName", is("Book name is required")))`
- **Impact:** Trades without a book are now correctly reject, returning 400 status and error message when a trade is created without a book

#### Fixed testCreateTradeValidationFailure_MissingTradeDate()
- **Problem:** `testCreateTradeValidationFailure_MissingTradeDate()` was failing due to an assertion error
- **Root Cause:** `.andExpect(content().string("Trade date is required"))` was incorrect
- **Solution:** Updated `testCreateTradeValidationFailure_MissingTradeDate()` with `.andExpect(jsonPath("$.tradeDate", is("Trade date is required")))`. Also, in previous commit, implemented `GlobalExceptionHandler.handleMethodArgumentNotValidException()` to return `400` and error message
- **Impact:** Trades without a trade date are now correctly reject, returning `400` status and error message when trade date is missing

#### Corrected expected status in testDeleteTrade()
- **Problem:** `testDeleteTrade()` threw an AssertionError for status value. Expected:`204`, Actual:`200`
- **Root Cause:** Incorrect expected status used in assertion. It should be `200` as defined in `TradeController`
- **Solution:** Changed `.andExpect(status().isNoContent())` to `.andExpect(status().isOk())`
- **Impact:** Enables proper verification of expected behaviour which is returning `200` status when a trade is deleted


#### Corrected mock service method for testUpdateTrade()
- **Problem:** `testUpdateTrade()` was returning a null response body
- **Root Cause:** The test mocked the wrong service method - `tradeService.saveTrade(any(Trade.class), any(TradeDTO.class))`
- **Solution:** Updated mocked service method to `amendTrade(any(Long.class), any(TradeDTO.class))`
- **Impact:** Enables proper verification of expected behaviour when trade is updated which is returning 200 status and trade

#### Fixed TradeIdMismatch
- **Problem:** `testUpdateTradeIdMismatch()` failed with an AssertionError for status. Expected:`400`, Actual:`200`
- **Root Cause:** `TradeController.updateTrade()` was missing validation to check that path id matches `tradeDTO.getTradeId()`
- **Solution:** added a validation check, `if(!Objects.equals(id, tradeDTO.getTradeId())){...}`, to `TradeController.updateTrade()`
- **Impact:** Requests where path id does not match tradeId in request body are now correctly rejected, returning 400 status and error message

### TradeLegControllerTest

#### Fixed NegativeNotional() validation
- **Problem:** `testCreateTradeLegValidationFailure_NegativeNotional()` was returning null response body and assertion failed. Response content expected:`<Notional must be positive>` but was:`<>`
- **Root Cause:** incorrect jsonPath matcher
- **Solution:** Updated test with `.andExpect(jsonPath("$.notional", is("Notional must be positive")))`
- **Impact:** Enables proper verification of expected behaviour when notional is negative which is `400` response with the expected error message

### Trade Service

#### Corrected expected exception message in testCreateTrade_InvalidDates
- **Problem:** `testCreateTrade_InvalidDates_ShouldFail()` was failing as the assertion was using the wrong error message
- **Root Cause:** The expected exception message is `"Start date cannot be before trade date"`
- **Solution:** Changed assertion to expect `"Start date cannot be before trade date"`
- **Impact:** Ensures the test validates the correct exception message

#### Invoked private generateCashflows for testCashflowGeneration_MonthlySchedule()
- **Problem:** `testCashflowGeneration_MonthlySchedule()` failed due to assertion error and missing `TradeService.generateCashflows()` execution
- **Root Cause:** `TradeService.generateCashflows()` is a private void method which the test couldn’t call and test data was missing schedule data
- **Solution:**
  1. Created `monthlySchedule` with `schedule = “1M”`
  2. Set the schedule on the leg - `leg.setCalculationPeriodSchedule(monthlySchedule)`
  3. Accessed the private method via reflection:
      - `Method generateCashflowsMethod = TradeService.class.getDeclaredMethod("generateCashflows", TradeLeg.class, LocalDate.class,LocalDate.class)`
      - `generateCashflowsMethod.setAccessible(true)`
  4. Invoked the method `generateCashflowsMethod.invoke(tradeService,leg,tradeDTO.getTradeStartDate(),tradeDTO.getTradeMaturityDate())`
  5. Replace previous assertion with `verify(cashflowRepository, times(12)).save(any(Cashflow.class))`
- **Impact:** Enables proper verification of cashflow generation for a monthly schedule in isolation

#### Stubbed TradeLegRepository for testAmendTrade_Success
- **Problem:** `testAmendTrade_Success()` failed due missing stub and a null trade version
- **Root Cause:** The trade version field was null and the required `TradeLegRepository.save()` was not stubbed.
- **Solution:**
  1. Set trade version - `trade.setVersion(1)`;
  2. Added `when(tradeLegRepository.save(any(TradeLeg.class))).thenReturn(new TradeLeg())`
- **Impact:** Enables verification that a trade is amended successfully when a versioned trade is received

#### Mocked missing repositories for testCreateTrade_Success
- **Problem:** `testCreateTrade_Success()` failed due required repositories not being mocked and the TradeDTO having null fields that were required
- **Root Cause:** `createTrade()` calls private methods `populateReferenceDataByName()` and `createTradeLegsWithCashflows()` which depend on `BookRepository` and `CounterpartyRepository`, `TradeStatusRepository` and `TradeLegRepository`. Also, `tradeDTO` was missing `bookName`, `counterpartyName` and `tradeStatus` which are required for trade creation
- **Solution:**
  1. Added mocks for `BookRepository` and `CounterpartyRepository` using `@Mock` annotation.
  2. Set `bookName`, `counterpartyName` and `tradeStatus` on `tradeDTO`.
  3. Added `when(bookRepository.findByBookName(anyString())).thenReturn(Optional.of(new Book()));`
  4. Added `when(counterpartyRepository.findByName(anyString())).thenReturn(Optional.of(new Counterparty()));`
  5. Added `when(tradeStatusRepository.findByTradeStatus(anyString())).thenReturn(Optional.of(tradeStatus));`
  6. Added `when(tradeLegRepository.save(any(TradeLeg.class))).thenReturn(new TradeLeg());`
- **Impact:** Enables verification that a trade is created successfully when all data is present