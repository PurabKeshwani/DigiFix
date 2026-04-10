package com.example.digifix;

/**
 * Supabase project constants.
 * Values match the .env file in the project root.
 */
public class SupabaseConfig {

    // From .env → SUPABSE_PROJECT_URL (note: typo in .env key name is intentional)
    public static final String PROJECT_URL = "https://lvkakhsrvipyeyenxcgs.supabase.co";

    // From .env → SUPABASE_ANON_KEY
    public static final String ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9" +
            ".eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imx2a2FraHNydmlweWV5ZW54Y2dzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzU3NjAzOTEsImV4cCI6MjA5MTMzNjM5MX0" +
            ".f2xDatRFjM-cw4i8hhsdlNWhzY1e8YN5geOmagKN6TA";

    // Storage bucket for device damage photos
    public static final String BUCKET_NAME = "device-photos";

    // REST API endpoints
    public static final String REST_URL = PROJECT_URL + "/rest/v1";
    public static final String STORAGE_URL = PROJECT_URL + "/storage/v1/object/" + BUCKET_NAME;
}
