# Arc Library davranış karşılaştırması

Bu tablo, `main` dalındaki [Arc Library](https://github.com/kuratlielia/arc-library) kaynakları ve bu depodaki native Compose uygulaması karşılaştırılarak tutulur. Arc'ın web çalışma zamanı eklenmez; tarih seçimi, klavye, erişilebilirlik ve pencere davranışı Android API'lerine uyarlanır.

Durumlar, yalnızca burada yazılan etkileşim kapsamını ifade eder:

- **Tam:** belirtilen davranış mevcut ve otomatik testle doğrulanmış.
- **Kısmi:** temel akış mevcut; en az bir davranış, erişilebilirlik yolu veya otomatik doğrulama eksik.
- **Bilinçli olarak farklı:** platforma özgü uygulama korunur ve fark burada açıklanır.
- **Kapsam dışı:** bu değişiklik setinde uygulanmadı.

## P0 etkileşim düzeltmeleri

| Bileşen | Durum | Native Compose uyarlaması |
| --- | --- | --- |
| `ArcTimePicker` | Tam | 24 saat biçiminde saat ve dakika ayrı seçilir; sınır değerleri normalleştirilir, saat/dakika adımları callback'i günceller. UI ve birim testleri. |
| `ArcShortcutRecorder` | Tam | Gerçek klavye olaylarını yakalar; Escape iptal eder, Delete/Backspace temizler, çakışma varsa mevcut bağ korunur. Tuş ve modifier UI testleri. |
| `ArcMorphSelect` | Tam | Aynı yüzey içinde açılır; ok, Enter, Escape ve yazıyla eşleştirme kullanır; seçili/etkin değer ve odak Compose durumunda tutulur. Odak, iptal ve klavye seçimi testleri. |
| `ArcElasticSlider` | Tam | Sınırda lastik bant görseli, adım yakalama, klavye, TalkBack `setProgress` ve sürükleme bitiminde commit sunar. Geçersiz adım yok sayılır; geçersiz aralık devre dışı kalır. Sınır callback'i ve birim testleri. |
| `ArcHoldToConfirm` | Tam | Erken bırakma ve Escape beklemeyi iptal eder; klavye ve erişilebilirlik aktivasyonu aynı süreli doğrulama yolunu kullanır; başarı tek seferliktir. Reduced motion yalnız görsel ilerlemeyi anında tamamlar, bekleme süresini korur. |
| `ArcResizablePanels` | Tam | Sürükleme, klavye, reset ve collapse/restore; komşu iki panelin toplamını korur. Dar görünüm dikey yerleşir. Klavye, collapse/restore ve boyut sınırı testleri. |
| `ArcCardStack` | Tam | 0/1/2/3+ kart doğru görünür; tek kart da kaydırılarak kaldırılabilir. TalkBack özel eylemi kaldırma sunar. Boş, çok kartlı ve tek kart dismiss UI testleri. |

## P1 bileşen davranışı

| Bileşen | Durum | Gözlenen eşdeğerlik / fark |
| --- | --- | --- |
| Checkbox, Switch, RadioGroup, RadioCards, SegmentedControl | Tam | Kontrollü seçim callback'leri ve Compose semantics üzerinden seçim etkileşimi UI testlerinde doğrulanır. |
| `ArcDateRangePicker` | Bilinçli olarak farklı | Tek native Material3 aralık seçicisi; başlangıç/bitiş, vurgulu aralık, geçersiz sıralamanın düzeltilmesi ve 7/30/90 gün kısayolları. Tarih formatı ve pencere erişilebilirliği Android locale/Compose bileşeninden gelir. |
| `ArcCombobox` | Tam | Arama, oklarla etkin sonuç, Enter seçimi, Escape kapatma, boş sonuç metni ve açılış odağı; arama ve klavye UI testleri. |
| `ArcMultiSelect` | Tam | Kontrollü çoklu seçim, checkbox semantiği ve isteğe bağlı arama; arama ve birden çok seçimi koruma UI testi. |
| `ArcContextMenu` | Tam | Uzun basma, Menü/Shift+F10 ve TalkBack özel eylemi; ok tuşları, Enter/Space ve Escape; callback seçilince kapanış. Açılış ve klavye ile seçim UI testli. |
| `ArcTheme` accent paletleri | Tam | Neutral, violet, blue, green, amber, orange, coral ve rose; açık/koyu semantic tokenlar ve ayrı chart serileri. JVM palette testi mevcut. |
| Hareket tokenları | Tam | 120/160/180/240/480/580ms süreler, easing, spring, stagger ve blur tokenları; görünen Compose geçişleri ortak süre/easing spec'lerini kullanır. |
| Reduced motion | Tam | Sistem animator ölçeği ve theme override; geçişler snap olur, stagger sıfırlanır, sonsuz shimmer sabitlenir, otomatik liste kaydırma anlık olur. Hold-to-confirm süresi ve toast zaman aşımı görsel tercihten bağımsız kalır. |
| `ArcSignaturePad` | Tam | Normalize stroke/point/pressure/zaman verisi, undo/redo/clear ve SVG/PNG dışa aktarımı; çizim ve dışa aktarım UI testi. |
| `ArcSortableDataTable` | Tam | Kontrollü satır ve sayfa seçimi, küresel sıralama, sayfalama ve değişen sütun şemasında sort key korunması. Sıralama/seçim/sayfalama/şema geçişi testli. |
| `ArcCommandPalette` | Tam | Arama, etkin sonuç, yukarı/aşağı, Enter/Escape, boş sonuç ve modal kapanışı; invoker odak geri yüklemesi için FocusRequester overload'u ve UI testi. |
| Genel API metinleri | Tam | Tarih/arama/numara varsayılanları ve API açıklamaları alan bağımsızdır; Kotlin çağrıları korunuyor. Spring tokenlarının dönüş tipi ve ikili tüketici yeniden derleme notu [API geçiş rehberinde](api-migration.md) açıklanıyor. |

## P2 kapsam ve farklar

| Alan | Durum | Not |
| --- | --- | --- |
| Gallery uygulaması | Kısmi | Ayrı `:gallery` Android uygulaması; accent, açık/koyu, reduced motion ve temsilî gerçek etkileşimler. Tüm mevcut bileşenler henüz galeride değil. |
| CI | Tam | PR/push derlemesi, JVM ve API 35 Compose UI testleri, Android lint ve Kotlin ABI kontrolü. |
| Rich text editor | Bilinçli olarak farklı | Hafif Markdown desteği bold/italic, quote ve toolbar ile liste eklemedir. Nested block, link, code, table ve HTML export sağlamaz. |
| `icon-morph`, `money-input`, `password-field`, `otp-input`, `phone-input` | Kapsam dışı | Bu değişiklikte düşük doğruluklu ek portlar yerine mevcut ortak bileşenler güvenilirleştirildi. Ülke, para ve doğrulama kuralları ürün API'si olarak ayrıca belirlenmeli. |
| Grafikler | Kısmi | Negatif/bozuk değer temizleme, aralık sınırlama, tema paleti, yatay taşma ve erişilebilir açıklamalar eklendi. Telefon/tablet etiket yerleşimi ayrıca görsel QA gerektirir. |
| Responsive Card/Tabs/Drawer/BottomSheet/Toast/Carousel/ScrollArea | Kısmi | Mevcut API'ler korunmuştur; bütün cihaz boyutları için görsel regresyon testi bu kapsamda tamamlanmadı. |

## Doğrulama kapsamı

UI testleri `design-system/src/androidTest` altında tutulur. İlgili testin derlenmesi, testin cihaz üzerinde başarılı çalıştığı anlamına gelmez; bu tablo yalnızca başarılı çalışma sonrasında **Tam** durumuna yükseltilmelidir. CI API 35 emülatöründe tekrar çalıştırır.
