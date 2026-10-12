# Dokunma geri bildirimi ilkeleri

- Görsel olarak yuvarlatılmış kontrolün ripple sınırı da aynı `shape` olmalıdır. `Modifier.clip(shape)`, `clickable` / `selectable` / `toggleable` **öncesine** konur.
- Erişilebilirlik için geniş tutulan ama görünür yüzeyi bulunmayan hedeflerde (Dock hücresi, ikon, checkbox/switch ve radyo satırları) tam hedef boyunca dikdörtgen ripple çizilmez. Bunun yerine mevcut seçim/press animasyonu kullanılır: `indication = null`.
- Ana `ArcButton` bileşeninin hafif basma ölçeği ve ripple'sız davranışı korunur.
- Gölge kullanan hareketli kartlara dıştan kırpma uygulanmaz; gölge kesilmesin diye bu kartların dikdörtgen ripple'ı kaldırılır.
- `role`, `enabled`, dokunma hedefi, klavye davranışı ve hareket azaltma tercihleri bozulmamalıdır.

`TouchFeedbackRegressionTest` etkileşimlerin sürmesini kontrol eder. Ripple sınırının gerçek piksel çıktısı ise Android cihaz/emülatör üzerinde ayrıca gözle denetlenmelidir.
