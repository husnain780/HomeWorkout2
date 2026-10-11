// supabase/functions/ai-coach/index.ts
// Secure Supabase Edge Function for HomeWorkout2 AI Coach & Assistant

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

    const { message, history } = await req.json();
    const cleanMessage = (message || "").trim();

    if (!cleanMessage) {
      return new Response(
        JSON.stringify({ error: "Message cannot be empty." }),
        { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    const systemInstruction = `
You are a versatile, highly intelligent, general-purpose AI assistant and coach integrated into the HomeWorkout app.

You have broad, comprehensive expertise and excel at:
- Software engineering, coding, syntax, architecture, and debugging (Kotlin, Android, Jetpack Compose, Python, JavaScript, Java, C++, algorithms, data structures)
- Mathematics, physics, logic, problem-solving, and step-by-step solutions
- Academic learning, study techniques, science, history, and conceptual explanations
- Writing, proofreading, creative editing, summarization, and business communications
- Productivity, habits, time management, and mental focus
- Comprehensive fitness, biomechanics, exercise technique, training splits, progressive overload, recovery, and home workouts.

Guidelines:
1. Answer all user questions thoroughly, accurately, and articulately across all subjects without refusing non-fitness requests.
2. When answering programming or math queries, format explanations clearly with readable code or mathematical steps.
3. For fitness or health topics, provide safe, practical, evidence-based recommendations. Never encourage dangerous physical challenges or exercising through severe pain. Do not provide medical diagnoses.
4. Maintain a friendly, supportive, intelligent, and professional tone.
`.trim();

    let contextTelemetry = "";
    const isFitness = /workout|exercise|fitness|training|muscle|rep|set|streak|progress|calorie|cardio|squat|pushup|plank|routine|recovery|diet|weight/i.test(cleanMessage);
    if (isFitness && Array.isArray(history) && history.length > 0) {
      const totalWorkouts = history.length;
      const totalMinutes = history.reduce((acc, h) => acc + (Number(h.durationMinutes) || 0), 0);
      const totalCalories = history.reduce((acc, h) => acc + (Number(h.caloriesBurned) || 0), 0);
      contextTelemetry = `[User Workout Telemetry: ${totalWorkouts} sessions, ${totalMinutes} mins active, ${totalCalories} kcal burned]\n\n`;
    }

    const geminiBody = {
      contents: [
        {
          role: "user",
          parts: [{ text: `${systemInstruction}\n\n${contextTelemetry}User question:\n${cleanMessage}` }],
        },
      ],
      generationConfig: {
        temperature: 0.7,
        maxOutputTokens: 2048,
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
        JSON.stringify({ error: `Gemini API request failed (${lastStatus}): ${lastErrorText.slice(0, 150)}` }),
        { status: 502, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    const data = await response.json();
    const candidate = data.candidates?.[0]?.content?.parts?.[0]?.text;
    const replyText = candidate?.trim() || "I couldn't generate a response. Please try asking again.";

    return new Response(
      JSON.stringify({ reply: replyText }),
      { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } }
    );

  } catch (err) {
    return new Response(
      JSON.stringify({ error: err instanceof Error ? err.message : "Internal error" }),
      { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } }
    );
  }
});
