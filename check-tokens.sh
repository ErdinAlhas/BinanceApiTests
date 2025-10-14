#!/bin/bash

echo "🔍 Checking for hardcoded tokens..."

# .sh dosyalarında token ara
if grep -r "TELEGRAM_BOT_TOKEN=\"[0-9]" *.sh 2>/dev/null; then
    echo "❌ UYARI: .sh dosyalarında hardcoded token bulundu!"
else
    echo "✅ .sh dosyalarında token yok"
fi

# .telegram-config kontrolü
if [ -f ".telegram-config" ]; then
    echo "✅ .telegram-config mevcut"
else
    echo "❌ UYARI: .telegram-config bulunamadı!"
fi

# .gitignore kontrolü
if grep -q ".telegram-config" .gitignore; then
    echo "✅ .telegram-config gitignore'da"
else
    echo "❌ UYARI: .telegram-config gitignore'da değil!"
fi

# Git'te .telegram-config var mı?
if git ls-files | grep -q "^\.telegram-config$"; then
    echo "❌ UYARI: .telegram-config Git'te takip ediliyor!"
else
    echo "✅ .telegram-config Git'te yok"
fi

echo ""
echo "✅ = Güvenli | ❌ = Sorun var"
