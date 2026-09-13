# CalmLearn - Android Frontend

Project Android Studio (Kotlin + XML) tai hien lai giao dien app CalmLearn tu ban thiet ke goc (Google AI Studio).

## Trang thai

Luong tai khoan (Dang ky / Dang nhap / Quen mat khau / Dang xuat) da noi **Firebase Authentication
that** (khong con la mock). Trang chu va Ho so hien ten/email/gioi tinh doc tu tai khoan dang dang
nhap. Cac phan con lai (Bai hoc, Luyen tap, Tien do, Quiz...) van la giao dien tinh, du lieu hardcode
trong XML - chua ket noi backend.

## Cau hinh Firebase (bat buoc de dang ky/dang nhap that hoat dong)

Project da co san toan bo code goi Firebase (`FirebaseAuthRepository`), chi con thieu **cau hinh
rieng cua tung nguoi/nhom** (khong the commit chung vi gan voi 1 project Firebase cu the):

1. Vao https://console.firebase.google.com → **Add project** → dat ten tuy y (vd `calmlearn-dev`).
2. Trong project vua tao → bam icon Android → **Add app**:
   - Android package name: `com.example.calmlearn` (phai go dung, khong duoc sai 1 ky tu).
   - Cac buoc con lai (nickname, SHA-1) co the bo qua vi chua dung Google Sign-In.
   - Tai file **`google-services.json`** ve, thay the file placeholder tai
     `CalmLearn/app/google-services.json` (file cu chi la du lieu gia de project build duoc khi
     chua co cau hinh that - xem `CalmLearn/app/google-services.example.json` de biet cau truc).
3. Vao **Authentication** (menu trai) → tab **Sign-in method** → bat **Email/Password**.
4. Vao **Firestore Database** → **Create database** → chon 1 vung gan (vd `asia-southeast1`) → bat
   dau o che do **Production mode**.
5. Vao tab **Rules** cua Firestore, dan noi dung file [`firestore.rules`](firestore.rules) (trong
   thu muc goc project nay) vao roi bam **Publish**. Rule nay dam bao moi tai khoan **chi doc/ghi
   duoc ho so cua chinh minh**, khong ai xem duoc ho so nguoi khac.
6. Mo lai Android Studio → **Sync Gradle** → chay app tren emulator (co Google Play Services) hoac
   dien thoai that.

**Emulator vs dien thoai that:** khong can cau hinh gi khac nhau - Firebase la dich vu tren mang
(khong phai server chay tren laptop ban), nen ca emulator lan dien thoai that deu goi thang den
Firebase qua Internet. Chi luu y: emulator phai la image co **Google Play** (khong phai image
"Google APIs" thieu Play Services se khong chay duoc Firebase Auth).

**Email xac minh / dat lai mat khau** duoc Firebase tu gui va tu xu ly: nguoi dung bam vao lien ket
trong email se mo mot trang web do Firebase host san (khong phai man hinh trong app Android nay) de
xac minh / dat mat khau moi. Neu muon doi ten nguoi gui, ngon ngu email... vao **Authentication >
Templates** trong Firebase Console.

## Cau truc project

```
app/src/main/java/com/example/calmlearn/
    MainActivity.kt              # Host chua BottomNavigationView + Navigation graph
    ui/home/HomeFragment.kt      # Man hinh Trang chu
    ui/lessons/LessonsFragment.kt# Man hinh Bai hoc (co toggle Tu vung / Ngu phap)
    ui/practice/PracticeFragment.kt
    ui/progress/ProgressFragment.kt
    ui/profile/ProfileFragment.kt

app/src/main/res/
    layout/        # Toan bo giao dien XML (fragment_*.xml + cac item_*.xml)
    drawable/      # Icon vector + background bo tron
    values/        # colors.xml, strings.xml, themes.xml
    navigation/    # nav_graph.xml
    menu/          # bottom_nav_menu.xml
```

## Cach mo project

1. Mo Android Studio > Open > chon thu muc `CalmLearn`.
2. Doi Gradle sync xong (lan dau can internet de tai dependencies).
3. Chay tren emulator hoac thiet bi that (minSdk 24).

## Ghi chu

- 5 tab duoi cung (Trang chu, Bai hoc, Luyen tap, Tien do, Ho so) duoc dung lai chinh xac tu 5 man hinh cua ban thiet ke goc.
- Rieng 2 muc ngu phap cuoi trong tab "Ngu phap can ban" ("So sanh hon/nhat" va "Danh dong tu") la noi dung tam dien them vi ban thiet ke goc chi hien thi 4/6 muc khi xem - ban co the sua truc tiep trong `item_grammar_comparison.xml` va `item_grammar_gerund.xml` (hoac file strings.xml) cho dung noi dung that.
- Toan bo mau sac (xanh teal, cam san ho, vang amber) va icon duoc dung lai bang vector drawable, khong dung anh ngoai nen khong can internet khi build giao dien.
- XP/Streak o Trang chu va Ho so hien "0" cho moi tai khoan that (chua trien khai theo doi tien do that trong buoc nay) thay vi so lieu mau 440 XP/7 ngay truoc day - tranh gan nham thanh tich cho tai khoan moi.
