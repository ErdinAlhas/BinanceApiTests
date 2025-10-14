#!/bin/bash

# ====== CONFIG YÜKLEME ======
CONFIG_FILE="/Users/erdinalhas/IdeaProjects/BinanceApiTests/.telegram-config"

if [ -f "$CONFIG_FILE" ]; then
    source "$CONFIG_FILE"
else
    echo "❌ Error: .telegram-config file not found!"
    echo "Please copy .telegram-config.example to .telegram-config and fill in your credentials."
    exit 1
fi

# Token kontrolü
if [ -z "$TELEGRAM_BOT_TOKEN" ] || [ -z "$TELEGRAM_CHAT_ID" ]; then
    echo "❌ Error: Telegram credentials not set in .telegram-config"
    exit 1
fi

# Telegram'a mesaj gönder fonksiyonu
send_telegram_message() {
    local message="$1"
    curl -s -X POST "https://api.telegram.org/bot${TELEGRAM_BOT_TOKEN}/sendMessage" \
        -d chat_id="${TELEGRAM_CHAT_ID}" \
        -d text="${message}" \
        -d parse_mode="HTML" > /dev/null 2>&1
}

# ====== MAVEN AYARLARI ======
export PATH="/opt/homebrew/bin:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin"

cd /Users/erdinalhas/IdeaProjects/BinanceApiTests

# Maven yolunu tespit et
if [ -f "/opt/homebrew/bin/mvn" ]; then
    MVN="/opt/homebrew/bin/mvn"
elif [ -f "/usr/local/bin/mvn" ]; then
    MVN="/usr/local/bin/mvn"
else
    MVN=$(which mvn 2>/dev/null)
    if [ -z "$MVN" ]; then
        echo "ERROR: Maven not found!" >> test-logs/error.log
        send_telegram_message "❌ <b>ERROR</b>: Maven not found!"
        exit 1
    fi
fi

# ====== TEST ÇALIŞTIRMA ======
DATE=$(date +%Y%m%d_%H%M%S)
READABLE_DATE=$(date "+%d.%m.%Y %H:%M:%S")
LOG_DIR="test-logs"
mkdir -p $LOG_DIR

echo "=== Test Started at $(date) ===" >> "$LOG_DIR/summary.log"

# Test başladı bildirimi
send_telegram_message "🔄 <b>Test Başladı</b>%0A⏰ ${READABLE_DATE}%0A📝 MarketTests çalıştırılıyor..."

# Testleri çalıştır
$MVN clean test -Dtest=MarketTests > "$LOG_DIR/test_$DATE.log" 2>&1
EXIT_CODE=$?

# ====== SONUÇ ANALİZİ ======
if [ $EXIT_CODE -eq 0 ]; then
    # Başarılı
    echo "[$DATE] ✅ Tests PASSED" >> "$LOG_DIR/summary.log"
    
    # Test sayılarını bul (farklı formatları dene)
    TOTAL_TESTS=$(grep -oE "Tests run: [0-9]+" "$LOG_DIR/test_$DATE.log" | grep -oE "[0-9]+" | tail -1)
    
    # Eğer bulunamadıysa alternatif format dene
    if [ -z "$TOTAL_TESTS" ]; then
        TOTAL_TESTS=$(grep -oE "test.*run.*[0-9]+" "$LOG_DIR/test_$DATE.log" | grep -oE "[0-9]+" | tail -1)
    fi
    
    # Hala bulunamadıysa TestNG formatını dene
    if [ -z "$TOTAL_TESTS" ]; then
        TOTAL_TESTS=$(grep -oE "Total tests run: [0-9]+" "$LOG_DIR/test_$DATE.log" | grep -oE "[0-9]+" | tail -1)
    fi
    
    # Başarılı test sayısını da göster
    PASSED_TESTS=$(grep -oE "Passed: [0-9]+" "$LOG_DIR/test_$DATE.log" | grep -oE "[0-9]+" | tail -1)
    
    # Telegram bildirimi
    MESSAGE="✅ <b>Testler BAŞARILI</b>%0A%0A"
    MESSAGE+="⏰ ${READABLE_DATE}%0A"
    MESSAGE+="📊 Toplam Test: ${TOTAL_TESTS:-'N/A'}%0A"
    
    if [ ! -z "$PASSED_TESTS" ]; then
        MESSAGE+="✅ Başarılı: ${PASSED_TESTS}%0A"
    fi
    
    MESSAGE+="✨ Tüm testler geçti!"
    
    send_telegram_message "$MESSAGE"
    
else
    # Başarısız
    echo "[$DATE] ❌ Tests FAILED (Exit code: $EXIT_CODE)" >> "$LOG_DIR/summary.log"
    
    # Hata detaylarını bul (farklı formatları dene)
    TOTAL_TESTS=$(grep -oE "Tests run: [0-9]+" "$LOG_DIR/test_$DATE.log" | grep -oE "[0-9]+" | tail -1)
    FAILURES=$(grep -oE "Failures: [0-9]+" "$LOG_DIR/test_$DATE.log" | grep -oE "[0-9]+" | tail -1)
    ERRORS=$(grep -oE "Errors: [0-9]+" "$LOG_DIR/test_$DATE.log" | grep -oE "[0-9]+" | tail -1)
    SKIPPED=$(grep -oE "Skipped: [0-9]+" "$LOG_DIR/test_$DATE.log" | grep -oE "[0-9]+" | tail -1)
    
    # TestNG formatı için alternatif
    if [ -z "$TOTAL_TESTS" ]; then
        TOTAL_TESTS=$(grep -oE "Total tests run: [0-9]+" "$LOG_DIR/test_$DATE.log" | grep -oE "[0-9]+" | tail -1)
        FAILURES=$(grep -oE "Failures: [0-9]+" "$LOG_DIR/test_$DATE.log" | grep -oE "[0-9]+" | tail -1)
    fi
    
    # Başarısız test isimlerini bul
    FAILED_TESTS=$(grep -E "FAILED|FAILURE" "$LOG_DIR/test_$DATE.log" | grep -oE "test[A-Za-z0-9_]+" | head -3 | tr '\n' ', ')
    
    # Telegram bildirimi
    MESSAGE="❌ <b>Testler BAŞARISIZ</b>%0A%0A"
    MESSAGE+="⏰ ${READABLE_DATE}%0A"
    MESSAGE+="📊 Toplam Test: ${TOTAL_TESTS:-'N/A'}%0A"
    MESSAGE+="❌ Başarısız: ${FAILURES:-0}%0A"
    MESSAGE+="⚠️ Hatalar: ${ERRORS:-0}%0A"
    
    if [ ! -z "$SKIPPED" ] && [ "$SKIPPED" -gt 0 ]; then
        MESSAGE+="⏭️ Atlanan: ${SKIPPED}%0A"
    fi
    
    if [ ! -z "$FAILED_TESTS" ]; then
        MESSAGE+="%0A🔍 <b>Başarısız Testler:</b>%0A${FAILED_TESTS}"
    fi
    
    send_telegram_message "$MESSAGE"
    
    # Hata detaylarını logla
    {
        echo "--- Test Statistics ---"
        echo "Total: ${TOTAL_TESTS:-N/A}, Failures: ${FAILURES:-0}, Errors: ${ERRORS:-0}, Skipped: ${SKIPPED:-0}"
        echo "--- Last 30 lines of test log ---"
        tail -30 "$LOG_DIR/test_$DATE.log"
        echo "---------------------------------"
    } >> "$LOG_DIR/summary.log"
fi

echo "=== Test Finished at $(date) ===" >> "$LOG_DIR/summary.log"
echo "" >> "$LOG_DIR/summary.log"

tail -30 "$LOG_DIR/summary.log"
