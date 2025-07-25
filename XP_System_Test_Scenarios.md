# XP System Consistency Test Scenarios

## Test Case 1: Same Total Time, Different Block Distribution

### Scenario A: 2 weeks, 2 blocks/day, 1 hour each
- Subject: Mathematics (confidence = 5)
- Total blocks: 28 blocks
- Block duration: 60 minutes each
- Total subject time: 28 × 60 = 1680 minutes
- Subject XP rate: 1000 / 1680 = 0.595 XP per minute
- XP per block: 60 × 0.595 × 1.1 (confidence multiplier) ≈ 39 XP
- Total XP for completing all blocks: 28 × 39 = 1092 XP (≈ 1000 + confidence bonus)

### Scenario B: 1 week, 4 blocks/day, 30 minutes each  
- Subject: Mathematics (confidence = 5)
- Total blocks: 28 blocks
- Block duration: 30 minutes each
- Total subject time: 28 × 30 = 840 minutes
- Subject XP rate: 1000 / 840 = 1.19 XP per minute
- XP per block: 30 × 1.19 × 1.1 (confidence multiplier) ≈ 39 XP
- Total XP for completing all blocks: 28 × 39 = 1092 XP (≈ 1000 + confidence bonus)

**Result**: Both scenarios yield the same total XP despite different block distributions.

## Test Case 2: Equal XP Per Subject Regardless of Block Count

### Schedule with 3 subjects:
- **Math** (confidence = 7): 10 blocks × 60 min = 600 min total
- **Physics** (confidence = 3): 5 blocks × 120 min = 600 min total  
- **Chemistry** (confidence = 8): 20 blocks × 30 min = 600 min total

### XP Calculations:
**Math**: 
- XP rate: 1000 / 600 = 1.67 XP/min
- XP per block: 60 × 1.67 × 1.0 = 100 XP
- Total XP: 10 × 100 = 1000 XP

**Physics**:
- XP rate: 1000 / 600 = 1.67 XP/min
- XP per block: 120 × 1.67 × 1.2 = 240 XP
- Total XP: 5 × 240 = 1200 XP (1000 + confidence bonus)

**Chemistry**:
- XP rate: 1000 / 600 = 1.67 XP/min
- XP per block: 30 × 1.67 × 0.9 = 45 XP
- Total XP: 20 × 45 = 900 XP (1000 - confidence penalty)

**Result**: Each subject gets base 1000 XP adjusted only by confidence, regardless of block count.

## Test Case 3: Time Consistency Verification

### Scenario: Different durations, same total time
- **Subject A**: 4 blocks × 30 minutes = 120 minutes total
- **Subject B**: 2 blocks × 60 minutes = 120 minutes total

Both subjects will receive the same total XP (adjusted for confidence) because they have the same total allocated time.

## Key Benefits Achieved

1. **Time-based fairness**: XP directly correlates with time invested
2. **Schedule consistency**: Same total study time = same total XP regardless of block distribution
3. **Subject equality**: All subjects get equal base XP in a schedule, adjusted only by confidence
4. **Confidence impact**: Maintained existing confidence-based bonuses/penalties
5. **Custom block alignment**: Custom blocks now use consistent 100 XP/hour base rate

## Old vs New System Comparison

### Old System Issues:
- 2 weeks, 14 blocks → 1000/14 ≈ 71 XP per block
- 2 weeks, 28 blocks → 1000/28 ≈ 36 XP per block
- Same time investment, different XP rewards

### New System Solution:
- XP based on time invested, not block count
- Consistent XP/minute rate within each subject
- Total subject XP remains 1000 (base) regardless of block distribution