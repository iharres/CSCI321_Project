package com.legoproject.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import org.springframework.stereotype.Service;

@Service
public class GeminiService {

    private final Client client;
    private final ObjectMapper objectMapper;

    public GeminiService() {

        this.objectMapper = new ObjectMapper();

        String apiKey = System.getenv("GEMINI_API_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "GEMINI_API_KEY environment variable is not set"
            );
        }

        this.client = Client.builder()
                .apiKey(apiKey)
                .build();
    }


    public GeminiLegoResult identifyLegoSet(
            byte[] imageBytes,
            String mimeType) throws Exception {

        if (mimeType == null || mimeType.isBlank()) {
            mimeType = "image/jpeg";
        }


        Part imagePart = Part.fromBytes(
                imageBytes,
                mimeType
        );


        String prompt = """
                Identify the LEGO set shown in this image.

                Determine the LEGO set number printed on the packaging.

                Do NOT confuse the set number with:
                - age range
                - piece count
                - barcode
                - copyright year
                - quantity
                - another unrelated number

                Also identify the product name.

                If you cannot confidently identify the set,
                return an empty setNumber.

                Return JSON only in exactly this format:

                {
                  "setNumber": "10294",
                  "name": "Titanic"
                }
                """;


        Content content = Content.fromParts(
                Part.fromText(prompt),
                imagePart
        );


        GenerateContentConfig config =
                GenerateContentConfig.builder()
                        .responseMimeType("application/json")
                        .temperature(0.0f)
                        .build();


        GenerateContentResponse response =
                client.models.generateContent(
                        "gemini-3.6-flash",
                        content,
                        config
                );


        String json = response.text();


        if (json == null || json.isBlank()) {
            throw new RuntimeException(
                    "Gemini returned an empty response"
            );
        }


        System.out.println(
                "Gemini response: " + json
        );


        return objectMapper.readValue(
                json,
                GeminiLegoResult.class
        );
    }
}