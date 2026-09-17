# Nhóm chủ đề 2 — Tài liệu lý thuyết (đối chiếu code thật trong project CalmLearn)

Yêu cầu của thầy gồm 2 nội dung. Bên dưới là phần trình bày cho từng nội dung,
lấy ví dụ trực tiếp từ code của project (không dùng ví dụ ảo) để thuyết trình
cho thuyết phục.

> Trước khi bổ sung, project **còn thiếu**:
> - Nội dung 1: thiếu `RelativeLayout` và `TableLayout` (project mới chỉ dùng LinearLayout, ConstraintLayout, FrameLayout, ScrollView).
> - Nội dung 2: project **chưa có màn hình Đăng ký nào cả** (không có EditText/CheckBox/RadioButton + xử lý sự kiện).
>
> Đã bổ sung màn hình **Đăng ký** (`fragment_register.xml` + `RegisterFragment.kt`, mở từ **Đăng nhập**)
> để vừa lấp đủ 5 loại layout, vừa có đủ view + sự kiện cho nội dung 2.
>
> **Cập nhật (bước hoàn thiện luồng tài khoản):** đã dựng thêm màn hình **Đăng nhập**
> (`fragment_login.xml` + `LoginFragment.kt`) và **Quên mật khẩu**
> (`fragment_forgot_password.xml` + `ForgotPasswordFragment.kt`), nối đủ điều hướng
> Onboarding → Đăng nhập → Đăng ký / Quên mật khẩu, và tách logic kiểm tra + gọi xác thực
> ra `RegisterViewModel` / `LoginViewModel` / `ForgotPasswordViewModel` (Fragment chỉ còn hiển thị
> và nhận thao tác). Xem mục 4 (`README.md`/PR mô tả) để biết chi tiết vì sao chưa gọi dịch vụ
> xác thực thật — các ví dụ layout/View bên dưới vẫn còn nguyên trong `fragment_register.xml`.

---

## 1. Trình bày 5 loại layout + XML attributes + ví dụ minh họa

### 1.1. LinearLayout
Sắp xếp các view theo **một hàng hoặc một cột**, theo thứ tự lần lượt.

**Thuộc tính chính:** `android:orientation` (vertical/horizontal), `android:layout_weight`
(chia tỉ lệ không gian còn lại), `android:gravity` / `layout_gravity`, `android:weightSum`.

**Ví dụ trong project** — [`fragment_topic_detail.xml`](app/src/main/res/layout/fragment_topic_detail.xml):
```xml
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">
    ...
    <EditText
        android:layout_width="0dp"
        android:layout_weight="1"   <!-- chiếm hết phần còn lại sau icon search -->
        ... />
</LinearLayout>
```
Dùng nhiều nhất trong project: gần như mọi `item_*.xml` (danh sách RecyclerView) và mọi
khối "hàng ngang icon + chữ" đều là LinearLayout.

### 1.2. RelativeLayout
Định vị view **dựa trên vị trí tương đối** với view khác hoặc với parent
(không xếp tuần tự như LinearLayout).

**Thuộc tính chính:** `android:layout_alignParentTop/Bottom/Start/End`,
`android:layout_below`, `android:layout_above`, `android:layout_toStartOf/EndOf`,
`android:layout_centerInParent`.

**Ví dụ trong project** — [`fragment_register.xml`](app/src/main/res/layout/fragment_register.xml)
(màn hình Đăng ký mới thêm):
```xml
<RelativeLayout android:id="@+id/registerRoot" ...>

    <LinearLayout android:id="@+id/header"
        android:layout_alignParentTop="true" ... />

    <Button android:id="@+id/btnRegister"
        android:layout_alignParentBottom="true" ... />

    <androidx.core.widget.NestedScrollView
        android:layout_below="@id/header"
        android:layout_above="@id/btnRegister" ... />
</RelativeLayout>
```
→ Header luôn dính trên cùng, nút Đăng ký luôn dính dưới cùng, phần nội dung
form tự cuộn ở khoảng giữa còn lại — đây là bài toán kinh điển mà RelativeLayout
giải quyết gọn hơn LinearLayout.

### 1.3. ConstraintLayout
Định vị view bằng **ràng buộc (constraint)** hai chiều tới parent hoặc view khác,
giúp layout phẳng (ít lồng nhau), dựng UI phức tạp mà vẫn tối ưu hiệu năng.

**Thuộc tính chính:** `app:layout_constraintTop/Bottom/Start/EndToTopOf/BottomOf/...`,
`app:layout_constraintWidth_percent`, `layout_width/height = 0dp` (match constraint).

**Ví dụ trong project** — [`fragment_onboarding.xml`](app/src/main/res/layout/fragment_onboarding.xml):
```xml
<androidx.constraintlayout.widget.ConstraintLayout ...>
    <androidx.viewpager2.widget.ViewPager2
        android:layout_width="0dp"
        android:layout_height="0dp"
        app:layout_constraintTop_toBottomOf="@id/btnSkip"
        app:layout_constraintBottom_toTopOf="@id/dotsContainer"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />
</androidx.constraintlayout.widget.ConstraintLayout>
```
Cũng là root layout của `activity_main.xml` (chứa NavHostFragment + BottomNavigationView).

### 1.4. FrameLayout
Dùng để **chồng nhiều view lên nhau** (view thêm sau vẽ đè lên view thêm trước) —
hợp cho ảnh + badge, ảnh + lớp phủ, hoặc làm khung chứa 1 view duy nhất.

**Thuộc tính chính:** `android:layout_gravity` (canh vị trí từng view con bên trong khung),
`android:foreground`.

**Ví dụ trong project** — [`fragment_home.xml`](app/src/main/res/layout/fragment_home.xml)
(avatar + chấm online):
```xml
<FrameLayout android:layout_width="48dp" android:layout_height="48dp">
    <ImageView android:src="@drawable/ic_person" ... />
    <View
        android:layout_width="12dp"
        android:layout_height="12dp"
        android:layout_gravity="bottom|end"
        android:background="@drawable/bg_online_dot" />
</FrameLayout>
```
Và [`item_flashcard_page.xml`](app/src/main/res/layout/item_flashcard_page.xml) dùng
FrameLayout làm khung chứa mặt trước/sau của flashcard.

### 1.5. TableLayout
Sắp xếp view theo **dạng bảng (hàng/cột)**, mỗi `TableRow` là một hàng, số cột
tự động bằng số view nhiều nhất trong các hàng.

**Thuộc tính chính:** `android:stretchColumns` (cột nào giãn ra lấp đầy chiều rộng),
`android:shrinkColumns`, `android:collapseColumns`.

**Ví dụ trong project** — [`fragment_register.xml`](app/src/main/res/layout/fragment_register.xml):
```xml
<TableLayout
    android:id="@+id/tableForm"
    android:layout_width="match_parent"
    android:stretchColumns="1">   <!-- cột 1 (ô nhập) giãn hết chiều rộng còn lại -->

    <TableRow>
        <TextView android:text="@string/register_label_fullname" />  <!-- cột 0: nhãn -->
        <EditText android:id="@+id/etFullName" ... />                <!-- cột 1: ô nhập -->
    </TableRow>
    <TableRow> ... email ... </TableRow>
    <TableRow> ... password ... </TableRow>
    <TableRow> ... confirm password ... </TableRow>
</TableLayout>
```
→ 4 hàng "nhãn + ô nhập" luôn thẳng cột với nhau mà không cần tự tính margin thủ công.

*(Ghi chú: project còn dùng thêm `ScrollView`/`NestedScrollView` ở hầu hết các
fragment để cuộn nội dung dài hơn màn hình — có thể nhắc thêm nếu thầy hỏi mở rộng,
nhưng 5 loại bắt buộc là 5 loại ở trên.)*

---

## 2. Các View trong màn hình Đăng ký + XML attributes + danh sách Events + phương thức xử lý + ví dụ

File liên quan: [`fragment_register.xml`](app/src/main/res/layout/fragment_register.xml) và
[`RegisterFragment.kt`](app/src/main/java/com/education/calmlearn/ui/register/RegisterFragment.kt).

### 2.1. EditText — ô nhập liệu
**View dùng:** `etFullName`, `etEmail`, `etPassword`, `etConfirmPassword`.

**XML attributes tiêu biểu:**
- `android:hint` — chữ gợi ý khi ô trống.
- `android:inputType` — kiểu bàn phím/định dạng: `textPersonName`, `textEmailAddress`, `textPassword`.
- `android:maxLines` — giới hạn số dòng.
- `android:background` — ở đây dùng `@drawable/bg_input_field` (khung bo góc).

**Sự kiện (Events) & phương thức xử lý:**
| Sự kiện | Cách gán | Mục đích trong app |
|---|---|---|
| `TextChanged` | `addTextChangedListener(TextWatcher)` → override `afterTextChanged()` | Kiểm tra hợp lệ & bật/tắt nút Đăng ký theo thời gian thực |
| `FocusChange` | `setOnFocusChangeListener { view, hasFocus -> }` | Kiểm tra định dạng email ngay khi rời khỏi ô (trên `etEmail`) |

**Ví dụ code:**
```kotlin
val watcher = simpleTextWatcher { validateForm(showError = false) }
binding.etFullName.addTextChangedListener(watcher)
binding.etEmail.addTextChangedListener(watcher)

binding.etEmail.setOnFocusChangeListener { _, hasFocus ->
    if (!hasFocus) validateForm(showError = false)
}
```

### 2.2. RadioGroup / RadioButton — chọn giới tính (chọn 1 trong 3, không dùng Boolean)
**View dùng:** `rgGender` chứa `rbMale`, `rbFemale`, `rbOther` (Nam / Nữ / Khác — đúng 3 lựa chọn
theo thiết kế, đại diện bằng `enum class Gender { MALE, FEMALE, OTHER }` chứ không phải Boolean
`isGenderMale` như bản cũ).

**XML attributes tiêu biểu:** `android:orientation` + `android:weightSum` (trên RadioGroup),
`android:button="@null"` + `android:background` (selector `bg_gender_option_selector` đổi màu
theo `state_checked` để có dạng "pill" 3 ô bằng nhau thay vì nút tròn mặc định), `android:text`.

**Sự kiện & phương thức xử lý:**
| Sự kiện | Cách gán |
|---|---|
| `CheckedChanged` | `rgGender.setOnCheckedChangeListener { group, checkedId -> }` |

**Ví dụ code** (`RegisterFragment.kt`, cập nhật `RegisterViewModel.gender` thay vì Boolean cục bộ):
```kotlin
binding.rgGender.setOnCheckedChangeListener { _, checkedId ->
    viewModel.gender = when (checkedId) {
        R.id.rbMale -> Gender.MALE
        R.id.rbFemale -> Gender.FEMALE
        R.id.rbOther -> Gender.OTHER
        else -> null
    }
    viewModel.onFieldChanged()
}
```

### 2.3. CheckBox — đồng ý điều khoản
**View dùng:** `cbAgree`.

**XML attributes tiêu biểu:** `android:text`, `android:checked` (mặc định false).

**Sự kiện & phương thức xử lý:**
| Sự kiện | Cách gán |
|---|---|
| `CheckedChanged` | `cbAgree.setOnCheckedChangeListener { buttonView, isChecked -> }` |

**Ví dụ code:**
```kotlin
binding.cbAgree.setOnCheckedChangeListener { _, _ ->
    validateForm(showError = false)   // chưa tick thì nút Đăng ký vẫn bị khoá
}
```

### 2.4. Button — nút Đăng ký
**View dùng:** `btnRegister`.

**XML attributes tiêu biểu:** `android:enabled` (mặc định `false`, chỉ bật khi form hợp lệ),
`app:backgroundTint`, `app:cornerRadius` (bo góc dạng pill), `android:textAllCaps="false"`.

**Sự kiện & phương thức xử lý:**
| Sự kiện | Cách gán |
|---|---|
| `Click` | `btnRegister.setOnClickListener { }` |

**Ví dụ code** (logic kiểm tra + gọi xác thực nay nằm trong `RegisterViewModel.submit()`,
Fragment chỉ gọi và quan sát trạng thái):
```kotlin
binding.btnRegister.setOnClickListener { viewModel.submit() }

viewModel.uiState.observe(viewLifecycleOwner) { state ->
    when (state) {
        is FormUiState.Error -> binding.tvError.text = getString(state.reason.toMessageRes())
        FormUiState.Success -> findNavController().navigate(R.id.action_global_home)
        else -> Unit
    }
}
```
> Lưu ý: `RegisterViewModel` gọi `AuthRepository.register(...)`, hiện là
> `UnavailableAuthRepository` (chưa nối dịch vụ xác thực thật) nên `FormUiState.Success`
> sẽ không xảy ra cho tới khi nhóm chọn và nối một AuthRepository thật — xem `AuthRepository.kt`.

### 2.5. ImageView — nút quay lại
**View dùng:** `btnBack`.

**XML attributes tiêu biểu:** `android:src`, `android:tint`, `android:clickable="true"`,
`android:focusable="true"` (bắt buộc phải có 2 thuộc tính này thì ImageView mới nhận được sự kiện click).

**Sự kiện & phương thức xử lý:**
| Sự kiện | Cách gán |
|---|---|
| `Click` | `btnBack.setOnClickListener { findNavController().navigateUp() }` |

---

## 3. Luồng hoạt động màn hình Đăng ký (để demo trực tiếp khi thuyết trình)

1. Mở app → màn hình Onboarding → vuốt tới trang cuối → bấm **"Bắt đầu học"** (hoặc **"Bỏ qua"**)
   → cả hai đều chuyển sang màn hình **Đăng nhập** (`action_global_login`) vì project chưa có
   chế độ khách riêng, không được phép bỏ qua bước xác thực.
2. Ở màn hình Đăng nhập, bấm **"Đăng ký ngay"** → chuyển sang màn hình **Đăng ký**
   (`action_global_register`).
3. Gõ vào từng ô: nút "Đăng ký" tự chuyển từ mờ (disabled) sang rõ (enabled) khi
   toàn bộ điều kiện hợp lệ (đủ tên, email đúng định dạng, mật khẩu ≥ 6 ký tự — quy tắc tạm,
   xác nhận mật khẩu khớp, đã chọn giới tính, đã tick đồng ý điều khoản).
4. Bấm **Đăng ký** → nút chuyển sang trạng thái đang xử lý (ProgressBar) → vì `AuthRepository`
   hiện tại (`UnavailableAuthRepository`) chưa nối dịch vụ thật, màn hình sẽ hiện lỗi
   "Chức năng chưa khả dụng: dịch vụ xác thực chưa được cấu hình" thay vì Toast chúc mừng giả
   như bản cũ — đây là hành vi có chủ đích (không tạo tài khoản giả).
5. Bấm icon mũi tên (`btnBack`) hoặc **"Đăng nhập"** ở cuối form → quay lại màn hình Đăng nhập
   (không tạo thêm bản sao màn hình Đăng nhập trong back stack).
