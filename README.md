# Binance API Test Automation

REST Assured ile yazılmış Binance API testleri. Testler otomatik olarak zamanlanmış şekilde çalışır ve sonuçlar Telegram'a bildirilir.

## 🚀 Özellikler

- ✅ REST Assured ile API testleri
- ✅ TestNG test framework
- ✅ Maven build tool
- ✅ Otomatik zamanlama (macOS launchd)
- ✅ Telegram bot entegrasyonu
- ✅ Detaylı test raporlama
- ✅ Log yönetimi

## 📋 Gereksinimler

- Java 11+
- Maven 3.6+
- macOS (launchd için)
- Telegram Bot (bildirimler için)

## 🔧 Kurulum

### 1. Projeyi Klonlayın
```bash
git clone https://github.com/your-username/BinanceApiTests.git
cd BinanceApiTests 
```

### 2. Test Sonuçlarını Telegram Üzerinden Görmek İçin

- BinanceApiTests projesi içerisinde .telegram-config adında bir dosya oluşturun.
- Dosya içerisine:
  - TELEGRAM_BOT_TOKEN="abc"
    TELEGRAM_CHAT_ID="-def"    değerlerini ekleyin (abc ve def yerine kendi kullanacağınız değerleri yazın)
