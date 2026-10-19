<div align="center">
  <img src="images/app_icon_512.png" alt="कुंजिका लोगो" width="128" style="border-radius: 28px; box-shadow: 0 8px 24px rgba(245, 158, 11, 0.25);" />
  <h1>साइबर सुरक्षा जागरूकता व कुंजिका (Kunjika) हिंदी गाइड</h1>
  <p><em>Local-first Password Manager, 2FA Authentication और Android वॉल्ट गाइड</em></p>
  <p> <strong><a href="https://dwinsi.github.io/kunjika/hindi.html"> इंटरैक्टिव हिंदी वेब पेज खोलें (Interactive Web Page)</a></strong></p>
</div>

[![Version](https://img.shields.io/badge/version-1.9.0-F59E0B?style=flat-square&labelColor=07090E&color=F59E0B)](../RELEASE_NOTES.md)
[![Security](https://img.shields.io/badge/डिज़ाइन-Local--First-34D399?style=flat-square&labelColor=064E3B&color=34D399)](security.md)
[![Companion](https://img.shields.io/badge/Web%20Drop-बिना%20इंटरनेट%20PC%20ट्रांसफर-818CF8?style=flat-square&labelColor=07090E&color=818CF8)](https://dwinsi.github.io/kunjika/web-companion/)

---

##  साइबर सुरक्षा जागरूकता (Cybersecurity Awareness)

भारत में आज हर व्यक्ति डिजिटल पेमेंट (UPI), नेट बैंकिंग, सोशल मीडिया और ईमेल का उपयोग करता है। लेकिन ज्यादातर लोग साइबर खतरों के बारे में नहीं जानते। आइए बुनियादी बातें समझें:

### 1. पासवर्ड मैनेजर क्या है? (What is a Password Manager?)
- **सरल उपमा (Analogy):** जैसे घर के सभी कीमती सामान को रखने के लिए एक मजबूत लॉकर (Vault) होता है जिसकी सिर्फ एक मास्टर चाबी होती है।
- **यह क्या करता है:** 
  - यह आपके सभी 50-100 खातों के लिए कठिन और अलग-अलग पासवर्ड खुद बनाता है।
  - आपको केवल एक **Master PIN** याद रखना होता है।
  - **Autofill:** Android Autofill चालू करने के बाद समर्थित ऐप्स और वेबसाइटों में Kunjika से credential चुनें। उपलब्धता ऐप, वेबसाइट और Android के Autofill समर्थन पर निर्भर करती है।

### 2. जटिल पासवर्ड की जरूरत क्यों है? (Why We Need Complex Passwords)
हैकर्स खुद बैठकर पासवर्ड नहीं सोचते; वे स्वचालित कंप्यूटर बॉट्स (Supercomputers) चलाते हैं:
- **ब्रूट फोर्स अटैक (Brute Force Attack):** कंप्यूटर प्रति सेकंड अरबों पासवर्ड ट्राइ करता है। `123456` या `password` **1 सेकंड से भी कम** में टूट जाता है।
- **डिक्शनरी अटैक (Dictionary Attack):** नाम, शहर, भगवान का नाम या जन्मदिन (जैसे `Amit@1995` या `Pooja#123`) हैकर्स के सॉफ्टवेयर सबसे पहले ट्राई करते हैं।
- **क्रेडेंशियल स्टफिंग (Credential Stuffing):** जब किसी छोटी वेबसाइट का डेटा लीक होता है, तो हैकर्स वही ईमेल-पासवर्ड आपके बैंक और जीमेल पर डालकर देखते हैं।

####  मजबूत पासवर्ड कैसे चुनें? (Password Strength)
- लंबे, अनोखे और हर account के लिए अलग password चुनें।
- आम शब्द, व्यक्तिगत जानकारी और दोहराए गए password से बचें।
- अनुमानित crack-time केवल एक अनुमान है; परिणाम password, attacker के संसाधनों और इस्तेमाल किए गए अनुमान पर निर्भर करते हैं। कोई password “अटूट” नहीं है।

---

### 3. 2FA (Two-Factor Authentication) क्या होता है?
**2FA** का मतलब है **दोहरी सुरक्षा घेरा**। 
- **Factor 1 (Something you know):** आपका पासवर्ड।
- **Factor 2 (Something you have):** आपके फोन पर बनने वाला 6-अंकों का गुप्त कोड (TOTP)।

TOTP code password के अतिरिक्त एक सुरक्षा जांच देता है, लेकिन यह हर तरह के phishing या account takeover को नहीं रोकता। यदि password और TOTP secret एक ही vault में हैं, तो दोनों उसी device की सुरक्षा पर निर्भर करते हैं।

#### SMS OTP बनाम ऑथेंटिकेटर ऐप (Authenticator App / TOTP)
- **SMS OTP क्यों कमजोर है?**
  - **SIM Swapping:** धोखेबाज फर्जी कागजात से आपके नाम का दूसरा सिम कार्ड निकलवा लेते हैं और बैंक एसएमएस उनके पास चला जाता है।
  - **Network Interception:** टेलीकॉम नेटवर्क (SS7) से एसएमएस हवा में हैक किए जा सकते हैं।
- **Authenticator App (TOTP) का लाभ:**
  - यह code फोन की घड़ी और secret key से बनता है और आम तौर पर **flight mode में भी काम करता है**।
  - यह SMS पर निर्भर नहीं करता, लेकिन device या authenticator secret compromise होने पर जोखिम बना रहता है।
  - **कुंजिका में यह 2FA Authenticator अंदर ही इन-बिल्ट है।**

---

### 4. हाल के बड़े डेटा ब्रीच (Real-World Data Breaches - असली उदाहरण)
1. **LastPass क्लाउड हैक (2022-2023):** दुनिया के सबसे बड़े ऑनलाइन पासवर्ड मैनेजर के क्लाउड सर्वर से हैकर्स ने लाखों यूज़र्स के एन्क्रिप्टेड वॉल्ट्स चुरा लिए।  
   *सबक:* हर सेवा का डेटा और सुरक्षा मॉडल अलग होता है। कुंजिका cloud sync उपलब्ध नहीं कराता और आपका vault स्थानीय रूप से रखता है; बैकअप और ट्रांसफर आप शुरू करते हैं।
2. **RockYou2024 (10 अरब पासवर्ड्स):** वर्ष 2024 में डार्क वेब पर 9.94 अरब असली पासवर्ड्स लीक हुए। अगर आप पुराना पासवर्ड दोहराते हैं, तो वह पहले से ही हैकर्स के पास है।
3. **81.5 करोड़ भारतीयों का डेटा लीक (2023):** डार्क वेब पर आधार और आईसीएमआर का विशाल डेटा बिका, जिससे साइबर फ्रॉड और फर्जी कॉल्स बढ़ीं।
4. **Domino's और BigBasket लीक्स:** करोड़ों भारतीयों के फोन नंबर और पासवर्ड्स सार्वजनिक हुए।

---

### साइबर सुरक्षा के 5 सुनहरे नियम (5 Golden Rules)
1. **एक पासवर्ड, एक अकाउंट:** कभी भी एक ही पासवर्ड दो अलग-अलग जगह इस्तेमाल न करें।
2. **हर जगह 2FA ऑन करें:** Gmail, WhatsApp, Instagram और बैंकिंग में TOTP 2FA चालू करें।
3. **डायरी या मैसेज में न लिखें:** पासवर्ड को व्हाट्सएप चैट, स्क्रीनशॉट या नोटबुक में न रखें।
4. **फर्जी लिंक से बचें:** लॉटरी, बिजली बिल या मुफ्त ऑफर वाले अनजान लिंक पर कभी क्लिक न करें।
5. **कुंजिका (Kunjika) का उपयोग करें:** local-first vault में पासवर्ड रखें और तय करें कि कब कोई चुनी हुई जानकारी कॉपी, export या transfer करनी है।

---

## कुंजिका (Kunjika) क्यों इस्तेमाल करें? (Why Kunjika?)

1. **Local-first vault:** Kunjika account या cloud sync की आवश्यकता नहीं। Android app manifest में `INTERNET` permission हटाई गई है; फिर भी आप Autofill, clipboard, encrypted backup, QR और Bluetooth से चुनी हुई जानकारी share कर सकते हैं।
2. **एन्क्रिप्टेड स्थानीय स्टोरेज:** SQLCipher database और Android Keystore का उपयोग करते हैं। Password और TOTP secret fields भी AES-GCM से सुरक्षित हैं; hardware backing device पर निर्भर करती है।
3. **इन-बिल्ट 2FA ऑथेंटिकेटर:** TOTP secret को vault entry के साथ सहेजें। अधिक separation पसंद हो तो अलग authenticator इस्तेमाल कर सकते हैं।
4. **वेब ड्रॉप (Web Drop):** hosted companion page खोलें, QR scan करें, फोन पर pairing authorize करें, फिर कंप्यूटर पर Bluetooth pairing शुरू करें। दोनों स्क्रीन के codes मिलाकर browser और फोन पर transfer approve करें। Credential Bluetooth से सीधे browser तक जाता है; receiving computer और browser trusted होने चाहिए।
5. **सुरक्षित बैकअप (Encrypted Backup):** नया फोन लेने पर सुरक्षित पासवर्ड से फाइल ट्रांसफर करें।
6. **Open source:** Kunjika MIT License के तहत प्रकाशित है। मौजूदा मूल्य और उपलब्धता Google Play listing में देखें।

---

## इस ऐप का उपयोग कैसे करें? (Step-by-Step Guide)

| 1. वेलकम स्क्रीन | 2. मास्टर पिन व फिंगरप्रिंट |
| :---: | :---: |
| ![Welcome Screen](images/screenshot_phone_01.png) | ![Unlock Screen](images/screenshot_phone_02.png) |

### कदम 1: पहली बार सेटअप (First Time Setup)
1. ऐप इंस्टॉल करें और खोलें।
2. कम से कम **6 अंकों का Master PIN** सेट करें और कन्फर्म करें।
3. समर्थित device पर optional **Biometric App Access** चालू कर सकते हैं। ध्यान दें: मौजूदा no-saved-PIN flow में biometric prompt के बाद app खुल सकता है, भले Master PIN verify न हुआ हो। विवरण के लिए Security Guide देखें।

> [!WARNING]
> **मास्टर पिन हमेशा याद रखें!** चूंकि ऐप ऑफलाइन है, इसलिए यहां "Forgot Password" का कोई विकल्प नहीं होता।

---

### कदम 2: मजबूत पासवर्ड बनाएं (Generator Tab)

<div align="center">
  <img src="images/screenshot_phone_03.png" alt="पासवर्ड जनरेटर" width="300" />
</div>

1. नीचे दिए गए मेन्यू में ** Generator** टैब पर जाएं।
2. प्रकार चुनें:
   - **Password:** अक्षरों, अंकों और चिन्हों का मजबूत मेल (जैसे: `k8#Q!v9$L2z*M`)
   - **Passphrase:** याद रखने योग्य 4-5 आसान शब्द (जैसे: `kunjika-suraksha-sundar-deep`)
   - **PIN:** बैंक या यूपीआई के लिए सुरक्षित नंबर पिन।
3. स्लाइडर से लंबाई बढ़ाएं (कम से कम 16 अक्षर सुरक्षित होते हैं)।
4. **Copy** दबाकर इस्तेमाल करें या **Save** दबाकर सीधे वॉल्ट में सहेजें।

---

### कदम 3: पासवर्ड सेव और मैनेज करें (Vault Tab)

<div align="center">
  <img src="images/screenshot_phone_04.png" alt="वॉल्ट लिस्ट" width="300" />
</div>

1. ** Vault** टैब पर जाएं और **'+'** (प्लस) बटन दबाएं।
2. ऐप या वेबसाइट का नाम (जैसे SBI, Gmail, Amazon), यूजरनेम और पासवर्ड भरें।
3. श्रेणी चुनें: **Personal**, **Work**, **Finance**, या **Social**।
4. ज़रूरत पड़ने पर रिकवरी कोड्स या सीक्रेट नोट्स भी जोड़ें।
5. Favorite control दबाकर अक्सर उपयोग होने वाली entries को पसंदीदा में रखें।

---

### कदम 4: कंप्यूटर/लैपटॉप पर पासवर्ड भेजें (Web Drop)
1. अपने कंप्यूटर ब्राउज़र में खोलें: [`https://dwinsi.github.io/kunjika/web-companion/`](https://dwinsi.github.io/kunjika/web-companion/)
2. स्क्रीन पर आने वाले क्यूआर कोड को फोन से स्कैन करें।
3. फोन पर **Authorize Pairing** दबाएं और biometrics से अनुमति दें।
4. कंप्यूटर पर **Pair Bluetooth** दबाएं और browser के device picker में अपना फोन चुनें।
5. दोनों स्क्रीन के छह अंकों के codes मिलाएं; browser में confirm करें और फोन पर भेजने की अनुमति दें।
6. Credential browser में masked दिखेगा। Copy करना आपकी कार्रवाई है; clipboard manager इसे सहेज सकता है। Page बंद करने पर visible fields साफ होते हैं, लेकिन JavaScript memory या clipboard history मिटाने की guarantee नहीं देता।

---

### कदम 5: ऑटोफिल (Autofill) और बैकअप
- **Autofill चालू करें:** Kunjika सेटिंग्स में जाकर 'Autofill Service' चालू करें। समर्थित ऐप्स और वेबसाइटों में Android Autofill से credential चुनें; कुछ सेवाएं अतिरिक्त authentication मांग सकती हैं।
- **सुरक्षित बैकअप:** सेटिंग्स में 'Export Encrypted Backup' चुनें और एक मजबूत बैकअप पासवर्ड डालकर फाइल को सुरक्षित स्थान पर रखें।

---

## अक्सर पूछे जाने वाले सवाल (FAQ)

### 1. क्या कुंजिका vault को internet पर sync करती है?
नहीं। Kunjika account या cloud sync नहीं देता और Android manifest में `INTERNET` permission हटाई गई है। चुनी हुई जानकारी clipboard, Autofill, backup, QR या Bluetooth से share हो सकती है। Optional Play Integrity token request Google Play services के माध्यम से होता है।

### 2. यदि मेरा फोन खो जाए, खराब हो जाए या मैं नया फोन खरीदूँ, तो क्या मैं अपने पासवर्ड नए फोन में वापस पा सकता हूँ?
यदि आपने पहले से encrypted `.kunjika` backup बनाया है, तो उसे नए फोन पर import करने के लिए वही backup passphrase चाहिए। Backup और passphrase अलग-अलग सुरक्षित रखें। केवल स्थानीय database recovery PDF नए फोन पर entries restore नहीं करती।

### 3. मैं अपनी बैकअप `.kunjika` फाइल पेनड्राइव (USB), SD कार्ड या लोकल फोल्डर में कैसे सेव करूँ?
ऐप की **सेटिंग्स → Encrypted Offline Backup → Save File** पर टैप करें। कुंजिका एंड्रॉयड के ऑफिशियल स्टोरेज पिकर (SAF) का इस्तेमाल करता है, जिससे आप पेनड्राइव, SD कार्ड या कंप्यूटर में बिना इंटरनेट के सीधे फाइल सेव कर सकते हैं।

### 4. क्या कुंजिका मुझे बैकअप लेने की याद दिलाता है?
अभी app कोई automatic backup reminder नहीं दिखाता। समय-समय पर encrypted backup बनाएं और उसे restore करने के लिए passphrase अलग से सुरक्षित रखें।

### 5. वॉल्ट खोलने के लिए फिंगरप्रिंट और मास्टर पिन दोनों (PIN + Fingerprint) की आवश्यकता क्यों होती है?
Kunjika में Master PIN और वैकल्पिक biometric app access हैं। मौजूदा no-saved-PIN flow में biometric prompt के बाद app खुल सकता है, भले Master PIN verify न हुआ हो। ये app-level controls हैं; compromised या rooted device पर इन्हें bypass किया जा सकता है।

### 6. "High-Security Entries" (हाई-सिक्योरिटी खाते) क्या हैं और ये मेरे बैंक खाते को कैसे सुरक्षित रखते हैं?
आप चुने हुए entries को **High-Security** mark कर सकते हैं। Device support और action के अनुसार password दिखाने या copy करने के लिए biometric re-verification मांगा जा सकता है। यह अतिरिक्त app-level check है, compromised device से सुरक्षा की guarantee नहीं।

### 7. एंटी-कोएर्शन (Duress PIN) क्या है और यदि कोई मुझसे जबर्दस्ती ऐप खुलवाए तो Decoy Vault मेरी सुरक्षा कैसे करता है?
वैकल्पिक **Duress PIN** डालने पर सामान्य vault list की जगह dummy entries वाला decoy view खुलता है। यह app-level सुविधा है और device तक पहुंच रखने वाले व्यक्ति या compromised operating system से सुरक्षा की guarantee नहीं देती।

### 8. बैकअप एक्सपोर्ट करते समय 5-सेकंड की उल्टी गिनती (काउंटडाउन) क्यों चलती है?
यह 5-सेकंड की देरी आपको अनचाहे export को cancel करने का समय देती है। यह unlocked device को नियंत्रित करने वाले व्यक्ति से सुरक्षा की guarantee नहीं है।

### 9. क्या ऐप इस्तेमाल करने के कोई पैसे लगते हैं?
Kunjika MIT License के तहत open-source है। वर्तमान मूल्य और उपलब्धता के लिए Google Play listing देखें।

---

<div align="center">
  <p><strong>कुंजिका - आपका पासवर्ड, आपका नियंत्रण।</strong></p>
  <p><a href="https://dwinsi.github.io/kunjika/hindi.html"> इंटरैक्टिव हिंदी वेब पेज खोलें</a> | <a href="user-guide.md">Read in English</a></p>
</div>
