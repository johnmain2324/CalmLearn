# Nhóm chủ đề 3 — Activity + Intent + Layout + truyền dữ liệu giữa các màn hình

Yêu cầu: minh họa Activity + Intent bằng một tính năng thật có 2-3 màn hình, có
truyền dữ liệu qua `putExtra`/`getIntent`. Bên dưới lấy ví dụ trực tiếp từ code
mới thêm vào project CalmLearn (không dùng ví dụ ảo).

> Trước khi bổ sung, project **còn thiếu**:
> - Toàn bộ app chỉ có **một Activity duy nhất** (`MainActivity`, khai báo trong
>   `AndroidManifest.xml`), mọi màn hình còn lại là 18 Fragment điều hướng bằng
>   Jetpack Navigation Component (`nav_graph.xml`, `NavController.navigate()`,
>   Safe Args/Bundle) — project **chưa từng dùng `Intent` để chuyển màn hình**,
>   chưa có `Intent.putExtra()`/`getIntent()`.
> - Trong `data/mock/MockData.kt` đã có sẵn danh sách `achievements: List<Achievement>`
>   và các layout mẫu `item_achievement_*.xml`, `item_badge_*.xml`, icon
>   `ic_medal`, `ic_lock`, `ic_check_circle` — nhưng **chưa có màn hình nào cho
>   phép bấm vào một huy hiệu để xem chi tiết**, dữ liệu huy hiệu chỉ được
>   `<include>` tĩnh (không có id, không bắt sự kiện) trong `fragment_progress.xml`.
>
> Đã bổ sung tính năng **"Huy hiệu của tôi"**: từ màn hình **Hồ sơ** (Fragment,
> vẫn ở trong luồng Navigation cũ), bấm vào dòng **"Huy hiệu của tôi"** sẽ mở
> một luồng **Activity độc lập** (không đụng tới back stack của Navigation
> Component): `AchievementListActivity` → `AchievementDetailActivity`, dùng
> `Intent` tường minh và `putExtra`/`getIntent` để truyền dữ liệu huy hiệu thật
> (lấy từ `MockData.achievements`) giữa hai Activity.

---

## 1. Sơ đồ luồng Activity + Intent

```
ProfileFragment (trong Navigation Component)
        │  startActivity(Intent(..., AchievementListActivity::class.java))
        ▼
AchievementListActivity            (Activity mới #1, activity_achievement_list.xml)
        │  detailLauncher.launch(
        │      Intent(..., AchievementDetailActivity::class.java)
        │          .putExtra(EXTRA_ACHIEVEMENT_ID, ...)
        │          .putExtra(EXTRA_ACHIEVEMENT_TITLE, ...)
        │          .putExtra(EXTRA_ACHIEVEMENT_DESCRIPTION, ...)
        │          .putExtra(EXTRA_ACHIEVEMENT_ICON_RES, ...)
        │          .putExtra(EXTRA_ACHIEVEMENT_UNLOCKED, ...)
        │          .putExtra(EXTRA_PROGRESS_CURRENT, ...)
        │          .putExtra(EXTRA_PROGRESS_TOTAL, ...))
        ▼
AchievementDetailActivity          (Activity mới #2, activity_achievement_detail.xml)
        │  setResult(RESULT_OK, Intent().putExtra(EXTRA_VIEWED_ACHIEVEMENT_ID, id))
        │  finish()
        ▼
AchievementListActivity nhận lại kết quả qua ActivityResultLauncher
→ đánh dấu huy hiệu đó là "đã xem" (icon check nhỏ ở góc item)
```

Cả hai Activity đều được khai báo trong `AndroidManifest.xml`:

```xml
<activity
    android:name=".ui.achievement.AchievementListActivity"
    android:exported="false"
    android:theme="@style/Theme.CalmLearn" />

<activity
    android:name=".ui.achievement.AchievementDetailActivity"
    android:exported="false"
    android:theme="@style/Theme.CalmLearn" />
```

`exported="false"` vì hai màn hình này chỉ được mở từ bên trong app (không phải
điểm vào từ Launcher/app khác), khác với `MainActivity` (`exported="true"` vì
có `intent-filter` LAUNCHER).

**Nguồn dữ liệu:** `Achievement` lấy từ `MockData.achievements`
(`data/mock/MockData.kt`) — dữ liệu mẫu trong bộ nhớ, giống mọi màn hình khác
của prototype (chưa nối Firestore cho phần này), không phải dữ liệu giả trông
như đã kết nối backend thật.

---

## 2. Điểm bắt đầu: `ProfileFragment` mở Activity bằng Intent

File: [`ProfileFragment.kt`](app/src/main/java/com/education/calmlearn/ui/profile/ProfileFragment.kt),
dòng mới thêm trong `onViewCreated`:

```kotlin
binding.rowAchievements.root.setOnClickListener {
    startActivity(Intent(requireContext(), AchievementListActivity::class.java))
}
```

`rowAchievements` là một dòng cài đặt mới trong `fragment_profile.xml`
(`item_settings_row_achievements.xml`, tái sử dụng đúng kiểu dòng "icon + tiêu
đề + mô tả + chevron" đã có sẵn cho `rowAbout`/`rowAccount`, dùng lại màu
`amber`, icon `ic_medal`, nền `bg_icon_circle_amber_light` để đồng bộ giao
diện). Đây là **sự kiện thật** (click chuột người dùng), không gọi Activity
sẵn khi vào màn hình.

---

## 3. `AchievementListActivity` — Activity #1

File: [`AchievementListActivity.kt`](app/src/main/java/com/education/calmlearn/ui/achievement/AchievementListActivity.kt),
layout: [`activity_achievement_list.xml`](app/src/main/res/layout/activity_achievement_list.xml).

- Header dùng lại đúng pattern `btnBack` (icon `ic_arrow_left`, nền `bg_card`)
  đã thấy ở `fragment_topic_detail.xml`, `fragment_register.xml`... để giao
  diện nhất quán với các màn hình còn lại.
- `RecyclerView` hiển thị toàn bộ `MockData.achievements` qua
  `AchievementAdapter` (item: `item_achievement_row.xml`, dùng lại
  `bg_card_stroke`, `bg_icon_circle_amber_light`/`bg_icon_circle_gray`,
  `bg_pill_teal_light`/`bg_pill_light_gray` tuỳ trạng thái mở khóa).
- Mỗi item có `OnClickListener` thật (không hardcode gọi sẵn):

```kotlin
adapter = AchievementAdapter(MockData.achievements) { achievement ->
    val intent = Intent(this, AchievementDetailActivity::class.java).apply {
        putExtra(AchievementDetailActivity.EXTRA_ACHIEVEMENT_ID, achievement.id)
        putExtra(AchievementDetailActivity.EXTRA_ACHIEVEMENT_TITLE, achievement.title)
        putExtra(AchievementDetailActivity.EXTRA_ACHIEVEMENT_DESCRIPTION, achievement.description)
        putExtra(AchievementDetailActivity.EXTRA_ACHIEVEMENT_ICON_RES, achievement.iconRes)
        putExtra(AchievementDetailActivity.EXTRA_ACHIEVEMENT_UNLOCKED, achievement.isUnlocked)
        putExtra(AchievementDetailActivity.EXTRA_PROGRESS_CURRENT, achievement.progressCurrent)
        putExtra(AchievementDetailActivity.EXTRA_PROGRESS_TOTAL, achievement.progressTotal)
    }
    detailLauncher.launch(intent)
}
```

**Bảng khóa (`key`) truyền qua `putExtra` → kiểu dữ liệu → API đọc lại ở phía nhận:**

| Key | Kiểu | Đọc ở `AchievementDetailActivity` bằng |
|---|---|---|
| `EXTRA_ACHIEVEMENT_ID` | `String` | `intent.getStringExtra(...)` |
| `EXTRA_ACHIEVEMENT_TITLE` | `String` | `intent.getStringExtra(...)` |
| `EXTRA_ACHIEVEMENT_DESCRIPTION` | `String` | `intent.getStringExtra(...)` |
| `EXTRA_ACHIEVEMENT_ICON_RES` | `Int` (drawable res id) | `intent.getIntExtra(..., default)` |
| `EXTRA_ACHIEVEMENT_UNLOCKED` | `Boolean` | `intent.getBooleanExtra(..., default)` |
| `EXTRA_PROGRESS_CURRENT` | `Int` | `intent.getIntExtra(..., default)` |
| `EXTRA_PROGRESS_TOTAL` | `Int` | `intent.getIntExtra(..., default)` |

Các hằng số `EXTRA_*` được khai báo trong `companion object` của chính
`AchievementDetailActivity` (Activity nhận tự khai báo key mình cần, Activity
gửi chỉ import và dùng lại — tránh trùng lặp/gõ sai chuỗi key).

**Cộng điểm — `ActivityResultLauncher` thay cho `startActivityForResult` cũ:**

```kotlin
private val detailLauncher = registerForActivityResult(
    ActivityResultContracts.StartActivityForResult()
) { result ->
    if (result.resultCode == Activity.RESULT_OK) {
        val viewedId = result.data?.getStringExtra(AchievementDetailActivity.EXTRA_VIEWED_ACHIEVEMENT_ID)
        if (viewedId != null) adapter.markSeen(viewedId)
    }
}
```

---

## 4. `AchievementDetailActivity` — Activity #2

File: [`AchievementDetailActivity.kt`](app/src/main/java/com/education/calmlearn/ui/achievement/AchievementDetailActivity.kt),
layout: [`activity_achievement_detail.xml`](app/src/main/res/layout/activity_achievement_detail.xml).

Đọc dữ liệu ngay trong `onCreate` bằng `intent.getXxxExtra(...)`:

```kotlin
val achievementId = intent.getStringExtra(EXTRA_ACHIEVEMENT_ID)
val title = intent.getStringExtra(EXTRA_ACHIEVEMENT_TITLE).orEmpty()
val description = intent.getStringExtra(EXTRA_ACHIEVEMENT_DESCRIPTION).orEmpty()
val iconRes = intent.getIntExtra(EXTRA_ACHIEVEMENT_ICON_RES, R.drawable.ic_medal)
val isUnlocked = intent.getBooleanExtra(EXTRA_ACHIEVEMENT_UNLOCKED, false)
val progressCurrent = intent.getIntExtra(EXTRA_PROGRESS_CURRENT, 0)
val progressTotal = intent.getIntExtra(EXTRA_PROGRESS_TOTAL, 0)
```

Nếu `achievementId` null (trường hợp lý thuyết: Activity bị hệ thống gọi lại
mà không có Intent hợp lệ) thì `finish()` ngay — không hiển thị màn hình rỗng.

Màn hình hiển thị icon lớn, tiêu đề, mô tả, pill trạng thái (Đã mở khóa/Đang
tiến hành, tô màu theo `isUnlocked`), `ProgressBar` (`progress_teal`/
`progress_amber` — tái sử dụng đúng 2 drawable progress đã có, giống cách
`fragment_home.xml` dùng cho thanh mục tiêu ngày).

**Trả kết quả về Activity cha** khi bấm nút "Đánh dấu đã xem":

```kotlin
binding.btnMarkViewed.setOnClickListener {
    val resultIntent = Intent().putExtra(EXTRA_VIEWED_ACHIEVEMENT_ID, achievementId)
    setResult(Activity.RESULT_OK, resultIntent)
    finish()
}
```

---

## 5. Danh sách file đã thêm / sửa

**Mới thêm:**
- `app/src/main/java/com/education/calmlearn/ui/achievement/AchievementListActivity.kt`
- `app/src/main/java/com/education/calmlearn/ui/achievement/AchievementDetailActivity.kt`
- `app/src/main/java/com/education/calmlearn/ui/achievement/AchievementAdapter.kt`
- `app/src/main/res/layout/activity_achievement_list.xml`
- `app/src/main/res/layout/activity_achievement_detail.xml`
- `app/src/main/res/layout/item_achievement_row.xml`
- `app/src/main/res/layout/item_settings_row_achievements.xml`
- `CalmLearn/NHOM_CHU_DE_3.md` (tài liệu này)

**Đã sửa:**
- `app/src/main/AndroidManifest.xml` — khai báo `AchievementListActivity`,
  `AchievementDetailActivity`.
- `app/src/main/res/layout/fragment_profile.xml` — thêm dòng
  `rowAchievements` (điểm mở luồng Activity mới).
- `app/src/main/java/com/education/calmlearn/ui/profile/ProfileFragment.kt` —
  gán sự kiện click cho `rowAchievements`.
- `app/src/main/res/values/strings.xml` — thêm chuỗi cho 2 màn hình mới và
  dòng "Huy hiệu của tôi" trong Hồ sơ.

---

## 6. Luồng hoạt động (để demo trực tiếp)

1. Mở app, đăng nhập (hoặc đã đăng nhập sẵn) → vào tab **Hồ sơ** (`profileFragment`,
   vẫn trong Navigation Component như cũ).
2. Cuộn xuống, bấm dòng **"Huy hiệu của tôi"** (nằm dưới khối "Giới thiệu Đề tài
   Đồ án") → `startActivity(Intent(...))` mở **`AchievementListActivity`** —
   đây là lần đầu tiên app có màn hình thứ hai không do Navigation Component
   quản lý.
3. Màn hình danh sách hiển thị 5 huy hiệu lấy từ `MockData.achievements` (3
   đã mở khóa, 2 đang tiến hành), có thanh trạng thái và tiến độ `x/y` cho
   từng huy hiệu.
4. Bấm vào một huy hiệu bất kỳ (ví dụ "7 Day Streak") →
   `detailLauncher.launch(intent)` mở **`AchievementDetailActivity`**, đồng
   thời 7 giá trị dữ liệu thật (id, tên, mô tả, icon, trạng thái, tiến độ)
   được truyền qua `putExtra`.
5. Màn hình chi tiết đọc lại toàn bộ dữ liệu bằng `getStringExtra`/
   `getIntExtra`/`getBooleanExtra`, hiển thị icon lớn, mô tả đầy đủ, thanh
   tiến độ đúng theo dữ liệu vừa nhận (không tính toán lại từ đầu).
6. Bấm **"Đánh dấu đã xem"** → Activity chi tiết gọi `setResult(RESULT_OK, ...)`
   kèm id huy hiệu rồi `finish()` → quay lại màn hình danh sách.
7. Màn hình danh sách nhận kết quả qua `ActivityResultLauncher`, hiển thị icon
   dấu tích nhỏ ở góc huy hiệu vừa xem — chứng minh dữ liệu đã đi **hai
   chiều**: cha → con (`putExtra`/`getIntent`) và con → cha (`setResult`/
   `ActivityResultLauncher`).
8. Bấm icon mũi tên quay lại (`btnBack`) ở màn hình danh sách để `finish()`
   và trở về đúng vị trí cũ trong tab **Hồ sơ** — back stack của Navigation
   Component (Onboarding → Login/Register → Home → các tab) không bị ảnh
   hưởng vì toàn bộ luồng Activity này nằm ngoài `NavHostFragment`.
