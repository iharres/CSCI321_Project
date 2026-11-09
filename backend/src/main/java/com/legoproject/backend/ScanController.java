package com.legoproject.backend;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@CrossOrigin
@RestController
public class ScanController {

    private final GeminiService geminiService;
    private final BrickLinkService brickLinkService;

    public ScanController(
            GeminiService geminiService,
            BrickLinkService brickLinkService) {

        this.geminiService = geminiService;
        this.brickLinkService = brickLinkService;
    }


    @PostMapping("/api/scan-lego")
    public GeminiLegoResult scanLego(
            @RequestParam("image") MultipartFile image) throws Exception {

        // Make sure an image was uploaded
        if (image.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No image was uploaded"
            );
        }


        // Make sure the uploaded file is actually an image
        String mimeType = image.getContentType();

        if (mimeType == null || !mimeType.startsWith("image/")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Uploaded file must be an image"
            );
        }


        // Send image to Gemini
        GeminiLegoResult geminiResult =
                geminiService.identifyLegoSet(
                        image.getBytes(),
                        mimeType
                );


        String setNumber =
                geminiResult.getSetNumber();


        // Gemini could not identify the set
        if (setNumber == null || setNumber.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Gemini could not identify the LEGO set"
            );
        }


        // Clean the set number
        // Example: 10294-1 becomes 10294
        setNumber =
                setNumber
                        .trim()
                        .replaceAll("-\\d+$", "");


        geminiResult.setSetNumber(setNumber);


        System.out.println(
                "Gemini identified: "
                        + setNumber
                        + " | "
                        + geminiResult.getName()
        );


        /*
         * Try BrickLink verification.
         * If BrickLink is unavailable, use Gemini result.
         */
        try {

            String officialName =
                    brickLinkService.getSetName(setNumber);


            GeminiLegoResult verifiedResult =
                    new GeminiLegoResult();

            verifiedResult.setSetNumber(setNumber);
            verifiedResult.setName(officialName);


            System.out.println(
                    "BrickLink verified: "
                            + setNumber
                            + " | "
                            + officialName
            );


            return verifiedResult;


        } catch (Exception e) {

            System.out.println(
                    "BrickLink unavailable or verification failed."
            );

            System.out.println(
                    "Using Gemini result instead: "
                            + setNumber
                            + " | "
                            + geminiResult.getName()
            );


            return geminiResult;
        }
    }
}