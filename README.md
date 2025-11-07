# Trading Application Technical Challenge Submission

## Submission Overview

This document provides an overview of my approach and high level solution details of my submission for the Trading Application 
Technical Challenge in which the following steps have been attempted:

- ✅ **Test Case Fixes** - Debugging and fixing failing tests
- ✅ **Missing Functionality** - Implementing advanced search, validation, and dashboard features
- ✅ **Bug Investigation** - Root cause analysis and bug fixing
- ✅ **Full-Stack Feature** - Settlement instructions implementation 
- ✅ **Containerization** - Docker and DevOps 
- ✅ **Cloud Architecture** - Azure design documentation 

## Approach Overview

### Project Management
I used a GitHub Projects Kanban board to plan and manage the completion of tasks. Each project step was broken down into 
subtasks to helped me track what I needed to do. This was especially helpful when I got stuck on certain tasks 
which I found to be more complicated than I first thought and I needed to ask questions to clarify requirements, for example implementing dashboard requirements in step 3 which 
requires Spring Security. I was able to go back to the board and easily identify other tasks to start work on while I waited for classification on requirements.
As a result, this helped me to work through project steps more efficiently and keep track of work in progress. 

🔗 [Trade Capture System Project](https://github.com/users/codebygrace/projects/6/views/1)

### Branching Strategy

I used fix branches to isolate bug fixes and feature branches to develop and test implemented requirements. Once code was tested, 
it was then merged into the release branch for final integration.


### Continuous Integration
I also incorporated a GitHub Actions workflow into the release branch to compile, test, and package the backend 
when triggered by pull request and push events. This helped to validate code quality before merging and after 
changes are incorporated into the release branch. This workflow also automates the Docker image build process for the backend application
and publishes it to [Docker Hub](https://hub.docker.com/r/codebygrace/backend/tags).


### Testing

### Key Resources
- **Detailed Setup Guide**: See `PROJECT-SETUP-GUIDE.md`
- **Database Console**: JDBC URL: `jdbc:h2:file:./data/tradingdb`, User: `sa`, Password: password

[<img src="https://run.pstmn.io/button.svg" alt="Run In Postman" style="width: 128px; height: 32px;">](https://god.gw.postman.com/run-collection/46779254-6ffd0ffa-65f2-4ae3-9acc-1ad5088df481?action=collection%2Ffork&source=rip_markdown&collection-url=entityId%3D46779254-6ffd0ffa-65f2-4ae3-9acc-1ad5088df481%26entityType%3Dcollection%26workspaceId%3D2f6f14a3-44c4-433e-b9ec-e9fbeafbc238)


## Solution Details by Step

### Step 2: Fix Failing Test Cases
#### Objective: Identify and fix failing test cases in the backend application while documenting your debugging process and understanding of the fixes.

During this step I found that 14 tests failed. I was able to resolve all issues and documented my findings in `solution-design-docs/test-fixes-for-step-2.md` 
which contains full details for this step.


### Step 3: Implement Missing Functionality
#### 3.1: Advanced Trade Search System

### Templates
- **Git Standards**: `git-commit-standards.md`
- **Self-Assessment**: `test-fix-checklist.md`

---

## Step 3: Implement Missing Functionality (REQUIRED)

### Business Enhancement Request

**Request ID:** TRADE-2025-REQ-003  
**Priority:** High  
**Requested By:** Trading Desk Operations Team  
**Business Sponsor:** Head of Trading Operations  
**Date Requested:** September 15, 2025  

### **Business Case:**
The trading desk has identified critical gaps in the current trading application that are impacting daily operations and trader productivity. Traders are struggling with basic operational tasks due to missing search capabilities, insufficient validation systems, and lack of personalized dashboard views.

### **Current Pain Points:**
- **Inefficient Trade Search**: Traders spend 30+ minutes daily manually scrolling through trade lists to find specific trades
- **Validation Gaps**: Invalid trades are being created, causing downstream settlement issues and operational risk
- **No Personalized Views**: Traders cannot easily see their own trades or get summary statistics for decision making
- **Manual Processes**: Lack of dashboard functionality forces traders to use spreadsheets for trade monitoring

### **Business Impact:**
- Reduced trading efficiency and productivity
- Increased operational risk due to validation gaps
- Poor user experience leading to trader frustration
- Manual workarounds creating audit trail issues

## Required Enhancement Implementation

You must implement **ALL THREE** of the following critical enhancements:

### **Enhancement 1: Advanced Trade Search System**

**Business Requirement**: "As a trader, I need to quickly find trades using multiple search criteria so that I can efficiently manage my trading portfolio."

**Current Problem**: The application only supports basic trade retrieval (get all, get by ID) but lacks the advanced search capabilities traders need.

**Required Implementation:**
```java
@GetMapping("/search") // Multi-criteria search
@GetMapping("/filter") // Paginated filtering  
@GetMapping("/rsql")   // RSQL query support for power users
```

They all provide users with multi-criteria search and filtering abilities as I assumed that users will need to find trades 
by one or more of the following search criteria:
- Counterparty name
- Book name
- Trader
- Status
- Trade Date (users are able to search over a range)

**Search:** This was the first endpoint I implemented, and so I used a JPA query which has optional query parameters.

**RSQL Query Support:** I used Perplexhub's RSQL JPA Spring Boot Starter as it includes the rsql-jpa-specification library
which translates RSQL queries into Spring Data JPA Specifications without needing to hardcode query logic. This minimised
the amount of boilerplate code needed as the library generates the appropriate query dynamically at runtime based on the RSQL input. 

**Filter:** I also used the rsql-jpa-specification library to enable filtering as it provides future extensibility with minimal 
code changes needed if additional filter parameters are required.


#### 3.2: Comprehensive Trade Validation Engine

I implemented all 3 required implementations:
```java
public ValidationResult validateTradeBusinessRules(TradeDTO tradeDTO);
public boolean validateUserPrivileges(String userId, String operation, TradeDTO tradeDTO);
public ValidationResult validateTradeLegConsistency(List<TradeLegDTO> legs);
```

**ValidationResult:** Represents the result of a validation process and uses a multimap so that each field can be associated with multiple error messages. 
I used multimap from Google Guava as when I initially used a list, error messages for the same field were overwritten.
With the use of multimap, client users are able to see all errors messages associated with the field failing validation checks. 

I implemented the following 3 validator services:
- **TradeValidator:** validates the business rules of a TradeDTO and incorporates the results of leg-specific consistency checks from TradeLegValidator and aggregates any validation errors into a single ValidationResult object
- **TradeLegValidator:** validates the consistency of a pair of TradeLegDTO objects
- **UserPrivilegeValidator:** validates whether a given user is allowed to perform a specific operation

**Custom exceptions:**
- TradeValidationException - this carries the multimap of error messages to the client
- UserPrivilegeValidationException

#### 3.3: Trader Dashboard and Blotter System

I implemented and tested the required implementations: 
```java
@GetMapping("/my-trades")      // Trader's personal trades
@GetMapping("/book/{id}/trades") // Book-level trade aggregation
@GetMapping("/summary")        // Trade portfolio summaries
@GetMapping("/daily-summary")  // Daily trading statistics

public class TradeSummaryDTO { }
    
public class DailySummaryDTO { }
```

I also implemented the following Spring Security features to help provide authenticated users with personalised views: 

**New security features:**
- Role-based permissions
- HTTP Basic Authentication
- Session management
- Role-based route protection
- Security-specific exceptions
- Password hashing using BCrypt


### Step 4: Bug Investigation and Fix
#### Objective: Investigate and fix a critical bug in the cashflow calculation logic that's affecting production trading operations.

I was able to resolve all issues and documented my findings in `solution-design-docs/bug-report-TRD-2025-001-incorrect-cashflow-calculations.md` 

### Step 5: Full-Stack Feature Implementation
#### Objective: Full-Stack Feature Implementation

I chose to implement Option B Extensible AdditionalInfo Architecture and utilised the existing Additional Info service. This 
design leverages the EAV pattern, which only uses entityType and entityId for generic storage across multiple entity types.

I also implemented SettlementInstructionsUpdateDTO which encompasses the character validation check along with required endpoints

```java
@GetMapping("/search/settlement-instructions")
public ResponseEntity<List<TradeDTO>> searchBySettlementInstructions(
    @RequestParam String instructions) {
    // Search trades by settlement instruction content
}

@PutMapping("/{id}/settlement-instructions")
public ResponseEntity<?> updateSettlementInstructions(
    @PathVariable Long id, 
    @RequestBody SettlementInstructionsUpdateDTO request) {
    // Update settlement instructions for existing trades
}
```

A `Settlement Instructions` field was added to the trade booking modal and further work is required to deliver the remaining frontend requirements. 


### Step 6: Application Containerization
#### Objective: Dockerize both applications with Docker Compose

I have implemented Docker files for both backend and frontend applications using multi-layered and multi-stage builds 
to make things more efficient and reduce the final image size as it excludes unnecessary build-time files.

A GitHub Actions workflow also automates the Docker image build process for the backend application and publishes 
it to [Docker Hub](https://hub.docker.com/r/codebygrace/backend/tags).

### Step 7: Azure Cloud Architecture Design
I documented my design in `solution-design-docs/azure-architecture-design.md` which also includes architectural diagrams
