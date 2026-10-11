// supabase/functions/ai-workout-plan/index.ts
// Secure Supabase Edge Function for HomeWorkout2 AI Plan Generation

import { serve } from "https://deno.land/std@0.168.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
};

serve(async (req) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  try {
    const authHeader = req.headers.get("Authorization");
    if (!authHeader) {
      return new Response(
        JSON.stringify({ error: "Missing Authorization header" }),
        { status: 401, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    const supabaseUrl = Deno.env.get("SUPABASE_URL") ?? "";
    const supabaseAnonKey = Deno.env.get("SUPABASE_ANON_KEY") ?? "";
    const supabase = createClient(supabaseUrl, supabaseAnonKey, {
      global: { headers: { Authorization: authHeader } },
    });

    const token = authHeader.replace("Bearer ", "").trim();
    const { data: { user }, error: userError } = await supabase.auth.getUser(token);

    if (userError || !user) {
      return new Response(
        JSON.stringify({ error: "Invalid or expired session. Please sign in again." }),
        { status: 401, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    const geminiApiKey = Deno.env.get("GEMINI_API_KEY");
    if (!geminiApiKey) {
      return new Response(
        JSON.stringify({ error: "GEMINI_API_KEY secret is not configured in Supabase Edge Functions." }),
        { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    const { goal, difficulty, daysPerWeek, minutesPerWorkout, availableExercises, history } = await req.json();

    const allowedExercises = Array.isArray(availableExercises)
      ? availableExercises.map((e: any) => `${e.id} | ${e.name} | ${e.category} | ${e.difficulty}`).join("\n")
      : "";

    const prompt = `
Create a safe, age-appropriate home workout plan.

Goal: ${goal || "General Fitness"}
Difficulty: ${difficulty || "Beginner"}
Days per week: ${daysPerWeek || 3}
Minutes per workout: ${minutesPerWorkout || 20}

ONLY use these exercise IDs:
${allowedExercises}

Rules:
- Use only exercise IDs from the catalog.
- Return exactly ${daysPerWeek || 3} days.
- Use 2 to 4 exercises per day.
- Keep the routine manageable and progressive.
- Do not recommend dangerous challenges.
- Return ONLY valid JSON matching this schema:
{
  "title": "string",
  "description": "string",
  "days": [
    {
      "day": 1,
      "focus": "string",
      "exerciseIds": ["id1", "id2"]
    }
  ]
}
`.trim();

    const geminiBody = {
      contents: [{ role: "user", parts: [{ text: prompt }] }],
      generationConfig: {
        responseMimeType: "application/json",
        temperature: 0.4,
      },
    };

    const models = ["gemini-2.5-flash", "gemini-2.0-flash", "gemini-1.5-flash", "gemini-2.0-flash-lite"];
    let response: Response | null = null;
    let lastErrorText = "";
    let lastStatus = 502;

    for (const model of models) {
      const geminiUrl = `https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent?key=${geminiApiKey}`;
      try {
        response = await fetch(geminiUrl, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(geminiBody),
        });
        if (response.ok) {
          break;
        }
        lastStatus = response.status;
        lastErrorText = await response.text();
        console.warn(`Model ${model} returned HTTP ${lastStatus}: ${lastErrorText}`);
      } catch (fetchErr) {
        lastStatus = 502;
        lastErrorText = fetchErr instanceof Error ? fetchErr.message : "Fetch error";
        console.warn(`Model ${model} fetch error: ${lastErrorText}`);
      }
    }

    if (!response || !response.ok) {
      return new Response(
        JSON.stringify({ error: `Gemini API plan generation failed (${lastStatus}): ${lastErrorText.slice(0, 150)}` }),
        { status: 502, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    const data = await response.json();
    const candidate = data.candidates?.[0]?.content?.parts?.[0]?.text;
    const planJson = candidate ? JSON.parse(candidate) : null;

    return new Response(
      JSON.stringify({ plan: planJson }),
      { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } }
    );

  } catch (err) {
    return new Response(
      JSON.stringify({ error: err instanceof Error ? err.message : "Internal error" }),
      { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } }
    );
  }
});
