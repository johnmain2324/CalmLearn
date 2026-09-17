package com.education.calmlearn.data.mock

import com.education.calmlearn.R
import com.education.calmlearn.data.model.Achievement
import com.education.calmlearn.data.model.DailyActivity
import com.education.calmlearn.data.model.GrammarExample
import com.education.calmlearn.data.model.GrammarLesson
import com.education.calmlearn.data.model.ListeningLesson
import com.education.calmlearn.data.model.OnboardingPage
import com.education.calmlearn.data.model.QuizQuestion
import com.education.calmlearn.data.model.SpeakingItem
import com.education.calmlearn.data.model.SpeakingLevel
import com.education.calmlearn.data.model.Topic
import com.education.calmlearn.data.model.TranscriptLine
import com.education.calmlearn.data.model.User
import com.education.calmlearn.data.model.VocabWord

/**
 * In-memory sample data for the CalmLearn UI prototype.
 * Nothing here is persisted; the app restarts fresh every launch.
 */
object MockData {

    val currentUser = User(
        name = "Alex Nguyen",
        email = "alex.nguyen@student.edu.vn",
        level = "Sơ cấp A2",
        xp = 440,
        streakDays = 7,
        avatarInitials = "AN"
    )

    val onboardingPages = listOf(
        OnboardingPage(
            iconRes = R.drawable.ic_graduation_cap,
            title = "Học từ vựng theo chủ đề",
            description = "Khám phá hơn 300 từ vựng được phân loại theo 8 chủ đề thực tế: du lịch, công việc, ẩm thực và nhiều hơn nữa."
        ),
        OnboardingPage(
            iconRes = R.drawable.ic_headphones,
            title = "Luyện nghe hội thoại thực tế",
            description = "Nghe các đoạn hội thoại tự nhiên, đọc transcript và trả lời câu hỏi để cải thiện kỹ năng nghe hiểu."
        ),
        OnboardingPage(
            iconRes = R.drawable.ic_mic,
            title = "Luyện phát âm tự tin",
            description = "Thực hành phát âm theo 3 cấp độ Từ - Cụm từ - Câu, nhận phản hồi thân thiện và không áp lực."
        ),
        OnboardingPage(
            iconRes = R.drawable.ic_trending_up,
            title = "Theo dõi tiến độ mỗi ngày",
            description = "Duy trì chuỗi học tập, thu thập XP và huy hiệu, xem biểu đồ tiến bộ theo tuần của riêng bạn."
        )
    )

    val topics = listOf(
        Topic("travel", "Du lịch & Di chuyển", R.drawable.ic_airplane, R.drawable.bg_icon_square_teal, 42, 28),
        Topic("daily", "Đời sống hằng ngày", R.drawable.ic_coffee, R.drawable.bg_icon_square_coral, 50, 45),
        Topic("food", "Ẩm thực & Nhà hàng", R.drawable.ic_food, R.drawable.bg_icon_square_amber, 36, 19),
        Topic("tech", "Công nghệ & Thiết bị", R.drawable.ic_laptop, R.drawable.bg_icon_square_teal, 30, 12),
        Topic("school", "Trường học & Đại học", R.drawable.ic_graduation_cap, R.drawable.bg_icon_square_teal, 38, 30),
        Topic("work", "Công việc & Phỏng vấn", R.drawable.ic_briefcase, R.drawable.bg_icon_square_coral, 44, 16),
        Topic("social", "Giao tiếp xã hội", R.drawable.ic_chat, R.drawable.bg_icon_square_teal, 35, 22),
        Topic("movie", "Giải trí & Phim ảnh", R.drawable.ic_film, R.drawable.bg_icon_square_amber, 32, 8)
    )

    // 5 sample words per topic — enough to demonstrate list, detail and flashcard flows.
    val vocabWords: MutableList<VocabWord> = mutableListOf(
        VocabWord("travel_1", "travel", "journey", "/ˈdʒɜːni/", "n.", "hành trình, chuyến đi", "a trip from one place to another, especially a long one", "Our journey to the mountains took six hours.", "Hành trình của chúng tôi đến vùng núi mất sáu tiếng.", listOf("trip", "voyage"), isFavorite = true, isLearned = true),
        VocabWord("travel_2", "travel", "itinerary", "/aɪˈtɪnərəri/", "n.", "lịch trình", "a planned route or schedule for a journey", "She emailed everyone the full itinerary before the trip.", "Cô ấy đã gửi email lịch trình đầy đủ cho mọi người trước chuyến đi.", listOf("schedule", "plan"), isLearned = true),
        VocabWord("travel_3", "travel", "luggage", "/ˈlʌɡɪdʒ/", "n.", "hành lý", "bags and suitcases used to carry belongings while travelling", "Please keep your luggage with you at all times.", "Vui lòng giữ hành lý bên mình mọi lúc.", listOf("baggage"), isLearned = true),
        VocabWord("travel_4", "travel", "boarding pass", "/ˈbɔːdɪŋ pɑːs/", "n.", "thẻ lên máy bay", "a document allowing a passenger to board a flight", "Don't forget to show your boarding pass at the gate.", "Đừng quên xuất trình thẻ lên máy bay tại cổng.", listOf("ticket")),
        VocabWord("travel_5", "travel", "delay", "/dɪˈleɪ/", "n./v.", "sự trì hoãn / trì hoãn", "a period of time when something is later than expected", "The train delay made us miss our connection.", "Sự trì hoãn của tàu khiến chúng tôi lỡ chuyến nối tiếp.", listOf("postponement")),

        VocabWord("daily_1", "daily", "routine", "/ruːˈtiːn/", "n.", "thói quen hằng ngày", "the usual series of things you do at a particular time", "Waking up at 6 AM is part of my daily routine.", "Thức dậy lúc 6 giờ sáng là một phần thói quen hằng ngày của tôi.", listOf("habit"), isFavorite = true, isLearned = true),
        VocabWord("daily_2", "daily", "chore", "/tʃɔːr/", "n.", "việc vặt", "a routine task, especially a household one", "Washing dishes is my least favorite chore.", "Rửa bát là việc vặt tôi ít thích nhất.", listOf("task"), isLearned = true),
        VocabWord("daily_3", "daily", "errand", "/ˈerənd/", "n.", "việc lặt vặt cần đi ra ngoài", "a short trip to do a specific task, like shopping", "I need to run a few errands this afternoon.", "Chiều nay tôi cần đi làm vài việc lặt vặt.", listOf("task"), isLearned = true),
        VocabWord("daily_4", "daily", "commute", "/kəˈmjuːt/", "v./n.", "đi lại (giữa nhà và nơi làm)", "to travel regularly between home and work", "She commutes to the city by bus every day.", "Cô ấy đi làm bằng xe buýt vào thành phố mỗi ngày.", listOf("travel"), isLearned = true),
        VocabWord("daily_5", "daily", "leisure", "/ˈleʒər/", "n.", "thời gian rảnh rỗi", "time spent doing what you enjoy, not working", "He spends his leisure time reading novels.", "Anh ấy dành thời gian rảnh để đọc tiểu thuyết.", listOf("free time")),

        VocabWord("food_1", "food", "appetizer", "/ˈæpɪtaɪzər/", "n.", "món khai vị", "a small dish served before the main course", "We shared an appetizer while waiting for the main dish.", "Chúng tôi dùng chung một món khai vị trong lúc chờ món chính.", listOf("starter"), isFavorite = true),
        VocabWord("food_2", "food", "reservation", "/ˌrezərˈveɪʃn/", "n.", "sự đặt chỗ", "an arrangement to have a table, room, etc. kept for you", "I made a reservation for two at 7 PM.", "Tôi đã đặt chỗ cho hai người lúc 7 giờ tối.", listOf("booking")),
        VocabWord("food_3", "food", "recipe", "/ˈresəpi/", "n.", "công thức nấu ăn", "a set of instructions for preparing a dish", "This is my grandmother's recipe for spring rolls.", "Đây là công thức làm chả giò của bà tôi.", listOf("formula")),
        VocabWord("food_4", "food", "bland", "/blænd/", "adj.", "nhạt, ít gia vị", "having little or no flavor", "The soup tasted a bit bland without salt.", "Món súp có vị hơi nhạt khi thiếu muối.", listOf("tasteless")),
        VocabWord("food_5", "food", "leftovers", "/ˈleftoʊvərz/", "n.", "đồ ăn thừa", "food remaining after a meal", "We kept the leftovers in the fridge for tomorrow.", "Chúng tôi để đồ ăn thừa trong tủ lạnh cho ngày mai.", listOf("remains")),

        VocabWord("tech_1", "tech", "upgrade", "/ˈʌpɡreɪd/", "v./n.", "nâng cấp", "to improve a device or system to a better version", "I upgraded my phone's storage last week.", "Tôi đã nâng cấp dung lượng lưu trữ điện thoại tuần trước.", listOf("update")),
        VocabWord("tech_2", "tech", "battery life", "/ˈbætəri laɪf/", "n.", "thời lượng pin", "how long a device runs before needing a charge", "This laptop has excellent battery life.", "Chiếc laptop này có thời lượng pin rất tốt.", listOf("charge duration")),
        VocabWord("tech_3", "tech", "malware", "/ˈmælwer/", "n.", "phần mềm độc hại", "software designed to damage or disrupt a system", "The download contained hidden malware.", "Tệp tải xuống chứa phần mềm độc hại ẩn.", listOf("virus")),
        VocabWord("tech_4", "tech", "password", "/ˈpæswɜːrd/", "n.", "mật khẩu", "a secret word or phrase used to access an account", "Never share your password with anyone.", "Đừng bao giờ chia sẻ mật khẩu của bạn với bất kỳ ai.", listOf("passcode")),
        VocabWord("tech_5", "tech", "device", "/dɪˈvaɪs/", "n.", "thiết bị", "a piece of equipment made for a particular purpose", "Please turn off all electronic devices before takeoff.", "Vui lòng tắt tất cả thiết bị điện tử trước khi cất cánh.", listOf("gadget")),

        VocabWord("school_1", "school", "assignment", "/əˈsaɪnmənt/", "n.", "bài tập được giao", "a task or piece of work given to someone", "The assignment is due next Monday.", "Bài tập phải nộp vào thứ Hai tới.", listOf("homework")),
        VocabWord("school_2", "school", "lecture", "/ˈlektʃər/", "n.", "bài giảng", "an educational talk to an audience", "The professor gave a two-hour lecture on economics.", "Giáo sư đã giảng bài hai tiếng về kinh tế học.", listOf("class")),
        VocabWord("school_3", "school", "scholarship", "/ˈskɒlərʃɪp/", "n.", "học bổng", "financial aid given to a student for their studies", "She received a full scholarship to study abroad.", "Cô ấy được nhận học bổng toàn phần để du học.", listOf("grant")),
        VocabWord("school_4", "school", "deadline", "/ˈdedlaɪn/", "n.", "hạn chót", "the latest time by which something must be finished", "The submission deadline is Friday at noon.", "Hạn nộp bài là trưa thứ Sáu.", listOf("due date")),
        VocabWord("school_5", "school", "campus", "/ˈkæmpəs/", "n.", "khuôn viên trường", "the grounds and buildings of a university", "The campus has a large library and a sports center.", "Khuôn viên trường có thư viện lớn và trung tâm thể thao.", listOf("grounds")),

        VocabWord("work_1", "work", "resume", "/ˈrezʊmeɪ/", "n.", "sơ yếu lý lịch", "a document summarizing your work experience and skills", "Update your resume before applying for the job.", "Hãy cập nhật sơ yếu lý lịch trước khi ứng tuyển.", listOf("CV")),
        VocabWord("work_2", "work", "candidate", "/ˈkændɪdət/", "n.", "ứng viên", "a person who applies for a job", "Three candidates were shortlisted for the interview.", "Ba ứng viên đã được chọn vào vòng phỏng vấn.", listOf("applicant")),
        VocabWord("work_3", "work", "deadline", "/ˈdedlaɪn/", "n.", "hạn chót", "the latest time a task must be completed", "We must meet the project deadline this Friday.", "Chúng ta phải hoàn thành dự án đúng hạn thứ Sáu này.", listOf("due date")),
        VocabWord("work_4", "work", "promotion", "/prəˈmoʊʃn/", "n.", "sự thăng chức", "advancement to a higher position at work", "He got a promotion after two years at the company.", "Anh ấy được thăng chức sau hai năm làm việc tại công ty.", listOf("advancement")),
        VocabWord("work_5", "work", "colleague", "/ˈkɒliːɡ/", "n.", "đồng nghiệp", "a person you work with", "My colleague helped me finish the report.", "Đồng nghiệp của tôi đã giúp tôi hoàn thành báo cáo.", listOf("coworker")),

        VocabWord("social_1", "social", "acquaintance", "/əˈkweɪntəns/", "n.", "người quen", "someone you know but not closely", "He's just a business acquaintance, not a close friend.", "Anh ấy chỉ là người quen trong công việc, không phải bạn thân.", listOf("contact")),
        VocabWord("social_2", "social", "apologize", "/əˈpɒlədʒaɪz/", "v.", "xin lỗi", "to say sorry for something you did wrong", "She apologized for being late to the meeting.", "Cô ấy đã xin lỗi vì đến trễ cuộc họp.", listOf("say sorry")),
        VocabWord("social_3", "social", "compliment", "/ˈkɒmplɪmənt/", "n./v.", "lời khen / khen ngợi", "an expression of praise or admiration", "He complimented her on the excellent presentation.", "Anh ấy khen cô ấy về bài thuyết trình xuất sắc.", listOf("praise")),
        VocabWord("social_4", "social", "invite", "/ɪnˈvaɪt/", "v.", "mời", "to ask someone to attend an event", "We invited our neighbors to the housewarming party.", "Chúng tôi mời hàng xóm đến tiệc tân gia.", listOf("ask")),
        VocabWord("social_5", "social", "gathering", "/ˈɡæðərɪŋ/", "n.", "buổi tụ họp", "an occasion when people come together", "The family gathering happens every Lunar New Year.", "Buổi tụ họp gia đình diễn ra vào mỗi dịp Tết.", listOf("get-together")),

        VocabWord("movie_1", "movie", "plot", "/plɒt/", "n.", "cốt truyện", "the main events of a story", "The plot twist at the end surprised everyone.", "Tình tiết bất ngờ ở cuối phim khiến mọi người ngạc nhiên.", listOf("storyline")),
        VocabWord("movie_2", "movie", "subtitle", "/ˈsʌbtaɪtl/", "n.", "phụ đề", "translated text shown on screen during a film", "I watched the film with English subtitles.", "Tôi đã xem phim với phụ đề tiếng Anh.", listOf("caption")),
        VocabWord("movie_3", "movie", "sequel", "/ˈsiːkwəl/", "n.", "phần tiếp theo", "a film that continues the story of an earlier one", "The sequel was even better than the original.", "Phần tiếp theo còn hay hơn cả bản gốc.", listOf("follow-up")),
        VocabWord("movie_4", "movie", "cast", "/kæst/", "n.", "dàn diễn viên", "the actors who perform in a film", "The cast includes several award-winning actors.", "Dàn diễn viên gồm nhiều diễn viên từng đoạt giải.", listOf("actors")),
        VocabWord("movie_5", "movie", "premiere", "/prɪˈmɪər/", "n.", "buổi công chiếu", "the first public showing of a film", "Fans lined up outside the premiere all night.", "Người hâm mộ xếp hàng bên ngoài buổi công chiếu suốt đêm.", listOf("opening"))
    )

    fun wordsForTopic(topicId: String): List<VocabWord> = vocabWords.filter { it.topicId == topicId }

    val grammarLessons = listOf(
        GrammarLesson(
            id = "present_simple",
            levelTag = "A1 - Căn bản",
            title = "Hiện tại đơn",
            formula = "S + V(s/es) + O | Phủ định: S + do/does not + V_inf",
            explanation = "Dùng để diễn tả thói quen, sự thật hiển nhiên hoặc lịch trình cố định.",
            examples = listOf(
                GrammarExample("She works at a hospital.", "Cô ấy làm việc tại bệnh viện."),
                GrammarExample("The sun rises in the east.", "Mặt trời mọc ở hướng đông.")
            ),
            note = "Thêm -s/-es cho ngôi thứ 3 số ít (he/she/it).",
            quiz = listOf(
                QuizQuestion("ps_q1", "Chọn câu đúng ngữ pháp:", listOf("She work every day.", "She works every day.", "She working every day.", "She worked every day."), 1, "Ngôi thứ ba số ít (she) cần thêm -s vào động từ ở hiện tại đơn.")
            )
        ),
        GrammarLesson(
            id = "present_continuous",
            levelTag = "A1 - Căn bản",
            title = "Hiện tại tiếp diễn",
            formula = "S + am/is/are + V-ing",
            explanation = "Dùng để diễn tả hành động đang xảy ra tại thời điểm nói.",
            examples = listOf(
                GrammarExample("I am studying English right now.", "Tôi đang học tiếng Anh ngay lúc này."),
                GrammarExample("They are watching a movie.", "Họ đang xem phim.")
            ),
            note = "Không dùng thì này với các động từ chỉ trạng thái như: know, like, want.",
            quiz = listOf(
                QuizQuestion("pc_q1", "Điền vào chỗ trống: He ___ dinner now.", listOf("cook", "cooks", "is cooking", "cooked"), 2, "Có từ \"now\" nên cần dùng hiện tại tiếp diễn: is + V-ing.")
            )
        ),
        GrammarLesson(
            id = "past_simple",
            levelTag = "A2 - Cơ bản",
            title = "Quá khứ đơn",
            formula = "S + V2/V-ed | Phủ định: S + did not + V_inf",
            explanation = "Dùng để diễn tả hành động đã xảy ra và kết thúc trong quá khứ.",
            examples = listOf(
                GrammarExample("We visited Da Lat last summer.", "Chúng tôi đã đến Đà Lạt vào mùa hè trước."),
                GrammarExample("She didn't call me yesterday.", "Cô ấy đã không gọi cho tôi hôm qua.")
            ),
            note = "Nhiều động từ bất quy tắc không thêm -ed (go → went, eat → ate).",
            quiz = listOf(
                QuizQuestion("pas_q1", "Chọn câu đúng:", listOf("I go to school yesterday.", "I goed to school yesterday.", "I went to school yesterday.", "I going to school yesterday."), 2, "\"Go\" là động từ bất quy tắc: go → went ở quá khứ đơn.")
            )
        ),
        GrammarLesson(
            id = "future_simple",
            levelTag = "A2 - Cơ bản",
            title = "Tương lai đơn",
            formula = "S + will + V_inf | Phủ định: S + will not + V_inf",
            explanation = "Dùng để diễn tả dự đoán hoặc quyết định tức thời trong tương lai.",
            examples = listOf(
                GrammarExample("I will call you tomorrow.", "Tôi sẽ gọi cho bạn vào ngày mai."),
                GrammarExample("It will probably rain tonight.", "Có lẽ tối nay trời sẽ mưa.")
            ),
            note = "Will không đổi dạng theo chủ ngữ (không thêm -s ở ngôi thứ 3).",
            quiz = listOf(
                QuizQuestion("fs_q1", "Chọn câu đúng:", listOf("She wills help you.", "She will helps you.", "She will help you.", "She will to help you."), 2, "Sau \"will\" luôn dùng động từ nguyên mẫu (V_inf), không chia dạng.")
            )
        ),
        GrammarLesson(
            id = "comparison",
            levelTag = "A2 - Cơ bản",
            title = "So sánh hơn / so sánh nhất",
            formula = "So sánh hơn: S + to be + Adj-er/more Adj + than + O\nSo sánh nhất: S + to be + the Adj-est/most Adj",
            explanation = "Dùng để so sánh đặc điểm giữa hai hoặc nhiều đối tượng.",
            examples = listOf(
                GrammarExample("Hanoi is colder than Ho Chi Minh City.", "Hà Nội lạnh hơn Thành phố Hồ Chí Minh."),
                GrammarExample("This is the most interesting book I've read.", "Đây là cuốn sách thú vị nhất tôi từng đọc.")
            ),
            note = "Tính từ ngắn (1 âm tiết) thêm -er/-est; tính từ dài (2 âm tiết trở lên) dùng more/most.",
            quiz = listOf(
                QuizQuestion("cmp_q1", "Chọn câu đúng:", listOf("She is more tall than him.", "She is taller than him.", "She is tallest than him.", "She is the taller than him."), 1, "\"Tall\" là tính từ ngắn nên thêm -er: taller than.")
            )
        ),
        GrammarLesson(
            id = "gerund",
            levelTag = "A2 - Cơ bản",
            title = "Danh động từ (V-ing) & to-V",
            formula = "V-ing / to + V_inf sau một số động từ",
            explanation = "Một số động từ theo sau bởi V-ing (enjoy, avoid, finish...), một số khác theo sau bởi to-V (want, plan, decide...).",
            examples = listOf(
                GrammarExample("I enjoy reading books before bed.", "Tôi thích đọc sách trước khi ngủ."),
                GrammarExample("She decided to move to a new city.", "Cô ấy quyết định chuyển đến một thành phố mới.")
            ),
            note = "Học thuộc theo nhóm động từ vì không có quy tắc chung tuyệt đối.",
            quiz = listOf(
                QuizQuestion("ger_q1", "Chọn câu đúng:", listOf("I enjoy to swim.", "I enjoy swimming.", "I enjoy swim.", "I enjoy swam."), 1, "\"Enjoy\" luôn theo sau bởi V-ing: enjoy swimming.")
            )
        )
    )

    val speakingItems = listOf(
        SpeakingItem("sw_1", SpeakingLevel.WORD, "Journey", "/ˈdʒɜːni/", "hành trình, chuyến đi", "Nhấn trọng âm ở âm tiết đầu: JOUR-ney."),
        SpeakingItem("sw_2", SpeakingLevel.WORD, "Itinerary", "/aɪˈtɪnərəri/", "lịch trình", "Chú ý âm \"r\" mềm, trọng âm rơi vào âm tiết thứ hai."),
        SpeakingItem("sw_3", SpeakingLevel.WORD, "Luggage", "/ˈlʌɡɪdʒ/", "hành lý", "Âm \"g\" đọc nhẹ như trong \"badge\"."),
        SpeakingItem("sw_4", SpeakingLevel.WORD, "Reservation", "/ˌrezərˈveɪʃn/", "sự đặt chỗ", "Trọng âm rơi vào âm tiết thứ ba: re-ser-VA-tion."),
        SpeakingItem("sw_5", SpeakingLevel.WORD, "Colleague", "/ˈkɒliːɡ/", "đồng nghiệp", "Kết thúc bằng âm \"g\" cứng, không phát âm \"ue\"."),

        SpeakingItem("sp_1", SpeakingLevel.PHRASE, "Book a table", "/bʊk ə ˈteɪbl/", "đặt bàn", "Nối âm giữa \"book\" và \"a\" khi nói nhanh."),
        SpeakingItem("sp_2", SpeakingLevel.PHRASE, "Check in luggage", "/tʃek ɪn ˈlʌɡɪdʒ/", "gửi hành lý ký gửi", "Nhấn trọng âm vào \"check\" và \"lug-\"."),
        SpeakingItem("sp_3", SpeakingLevel.PHRASE, "Run some errands", "/rʌn sʌm ˈerəndz/", "đi làm vài việc vặt", "Âm \"s\" cuối \"errands\" phát âm nhẹ /z/."),
        SpeakingItem("sp_4", SpeakingLevel.PHRASE, "Meet the deadline", "/miːt ðə ˈdedlaɪn/", "hoàn thành đúng hạn", "Nối âm \"meet\" và \"the\" tự nhiên."),
        SpeakingItem("sp_5", SpeakingLevel.PHRASE, "Apply for a job", "/əˈplaɪ fɔːr ə dʒɒb/", "ứng tuyển công việc", "Trọng âm rơi vào \"PLY\" trong \"apply\"."),

        SpeakingItem("ss_1", SpeakingLevel.SENTENCE, "Could you tell me the way to the station?", "/kʊd juː tel miː ðə weɪ tuː ðə ˈsteɪʃn/", "Bạn có thể chỉ đường đến nhà ga giúp tôi không?", "Lên giọng nhẹ ở cuối câu hỏi lịch sự."),
        SpeakingItem("ss_2", SpeakingLevel.SENTENCE, "I would like to make a reservation for two.", "/aɪ wʊd laɪk tuː meɪk ə ˌrezərˈveɪʃn fɔːr tuː/", "Tôi muốn đặt chỗ cho hai người.", "Nói chậm rãi, nhấn vào \"reservation\" và \"two\"."),
        SpeakingItem("ss_3", SpeakingLevel.SENTENCE, "She has been working here for three years.", "/ʃiː hæz biːn ˈwɜːrkɪŋ hɪər fɔːr θriː jɪərz/", "Cô ấy đã làm việc ở đây được ba năm rồi.", "Nối âm \"has been\" thành /hæzbɪn/."),
        SpeakingItem("ss_4", SpeakingLevel.SENTENCE, "The meeting has been postponed until next week.", "/ðə ˈmiːtɪŋ hæz biːn ˌpoʊstˈpoʊnd ənˈtɪl nekst wiːk/", "Cuộc họp đã bị hoãn đến tuần sau.", "Trọng âm rơi vào \"postponed\" và \"next\"."),
        SpeakingItem("ss_5", SpeakingLevel.SENTENCE, "I'm really looking forward to the trip.", "/aɪm ˈrɪəli ˈlʊkɪŋ ˈfɔːrwərd tuː ðə trɪp/", "Tôi thực sự rất mong chờ chuyến đi này.", "Nối âm \"looking forward to\" mượt mà, không tách từng từ.")
    )

    fun speakingItemsFor(level: SpeakingLevel): List<SpeakingItem> = speakingItems.filter { it.level == level }

    val listeningLessons = listOf(
        ListeningLesson(
            id = "listen_station",
            title = "Tại ga tàu hỏa",
            levelTag = "A2 - Du lịch",
            durationSeconds = 95,
            transcript = listOf(
                TranscriptLine("Nhân viên", "Good morning! How can I help you?"),
                TranscriptLine("Khách", "Hi, I'd like a ticket to Da Nang, please."),
                TranscriptLine("Nhân viên", "One way or round trip?"),
                TranscriptLine("Khách", "Round trip, returning next Sunday."),
                TranscriptLine("Nhân viên", "Great, that will be 450,000 dong. The train departs from platform 3.")
            ),
            questions = listOf(
                QuizQuestion("ls_q1", "Khách muốn đi đâu?", listOf("Hà Nội", "Đà Nẵng", "Nha Trang", "Huế"), 1, "Khách nói: \"I'd like a ticket to Da Nang, please.\""),
                QuizQuestion("ls_q2", "Tàu khởi hành từ sân ga số mấy?", listOf("Số 1", "Số 2", "Số 3", "Số 4"), 2, "Nhân viên nói: \"The train departs from platform 3.\"")
            )
        ),
        ListeningLesson(
            id = "listen_restaurant",
            title = "Gọi món tại nhà hàng",
            levelTag = "A1 - Ẩm thực",
            durationSeconds = 80,
            transcript = listOf(
                TranscriptLine("Phục vụ", "Are you ready to order?"),
                TranscriptLine("Khách", "Yes, I'll have the grilled chicken with rice."),
                TranscriptLine("Phục vụ", "Would you like anything to drink?"),
                TranscriptLine("Khách", "Just water, thank you.")
            ),
            questions = listOf(
                QuizQuestion("lr_q1", "Khách gọi món gì?", listOf("Gà nướng với cơm", "Cá chiên với khoai tây", "Mì Ý", "Salad"), 0, "Khách nói: \"I'll have the grilled chicken with rice.\""),
                QuizQuestion("lr_q2", "Khách uống gì?", listOf("Nước ngọt", "Nước lọc", "Cà phê", "Trà"), 1, "Khách nói: \"Just water, thank you.\"")
            )
        ),
        ListeningLesson(
            id = "listen_interview",
            title = "Phỏng vấn xin việc",
            levelTag = "B1 - Công việc",
            durationSeconds = 110,
            transcript = listOf(
                TranscriptLine("Nhà tuyển dụng", "Tell me about your previous work experience."),
                TranscriptLine("Ứng viên", "I worked as a customer support agent for two years."),
                TranscriptLine("Nhà tuyển dụng", "What is your biggest strength?"),
                TranscriptLine("Ứng viên", "I'm good at solving problems quickly under pressure.")
            ),
            questions = listOf(
                QuizQuestion("li_q1", "Ứng viên đã làm công việc gì trước đây?", listOf("Kế toán", "Chăm sóc khách hàng", "Lập trình viên", "Giáo viên"), 1, "Ứng viên nói: \"I worked as a customer support agent for two years.\""),
                QuizQuestion("li_q2", "Điểm mạnh nhất của ứng viên là gì?", listOf("Giao tiếp tốt", "Giải quyết vấn đề nhanh dưới áp lực", "Quản lý thời gian", "Làm việc nhóm"), 1, "Ứng viên nói: \"I'm good at solving problems quickly under pressure.\"")
            )
        )
    )

    val quizQuestions = listOf(
        QuizQuestion("q1", "\"Itinerary\" có nghĩa là gì?", listOf("Hành lý", "Lịch trình", "Vé máy bay", "Hộ chiếu"), 1, "\"Itinerary\" nghĩa là lịch trình chuyến đi."),
        QuizQuestion("q2", "Chọn câu đúng ở thì hiện tại đơn:", listOf("She work every day.", "She works every day.", "She working every day.", "She worked every day."), 1, "Ngôi thứ ba số ít cần thêm -s: works."),
        QuizQuestion("q3", "Từ nào đồng nghĩa với \"colleague\"?", listOf("Coworker", "Stranger", "Customer", "Manager"), 0, "\"Colleague\" và \"coworker\" đều có nghĩa là đồng nghiệp."),
        QuizQuestion("q4", "Chọn câu đúng ở thì quá khứ đơn:", listOf("I goed to school.", "I went to school.", "I go to school.", "I going to school."), 1, "\"Go\" là động từ bất quy tắc: go → went."),
        QuizQuestion("q5", "\"Deadline\" có nghĩa là gì?", listOf("Kỳ nghỉ", "Hạn chót", "Cuộc họp", "Danh sách"), 1, "\"Deadline\" nghĩa là hạn chót phải hoàn thành công việc."),
        QuizQuestion("q6", "Chọn dạng so sánh hơn đúng của \"tall\":", listOf("more tall", "tallest", "taller", "most tall"), 2, "Tính từ ngắn 1 âm tiết thêm -er: taller."),
        QuizQuestion("q7", "\"I enjoy ___ books.\" — chọn đáp án đúng:", listOf("read", "to read", "reading", "reads"), 2, "\"Enjoy\" luôn theo sau bởi V-ing."),
        QuizQuestion("q8", "\"Reservation\" liên quan nhiều nhất đến hoạt động nào?", listOf("Đặt bàn nhà hàng", "Sửa xe", "Học bài", "Xem phim"), 0, "\"Reservation\" thường dùng khi đặt bàn, đặt phòng, đặt vé.")
    )

    val weeklyActivity = listOf(
        DailyActivity("T2", 18, false),
        DailyActivity("T3", 22, true),
        DailyActivity("T4", 25, true),
        DailyActivity("T5", 15, false),
        DailyActivity("T6", 20, true),
        DailyActivity("T7", 28, true),
        DailyActivity("CN", 12, false)
    )

    val achievements = listOf(
        Achievement("ach_streak", "7 Day Streak", "Học liên tục 7 ngày không nghỉ", R.drawable.ic_flame, true, 7, 7),
        Achievement("ach_words", "100 Words", "Học thuộc 100 từ vựng mới", R.drawable.ic_book, true, 100, 100),
        Achievement("ach_speaking", "Speaking Star", "Hoàn thành 50 lượt luyện phát âm", R.drawable.ic_mic, true, 50, 50),
        Achievement("ach_quiz", "Quiz Master", "Đạt độ chính xác 90% trong 10 bài Quiz", R.drawable.ic_target, false, 6, 10),
        Achievement("ach_listener", "Great Listener", "Hoàn thành 30 bài luyện nghe", R.drawable.ic_headphones, false, 14, 30)
    )
}
