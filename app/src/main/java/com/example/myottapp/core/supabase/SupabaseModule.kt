package com.example.myottapp.core.network

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

// ─────────────────────────────────────────────────────────────────────
//  SupabaseModule.kt
//  Place at: core/network/SupabaseModule.kt
// ─────────────────────────────────────────────────────────────────────

object SupabaseModule {

    // ✅ Your real credentials from supabase.xml
    private const val SUPABASE_URL      = "https://jngqzbroftbvuhxwfgrl.supabase.co"
    private const val SUPABASE_ANON_KEY =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImpuZ3F6YnJvZnRidnVoeHdmZ3JsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzcxNjU1MTYsImV4cCI6MjA5Mjc0MTUxNn0.BAiB5hkZC6e0ZmV48iRi7ChEDv_f66PlLLBBPIt2-Z0"

    // ✅ Supabase client — used for Storage and Postgrest
    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_ANON_KEY
    ) {
        install(Postgrest)
        install(Storage)
    }

    // ✅ Builds public URL for any file in "movies" bucket
    // getVideoUrl("hls/playlist.m3u8")
    // → https://jngqzbroftbvuhxwfgrl.supabase.co/storage/v1/object/public/movies/hls/playlist.m3u8
    fun getVideoUrl(fileName: String): String {
        return "$SUPABASE_URL/storage/v1/object/public/movies/$fileName"
    }
}
