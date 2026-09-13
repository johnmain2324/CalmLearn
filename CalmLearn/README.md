# CalmLearn - Android Frontend

Project Android Studio (Kotlin + XML) tai hien lai giao dien app CalmLearn tu ban thiet ke goc (Google AI Studio).

## Trang thai

Day la phan **giao dien (frontend) tinh**, chua ket noi backend/API. Toan bo du lieu (ten, so lieu, tien do...) dang duoc hardcode truc tiep trong file XML de de doc va de thay doi. Buoc tiep theo se noi Retrofit + REST API + MongoDB nhu trong bang cong nghe da mo ta.

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
