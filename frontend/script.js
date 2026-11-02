console.log("Script is running");

const API_URL = "http://localhost:8080";


// ======================================================
// LEGO BOX IMAGE SCANNER
// ======================================================

document
    .getElementById("scan-image-button")
    .addEventListener("click", async () => {

        const imageInput =
            document.getElementById("lego-image");

        const status =
            document.getElementById("scan-status");

        const setNumberInput =
            document.getElementById("set-number");


        if (!imageInput.files.length) {

            status.textContent =
                "Please select an image first.";

            return;
        }


        const file =
            imageInput.files[0];


        let worker = null;

        let currentStep =
            "starting";


        try {

            // ==================================================
            // LOAD IMAGE
            // ==================================================

            currentStep =
                "loading image";

            status.textContent =
                "Loading image...";


            const imageBitmap =
                await createImageBitmap(file);


            const imageWidth =
                imageBitmap.width;

            const imageHeight =
                imageBitmap.height;



            // ==================================================
            // CREATE TESSERACT WORKER
            // ==================================================

            currentStep =
                "creating Tesseract worker";

            status.textContent =
                "Starting OCR...";


            worker =
                await Tesseract.createWorker(
                    "eng",
                    1,
                    {

                        logger: info => {

                            console.log(
                                "TESSERACT:",
                                info
                            );


                            if (
                                info.status ===
                                "recognizing text"
                            ) {

                                const percent =
                                    Math.round(
                                        info.progress * 100
                                    );


                                status.textContent =
                                    `Scanning... ${percent}%`;
                            }
                        }
                    }
                );



            // ==================================================
            // PASS 1
            // READ NORMAL TEXT FROM LEGO BOX
            // ==================================================

            currentStep =
                "reading box text";

            status.textContent =
                "Reading LEGO box...";


            await worker.setParameters({

                tessedit_pageseg_mode:
                    Tesseract.PSM.SPARSE_TEXT

            });


            const textResult =
                await worker.recognize(file);


            const boxText =
                textResult.data.text || "";


            console.log(
                "BOX TEXT:"
            );

            console.log(
                boxText
            );



            // ==================================================
            // NOW SWITCH TO NUMBER-ONLY OCR
            // ==================================================

            currentStep =
                "setting number OCR";

            status.textContent =
                "Looking for LEGO set numbers...";


            await worker.setParameters({

                tessedit_char_whitelist:
                    "0123456789"

            });



            // ==================================================
            // STORE POSSIBLE SET NUMBERS
            // ==================================================

            const candidateScores =
                new Map();



            function saveCandidates(
                text,
                score
            ) {

                if (!text) {
                    return;
                }


                console.log(
                    "NUMBER OCR TEXT:",
                    text
                );


                /*
                    Accepts:

                    10294

                    10 294

                    1 0 2 9 4

                    10-294
                */

                const matches =
                    text.match(
                        /(?:\d[\s.\-_]*){4,6}/g
                    );


                if (!matches) {

                    return;
                }


                matches.forEach(match => {

                    const digits =
                        match.replace(
                            /\D/g,
                            ""
                        );


                    if (
                        digits.length >= 4 &&
                        digits.length <= 6
                    ) {

                        let finalScore =
                            score;


                        // Most modern LEGO sets
                        // commonly use 5 digits.

                        if (
                            digits.length === 5
                        ) {

                            finalScore += 3;
                        }


                        const previousScore =
                            candidateScores.get(
                                digits
                            ) || 0;


                        candidateScores.set(
                            digits,
                            previousScore + finalScore
                        );
                    }
                });
            }



            // ==================================================
            // CREATE CROPPED REGION
            // ==================================================

            function createCrop(
                x,
                y,
                width,
                height
            ) {

                const canvas =
                    document.createElement(
                        "canvas"
                    );


                const ctx =
                    canvas.getContext(
                        "2d"
                    );


                /*
                    Enlarge smaller parts of the box
                    so Tesseract has an easier time
                    reading small numbers.
                */

                const scale =
                    Math.max(
                        1,
                        Math.min(
                            3,
                            1400 / width
                        )
                    );


                canvas.width =
                    Math.round(
                        width * scale
                    );


                canvas.height =
                    Math.round(
                        height * scale
                    );


                ctx.drawImage(

                    imageBitmap,

                    x,
                    y,
                    width,
                    height,

                    0,
                    0,
                    canvas.width,
                    canvas.height

                );


                return canvas;
            }



            // ==================================================
            // PASS 2
            // WHOLE IMAGE AS A SINGLE NUMBER
            // ==================================================

            currentStep =
                "whole-image number OCR";

            status.textContent =
                "Checking for a clear set number...";


            await worker.setParameters({

                tessedit_pageseg_mode:
                    Tesseract.PSM.SINGLE_WORD

            });


            const fullResult =
                await worker.recognize(
                    file
                );


            console.log(
                "WHOLE IMAGE NUMBER RESULT:",
                fullResult.data.text
            );


            saveCandidates(
                fullResult.data.text,
                2
            );



            // ==================================================
            // PASS 3
            // REGIONAL NUMBER SCANNING
            // ==================================================

            currentStep =
                "setting regional OCR";

            await worker.setParameters({

                tessedit_pageseg_mode:
                    Tesseract.PSM.SPARSE_TEXT

            });



            const regions = [

                {
                    name:
                        "whole-image",

                    x: 0,

                    y: 0,

                    width:
                        imageWidth,

                    height:
                        imageHeight,

                    score: 1
                },


                {
                    name:
                        "top-left",

                    x: 0,

                    y: 0,

                    width:
                        imageWidth * 0.60,

                    height:
                        imageHeight * 0.60,

                    score: 5
                },


                {
                    name:
                        "left-half",

                    x: 0,

                    y: 0,

                    width:
                        imageWidth * 0.55,

                    height:
                        imageHeight,

                    score: 3
                },


                {
                    name:
                        "top-half",

                    x: 0,

                    y: 0,

                    width:
                        imageWidth,

                    height:
                        imageHeight * 0.55,

                    score: 2
                },


                {
                    name:
                        "bottom-left",

                    x: 0,

                    y:
                        imageHeight * 0.45,

                    width:
                        imageWidth * 0.60,

                    height:
                        imageHeight * 0.55,

                    score: 2
                },


                {
                    name:
                        "right-half",

                    x:
                        imageWidth * 0.45,

                    y: 0,

                    width:
                        imageWidth * 0.55,

                    height:
                        imageHeight,

                    score: 1
                }

            ];



            for (
                const region of regions
            ) {

                currentStep =
                    `scanning ${region.name}`;


                status.textContent =
                    `Scanning ${region.name}...`;


                const canvas =
                    createCrop(

                        region.x,

                        region.y,

                        region.width,

                        region.height

                    );


                const result =
                    await worker.recognize(
                        canvas
                    );


                console.log(
                    `${region.name} NUMBER RESULT:`,
                    result.data.text
                );


                saveCandidates(

                    result.data.text,

                    region.score

                );
            }



            // ==================================================
            // SORT OCR CANDIDATES
            // ==================================================

            currentStep =
                "sorting OCR candidates";


const rawCandidates =
    [...candidateScores.entries()]
        .sort(
            (a, b) =>
                b[1] - a[1]
        )
        .map(
            entry =>
                entry[0]
        );


// ==================================================
// CREATE CORRECTED CANDIDATE VARIATIONS
// ==================================================

const expandedCandidates = [];

rawCandidates.forEach(candidate => {

    // Always include the original OCR result
    expandedCandidates.push(candidate);


    // If OCR found 6 digits,
    // also test both possible 5-digit versions.

    if (candidate.length === 6) {

        // Example:
        // 110294 -> 10294

        const removeFirst =
            candidate.substring(1);

        expandedCandidates.push(
            removeFirst
        );


        // Example:
        // 110294 -> 11029

        const removeLast =
            candidate.substring(
                0,
                5
            );

        expandedCandidates.push(
            removeLast
        );
    }


    // If OCR added leading zeros,
    // also test without them.

    const withoutLeadingZeros =
        candidate.replace(
            /^0+/,
            ""
        );

    if (
        withoutLeadingZeros.length >= 4 &&
        withoutLeadingZeros.length <= 6
    ) {

        expandedCandidates.push(
            withoutLeadingZeros
        );
    }
});


// Remove duplicates

const candidates =
    [...new Set(
        expandedCandidates
    )];


console.log(
    "RAW OCR CANDIDATES:",
    rawCandidates
);

console.log(
    "EXPANDED CANDIDATES:",
    candidates
);



            console.log(
                "FINAL OCR CANDIDATES:",
                candidates
            );


            console.log(
                "CANDIDATE SCORES:",
                Object.fromEntries(
                    candidateScores
                )
            );



            if (
                candidates.length === 0
            ) {

                status.textContent =
                    "No possible LEGO set numbers were found.";

                return;
            }



            // ==================================================
            // SEND NUMBERS + BOX TEXT TO BACKEND
            // ==================================================

            currentStep =
                "checking BrickLink";


            status.textContent =
                "Matching LEGO set with BrickLink...";


            const response =
                await fetch(

                    `${API_URL}/api/bricklink/identify`,

                    {

                        method:
                            "POST",


                        headers: {

                            "Content-Type":
                                "application/json"

                        },


                        body:
                            JSON.stringify({

                                candidates:
                                    candidates,

                                ocrText:
                                    boxText

                            })
                    }
                );



            if (!response.ok) {

                throw new Error(
                    `BrickLink returned HTTP ${response.status}`
                );
            }



            const validSetNumber =
                (await response.text())
                    .trim();



            console.log(
                "BRICKLINK SELECTED:",
                validSetNumber
            );



            if (!validSetNumber) {

                status.textContent =
                    "Possible numbers were found, but the LEGO set could not be identified.";

                return;
            }



            // ==================================================
            // SUCCESS
            // ==================================================

            setNumberInput.value =
                validSetNumber;


            status.textContent =
                `Found LEGO set: ${validSetNumber}`;


            console.log(
                "VALIDATED LEGO SET:",
                validSetNumber
            );


        } catch (error) {

            console.error(
                "SCAN FAILED AT:",
                currentStep
            );


            console.error(
                "FULL ERROR:",
                error
            );


            status.textContent =
                `Failed at "${currentStep}": ${
                    error.message || error
                }`;


        } finally {

            if (worker) {

                try {

                    await worker.terminate();

                } catch (error) {

                    console.error(
                        "Worker termination error:",
                        error
                    );
                }
            }
        }

    });



// ======================================================
// ACCOUNT / LOGIN SYSTEM
// ======================================================

const loginButton =
    document.getElementById(
        "login-button"
    );


const loginDropdown =
    document.getElementById(
        "login-dropdown"
    );


const userArea =
    document.getElementById(
        "user-area"
    );


const usernameButton =
    document.getElementById(
        "username-button"
    );


const userDropdown =
    document.getElementById(
        "user-dropdown"
    );



function checkLogin() {

    const userId =
        localStorage.getItem(
            "userId"
        );


    const username =
        localStorage.getItem(
            "username"
        );


    if (
        userId &&
        username
    ) {

        loginButton.style.display =
            "none";


        loginDropdown.style.display =
            "none";


        userArea.style.display =
            "block";


        usernameButton.textContent =
            username;


    } else {

        loginButton.style.display =
            "block";


        userArea.style.display =
            "none";
    }
}



// ======================================================
// LOGIN DROPDOWN
// ======================================================

loginButton.addEventListener(
    "click",
    () => {

        if (
            loginDropdown.style.display ===
            "none"
        ) {

            loginDropdown.style.display =
                "block";

        } else {

            loginDropdown.style.display =
                "none";
        }
    }
);



// ======================================================
// USER DROPDOWN
// ======================================================

usernameButton.addEventListener(
    "click",
    () => {

        if (
            userDropdown.style.display ===
            "none"
        ) {

            userDropdown.style.display =
                "block";

        } else {

            userDropdown.style.display =
                "none";
        }
    }
);



// ======================================================
// LOGIN
// ======================================================

document
    .getElementById(
        "submit-login"
    )
    .addEventListener(
        "click",
        async () => {

            const username =
                document
                    .getElementById(
                        "login-username"
                    )
                    .value;


            const password =
                document
                    .getElementById(
                        "login-password"
                    )
                    .value;


            const message =
                document.getElementById(
                    "login-message"
                );


            try {

                const response =
                    await fetch(

                        `${API_URL}/api/login`,

                        {

                            method:
                                "POST",


                            headers: {

                                "Content-Type":
                                    "application/json"

                            },


                            body:
                                JSON.stringify({

                                    username:
                                        username,

                                    password:
                                        password

                                })
                        }
                    );



                if (!response.ok) {

                    throw new Error(
                        "Invalid username or password"
                    );
                }



                const user =
                    await response.json();



                localStorage.setItem(
                    "userId",
                    user.id
                );


                localStorage.setItem(
                    "username",
                    user.username
                );



                message.textContent =
                    "Login successful!";


                console.log(
                    "Logged in user:",
                    user
                );


                checkLogin();


                loadPortfolio();


            } catch (error) {

                message.textContent =
                    error.message;
            }
        }
    );



// ======================================================
// REGISTER
// ======================================================

document
    .getElementById(
        "submit-register"
    )
    .addEventListener(
        "click",
        async () => {

            const username =
                document
                    .getElementById(
                        "register-username"
                    )
                    .value;


            const password =
                document
                    .getElementById(
                        "register-password"
                    )
                    .value;


            const message =
                document.getElementById(
                    "register-message"
                );


            try {

                const response =
                    await fetch(

                        `${API_URL}/api/register`,

                        {

                            method:
                                "POST",


                            headers: {

                                "Content-Type":
                                    "application/json"

                            },


                            body:
                                JSON.stringify({

                                    username:
                                        username,

                                    password:
                                        password

                                })
                        }
                    );



                if (!response.ok) {

                    if (
                        response.status ===
                        409
                    ) {

                        throw new Error(
                            "Username already exists"
                        );
                    }


                    throw new Error(
                        "Registration failed"
                    );
                }



                const user =
                    await response.json();



                message.textContent =
                    "Account created! You can now log in.";


                console.log(
                    "Registered user:",
                    user
                );


            } catch (error) {

                message.textContent =
                    error.message;
            }
        }
    );



// ======================================================
// LOGOUT
// ======================================================

document
    .getElementById(
        "logout-button"
    )
    .addEventListener(
        "click",
        () => {

            localStorage.removeItem(
                "userId"
            );


            localStorage.removeItem(
                "username"
            );


            userDropdown.style.display =
                "none";


            checkLogin();


            location.reload();
        }
    );



// ======================================================
// LOAD PORTFOLIO
// ======================================================

function loadPortfolio() {

    const userId =
        localStorage.getItem(
            "userId"
        );


    if (!userId) {

        console.log(
            "No user is logged in."
        );

        return;
    }



    fetch(
        `${API_URL}/api/portfolio?userId=${userId}`
    )

        .then(response => {

            if (!response.ok) {

                throw new Error(
                    "Failed to load portfolio"
                );
            }


            return response.json();
        })


        .then(portfolio => {

            console.log(
                portfolio
            );



            document
                .getElementById(
                    "total-cost"
                )
                .innerHTML =

                "Total Cost: $" +
                portfolio.totalCost.toFixed(
                    2
                );



            document
                .getElementById(
                    "current-value"
                )
                .innerHTML =

                "Current Value: $" +
                portfolio.currentValue.toFixed(
                    2
                );



            document
                .getElementById(
                    "profit"
                )
                .innerHTML =

                "Profit: $" +
                portfolio.profit.toFixed(
                    2
                );



            const container =
                document.getElementById(
                    "lego-container"
                );


            container.innerHTML =
                "";



            portfolio.sets.forEach(
                set => {

                    const legoCard =
                        document.createElement(
                            "div"
                        );


                    legoCard.className =
                        "lego-set";


                    legoCard.innerHTML = `

                        <h3>${set.name}</h3>

                        <p>
                            Set #${set.setNumber}
                        </p>

                        <p>
                            Condition:
                            ${
                                set.condition === "NEW" ||
                                set.condition === "N"

                                    ? "New / Sealed"

                                    : "Built / Used"
                            }
                        </p>

                        <p>
                            Purchase Price:
                            $${set.purchasePrice.toFixed(2)}
                        </p>

                        <p>
                            Estimated Value:
                            $${set.currentValue.toFixed(2)}
                        </p>

                        <p>
                            Profit:
                            $${set.profit.toFixed(2)}
                        </p>

                        <button
                            class="remove-button"
                            data-id="${set.id}">
                            Remove
                        </button>
                    `;



                    container.appendChild(
                        legoCard
                    );



                    legoCard
                        .querySelector(
                            ".remove-button"
                        )
                        .addEventListener(
                            "click",
                            () => {

                                fetch(

                                    `${API_URL}/api/sets/${set.id}`,

                                    {

                                        method:
                                            "DELETE"

                                    }
                                )

                                    .then(
                                        response => {

                                            if (
                                                !response.ok
                                            ) {

                                                throw new Error(
                                                    "Failed to remove LEGO set"
                                                );
                                            }


                                            location.reload();
                                        }
                                    )


                                    .catch(
                                        error => {

                                            console.error(

                                                "Error removing LEGO set:",

                                                error

                                            );
                                        }
                                    );
                            }
                        );
                }
            );
        })


        .catch(error => {

            console.error(

                "Error loading portfolio:",

                error

            );
        });
}



// ======================================================
// ADD LEGO SET
// ======================================================

document
    .getElementById(
        "add-set-button"
    )
    .addEventListener(
        "click",
        () => {

            const setNumber =
                document
                    .getElementById(
                        "set-number"
                    )
                    .value;


            const purchasePrice =
                document
                    .getElementById(
                        "purchase-price"
                    )
                    .value;


            const condition =
                document
                    .getElementById(
                        "condition"
                    )
                    .value;


            const userId =
                localStorage.getItem(
                    "userId"
                );


            const button =
                document.getElementById(
                    "add-set-button"
                );



            if (!userId) {

                alert(
                    "Please log in before adding a LEGO set."
                );

                return;
            }



            button.innerHTML =
                "Adding...";


            button.disabled =
                true;



            const newSet = {

                setNumber:
                    setNumber,


                purchasePrice:
                    Number(
                        purchasePrice
                    ),


                condition:
                    condition

            };



            fetch(

                `${API_URL}/api/sets?userId=${userId}`,

                {

                    method:
                        "POST",


                    headers: {

                        "Content-Type":
                            "application/json"

                    },


                    body:
                        JSON.stringify(
                            newSet
                        )
                }
            )

                .then(response => {

                    if (!response.ok) {

                        throw new Error(
                            "Failed to add LEGO set"
                        );
                    }


                    location.reload();
                })


                .catch(error => {

                    console.error(

                        "Error adding LEGO set:",

                        error

                    );


                    button.innerHTML =
                        "Add Set";


                    button.disabled =
                        false;
                });
        }
    );



// ======================================================
// START APPLICATION
// ======================================================

checkLogin();

loadPortfolio();