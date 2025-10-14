#!/bin/bash

LOG_FILE=$(ls -t /Users/erdinalhas/IdeaProjects/BinanceApiTests/test-logs/test_*.log | head -1)

echo "=== Checking Log Format ==="
echo ""
echo "1. Lines with 'Tests run':"
grep -i "tests run" "$LOG_FILE"
echo ""
echo "2. Lines with 'Total':"
grep -i "total" "$LOG_FILE"
echo ""
echo "3. Lines with test counts:"
grep -E "[0-9]+ test" "$LOG_FILE"
echo ""
echo "4. TestNG summary lines:"
grep -A 5 "PASSED\|FAILED" "$LOG_FILE" | tail -10
echo ""
echo "5. Maven Surefire summary:"
grep -A 5 "Results:" "$LOG_FILE"
echo ""
echo "=== End of Debug ==="
