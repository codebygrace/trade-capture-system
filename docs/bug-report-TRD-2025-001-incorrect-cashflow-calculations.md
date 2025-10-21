# Bug Report: TRD-2025-001 - Incorrect cashflow values being calculated

## Bug Details
**Bug ID**: TRD-2025-001  
**Severity**: High


## Executive Summary

Incorrect payment values are being displayed when fixed-leg cashflows are generated. Cashflow amounts are approx. 100 times larger than expected (e.g. a \$10M trade with 3.5% rate is generating ~\$875,000 quarterly instead of ~\$87,500).
This poses operational risks, and can result in inaccurate regulatory reporting and potential regulatory penalties as the cashflows are highly inflated. Also, varying payment amounts are being displayed for each payment date on legs (e.g. a \$10M trade with 3.5% rate has payment values \$875,000, \$884,722.22 and \$894,444.44 for different dates on the same leg).


## Technical Investigation

To investigate, I reproduced the bug in a dev environment by following these steps:

1. Started both backend and frontend applications
2. Logged in as a user with the TRADER_SALES role
3. Went to Trade Actions
4. Created a trade 
5. On Leg 1, set Fixed Rate value to 3.5
6. Clicked the Cashflows button and saved the trade
7. Observed the amounts displayed on the UI and saved to the database

I reviewed the `calculateCashflowValue` method in `TradeService.java` where I examined the following code related to the bug:

```java

private BigDecimal calculateCashflowValue(TradeLeg leg, int monthsInterval) {
    if (leg.getLegRateType() == null) {
        return BigDecimal.ZERO;
    }

    String legType = leg.getLegRateType().getType();
    
    if ("Fixed".equals(legType)) {
        double notional = leg.getNotional().doubleValue();
        double rate = leg.getRate();
        double months = monthsInterval;

        double result = (notional * rate * months) / 12;  // This calculation has been identified as causing the ~100x inflated values

        return BigDecimal.valueOf(result);
    } else if ("Floating".equals(legType)) {
        return BigDecimal.ZERO;
    }

    return BigDecimal.ZERO;
}
```

I also reviewed the `generateCashflows` method in `CashflowController.java` as this controls cashflow values displayed in UI when the `Cashflows` button is clicked

```java
if ("Fixed".equalsIgnoreCase(leg.getLegType())) {
    long days = java.time.temporal.ChronoUnit.DAYS.between(valueDate, nextValueDate); // calculation uses days instead of months
    double rate = leg.getRate() != null ? leg.getRate() : 0.0; // percentage rate has not been converted
    paymentValue = leg.getNotional().multiply(BigDecimal.valueOf(rate)).multiply(BigDecimal.valueOf(days)).divide(BigDecimal.valueOf(360), 2, BigDecimal.ROUND_HALF_UP);
}
```


## Root Cause Details
The following causes have been identified:

**TradeService**
1. In `calculateCashflowValue`, `leg.getRate()` retrieves the percentage rate value (e.g. `3.5`) which is not being converted to its equivalent decimal value (e.g. `0.035`), resulting calculated values being inflated approximately 100 times larger than anticipated.
2.  `double` values are being used instead of `BigDecimal` in the cashflow calculation which can introduce errors into calculations. This is because double uses binary floating-point representation which can be inaccurate for decimal values and can lead to rounding or precision problems.

**CashflowController**
1. In `generateCashflows`, the `paymentValue` calculation is not aligned with (Notional × Rate × Months) ÷ 12 as it uses days instead of months. This results in varying `paymentValues` as the number of days in each month differs, leading to inconsistent cashflow amounts.
2. `rate` used to calculate `paymentValue` is the percentage rate value which is not being converted to its equivalent decimal value, resulting in inflated values being displayed in UI.

## Proposed Solution

**TradeService.calculateCashflowValue**
1. Convert `rate` percentage to decimal (by dividing `rate` by 100) to ensure the decimal value is used in the calculation
2. Use `BigDecimal` instead of `double` throughout the monetary calculation in `calculateCashflowValue` to minimise rounding and precision errors being introduced into the code

Here is the code with proposed changes incorporated:

```java

private BigDecimal calculateCashflowValue(TradeLeg leg, int monthsInterval) {
    if (leg.getLegRateType() == null) {
        return BigDecimal.ZERO;
    }

    String legType = leg.getLegRateType().getType();
    
    if ("Fixed".equals(legType)) {
        BigDecimal notional = leg.getNotional(); 
        BigDecimal ratePercentage = BigDecimal.valueOf(leg.getRate());
        BigDecimal rate = ratePercentage.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_EVEN);
        BigDecimal months = BigDecimal.valueOf(monthsInterval);
        
        return notional.multiply(rate).multiply(months).divide(BigDecimal.valueOf(12),2, RoundingMode.HALF_EVEN);
    } else if ("Floating".equals(legType)) {
        return BigDecimal.ZERO;
    }

    return BigDecimal.ZERO;
}
```

**CashflowController.generateCashflows**
1. Update `paymentValue` calculation to use months instead of days so it's aligned with (Notional × Rate × Months) ÷ 12
2. Convert `rate` percentage to decimal (by dividing `rate` by 100) to ensure the decimal value is used in the calculation

Here is the code with proposed changes incorporated:

```java
if ("Fixed".equalsIgnoreCase(leg.getLegType())) {
    double rate = leg.getRate() != null ? leg.getRate() : 0.0;
    BigDecimal decimalRate = BigDecimal.valueOf(rate).divide(BigDecimal.valueOf(100),10, RoundingMode.HALF_EVEN);
    paymentValue = leg.getNotional().multiply(decimalRate).multiply(BigDecimal.valueOf(months)).divide(BigDecimal.valueOf(12), 2, BigDecimal.ROUND_HALF_UP);
}
```

An alternative approach is to change data type of `rate` from `double` to `BigDecimal` throughout the codebase to ensure that the value of rate is stored and represented precisely. This requires further analysis to ensure other areas impacted (such as DTOs, models, getters, legacy data etc.) by this change are identified and correctly updated. 
