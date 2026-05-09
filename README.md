# ⚖️ Load Balancing Serüveni: Yükü Omuzlayan Algoritmalar

Backend dünyasında sistemin büyümesi demek, tek bir sunucunun artık yetmemesi demektir. Peki, kapıdan giren binlerce isteği arkadaki sunuculara nasıl dağıtacağız? İşte burada devreye "Yük Dengeleyiciler" (Load Balancers) giriyor. Bu seride, trafik polisliği yapan farklı algoritmaları ve hangisinin hangi durumda hayat kurtardığını inceleyeceğiz.

<img width="3735" height="3573" alt="image" src="https://github.com/user-attachments/assets/a1130c21-4728-4f7b-8ec4-39cac0f2931b" />

---

### 🔄 Round Robin (Sıralı Dağıtım)

Round Robin, yük dengeleme dünyasının en eski, en basit ve belki de en çok sevilen algoritmasıdır. Mantığı bir kart dağıtıcısı gibidir: Herkese sırayla birer kart verir ve deste bittiğinde en başa döner.

#### Nasıl Çalışır?
Her şey tam bir döngü içindedir. Diyelim ki elimizde üç tane sunucu (Server 1, Server 2, Server 3) var:
1.  **İlk istek** kapıdan girer ve Server 1'e gönderilir.
2.  **İkinci istek** gelir, sıra Server 2'dedir.
3.  **Üçüncü istek** Server 3'e gider.
4.  **Dördüncü istek** geldiğinde liste bittiği için algoritma en başa döner ve isteği tekrar Server 1'e iletir.

Bu döngü, sunucular ayakta olduğu sürece dairesel bir şekilde sonsuza kadar devam eder.

#### Ne Zaman Tercih Edilir?
*   **Eşit Güçteki Sunucular:** Eğer arkadaki sunucularının hepsi aynı işlem gücüne (CPU, RAM) sahipse Round Robin harika çalışır.
*   **Basitlik Arayışı:** Karmaşık sağlık kontrolleri veya anlık bağlantı hesaplamalarıyla uğraşmak istemediğin, hızlıca yük dağıtman gereken durumlarda ilk tercihtir.
*   **Eşit Dağıtım:** Trafiğin her sunucuya matematiksel olarak tam eşit gitmesini istiyorsan en dürüst yöntem budur.

#### Gerçek Hayat Örneği
Bir restoranda 3 garson olduğunu düşün. Karşılama görevlisi (Load Balancer), gelen müşterileri garsonlara sırayla yönlendirir: "Siz Ahmet'e, siz Mehmet'e, siz Can'a..." Ahmet'in o an masasında çok zor bir siparişle uğraşıp uğraşmadığına bakmaz, sadece sıradaki kimse işi ona paslar.

#### Artıları ve Eksileri
*   ✅ **Basitlik:** Hem anlaması hem de kodlaması çok kolaydır.
*   ✅ **Öngörülebilirlik:** Hangi isteğin kime gideceğini önceden tahmin edebilirsin.
*   ❌ **Kapasite Körlüğü:** Bir sunucun devasa bir makine, diğeri eski bir laptop bile olsa Round Robin bunu bilmez; her ikisine de aynı yükü bindirir.
*   ❌ **Durum Körlüğü:** Sunuculardan biri o an ağır bir işlemle boğuşuyor olsa bile yeni isteği ona göndermeye devam eder.

---

> [!TIP]
> **Mühendislik Notu:** Kodlamada `AtomicInteger` kullanmak kritiktir. Gerçek bir sistemde saniyede binlerce istek gelirken, normal bir `int` sayacı kullanırsan "yarış durumları" (race conditions) yüzünden sayaç hatalı artabilir ve bazı sunucular atlanabilir. Atomik yapılar, sıranın çok kanallı (multi-thread) ortamda asla karışmamasını sağlar.

### ⚖️ Weighted Round Robin (Ağırlıklı Sıralı Dağıtım)

Round Robin her ne kadar adaletli görünse de, bir "kapasite körlüğü" yaşar. Eğer elinde 32 GB RAM'li devasa bir sunucu ve yanında 4 GB RAM'li eski bir makine varsa, her ikisine de aynı işi paslamak eski makinenin çökmesine, dev sunucunun ise boşta yatmasına neden olur. **Weighted Round Robin**, bu adaletsizliği sunuculara "ağırlık" (weight) vererek çözer.

#### Nasıl Çalışır?
Her sunucuya, işlem gücüne veya kaynaklarına göre bir puan (ağırlık) verilir. Algoritma bu puanları kullanarak trafiği paylaştırır:
*   **Server A (Ağırlık: 5)**
*   **Server B (Ağırlık: 1)**
*   **Server C (Ağırlık: 1)**

Bu senaryoda, Server A diğerlerinden 5 kat daha fazla istek alır. Dağıtım genellikle şöyle görünür: `A, A, A, A, A, B, C`. Böylece güçlü olan sunucu daha çok çalışırken, zayıf olanlar kapasiteleri kadar yük alır.

#### Ne Zaman Tercih Edilir?
*   **Karma Sunucu Parkurları:** Elinde farklı nesil veya farklı kapasitelerde sunucular varsa (örneğin bir kısmı Cloud'da yüksek özellikli, bir kısmı yerel ve daha düşük özellikli).
*   **Maliyet Yönetimi:** Bazı sunucuların kullanım maliyeti daha yüksekse, ağırlıklarını düşürerek trafiği daha ucuz olan sunuculara yönlendirebilirsin.
*   **Kapasiteye Dayalı Dağıtım:** Yükü sadece sayıca değil, "kim neyi kaldırabilir" mantığıyla dağıtmak istediğinde.

#### Gerçek Hayat Örneği
Restoran örneğimize geri dönelim. Ahmet çok kıdemli ve hızlı bir garson, Mehmet ve Can ise işe yeni başlamış stajyerler. Karşılama görevlisi (Load Balancer) şöyle der: "Ahmet sen 5 masaya bakarken, Mehmet ve Can siz sadece birer masaya bakın." Böylece kimse kapasitesinin altında veya üstünde ezilmez.

#### Artıları ve Eksileri
*   ✅ **Verimlilik:** Sunucu kaynaklarını çok daha verimli kullanır; güçlü olanı yatırmaz, zayıf olanı yormaz.
*   ✅ **Kapasite Uyumu:** Farklı donanımlara sahip sunucu gruplarında mükemmel denge sağlar.
*   ❌ **Karmaşıklık:** Standart Round Robin'e göre kodlaması biraz daha detaylıdır.
*   ❌ **Anlık Durum Körlüğü:** Sunucu güçlü olabilir ama o an teknik bir arıza nedeniyle yavaşlamış olabilir. Algoritma "ağırlığa" baktığı için sunucunun o anki gerçek yorgunluğunu (response time) yine de göremez.

---

> [!IMPORTANT]
> **Mühendislik Notu:** Ağırlıkları belirlerken sadece CPU/RAM'e bakmak yetmeyebilir. Ağ gecikmesi (latency) veya sunucunun çalıştığı bölge gibi faktörleri de ağırlık puanına yansıtmak, sistemin toplam performansını ciddi oranda artırır.

---

### 📉 Least Connections (En Az Bağlantı)

Round Robin algoritmaları "sıradaki gelsin" mantığıyla çalışırken, sunucuların o an ne kadar terlediğini umursamazlar. **Least Connections** ise daha duyarlıdır; "Sıra kimde?" yerine "Şu an en boşta olan kim?" diye sorar.

#### Nasıl Çalışır?
Load Balancer, her sunucudaki aktif bağlantı (session) sayısını anlık olarak takip eder:
1. Bir istek geldiğinde, tüm sunucuların o anki aktif iş yüküne bakar.
2. İsteği, o an **en az aktif bağlantıya** sahip olan sunucuya paslar.
3. Sunucunun işi bittiğinde bağlantı sayısı düşer, böylece algoritma dinamik olarak güncel kalır.

#### Ne Zaman Tercih Edilir? 
*   **Değişken İşlem Süreleri:** Bazı istekler 2 saniye, bazıları 2 dakika sürüyorsa (örneğin dosya indirme vs. basit API çağrısı), Round Robin sınıfta kalır. Least Connections ise meşgul sunucuyu hemen fark eder.
*   **Benzer Donanım, Farklı Yük:** Sunucuların güçleri benzer olsa bile, kullanıcıların oturum süreleri çok farklıysa tercih edilir.

#### Gerçek Hayat Örneği
Restoran örneğimize devam edelim. Karşılama görevlisi artık sadece sıraya bakmıyor; garsonların masalarına bakıyor. Ahmet'in 2 masası var ama masadakiler sadece kahve içiyor (hızlı işlem). Mehmet'in tek masası var ama o masa 10 kişilik bir kutlama yemeği yiyor (uzun işlem). Görevli, Mehmet'in masası "dolu" olduğu için yeni gelenleri Ahmet'e veya tamamen boş olan garsona yönlendirir.

#### Artıları ve Eksileri
*   ✅ **Dinamik denge:** Sistemin o anki gerçek yüküne göre karar verir.
*   ✅ **Aşırı Yüklemeyi Önler:** Hiçbir sunucunun aktif bağlantı altında ezilmesine izin vermez.
*   ❌ **Takip Maliyeti:** Her sunucunun aktif bağlantı sayısını sürekli izlemek (state tracking) Load Balancer üzerinde ek bir yük oluşturur.
*   ❌ **Donanım Körlüğü:** Sunucuların güçleri birbirinden çok farklıysa (bir server çok hızlı, diğeri yavaş), sadece bağlantı sayısına bakmak yine yanıltıcı olabilir. (Bu durumda "Weighted Least Connections" devreye girer).

---

> [!NOTE]
> **Mühendislik Notu:** Bu algoritma, "Sticky Sessions" (Oturum Sadakati) gerektiren uygulamalarla (örneğin kullanıcının tüm işlemlerini aynı sunucuda yapması gereken durumlar) kombine edildiğinde oldukça verimli çalışır.

---

### ⚡ Least Response Time (En Az Yanıt Süresi)

Bazı sunucular donanımsal olarak aynı olsa bile ağ yoğunluğu, o an çalışan bir arka plan işlemi veya fiziksel konumları nedeniyle daha yavaş cevap verebilirler. **Least Response Time** algoritması, "Kimin kaç bağlantısı var?" sorusunu bir kenara bırakır ve doğrudan sonuca odaklanır: **"En hızlı kim?"**

#### Nasıl Çalışır? (How it Works)
Load Balancer, her sunucuya gönderilen isteklerin ne kadar sürede tamamlandığını (latency) sürekli ölçer:
1. Her sunucunun son performans verisi bir tabloda tutulur.
2. Yeni bir istek geldiğinde, tablodaki **en düşük yanıt süresine** sahip sunucu seçilir.
3. İstek tamamlandığında, sunucunun yeni süresi tabloya işlenir. Algoritma böylece dinamik olarak en "formda" sunucuyu bulur.

#### Ne Zaman Tercih Edilir? (When to Use)
*   **Hassas Kullanıcı Deneyimi:** Milisaniyelerin bile önemli olduğu, kullanıcının hızı hissettiği sistemlerde (örn. finansal işlemler, gerçek zamanlı veriler).
*   **Değişken Ağ Koşulları:** Sunucuların farklı veri merkezlerinde olduğu ve aradaki ağ gecikmesinin sürekli değiştiği durumlarda.

#### Gerçek Hayat Örneği
Restoranımızda işler iyice profesyonelleşti. Karşılama görevlisi artık garsonların kaç masası olduğuna bakmıyor; garsonun bir masaya siparişi ne kadar hızlı götürdüğüne (performansına) bakıyor. Ahmet bugün çok formda ve masalara jet hızıyla servis yapıyor. Mehmet ise yorgun ve adımları yavaşlamış. Görevli, Mehmet'in masası boş olsa bile, daha hızlı servis yapacağı kesin olan Ahmet'e yönlendiriyor müşterileri.

#### Artıları ve Eksileri (Benefits & Drawbacks)
*   ✅ **Minimum Gecikme:** Kullanıcıya her zaman en hızlı cevabı verecek sunucuyu seçerek performansı maksimize eder.
*   ✅ **Dinamik Adaptasyon:** Sunuculardan biri yavaşladığında algoritma bunu anında fark eder ve trafiği o sunucudan uzaklaştırır.
*   ❌ **Ölçüm Zorluğu:** Dağıtık bir sistemde her bir isteğin süresini doğru ölçmek ve bu veriyi anlık güncellemek Load Balancer üzerinde ek bir işlem yükü yaratır.
*   ❌ **Yanıltıcı Veri:** Bir sunucu çok basit bir hatayı çok hızlı döndüğü için (örn. 404 hatası) algoritma onu "en hızlı" sanıp tüm trafiği oraya yığabilir. (Bu yüzden genellikle sağlık kontrolleriyle beraber kullanılır).

---

> [!TIP]
> **Mühendislik Notu:** Bu algoritmayı kullanırken genellikle sadece "yanıt süresi"ne değil, aynı zamanda "aktif bağlantı sayısı"na da bakılır. Buna **Least Connections + Least Response Time** kombinasyonu denir ve modern bulut sağlayıcılarının (AWS ALB gibi) en gelişmiş modlarından biridir.

---

### 🔑 IP Hashing (Statik Eşleştirme)

Diğer tüm algoritmalar isteği her seferinde farklı bir sunucuya gönderebilirken, **IP Hashing** kararlılığa odaklanır. Bu algoritma, bir kullanıcıyı (IP adresini) belirli bir sunucuya "mühürler".

#### Nasıl Çalışır? (How it Works)
Algoritma, gelen isteğin IP adresini alır ve bir matematiksel fonksiyondan (Hash) geçirerek bir sayıya dönüştürür:
1. İstemcinin IP'si (örn. `192.168.0.1`) alınır.
2. Bu IP üzerinden bir `hash` değeri hesaplanır.
3. Çıkan sonuç, sunucu sayısına bölünür (Mod işlemi) ve kalan sayı hedef sunucunun indeksi olur.
4. **Sonuç:** IP adresi değişmediği sürece, o kullanıcı her zaman aynı sunucuya gider.

#### Ne Zaman Tercih Edilir? (When to Use)
*   **Oturum Sadakati (Sticky Sessions):** Kullanıcının sepet verileri veya oturum bilgileri (session) sadece o sunucunun belleğinde tutuluyorsa, kullanıcının hep aynı yere gitmesi hayati önem taşır.
*   **Önbellek (Caching) Verimliliği:** Sunucu, belirli bir kullanıcı için hazırladığı verileri önbelleğinde tutuyorsa, kullanıcının aynı sunucuya dönmesi hızı artırır.

#### Gerçek Hayat Örneği
Restoranımıza veda ederken son bir örnek verelim: Bu sefer karşılama görevlisi müşterileri tanıyan biridir. "Siz geçen geldiğinizde Ahmet size hizmet etmişti ve tercihlerinizi biliyor, sizi yine Ahmet'in masasına alalım" der. Böylece Ahmet müşteriyi sıfırdan tanımak zorunda kalmaz, süreç çok daha hızlı akar.

#### Artıları ve Eksileri (Benefits & Drawbacks)
*   ✅ **Devamlılık:** Uygulama tarafında ekstra bir "session paylaşım" mekanizması kurmaya gerek kalmadan oturum sürekliliği sağlar.
*   ✅ **Basit ve Deterministik:** Hangi IP'nin nereye gideceği matematiksel olarak bellidir, ekstra bir takip tablosu gerektirmez.
*   ❌ **Dengesiz Yük Riski:** Eğer bir IP adresi (örneğin büyük bir şirketin ana çıkış IP'si) çok fazla trafik oluşturuyorsa, o IP'nin atandığı sunucu aşırı yüklenirken diğerleri boş yatabilir.
*   ❌ **Esneklik Kaybı:** Bir sunucu çöktüğünde o sunucuya atanmış olan tüm IP'lerin eşleşmesi bozulur ve kullanıcılar başka sunuculara yönlendirildiğinde oturumları (eğer merkezi değilse) kaybolur.

---

> [!CAUTION]
> **Mühendislik Notu:** Modern sistemlerde sunucu eklendiğinde veya çıkarıldığında tüm eşleşmelerin bozulmaması için **Consistent Hashing** (Tutarlı Karma) adı verilen daha gelişmiş bir versiyonu kullanılır. Eğer sistemin çok sık ölçekleniyorsa (auto-scaling), standart IP Hash yerine Consistent Hashing'e göz atmalısın.
