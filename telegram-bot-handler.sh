#!/bin/bash

# ====== CONFIG YÜKLEME ======
PROJECT_DIR="/Users/erdinalhas/IdeaProjects/BinanceApiTests"
CONFIG_FILE="$PROJECT_DIR/.telegram-config"

if [ -f "$CONFIG_FILE" ]; then
    source "$CONFIG_FILE"
else
    echo "❌ Error: .telegram-config file not found!"
    exit 1
fi

# Token kontrolü
if [ -z "$TELEGRAM_BOT_TOKEN" ] || [ -z "$TELEGRAM_CHAT_ID" ]; then
    echo "❌ Error: Telegram credentials not set!"
    exit 1
fi

# ====== TELEGRAM FONKSİYONLARI ======
UPDATE_FILE="$PROJECT_DIR/test-logs/last_update_id.txt"

