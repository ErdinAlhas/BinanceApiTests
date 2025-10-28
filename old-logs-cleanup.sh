#!/bin/bash

# Config yükle
CONFIG_FILE="/Users/erdinalhas/IdeaProjects/BinanceApiTests/.telegram-config"
if [ -f "$CONFIG_FILE" ]; then
    source "$CONFIG_FILE"
fi

cd /Users/erdinalhas/IdeaProjects/BinanceApiTests/test-logs

# Temizlik öncesi log sayısı
BEFORE_COUNT=$(ls -1 test_*.log 2>/dev/null | wc -l)

# 7 günden eski test loglarını sil
find . -name "test_*.log" -mtime +7 -delete

# Temizlik sonrası log sayısı
AFTER_COUNT=$(ls -1 test_*.log 2>/dev/null | wc -l)
DELETED=$((BEFORE_COUNT - AFTER_COUNT))

# Log kaydet
echo "$(date '+%Y-%m-%d %H:%M:%S') - Deleted: $DELETED, Remaining: $AFTER_COUNT" >> cleanup-history.log

# Telegram bildirimi (eğer config varsa)
if [ ! -z "$TELEGRAM_BOT_TOKEN" ] && [ $DELETED -gt 0 ]; then
    MESSAGE="🧹 <b>Log Temizlendi</b>%0A%0A"
    MESSAGE+="🗑️ Silinen: $DELETED log%0A"
    MESSAGE+="📁 Kalan: $AFTER_COUNT log%0A"
    MESSAGE+="📅 $(date '+%d.%m.%Y %H:%M')"
    
    curl -s -X POST "https://api.telegram.org/bot${TELEGRAM_BOT_TOKEN}/sendMessage" \
        -d chat_id="${TELEGRAM_CHAT_ID}" \
        -d text="${MESSAGE}" \
        -d parse_mode="HTML" > /dev/null 2>&1
fi
