package com.ukulelestrum.school.data.sample

import com.ukulelestrum.school.domain.model.*

object SampleData {
    val chords = listOf(
        Chord("chord_c", "chord_c", listOf(0,0,0,3), listOf(0,0,0,3), "description_c", "tips_c"),
        Chord("chord_am", "chord_am", listOf(2,0,0,0), listOf(2,0,0,0), "description_am", "tips_am"),
        Chord("chord_f", "chord_f", listOf(2,0,1,0), listOf(2,0,1,0), "description_f", "tips_f"),
        Chord("chord_g", "chord_g", listOf(0,2,3,2), listOf(0,1,3,2), "description_g", "tips_g")
    )
    val patterns = listOf(
        StrumPattern("steady_fours", "steady_fours", listOf(1,0,1,0,1,0,1,0), "steady_fours_guide", "steady_fours_spoken"),
        StrumPattern("gentle_eighths", "gentle_eighths", listOf(1,-1,1,-1,1,-1,1,-1), "gentle_eighths_guide", "gentle_eighths_spoken"),
        StrumPattern("island_strum", "island_strum", listOf(1,0,1,-1,0,-1,1,-1), "island_strum_guide", "island_strum_spoken")
    )
    val plans = listOf(
        PracticePlan("plan_c", "plan_c", listOf("chord_c", "chord_c", "chord_c", "chord_c"), 60, "steady_fours"),
        PracticePlan("plan_am", "plan_am", listOf("chord_am", "chord_am", "chord_am", "chord_am"), 60, "steady_fours"),
        PracticePlan("plan_f", "plan_f", listOf("chord_f", "chord_f", "chord_f", "chord_f"), 60, "steady_fours"),
        PracticePlan("plan_g", "plan_g", listOf("chord_g", "chord_g", "chord_g", "chord_g"), 60, "gentle_eighths"),
        PracticePlan("plan_three", "plan_three", listOf("chord_c", "chord_am", "chord_f", "chord_c"), 60, "steady_fours"),
        PracticePlan("plan_circle", "plan_circle", listOf("chord_c", "chord_am", "chord_f", "chord_g"), 60, "island_strum"),
        PracticePlan("plan_lanai", "plan_lanai", listOf("chord_c", "chord_am", "chord_f", "chord_g", "chord_c", "chord_am", "chord_f", "chord_g"), 64, "steady_fours", "lyrics_lanai"),
        PracticePlan("plan_sunset", "plan_sunset", listOf("chord_c", "chord_c", "chord_f", "chord_f", "chord_c", "chord_c", "chord_g", "chord_c"), 72, "island_strum", "lyrics_sunset")
    )
    val lessons = listOf(
        Lesson("lesson_c", "lesson_c_title", 4, listOf("chord_c"), "steady_fours", "plan_c", listOf(
            KnowledgeQuestion("q_c_1", listOf(AnswerOption("option_g", "option_g"), AnswerOption("option_c", "option_c"), AnswerOption("option_e", "option_e"), AnswerOption("option_a", "option_a")), "option_a"),
            KnowledgeQuestion("q_c_2", listOf(AnswerOption("option_open", "option_open"), AnswerOption("option_1", "option_1"), AnswerOption("option_2", "option_2"), AnswerOption("option_3", "option_3")), "option_3"),
            KnowledgeQuestion("q_c_3", listOf(AnswerOption("option_g_0_c_0_e_0_a_3", "option_g_0_c_0_e_0_a_3"), AnswerOption("option_g_2_c_0_e_0_a_0", "option_g_2_c_0_e_0_a_0"), AnswerOption("option_g_2_c_0_e_1_a_0", "option_g_2_c_0_e_1_a_0"), AnswerOption("option_g_0_c_2_e_3_a_2", "option_g_0_c_2_e_3_a_2")), "option_g_0_c_0_e_0_a_3"),
            KnowledgeQuestion("q_c_4", listOf(AnswerOption("option_1", "option_1"), AnswerOption("option_2", "option_2"), AnswerOption("option_3", "option_3"), AnswerOption("option_4", "option_4")), "option_4")
        ), "guide_c"),
        Lesson("lesson_am", "lesson_am_title", 4, listOf("chord_am"), "steady_fours", "plan_am", listOf(
            KnowledgeQuestion("q_am_1", listOf(AnswerOption("option_g", "option_g"), AnswerOption("option_c", "option_c"), AnswerOption("option_e", "option_e"), AnswerOption("option_a", "option_a")), "option_g"),
            KnowledgeQuestion("q_am_2", listOf(AnswerOption("option_open", "option_open"), AnswerOption("option_1", "option_1"), AnswerOption("option_2", "option_2"), AnswerOption("option_3", "option_3")), "option_2"),
            KnowledgeQuestion("q_am_3", listOf(AnswerOption("option_g_0_c_0_e_0_a_3", "option_g_0_c_0_e_0_a_3"), AnswerOption("option_g_2_c_0_e_0_a_0", "option_g_2_c_0_e_0_a_0"), AnswerOption("option_g_2_c_0_e_1_a_0", "option_g_2_c_0_e_1_a_0"), AnswerOption("option_g_0_c_2_e_3_a_2", "option_g_0_c_2_e_3_a_2")), "option_g_2_c_0_e_0_a_0"),
            KnowledgeQuestion("q_am_4", listOf(AnswerOption("option_index_1", "option_index_1"), AnswerOption("option_middle_2", "option_middle_2"), AnswerOption("option_ring_3", "option_ring_3"), AnswerOption("option_little_4", "option_little_4")), "option_middle_2")
        ), "guide_am"),
        Lesson("lesson_f", "lesson_f_title", 5, listOf("chord_f"), "steady_fours", "plan_f", listOf(
            KnowledgeQuestion("q_f_1", listOf(AnswerOption("option_g_and_e", "option_g_and_e"), AnswerOption("option_c_and_a", "option_c_and_a"), AnswerOption("option_g_and_a", "option_g_and_a"), AnswerOption("option_c_and_e", "option_c_and_e")), "option_g_and_e"),
            KnowledgeQuestion("q_f_2", listOf(AnswerOption("option_open", "option_open"), AnswerOption("option_1", "option_1"), AnswerOption("option_2", "option_2"), AnswerOption("option_3", "option_3")), "option_1"),
            KnowledgeQuestion("q_f_3", listOf(AnswerOption("option_g_0_c_0_e_0_a_3", "option_g_0_c_0_e_0_a_3"), AnswerOption("option_g_2_c_0_e_0_a_0", "option_g_2_c_0_e_0_a_0"), AnswerOption("option_g_2_c_0_e_1_a_0", "option_g_2_c_0_e_1_a_0"), AnswerOption("option_g_0_c_2_e_3_a_2", "option_g_0_c_2_e_3_a_2")), "option_g_2_c_0_e_1_a_0"),
            KnowledgeQuestion("q_f_4", listOf(AnswerOption("option_index_on_e_fret_1", "option_index_on_e_fret_1"), AnswerOption("option_index_on_c_fret_1", "option_index_on_c_fret_1"), AnswerOption("option_ring_on_a_fret_3", "option_ring_on_a_fret_3"), AnswerOption("option_middle_on_g_fret_2", "option_middle_on_g_fret_2")), "option_index_on_e_fret_1")
        ), "guide_f"),
        Lesson("lesson_changes", "lesson_changes_title", 5, listOf("chord_c", "chord_am", "chord_f", "chord_c"), "steady_fours", "plan_three", listOf(
            KnowledgeQuestion("q_changes_1", listOf(AnswerOption("option_am", "option_am"), AnswerOption("option_f", "option_f"), AnswerOption("option_g", "option_g"), AnswerOption("option_c", "option_c")), "option_am"),
            KnowledgeQuestion("q_changes_2", listOf(AnswerOption("option_2", "option_2"), AnswerOption("option_3", "option_3"), AnswerOption("option_4", "option_4"), AnswerOption("option_6", "option_6")), "option_4"),
            KnowledgeQuestion("q_changes_3", listOf(AnswerOption("option_1", "option_1"), AnswerOption("option_2", "option_2"), AnswerOption("option_4", "option_4"), AnswerOption("option_8", "option_8")), "option_4"),
            KnowledgeQuestion("q_changes_4", listOf(AnswerOption("option_c", "option_c"), AnswerOption("option_am", "option_am"), AnswerOption("option_f", "option_f"), AnswerOption("option_g", "option_g")), "option_c")
        ), "guide_changes"),
        Lesson("lesson_g", "lesson_g_title", 5, listOf("chord_g"), "gentle_eighths", "plan_g", listOf(
            KnowledgeQuestion("q_g_1", listOf(AnswerOption("option_g_0_c_0_e_0_a_3", "option_g_0_c_0_e_0_a_3"), AnswerOption("option_g_2_c_0_e_0_a_0", "option_g_2_c_0_e_0_a_0"), AnswerOption("option_g_2_c_0_e_1_a_0", "option_g_2_c_0_e_1_a_0"), AnswerOption("option_g_0_c_2_e_3_a_2", "option_g_0_c_2_e_3_a_2")), "option_g_0_c_2_e_3_a_2"),
            KnowledgeQuestion("q_g_2", listOf(AnswerOption("option_open", "option_open"), AnswerOption("option_1", "option_1"), AnswerOption("option_2", "option_2"), AnswerOption("option_3", "option_3")), "option_2"),
            KnowledgeQuestion("q_g_3", listOf(AnswerOption("option_open", "option_open"), AnswerOption("option_1", "option_1"), AnswerOption("option_2", "option_2"), AnswerOption("option_3", "option_3")), "option_3"),
            KnowledgeQuestion("q_g_4", listOf(AnswerOption("option_open", "option_open"), AnswerOption("option_1", "option_1"), AnswerOption("option_2", "option_2"), AnswerOption("option_3", "option_3")), "option_2")
        ), "guide_g"),
        Lesson("lesson_island", "lesson_island_title", 6, listOf("chord_c", "chord_am", "chord_f", "chord_g"), "island_strum", "plan_circle", listOf(
            KnowledgeQuestion("q_island_1", listOf(AnswerOption("option_d_rest_d_u_rest_u_d_u", "option_d_rest_d_u_rest_u_d_u"), AnswerOption("option_d_u_d_u_d_u_d_u", "option_d_u_d_u_d_u_d_u"), AnswerOption("option_d_rest_d_rest_d_rest_d_rest", "option_d_rest_d_rest_d_rest_d_rest"), AnswerOption("option_rest_u_rest_u_rest_u_rest_u", "option_rest_u_rest_u_rest_u_rest_u")), "option_d_rest_d_u_rest_u_d_u"),
            KnowledgeQuestion("q_island_2", listOf(AnswerOption("option_rest", "option_rest"), AnswerOption("option_downstroke", "option_downstroke"), AnswerOption("option_upstroke", "option_upstroke"), AnswerOption("option_chord_change", "option_chord_change")), "option_rest"),
            KnowledgeQuestion("q_island_3", listOf(AnswerOption("option_rest", "option_rest"), AnswerOption("option_downstroke", "option_downstroke"), AnswerOption("option_upstroke", "option_upstroke"), AnswerOption("option_stop_the_song", "option_stop_the_song")), "option_upstroke"),
            KnowledgeQuestion("q_island_4", listOf(AnswerOption("option_keep_moving_without_touching_strings", "option_keep_moving_without_touching_strings"), AnswerOption("option_stop_completely", "option_stop_completely"), AnswerOption("option_press_harder", "option_press_harder"), AnswerOption("option_change_tuning", "option_change_tuning")), "option_keep_moving_without_touching_strings")
        ), "guide_island"),
        Lesson("lesson_lanai", "lesson_lanai_title", 6, listOf("chord_c", "chord_am", "chord_f", "chord_g"), "steady_fours", "plan_lanai", listOf(
            KnowledgeQuestion("q_lanai_1", listOf(AnswerOption("option_c", "option_c"), AnswerOption("option_am", "option_am"), AnswerOption("option_f", "option_f"), AnswerOption("option_g", "option_g")), "option_c"),
            KnowledgeQuestion("q_lanai_2", listOf(AnswerOption("option_c", "option_c"), AnswerOption("option_am", "option_am"), AnswerOption("option_f", "option_f"), AnswerOption("option_g", "option_g")), "option_am"),
            KnowledgeQuestion("q_lanai_3", listOf(AnswerOption("option_c", "option_c"), AnswerOption("option_am", "option_am"), AnswerOption("option_f", "option_f"), AnswerOption("option_g", "option_g")), "option_f"),
            KnowledgeQuestion("q_lanai_4", listOf(AnswerOption("option_c", "option_c"), AnswerOption("option_am", "option_am"), AnswerOption("option_f", "option_f"), AnswerOption("option_g", "option_g")), "option_g")
        ), "guide_lanai"),
        Lesson("lesson_sunset", "lesson_sunset_title", 6, listOf("chord_c", "chord_f", "chord_g"), "island_strum", "plan_sunset", listOf(
            KnowledgeQuestion("q_sunset_1", listOf(AnswerOption("option_c", "option_c"), AnswerOption("option_am", "option_am"), AnswerOption("option_f", "option_f"), AnswerOption("option_g", "option_g")), "option_c"),
            KnowledgeQuestion("q_sunset_2", listOf(AnswerOption("option_c", "option_c"), AnswerOption("option_am", "option_am"), AnswerOption("option_f", "option_f"), AnswerOption("option_g", "option_g")), "option_f"),
            KnowledgeQuestion("q_sunset_3", listOf(AnswerOption("option_c", "option_c"), AnswerOption("option_am", "option_am"), AnswerOption("option_f", "option_f"), AnswerOption("option_g", "option_g")), "option_g"),
            KnowledgeQuestion("q_sunset_4", listOf(AnswerOption("option_c", "option_c"), AnswerOption("option_am", "option_am"), AnswerOption("option_f", "option_f"), AnswerOption("option_g", "option_g")), "option_c")
        ), "guide_sunset")
    )
}

