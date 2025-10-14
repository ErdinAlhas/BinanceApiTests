#!/bin/bash

cd /Users/erdinalhas/IdeaProjects/BinanceApiTests/test-logs

# 7 günden eski test loglarını sil
find . -name "test_*.log" -mtime +7 -delete

# Log dosyası sayısı
LOG_COUNT=$(ls -1 test_*.log 2>/dev/null | wc -l)
echo "$(date): $LOG_COUNT log files remaining" >> cleanup.log
