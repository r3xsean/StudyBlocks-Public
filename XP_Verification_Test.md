# XP System Verification Test

## Test Scenario: Consistent Total XP + Equal Subject XP

### Scenario 1: Single Subject (100 blocks)
- Subject A: 100 blocks × 60 minutes = 6000 minutes total
- Subject count: 1
- XP per subject: 1000 XP ÷ 1 subject = 1000 XP
- XP per minute: 1000 XP ÷ 6000 minutes = 0.1667 XP/minute
- XP per block: 60 minutes × 0.1667 = **10 XP per block**
- **Subject A Total XP: 1000 XP**
- **Total Schedule XP: 1000 XP**

### Scenario 2: Two Subjects (Subject A: 20 blocks, Subject B: 80 blocks)
- Subject A: 20 blocks × 60 minutes = 1200 minutes
- Subject B: 80 blocks × 60 minutes = 4800 minutes  
- Subject count: 2
- XP per subject: 1000 XP ÷ 2 subjects = 500 XP each

**Subject A Calculation:**
- XP per minute: 500 XP ÷ 1200 minutes = 0.4167 XP/minute
- XP per block: 60 minutes × 0.4167 = **25 XP per block**
- **Subject A Total XP: 500 XP**

**Subject B Calculation:**
- XP per minute: 500 XP ÷ 4800 minutes = 0.1042 XP/minute  
- XP per block: 60 minutes × 0.1042 = **6.25 XP per block**
- **Subject B Total XP: 500 XP**

- **Total Schedule XP: 1000 XP**

### Result
✅ **Both scenarios award the same total XP (1000 XP) when all blocks are completed**
✅ **Each subject gets equal total XP (500 XP each in 2-subject scenario) regardless of time allocation**

## Key Changes Made

1. **XPManager.kt**: 
   - Changed from `BASE_XP_PER_SUBJECT = 1000` to `TOTAL_SCHEDULE_XP = 1000`
   - Modified `calculateBlockXP()` to distribute XP equally across subjects first, then proportionally within each subject
   - Added `totalSubjectCount` parameter to enable equal XP distribution per subject
   - Each subject gets `TOTAL_SCHEDULE_XP / totalSubjectCount` XP when fully completed

2. **StudyRepository.kt**:
   - Updated to get subject count and subject-specific time allocation
   - Passes both `totalSubjectTimeMinutes` and `totalSubjectCount` to XP calculation
   - Fixed variable naming conflict with `allUpdatedSubjects`

3. **StudyBlockDao.kt**:
   - Added `getAllBlocksForUser()` method (though ultimately not used in final solution)

## Mathematical Verification

For any schedule with `N` subjects:
- XP per subject = 1000 XP ÷ N subjects
- For each subject with time `T_subject`:
  - XP Rate = (1000 ÷ N) ÷ T_subject
  - XP per block = block_duration × XP_Rate
  - Subject Total XP = Σ(block_duration × XP_Rate) = (1000 ÷ N)

This ensures:
1. **Consistent total schedule XP**: Always 1000 XP regardless of subject count
2. **Equal subject XP**: Each subject contributes equally (1000/N XP) regardless of time allocation